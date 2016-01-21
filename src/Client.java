import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class Client  {
	//Fields
	private ObjectInputStream sInput; //Datastream for receiving data from socket
	private ObjectOutputStream sOutput; //Datastream for sending data to socket
	private Socket socket; //The connection to the server
	
	private InetAddress server; //Server address.
	private int port = 1500; //Server port.
	private static Client client;
	private boolean isGameOver, hasGameStarted, hasRequestedStart;
	private BufferedReader scan;
	private int players = 0;
	private String userName;
	private ChatThread chatThread;
	
	private GUI gui;

	//Constructor
	public Client(boolean hasGUI) throws UnknownHostException {
		server = InetAddress.getByName("127.0.0.1");
		if (hasGUI){
			gui = new GUI(this);
		}
	}
	
	public static void main(String[] args) throws IOException {
		boolean hasGUI = true;
		//create the Client
		client = new Client(hasGUI);
		//start the connection to the Server.
		if (hasGUI)
			client.connect();
		else
			client.start();
	}
	
	//Methods
	private void connect(){
		try {
			socket = new Socket(server, port);
			// creating data streams to receive and send data.
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e){
			if (gui != null)
				gui.statusMessage("Connection to Server failed.", true);
			else
				System.out.println("Connection to Server failed.");
			close();
		}
	}
	
	private void start() {
		//Connect to server.
		connect();
		
		//create reader for messages from user
		scan = new BufferedReader(new InputStreamReader(System.in));
		
		loginPhase();
		
		while (true) {
			
			optionPhase();
			
			startPhase();
			
			while (true) { // gamePhase
				
				boolean hasQuestion = false,
						hasAnswer = false,
						hasChoices = false,
						hasChoice = false,
						hasScores = false;
				
				while (!hasQuestion) {
					try {
						client.read(Tuple.QUESTION);
						hasQuestion = true;
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}

				if (isGameOver)
					break;

				// Answer question
				String answer = "";
				
				while (!hasAnswer) {
					try {
						answer = scan.readLine();
						client.putread(Tuple.ANSWER, answer);
						hasAnswer = true;
					} catch (IOException e) {
						System.out.println("Error reading line.");
					} catch (Exception e) {
						System.out.println(e.getMessage());
						answer = getNewAnswer(answer.toLowerCase());
						hasAnswer = true;
					}
				}
				
				
				// Get choices from server
				while (!hasChoices) {
					try {
						client.read(Tuple.CHOICES);
						hasChoices = true;
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}

				// Pick choice
				System.out.println("Choose an answer by its number:");
				int choice = 0;
				while (!hasChoice) {
					choice = getInteger();
					if (choice <= 0 || choice > players)
						System.out.println("Please choose a number between 1 and " + players);
					else 
						hasChoice = true;
				}
				
				client.put(Tuple.CHOOSE, choice);
				
				while (!hasScores) {
					try {
						client.read(Tuple.SCORES);
						hasScores = true;
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
				
			} // End while (gameStarted)
			
		} // End while (true)
	}
	
	private void loginPhase() {
		boolean userLoggedIn = false;
		
		System.out.println("Login - enter username: ");
		
		while (!userLoggedIn) {
			try {
				userName = scan.readLine();
				client.putread(Tuple.LOGIN, userName);
				userLoggedIn = true;
			} catch (IOException e) {
				System.out.println("Error scanning line.");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	private void optionPhase() {
		boolean optionPhase = true;
		
		while (optionPhase) {

			boolean hasOption = false,
					hasRequestedNewGame = false,
					hasJoinedGame = false;

			System.out.println("Options: \n Show games \n Create game \n Join game \n Write your choice:");
			String option = "";

			while (!hasOption) {
				try {
					option = scan.readLine().toLowerCase();
					switch (option) {
					case "show games":
					case "create game":
					case "join game":
						hasOption = true;
						break;
					default:
						System.out.println("Incorrect input. Try again.");
						break;
					}
				} catch (IOException e) {
					System.out.println("Error scanning line.");
				}
			}

			String gameName = "";
			int gameSize = 0;
			int gameLength = 0;

			optionSwitch: switch (option) {
			case "show games":
				try {
					client.read(Tuple.SHOWGAMES);
				} catch (Exception e) {
					e.getMessage();
				}
				break;

			case "create game":

				System.out.println("Enter maximum number of players:");
				gameSize = getInteger();
				System.out.println("Enter number of rounds:");
				gameLength = getInteger();

				while (!hasRequestedNewGame) {
					gameName = getGameName();
					if (gameName.equals("back")) {
						break optionSwitch;
					}

					ArrayList<Object> data = new ArrayList<Object>();
					data.add(gameName);
					data.add(gameSize);
					data.add(gameLength);

					try {
						client.putread(Tuple.CREATEGAME, data);
						hasRequestedNewGame = true;
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}

			case "join game":
				while (!hasJoinedGame) {
					try {
						if (!hasRequestedNewGame) {
							gameName = getGameName();
							if (gameName.equals("back")) {
								break optionSwitch;
							}
						}
						client.putread(Tuple.JOINGAME, gameName);
						hasJoinedGame = true;
						optionPhase = false;
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
				break;

			} // End switch (option)
		}
	}
	
	void startChatThread(){ //Create and start ChatThread.
		chatThread = new ChatThread();
		chatThread.start();
	}
	
	private void startPhase() {
		hasGameStarted = false;	
		hasRequestedStart = false;
		System.out.println("You can now chat with everyone in the game.");
		System.out.println("When you are ready to begin the game please enter: Start");
		String msg = "";
		chatThread = new ChatThread();
		chatThread.start();
		
		while (!hasGameStarted) {
			try {
				ArrayList<Object> data = new ArrayList<Object>();
				data.add(hasRequestedStart);
				
				msg = scan.readLine();
				data.add(msg);
				
				if (msg.toLowerCase().equals("start"))
					hasRequestedStart = true;
				
				client.put(Tuple.STARTGAME, data);
			} catch (IOException e) {
				System.out.println("Error reading line.");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		isGameOver = false;
	}
	
	private String getGameName() {
		boolean hasGameName = false;

		System.out.println("Enter game name: (To cancel enter: Back)");
		String gameName = "";
		
		while (!hasGameName) {
			try {
				gameName = scan.readLine().toLowerCase();
				hasGameName = true;
			} catch (IOException e) {
				System.out.println("Incorrect input. Try again.");
			}
		}
		
		return gameName;
	}
	
	private String getNewAnswer(String answer) {
		boolean hasNewAnswer = false;
		String newAnswer = "";
		
		while (!hasNewAnswer) {
			try {
				newAnswer = scan.readLine();
				if (newAnswer.toLowerCase().contains(answer) || answer.contains(newAnswer.toLowerCase()))
					System.out.println("Write an incorrect answer.");
				else {
					client.putread(Tuple.ANSWER, newAnswer);
					hasNewAnswer = true;
				}
			} catch (IOException e) {
				System.out.println("Error reading line.");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return newAnswer;
	}
	
	private int getInteger() {
		String readline;
		int integer = 0;
		boolean isInt = false;
		
		while (!isInt) {
			try {
				readline = scan.readLine();
				integer = Integer.parseInt(readline);
				if (integer < 0)
					System.out.println("Number entered is negative.");
				else
					isInt = true;
			} catch (IOException e2) {
				System.out.println("Error reading line.");
			} catch (NumberFormatException e) {
				System.out.println("Please enter an integer.");
			}
		}
		return integer;
	}

	void put(int command, Object data) { // Sends data
		Tuple tuple = new Tuple(command);
		tuple.put(data);
		try {
			sOutput.writeObject(tuple);
		} catch (IOException e) {
			e.printStackTrace();
			if (gui != null)
				gui.statusMessage("Connection to server lost!", true);
			close();
		}
	}

	void read(int command) throws Exception { // Requests a response
		Tuple tuple = new Tuple(command);
		try {
			sOutput.writeObject(tuple);
			listenFromServer(command);
		} catch (IOException e) {
			e.printStackTrace();
			if (gui != null)
				gui.statusMessage("Connection to server lost!", true);
			close();
		}
	}

	void putread(int command, Object data) throws Exception { // Sends data and waits for response
		Tuple tuple = new Tuple(command);
		tuple.put(data);
		try {
			sOutput.writeObject(tuple);
			listenFromServer(command);
		} catch (IOException e) {
			e.printStackTrace(); // OS, IS, Socket
			if (gui != null)
				gui.statusMessage("Connection to server lost!", true);
			close();
		}
	}
	
	private void listenFromServer(int cmdSent) throws Exception {
		try {
			Tuple tuple;
			int cmdReceived;
			
			do {
				
			tuple = (Tuple) sInput.readObject();
			
			cmdReceived = tuple.getCommand();
			
			if (cmdReceived == Tuple.MESSAGE){
				if (gui != null)
					gui.statusMessage((String) tuple.getData());
				else
					System.out.println((String) tuple.getData());
			}
			} while (!(cmdReceived == cmdSent || cmdReceived == Tuple.ERROR));
			
			switch (cmdReceived) {
			case Tuple.SHOWGAMES:
				printGames((List<?>) tuple.getData());
				break;
			case Tuple.STARTGAME:
				startGame((String) tuple.getData());
				break;
			case Tuple.QUESTION:
				printQuestion((String) tuple.getData());
				break;
			case Tuple.CHOICES: // Server returns choices as List<String>
				printChoices((List<?>) tuple.getData());
				break;
			case Tuple.SCORES: // Server returns scores as HashMap<User, Integer>
				printScores((HashMap<?, ?>) tuple.getData());
				break;
			case Tuple.ERROR:
				throw ((Exception) tuple.getData());
			default: // LOGIN, CREATEGAME, JOINGAME, STARTGAME, QUESTION
				if (tuple.getData() != null){
					if (gui != null)
						gui.statusMessage((String) tuple.getData());
					else
						System.out.println((String) tuple.getData());
				}
				break;
			}
		} catch(IOException e) {
			if (gui != null){
				gui.statusMessage(e.getMessage(), true);
			} else {
				e.printStackTrace();
			}
		}
		catch(ClassNotFoundException e2) {}
	}

	private void printGames(List<?> gameNames) {
		if (gui != null){
			if (gameNames.isEmpty())
				gui.statusMessage("No available games.");
			else
				gui.refreshGameList(gameNames);
		} else {
			if (gameNames.isEmpty())
				System.out.println("No available games.");
			else
				for (Object gameName : gameNames)
					System.out.println((String) gameName);
		}
	}
	
	private void startGame(String msg) {
		if (gui != null){
			if (msg.equals("start")) {
				gui.receiveChatMessage("Game starting!");
				gui.startGame();
				try {
					chatThread.join();
				} catch (InterruptedException e) {
					System.err.println("ChatThread interrupted current thread.");
				}
			} else
				gui.receiveChatMessage(msg);
		} else {
			if (msg.equals("start")) {
				System.out.println("Game starting! Wish your opponents good luck!");
				hasGameStarted = true;
				try {
					chatThread.join();
				} catch (InterruptedException e) {
					System.err.println("ChatThread interrupted current thread.");
				}
			} else
				System.out.println(msg);
		}
	}

	private void printQuestion(String question) {
		if (gui != null){
			if (question.equals("Game over."))
				gui.showGameover();
			else
				gui.receiveQuestion(question);
		} else {
			if (question.equals("Game over."))
				isGameOver = true;
			System.out.println(question);
		}
	}
	
	private void printChoices(List<?> choices) {
		if (gui != null)
			gui.refreshChoicesList(choices);
		else {
			int i = 0;
			for (Object choice : choices) {
				System.out.println(++i + ": " + (String) choice);
			}
			players = choices.size();
		}
	}

	private void printScores(HashMap<?,?> scores) {
		if (gui != null){
			gui.refreshScoreArea(scores);
		} else {
			System.out.println("Users with their corresponding score:");
			for (Entry<?, ?> entry : scores.entrySet())
				System.out.println((String) entry.getKey() + ": " + (int) entry.getValue());
		}
	}
	
	void ChatThreadRunning(){
		while (!socket.isClosed()) {
			try {
				Tuple tuple = (Tuple) sInput.readObject();
				
				int cmdReceived = tuple.getCommand();
				
				switch (cmdReceived) {
				case Tuple.SHOWGAMES:
					printGames((List<?>) tuple.getData());
					break;
				case Tuple.STARTGAME:
					startGame((String) tuple.getData());
					break;
				case Tuple.QUESTION:
					printQuestion((String) tuple.getData());
					break;
				case Tuple.CHOICES: // Server returns choices as List<String>
					printChoices((List<?>) tuple.getData());
					break;
				case Tuple.SCORES: // Server returns scores as HashMap<User, Integer>
					printScores((HashMap<?, ?>) tuple.getData());
					break;
				case Tuple.ERROR:
					throw ((Exception) tuple.getData());
				case Tuple.MESSAGE: // LOGIN, CREATEGAME, JOINGAME, STARTGAME, QUESTION, MESSAGE
					if (gui != null)
						gui.receiveChatMessage((String) tuple.getData());
					else
						System.out.println((String) tuple.getData());
					break;
				}
			} catch (Exception e) {
				if (gui != null)
					gui.statusMessage(e.getMessage(), true);
				else
					System.out.println(e.getMessage());
			}
		}
	}
	
	class ChatThread extends Thread {
		public void run() {
			ChatThreadRunning();
		}
	}
	
	public void close() { //Try to close everything in this thread. Should always be called before thread runs out.
		try {
			if (sOutput != null) sOutput.close();
		} catch (IOException e) {}
		try {
			if (sInput != null) sInput.close();
		} catch (IOException e) {}
		try {
			if (socket != null) socket.close();
		} catch (IOException e) {}
	}
}
