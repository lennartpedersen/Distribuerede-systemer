import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
	
	private int startRequests;
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
		
		startRequests = 0;
		questionRequests = 0;
		choiceRequests = 0;
		scoreRequests = 0;
		
	}

	private void newQuestion() throws Exception {
		
		if (iterator.hasNext()) {
			question = iterator.next();

			questionRequests = 0;
			choiceRequests = 0;
			scoreRequests = 0;

		} else
			throw new Exception("Error : Game had more rounds than amount of questions.");
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
		if (cAnswer.contains(uAnswer)) {
			user.incrementScore(3);
			
			// TODO: Make user unable to choose if he answered correctly

			// user needs to give another answer
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

			if (iChoice == iAnswer)
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

		int i = 0;
		
		for (User user : users) {
			System.out.println(user.getAnswer());
			choices.add(i, user.getAnswer());
			user.setIndex(i);
			i++;
		}
		System.out.println(question.getAnswer());
		choices.add(i, question.getAnswer());
		questionIndex = i;
		
		// TODO: Randomize order
		
		return choices;
		
	}


	public void requestStartGame() throws Exception {
		startRequests++;
//		System.out.println(startRequests + "/" + users.size());
	}

	public void requestQuestion() throws Exception {
		questionRequests++;
//		System.out.println(questionRequests + "/" + users.size());

		if (questionRequests >= users.size()) {
			newQuestion();
			Tuple tuple = new Tuple(Tuple.QUESTION);
			tuple.put(question.getQuestion());
			server.sendToAll(users, tuple);
		}
	}

	public void requestChoices() {
		choiceRequests++;
//		System.out.println(choiceRequests + "/" + users.size());
		
		if (choiceRequests >= users.size()) {
			Tuple tuple = new Tuple(Tuple.CHOICES);
			tuple.put(getChoices());
			server.sendToAll(users, tuple);
		}
	}

	public void requestScores() {
		scoreRequests++;
//		System.out.println(scoreRequests + "/" + users.size());
		
		if (scoreRequests >= users.size()) {
			Tuple tuple = new Tuple(Tuple.SCORES);
			tuple.put(getScores());
			server.sendToAll(users, tuple);
		}

	}
}
