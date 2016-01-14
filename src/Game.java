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
	private int gameSize;
	private int gameMode;
	private List<User> users;
	private Iterator<Question> iterator;

	private int statusRequests;
	private int questionRequests;
	private int choiceRequests;
	private int scoreRequests;

	private Question question;
	private int questionIndex;
	
	private static final int 	Default = 0,
								Humanity = 1;

	public Game(Server server, List<Question> questions, int gameSize) {
		this.server = server;
		this.gameSize = gameSize;
		gameMode = Default;
		users = new ArrayList<User>();
		iterator = questions.iterator();

		statusRequests = 0;
		questionRequests = 0;
		choiceRequests = 0;
		scoreRequests = 0;		
	}
	
	public void addUser(User user) throws Exception {
		if (users.size() < gameSize)
			users.add(user);
		else
			throw new Exception("Game is full.");
	}
	
	public void addAnswer(User user, String answer) throws Exception {
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

	public void addChoice(User user, int choice) throws Exception {
		user.setChoice(choice);
	}

	private HashMap<String, Integer> getScores() {
		int correct = questionIndex;
		int length = users.size();
		
		for (int i = 0; i < length; i++) {
			User iUser = users.get(i);
			int iAnswer = iUser.getIndex();
			int iChoice = iUser.getChoice();

			if (iUser.isCorrect())
				iUser.setCorrect(false); // No points for choosing if already answered correctly
			else if (iChoice == iAnswer)
				; // What happens if user chooses own answer?
			else if (iChoice == correct)
				iUser.incrementScore(2);
			else
				for (int j = 0; j < length; j++) {
					if (i != j) {
						User jUser = users.get(j);
						int jAnswer = jUser.getIndex();

						if (iChoice == jAnswer) {
							jUser.incrementScore(1);
						}
					}
				}
			// TODO: Reference from index to user
		}
		
		
		HashMap<String, Integer> scores = new HashMap<String, Integer>();
		
		for (User user : users) {
			scores.put(user.getName(), user.getScore());
		}
		
		return scores;
		
		// TODO: Implement comparator to sort scores
		
//		Collections.sort(users, new Comparator<User>() {
//			
//			@Override
//			public int compare(User s1, User s2) {
//				return (s1.getScore()- s2.getScore());
//			}
//		});

	}

	private List<String> getChoices() {
		List<String> choices = new ArrayList<String>();
		
		Random r = new Random();
		boolean isAdded = false;

		int i = 0;
		
		Collections.shuffle(users);
		
		for (User user : users) {
			if (!isAdded && r.nextDouble() < 1.0/(users.size()+1)) {
				choices.add(question.getAnswer());
				questionIndex = i;
				isAdded = true;
				i++;
			}
			choices.add(user.getAnswer());
			user.setIndex(i);
			i++;
		}
		
		if (!isAdded) {
			choices.add(question.getAnswer());
			questionIndex = i;
		}
		
		return choices;
		
	}
	
	public void requestNewRound() {
		statusRequests++;
		
		if (statusRequests >= users.size()) {
			if (iterator.hasNext()) {
				question = iterator.next();

				statusRequests = 0;
				questionRequests = 0;
				choiceRequests = 0;
				scoreRequests = 0;
				
				Tuple tuple = new Tuple(Tuple.STATUS);
				tuple.put("Next question:");
				server.sendToAll(users, tuple);

			} else {
				for (User user : users)
					user.setScore(0);
				Tuple tuple = new Tuple(Tuple.END);
				server.sendToAll(users, tuple);
			}
		}
	}

	public void requestQuestion() throws Exception {
		questionRequests++;

		if (questionRequests >= users.size()) {
			Tuple tuple = new Tuple(Tuple.QUESTION);
			tuple.put(question.getQuestion());
			server.sendToAll(users, tuple);
		}
	}

	public void requestChoices() {
		choiceRequests++;
		
		if (choiceRequests >= users.size()) {
			Tuple tuple = new Tuple(Tuple.CHOICES);
			tuple.put(getChoices());
			server.sendToAll(users, tuple);
		}
	}

	public void requestScores() {
		scoreRequests++;
		
		if (scoreRequests >= users.size()) {
			Tuple tuple = new Tuple(Tuple.SCORES);
			tuple.put(getScores());
			server.sendToAll(users, tuple);
		}

	}
}
