
public class Command {
	private String command;
	private int gameSize;
	private String gameName;
	private String answer;
	private User user;
	private Question question;
	private int choice;
	private boolean startGame;
	private int gameLength;
	private Exception exception;
	
	public Command(String command, String gameName, int gameSize, int gameLength) { //Request game
		this.command = command;
		this.gameSize = gameSize;
		this.gameName = gameName;
		this.gameLength = gameLength;
	}
	
	public Command(String command, String gameName, User user) { //Join game
		this.command = command;
		this.gameName = gameName;
		this.user = user;
	}
	
	public Command(String command, String answer) { //Answer
		this.command = command;
		this.answer = answer;
	}
	
	public Command(String command, int choice) { //Choose - valg er bliver givet som en int, 
		this.command = command;					 //da vi tænker at nummere svarmulighederne så man slipper for at taste 
		this.choice = choice;				 	 //hele svar ind
	}
	
	public Command(String command) { //Request questions from server
		this.command = command;
	}
	
	public Command(String command, boolean startGame) { //Request to start game
		this.command = command;
		this.startGame = startGame;
	}

	public Command(String command, Question currentQuestion) { //Send question.
		this.command = command;
		this.question = currentQuestion;
	}

	public Command(String command, Exception exception) { //Error exception thrown.
		this.command = command;
		this.exception = exception;
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

	public int getGameLength() {
		return gameLength;
	}

	public Question getQuestion() {
		return question;
	}

	public Exception getException() {
		return exception;
	}
	
}
