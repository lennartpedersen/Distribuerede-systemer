import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class Game {

	/*
	 * phases: 0 - Start - send question 1 - Answering - Answers are received
	 * and the list is sent to all users for the CHoosing phase 2 - Choosing -
	 * Answers are chosen and evaluated.
	 */
	private int phase;
	private int gameSize;
	private int gameRound;

	private int usersRequests;
	private int numOfAnswers;

	private Server server;
	private Iterator<Question> iterator;
	private Question currentQuestion;
	private List<User> users;
	private List<Question> questionList;

	private HashMap<User, Integer> scoresIndexMap;
	private List<Score> scores;
	private HashMap<String, User> answers;
	private HashMap<User, String> choices;

	public Game(Server server, List<Question> questions, int gameSize) {
		this.phase = -1;
		this.gameRound = 1;
		this.numOfAnswers = 0;
		this.server = server;
		this.gameSize = gameSize;
		this.users = new ArrayList<User>();
		this.usersRequests = 0;

		this.questionList = questions;
		this.currentQuestion = questionList.get(0);
		iterator = questionList.iterator();

		this.scores = new ArrayList<Score>();
		for (User user : users) {
			scores.add(new Score(users.get(users.indexOf(user)), 0));
		}

		answers = new HashMap<>();
		choices = new HashMap<>();
		scoresIndexMap = new HashMap<>();
	}

	private void nextPhase() throws Exception {

		if (this.phase == -1) {
			this.phase = 0;
		} else {
			this.phase = (phase % 3) + 1;
		}

		Tuple phaseTuple;
		phaseTuple = new Tuple(Tuple.PHASE);
		phaseTuple.put(this.phase);
		server.sendToAll(users, phaseTuple);

		System.out.println("Phase change: "+phase); //TODO Testing. remove later.
		
		Tuple tuple;
		switch (phase) {

		case 0:
			// Phase 0 - Next round, Set next Question as current question,
			// reset
			if (iterator.hasNext()) {
				this.currentQuestion = questionList.get(0);
				this.numOfAnswers = 0;
				this.answers.clear();
				this.choices.clear();
				this.usersRequests=0;
			} else {
				throw new Exception("Error : Game had more rounds than amount of questions.");
			}
			break;

		case 1:
			// Phase 1 - All users have send their answers. These have
			// been
			// stored and scores evaluated.
			// Send the list of answers to all users for the Choosing Phase.
			this.usersRequests=0;
			// send list of answers to server
			break;

		case 2:
			// Phase 2 - All users have given their choice. These have
			// been
			// stored and scores need to be evaluated.
			// Info on scores and positioning needs to be send to all users.
			evalateTotalScore();

			tuple = new Tuple(Tuple.SCORES);

			// Create lists of scores and users and sort both
			Collections.sort(this.scores, new Comparator<Score>() {

				@Override
				public int compare(Score s1, Score s2) {
					// TODO Auto-generated method stub
					return s1.getValue() - s2.getValue();
				}

			});

			// send score info and positions to server
			tuple.put(users);
			tuple.put(scores);
			this.server.sendToAll(this.users, tuple);

			// if last round
			if (gameRound >= questionList.size()) {
				// end game
			} else {
				gameRound++;

			}

			nextPhase();
			break;
		default:
			throw new Exception("Error : Invalid Game Phase.");
		}
	}

	private void evalateTotalScore() {
		Iterator<Entry<String, User>> answersIterator = this.answers.entrySet().iterator();
		Iterator<Entry<User, String>> choicesIterator = this.choices.entrySet().iterator();
		while (answersIterator.hasNext()) {
			HashMap.Entry<String, User> answersPair = (Entry<String, User>) answersIterator.next();
			HashMap.Entry<User, String> choicesPair = (Entry<User, String>) choicesIterator.next();
			while (choicesIterator.hasNext()) {
				// if answer was chosen, award points to associated user
				if (answersPair.getKey().equals(choicesPair.getValue()))
					incrementScore((User) answersPair.getValue(), 1);
			}
		}
	}

	public void addAnswer(User user, String answer) throws Exception {
		if (getPhase() == 0) {

			// if correct answer
			if (answerCheck(answer)) {
				incrementScore(user, 3);
				
				// user needs to give another answer
				throw new Exception("Correct answer, Provide new answer");

			} else {
				this.answers.put(answer, user);
				this.numOfAnswers++;

			}
			
			// if all users have send their answers, begin next phase
			if (this.numOfAnswers >= this.users.size())
				nextPhase();
		}
	}

	// make work
	public void addChoice(User user, String choice) throws Exception {
		if (getPhase() == 1) {
			this.choices.put(user, choice);
			// if all users have given their choice, go to the next phase

			if (choice.equals(currentQuestion.getAnswer()))
				incrementScore(user, 2);

			if (this.choices.size() >= this.users.size())
				nextPhase();
		}
	}

	public void addUser(User user) throws Exception {
		if (this.users.size() < gameSize) {
			this.users.add(user);
			this.scoresIndexMap.put(user, scores.size());
			this.scores.add(scores.size(), new Score(user, 0));

		} else {
			throw new Exception("Game is full, Tried to add new player to full game");
		}
	}

	public void requestStartGame() throws Exception {
		if (!isStarted()) {
			this.usersRequests++;
			if (this.usersRequests >= this.users.size())
				nextPhase();
		}
	}

	private void incrementScore(User user, int score) {
		scores.get(scoresIndexMap.get(user)).incrementValue(score);
	}

	private boolean isStarted() {
		return getPhase() >= 0;
	}

	public boolean answerCheck(String userAnswer) {
		String uAnswer = userAnswer.toLowerCase(), 
				cAnswer = currentQuestion.getAnswer().toLowerCase();

		// Add additional matching
		
		return cAnswer.equals(uAnswer);
	}

	private int getPhase() {
		return this.phase;
	}

	public List<String> getListOfAnswers() {
		List<String> listOfAnswers = new ArrayList<String>();
		Iterator<Entry<String, User>> answersIterator = this.answers.entrySet().iterator();
		while (answersIterator.hasNext()) {
			HashMap.Entry<String, User> answerPair = (Entry<String, User>) answersIterator.next();
			listOfAnswers.add(answerPair.getKey());
		}
		return listOfAnswers;
	}

	public void requestQuestion() {
		this.usersRequests++;
		if(this.usersRequests>=this.users.size()){
		Tuple tuple = new Tuple(Tuple.QUESTION);
		tuple.put(getCurrentQuestion().getQuestion());
		this.server.sendToAll(this.users, tuple);
		}
	}
	
	public void requestChoices(){
		this.usersRequests++;
		if(this.usersRequests>=this.users.size()){
		Tuple tuple = new Tuple(Tuple.CHOICES);
		tuple.put(getListOfAnswers());
		server.sendToAll(this.users, tuple);
		}
	}

	Question getCurrentQuestion() {
		return this.currentQuestion;
	}
}
