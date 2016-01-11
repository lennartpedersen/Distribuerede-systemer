import java.util.HashMap;
import java.util.List;

public class Tuple {
	private String command;
	private int gameSize;
	private String gameName;
	private String question;
	private String answer;
	private User user;
	private int choice;
	private List<String> choices;
	private boolean startGame;
	private int phase;
	private HashMap<User, Integer> scores;
	private String login;
	private boolean isLoggedIn;
	
	public Tuple(Object fields[]) {
		/*
		 * Argumenter fordeles således:
		 * 0 - Command string
		 * 1 - Game Name
		 * 2 - Game Size
		 * 3 - Question
		 * 4 - Answer
		 * 5 - User
		 * 6 - Choice
		 * 7 - Start Game bool
		 * 8 - phase
		 * 9 - Scores (HashMap<User, Integer>)
		 * 10 - Login (String)
		 * 11 - isLoggedIn (BOOL)
		 */
		this.command = (String) fields[0];
		this.gameName = (String) fields[1];
		this.gameSize = Integer.parseInt((String) fields[2]);
		this.question = ((String) fields[3]);
		this.answer = (String) fields[4];
		this.user = (User) fields[5];
		this.choice = (int) fields[6];
		this.startGame = (boolean) fields[7];
		this.phase = (int) fields[8];
		this.scores = (HashMap<User, Integer>) fields[9]; 
		this.login = (String) fields[10];
		this.isLoggedIn = (boolean) fields[11];
	}

	public String getCommand() {
		return command;
	}

	public int getGameSize() {
		return gameSize;
	}

	public String getGameName() {
		return gameName;
	}

	public String getAnswer() {
		return answer;
	}

	public User getUser() {
		return user;
	}

	public int getChoice() {
		return choice;
	}

	public List<String> getChoices() {
		return choices;
	}

	public boolean getStartGame() {
		return startGame;
	}

	public int getPhase() {
		return phase;
	}

	public HashMap<User, Integer> getScores() {
		return scores;
	}

	public String getLogin() {
		return login;
	}

	public boolean getIsLoggedIn() {
		return isLoggedIn;
	}

	public String getQuestion() {
		return question;
	}

	
}
