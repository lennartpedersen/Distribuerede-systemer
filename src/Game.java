import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class Game implements Serializable{

	/*
	 * phases: 0 - Start - send question 1 - Answering - Answers are received
	 * and the list is sent to all users for the CHoosing phase 2 - Choosing -
	 * Answers are chosen and evaluated.
	 */
	private int phase;
	private int gameSize;
	private int gameRound;

	private int startRequests;
	private int questionRequests;
	private int choiceRequests;


	private Server server;
	private Iterator<Question> iterator;
	private Question currentQuestion;
	private List<User> users;
	private List<Question> questionList;

	private List<String> listOfAnswers ;
	private HashMap<User, Integer> scoresIndexMap;
	private HashMap<String, User> answers;
	private HashMap<User, String> choices;
	private int numOfAnswers;
	private int choiceCounter;
	private int gameMode;
	private static final int Default = 0;
	private static final int Humanity = 1;

	public Game(Server server, List<Question> questions, int gameSize) {
		phase = -1;
		gameRound = 1;
		numOfAnswers = 0;
		this.gameMode = gameMode;
		this.server = server;
		this.gameSize = gameSize;
		users = new ArrayList<User>();
		startRequests = 0;
		questionRequests = 0;
		choiceRequests = 0;
		choiceCounter=0;

		this.questionList = questions;
		currentQuestion = questionList.get(0);
		iterator = questionList.iterator();

		answers = new HashMap<>();
		choices = new HashMap<>();
		scoresIndexMap = new HashMap<>();
	}

	private void newRound() throws Exception {
		if (iterator.hasNext()) {
			currentQuestion = iterator.next();
			numOfAnswers = 0;
			choiceCounter=0;
			answers.clear();
			choices.clear();
			questionRequests=0;
			choiceRequests=0;
		} else {
			throw new Exception("Error : Game had more rounds than amount of questions.");
		}
	}

	public void addChoice(User user, String choice) throws Exception {
			choices.put(user, choice);
			choiceCounter++;
			// if all users have given their choice, go to the next phase

			if (choice.equals(currentQuestion.getAnswer()))
				incrementScore(user, 2);
			if(choiceCounter>=users.size()){
			evalateTotalScore();
			newRound();;
			}
			
		}
	
	public void addUser(User user) throws Exception {
		if (users.size() < gameSize) {
			users.add(user);
		} else {
			throw new Exception("Game is full, Tried to add new player to full game");
		}
	}
	
	public void addAnswer(User user, String answer) throws Exception {
	
			// if correct answer
			if (answerCheck(answer)) {
				incrementScore(user, 3);
				
				// user needs to give another answer
				throw new Exception("Correct answer, Provide incorrect answer");

			} else {
				answers.put(answer, user);
				numOfAnswers++;
				if(numOfAnswers>=users.size())
					generateAnswerList();
			}
		}

	private boolean answerCheck(String userAnswer) {
		String uAnswer = userAnswer.toLowerCase(), 
				cAnswer = currentQuestion.getAnswer().toLowerCase();

		return cAnswer.contains(uAnswer);
		
	}

	private void evalateTotalScore() {
		
		Iterator<Entry<String, User>> answersIterator = answers.entrySet().iterator();
		Iterator<Entry<User, String>> choicesIterator = choices.entrySet().iterator();
		while (answersIterator.hasNext()) {
			HashMap.Entry<String, User> answersPair = (Entry<String, User>) answersIterator.next();
			HashMap.Entry<User, String> choicesPair = (Entry<User, String>) choicesIterator.next();
			while (choicesIterator.hasNext()) {
				// if answer was chosen, award points to associated user
				if (answersPair.getKey().equals(choicesPair.getValue()))
					incrementScore((User) answersPair.getValue(), 1);
			}
		}
//
//		Collections.sort(users, new Comparator<User>() {
//			
//			@Override
//			public int compare(User s1, User s2) {
//				return (s1.getScore()- s2.getScore());
//			}
//		});

	}

	private void generateAnswerList() {
		listOfAnswers = new ArrayList<String>();
		Iterator<Entry<String, User>> answersIterator = answers.entrySet().iterator();
		while (answersIterator.hasNext()) {
			HashMap.Entry<String, User> answerPair = (Entry<String, User>) answersIterator.next();
			listOfAnswers.add(answerPair.getKey());
		}
		listOfAnswers.add(new Random().nextInt(listOfAnswers.size()),getCurrentQuestion().getAnswer());
	}

	private void incrementScore(User user, int score) {
		user.incrementScore(score);
	}

	public void requestStartGame() throws Exception {
			startRequests++;
			System.out.println(startRequests + " " + users.size()); 
	}

	public void requestQuestion() {
		questionRequests++;
		System.out.println(questionRequests + " " + users.size()); 
		
		if(questionRequests>=users.size()){		
		Tuple tuple = new Tuple(Tuple.QUESTION);
		tuple.put(getCurrentQuestion().getQuestion());
		server.sendToAll(users, tuple);
		}
	}
	
	public void requestChoices(){
		choiceRequests++;
		System.out.println(choiceRequests + " " + users.size()); 
		if(choiceRequests>=users.size()){
		Tuple tuple = new Tuple(Tuple.CHOICES);
		tuple.put(getListOfAnswers());
		server.sendToAll(users, tuple);
		}
	}
	
	public void requestScores(){
		System.out.println("sys out at Game.requestScore");
		Tuple tuple = new Tuple(Tuple.SCORES);
		tuple.put("HEllos");
		server.sendToAll(users, tuple);

	}

	public List<String> getListOfAnswers() {
		return listOfAnswers;
	}

	private Question getCurrentQuestion() {
		return currentQuestion;
	}
}
