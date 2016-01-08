
public class Command {
	private String command;
	private int gameSize;
	private String gameName;
	private String answer;
	private User user;
	private int choice;
	
	public Command(String command, String gameName, int gameSize) { //Request game
		this.command = command;
		this.gameSize = gameSize;
		this.gameName = gameName;
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
