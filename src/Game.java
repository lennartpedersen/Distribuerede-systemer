import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Game {

	/*
	 * phases: 0 - Start - send question 1 - Answering - Answers are received
	 * and the list is sent to all users for the CHoosing phase 2 - Choosing -
	 * Answers are chosen and evaluated.
	 */

	private Server server;
	private String gameName;
	private int gameSize;
//	private int gameMode;
	private List<User> users;
	private Iterator<Question> iterator;
	private boolean gameStarted;

	private int startRequests;
	private int questionRequests;
	private int choiceRequests;
	private int scoreRequests;

	private Question question;
	private int questionIndex;
	private HashMap<Integer, User> indexMap;
	
//	private static final int 	Default = 0,
//								Humanity = 1;

	public Game(Server server, List<Question> questions, String gameName, int gameSize) {
		this.server = server;
		this.gameName = gameName;
		this.gameSize = gameSize;
		users = new ArrayList<User>();
		iterator = questions.iterator();
		gameStarted = false;

		startRequests = 0;
		questionRequests = 0;
		choiceRequests = 0;
		scoreRequests = 0;		
	}
	
	public synchronized void addUser(User user) throws Exception {
		if (users.size() < gameSize) {
			users.add(user);
		}
		else
			throw new Exception("Game is full.");
	}
	
	public synchronized void addAnswer(User user, String answer) throws Exception {
		String uAnswer = answer.toLowerCase(),
			   cAnswer = question.getAnswer().toLowerCase();
		

		// if correct answer
		if (cAnswer.equals(uAnswer)) {
			user.incrementScore(3);
			user.setCorrect(true);

			throw new Exception("Correct answer. Provide incorrect answer");

		} else {
			user.setAnswer(answer);
		}
	}

	public synchronized void addChoice(User user, int choice) {
		user.setChoice(choice);
	}

	private synchronized List<String> getChoices() {
		List<String> choices = new ArrayList<String>();
		indexMap = new HashMap<Integer, User>();
		
		Random r = new Random();
		boolean isAdded = false;

		int i = 0;
		
		Collections.shuffle(users);
		
		for (User user : users) {
			if (!isAdded && r.nextDouble() < 1.0 / (users.size() + 1)) {
				choices.add(question.getAnswer());
				questionIndex = i;
				isAdded = true;
				i++;
				
			}
			choices.add(user.getAnswer());
			user.setIndex(i);
			indexMap.put(i, user);
			i++;
		}
		
		if (!isAdded) {
			choices.add(question.getAnswer());
			questionIndex = i;
		}
		
		return choices;
	}

	private synchronized HashMap<String, Integer> getScores() {
		int correct = questionIndex;
		
		for (User user : users) {
			int answer = user.getIndex();
			int choice = user.getChoice();

			if (user.isCorrect())
				user.setCorrect(false); // No points for choosing if already answered correctly
			else if (choice == answer)
				; // What happens if user chooses own answer?
			else if (choice == correct)
				user.incrementScore(2);
			else
				indexMap.get(choice).incrementScore(1);
		}		
		
		HashMap<String, Integer> scores = new HashMap<String, Integer>();
		
		for (User user : users) {
			scores.put(user.getName(), user.getScore());
		}
		
		return scores;
	}
	
	public synchronized void readyStatus() {
		if (!gameStarted) {
			Tuple tuple = new Tuple(Tuple.MESSAGE);
			tuple.put("Users ready: " + startRequests + "/" + users.size());
			server.sendToAll(users, tuple);
		}
	}
	
	public synchronized void requestStart(User user, boolean hasRequestedStart, String msg) {
		boolean newRequest = msg.toLowerCase().equals("start") && !hasRequestedStart;
		
		if (newRequest) {
			startRequests++;
		}

		Tuple tuple = new Tuple(Tuple.STARTGAME);
		
		if (startRequests >= users.size() && !gameStarted) {
			readyStatus();
			gameStarted = true;
			tuple.put("start");
		} else
			tuple.put(user.getName() + ": " + msg);
		
		server.sendToAll(users, tuple);
		
		if (newRequest)
			readyStatus();
	}

	public synchronized void requestQuestion() throws Exception {
		questionRequests++;
		
		if (questionRequests >= users.size()) {
			questionRequests = 0;

			
			Tuple tuple = new Tuple(Tuple.QUESTION);
			
			if (iterator.hasNext()) {
				question = iterator.next();
				tuple.put(question.getQuestion());
			} else {
				for (User user : users)
					user.setScore(0);
				server.gameList.remove(gameName);
				tuple.put("Game over.");
			}
			server.sendToAll(users, tuple);
		}
	}

	public synchronized void requestChoices() {
		choiceRequests++;
		
		if (choiceRequests >= users.size()) {
			choiceRequests = 0;
			
			Tuple tuple = new Tuple(Tuple.CHOICES);
			tuple.put(getChoices());
			server.sendToAll(users, tuple);
		}
	}

	public synchronized void requestScores() {
		scoreRequests++;
		
		if (scoreRequests >= users.size()) {
			scoreRequests = 0;
			
			Tuple tuple = new Tuple(Tuple.SCORES);
			tuple.put(getScores());
			server.sendToAll(users, tuple);
		}
	}
	
	public boolean getGameStarted() {
		return gameStarted;
	}
	
	public List<User> getUsers() {
		return users;
	}
}
