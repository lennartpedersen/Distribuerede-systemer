import java.util.ArrayList;
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
	private int eligableUsers;
	private int numOfAnswers;

	private List<User> users;
	private List<User> usersRequestingStart;
	private List<Question> questionList;
	private Iterator<Question> iterator;
	private Question currentQuestion;
	private HashMap<User, Integer> scores;
	private HashMap<String, User> answers;
	private HashMap<User, String> choices;

	public Game(ArrayList<User> users, List<Question> questions, int gameSize) {
		this.phase = -1;
		this.gameSize = gameSize;
		this.gameRound = 1;
		this.eligableUsers = users.size();
		this.numOfAnswers = 0;
		this.users = users;
		this.usersRequestingStart = new ArrayList<User>();

		this.questionList = questions;
		this.currentQuestion = questionList.get(0);
		iterator = questionList.iterator();

		this.scores = new HashMap<>();
		for (User user : users) {
			scores.put(users.get(users.indexOf(user)), 0);
		}

		answers = new HashMap<>();
		choices = new HashMap<>();
	}

	// Begin game
	public Question beginGame() {
		phase = 0;
		// first Question is sent to users
		return getCurrentQuestion();

	}

	public void nextPhase() throws Exception {
		this.phase = (phase % 3) + 1;

		switch (phase) {
		// Phase 0 - Next round, Set next Question as current question, reset
		case 0:
			if (iterator.hasNext()) {
				currentQuestion = getCurrentQuestion();
				currentQuestion = iterator.next();
				this.numOfAnswers = 0;
				answers.clear();
				choices.clear();
			} else {
				throw new Exception("Error : Game had more rounds than amount of questions.");
			}
			break;

		// Phase 1 - All eligible users have send their answers. These have been
		// stored and scores evaluated.
		// Send the list of answers to all users for the Choosing Phase.
		case 1:
			// send list of answers to server
			break;

		// Phase 2 - All eligible users have given their choice. These have been
		// stored and scores need to be evaluated.
		// Info on scores and positioning needs to be send to all users.
		case 2:
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
			// send score info and positions to server

			// if last round
			if (gameRound >= questionList.size()) {
				// announce winner.

				// spectators can now participate
				for (User user : users)
					user.setSpectator(false);

			} else
				gameRound++;
			break;
		default:
			throw new Exception("Error : Invalid Game Phase.");
		}
	}

	public boolean addAnswer(User user, String answer) throws Exception {
		if (!user.isSpectator()) {
			this.answers.put(answer, user);
			this.numOfAnswers++;

			// if all non spectator users have send their answers, begin next
			// phase
			if (this.numOfAnswers >= this.eligableUsers)
				nextPhase();

			// if correct answer
			if (answerCheck(answer)) {
				incrementScore(user, 3);
				// user needs to give another answer
				return true;
			}
			// Answer was false, user won't need to give another answer
			return false;
		}
		return false;
	}

	// make work
	public void addChoice(User user, String choice) throws Exception {
		if (!user.isSpectator()) {
			this.choices.put(user, choice);
			// if all users have given their choice, go to the next phase

			if (choice.equals(currentQuestion.getAnswer()))
				incrementScore(user, 2);

			if (this.choices.size() >= this.eligableUsers)
				nextPhase();
		}
	}

	public void addUser(User user) throws Exception {
		if (this.users.size() < gameSize) {
			this.users.add(user);

			if (isStarted())
				user.setSpectator(true);
			else
				eligableUsers++;
		} else {
			throw new Exception("Game is full, Tried to add new player to full game");
		}
	}

	public void addRequest(User user) {
		this.usersRequestingStart.add(user);
	}

	private void incrementScore(User user, int score) {
		scores.put(user, scores.get(user) + score);
	}

	private boolean isStarted() {
		return phase >= 0;
	}

	public boolean isGameReady() {
		return (this.usersRequestingStart.size() >= this.users.size());
	}

	public boolean answerCheck(String userAnswer) {
		String uAnswer = userAnswer.toLowerCase(), cAnswer = currentQuestion.getAnswer().toLowerCase();

		//Removes whitespaces from both strings
		uAnswer = uAnswer.replaceAll("\\s", "");
		cAnswer = cAnswer.replaceAll("\\s", "");
		
		return cAnswer.equals(uAnswer);
	}

	public boolean phaseCheck(int phase) {
		return (getPhase() == phase);
	}

	private int getPhase() {
		return this.phase;
	}

	public List<String> getListOfAnswers() {
		List<String> listOfAnswers = new ArrayList<String>();
		Iterator<Entry<String, User>> answersIterator = answers.entrySet().iterator();
		while (answersIterator.hasNext()) {
			HashMap.Entry<String, User> answerPair = (Entry<String, User>) answersIterator.next();
			listOfAnswers.add(answerPair.getKey());
		}
		return listOfAnswers;
	}

	public HashMap<User, String> getListOfChoices() {
		return getChoicesMap();
	}

	private HashMap<User, String> getChoicesMap() {
		return this.choices;
	}

	public Question getCurrentQuestion() {
		return this.currentQuestion;
	}
}
