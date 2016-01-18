import java.io.Serializable;

public class Tuple implements Serializable {
	/*
	 * Serial number, allows client and server to recognize this object as the same class when sending and receiving data. Do not remove.
	 */
	private static final long serialVersionUID = -5751716628966339791L;
	
	/* 
	 * This should be used to request operations from client to server.
	 * Can be used to respond to the requested operation from server to client when successful or unsuccessful.
	 * When an operation is successful, server responds with a tuple object of the same operation type as requested (To a successful login operation, Server responds with a tuple with the LOGIN constant as command)
	 * and any requested or required data attached to the data field. With a failed operation the server responds with a tuple using the ERROR command and the attached exception.
	 * 
	 * Operations:
	 * 0 ERROR: Used to indicate that an error has occurred with latest operation. Only used by server as a responds to failed operations.
	 * Attached Object/s: Exception 'the thrown exception'.
	 * 
	 * 1 LOGIN: Used to request a login as an active client with a username. User is remembered by server and is required for all other operations.
	 * Attached Object/s: User 'the new user'.
	 * 
	 * 2 REQUESTNEWGAME: Used to request a new game to be created.
	 * Attached Object/s: String 'the requested game name', Integer 'the max amount of players', Integer 'the number of rounds'.
	 * 
	 * 3 JOINGAME: Used to request to join an already created game as the currently logged in user.
	 * Attached Object/s: String 'the name of the game to join'.
	 * 
	 * 4 ANSWER: Used to send in an answer to the current question of the active game.
	 * Attached Object/s: String 'the given answer'.
	 * 
	 * 5 CHOICE: Used to send in an choose of what the user believes to be the correct answer in the current game.
	 * Attached Object/s: Integer 'the number of the chosen answer as index in the given answer list'.
	 * 
	 * 6 QUESTION: Used by the server to send out the current question for the active game. Done when the game starts and every new round.
	 * Attached Object/s: Question 'the current question'
	 * 
	 * 7 STARTGAMEREQUEST: Used to request or cancel a game start from the client.
	 * Attached Object/s: Boolean 'if user is ready for the game to start'.
	 * 
	 * 
	 * Do we need more operations? We probably do.
	 */
	public static final int 
			HANDSHAKE = -2,
			MESSAGE = -1,
			ERROR = 0,
			LOGIN = 1,
			CREATEGAME = 2,
			JOINGAME = 3,
			ANSWER = 4,
			CHOOSE = 5,
			QUESTION = 6,
			STARTGAME = 7,
			CHOICES = 8,
			END = 9,
			SCORES = 10,
			SHOWGAMES = 12;

	private int command;
	private Object data;
	
	
	public Tuple(int command) {
		this.command = command;
	}

	public Object getData() {
		return data;
	}

	public void put(Object data) {
		this.data = data;
	}

	public int getCommand() {
		return command;
	}
	
}
