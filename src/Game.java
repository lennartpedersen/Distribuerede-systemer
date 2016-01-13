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
	private int eligableUsers;
	private int numOfAnswers;

	private Server server;
	private Iterator<Question> iterator;
	private Question currentQuestion;
	public List<User> users;
	public List<User> usersRequestingStart;
	private List<Question> questionList;
	private List<Score> scores;
	private HashMap<String, User> answers;
	private HashMap<User, String> choices;

	public Game(Server server, List<Question> questions, int gameSize) {
		this.phase = -1;
		this.gameRound = 1;
		this.numOfAnswers = 0;
		this.server = server;
		this.gameSize = gameSize;
		this.eligableUsers = 0;
		this.users = new ArrayList<User>();
		this.usersRequestingStart = new ArrayList<User>();

		this.questionList = questions;
		this.currentQuestion = questionList.get(0);
		iterator = questionList.iterator();

		this.scores = new ArrayList<Score>();
		for (User user : users) {
			scores.add(new Score(users.get(users.indexOf(user)), 0));
		}

		answers = new HashMap<>();
		choices = new HashMap<>();

	}

	private void nextPhase() throws Exception {

		if (this.phase == -1) {
			this.phase = 0;
		} else {
			this.phase = (phase % 3) + 1;
		}

		Tuple phaseTuple;
		phaseTuple = new Tuple(9);
		phaseTuple.put(this.phase);
		server.sendToAll(users, phaseTuple);

		Tuple tuple;
			switch (phase) {

		case 0:
			// Phase 0 - Next round, Set next Question as current question,
			// reset
			if (iterator.hasNext()) {
				currentQuestion = iterator.next();
				this.numOfAnswers = 0;
				answers.clear();
				choices.clear();
			} else {
				throw new Exception("Error : Game had more rounds than amount of questions.");
			}
			// send Question to users
			tuple = new Tuple(6);
			tuple.put(getCurrentQuestion());

			this.server.sendToAll(this.users, tuple);
			break;

		case 1:
			// Phase 1 - All eligible users have send their answers. These have
			// been
			// stored and scores evaluated.
			// Send the list of answers to all users for the Choosing Phase.
			tuple = new Tuple(8);
			tuple.put(getListOfAnswers());
			server.sendToAll(this.users, tuple);
			// send list of answers to server
			break;

		case 2:
			// Phase 2 - All eligible users have given their choice. These have
			// been
			// stored and scores need to be evaluated.
			// Info on scores and positioning needs to be send to all users.
			evalateTotalScore();

			tuple = new Tuple(10);

			//Create lists of scores and users and sort both
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
			server.sendToAll(this.users, tuple);
		
			// if last round
			if (gameRound >= questionList.size()) {
				// end game
			} else {
				gameRound++;

				// spectators can now participate
				for (User user : users)
					user.setSpectator(false);
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
		if (!user.isSpectator() && getPhase()==0){

			// if correct answer
			if (answerCheck(answer)) {
				incrementScore(user, 3);
				
				// user needs to give another answer
				Tuple tuple = new Tuple(11);
				tuple.put(user);
				server.sendToAll(this.users,tuple);
				
			} else {
				this.answers.put(answer, user);
				this.numOfAnswers++;
			}

			// if all non spectator users have send their answers, begin next
			// phase
			if (this.numOfAnswers >= this.eligableUsers)
				nextPhase();
		}
	}

	// make work
	public void addChoice(User user, String choice) throws Exception {
		if (!user.isSpectator() && getPhase()==1) {
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

	public void addRequest(User user) throws Exception {
		if(!isStarted()){
		this.usersRequestingStart.add(user);
		if (this.usersRequestingStart.size() >= this.eligableUsers)
			nextPhase();
		}
	}

	private void incrementScore(User user, int score) {
		int index = users.indexOf(user);
		scores.add(new Score(users.get(index), scores.remove(index).getValue()+0));
	}

	private boolean isStarted() {
		return getPhase() >= 0;
	}

	public boolean answerCheck(String userAnswer) {
		String uAnswer = userAnswer.toLowerCase(), cAnswer = currentQuestion.getAnswer().toLowerCase();

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

	Question getCurrentQuestion() {
		return this.currentQuestion;
	}
}
