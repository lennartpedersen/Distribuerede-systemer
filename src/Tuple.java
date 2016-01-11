import java.util.ArrayList;

public class Tuple {
	private String command;
	private int gameSize;
	private String gameName;
	private String question;
	private String answer;
	private User user;
	private int choice;
	private boolean startGame;
	/*
	public Command(String command, String gameName, int gameSize) { //Request game
		this.command = command;
		this.gameSize = gameSize;
		this.gameName = gameName;
	}*/
	
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
		 */
		this.command = (String) fields[0];
		this.gameName = (String) fields[1];
		this.gameSize = Integer.parseInt((String) fields[2]);
		this.question = (String) fields[3];
		this.answer = (String) fields[4];
		this.user = (User) fields[5];
		this.choice = (int) fields[6];
		this.startGame = (boolean) fields[7];
	}
	
	public Tuple(String command, String gameName, User user) { //Join game
		this.command = command;
		this.gameName = gameName;
		this.user = user;
	}
	
	public Tuple(String command, String answer) { //Answer
		this.command = command;
		this.answer = answer;
	}
	
	public Tuple(String command, int choice) { //Choose - valg er bliver givet som en int, 
		this.command = command;					 //da vi tænker at nummere svarmulighederne så man slipper for at taste 
		this.choice = choice;				 	 //hele svar ind
	}
	
	public Tuple(String command) { //Request questions from server
		this.command = command;
	}
	
	public Tuple(String command, boolean startGame) { //Request to start game
		this.command = command;
		this.startGame = startGame;
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
	
}
