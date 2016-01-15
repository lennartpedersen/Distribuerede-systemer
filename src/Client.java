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
	private boolean gameOver;
	private BufferedReader scan;
	private int players = 0;

	//Constructor
	public Client() throws UnknownHostException {
		server = InetAddress.getByName("127.0.0.1");
	}
	
	public static void main(String[] args) throws IOException {
		//create the Client
		client = new Client();
		//start the connection to the Server.
		client.start();	
	}
	
	//Methods
	private void start() {
		//try to connect to the server.
		try {
			socket = new Socket(server, port);
			// creating data streams to receive and send data.
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
			
			//create reader for messages from user
			scan = new BufferedReader(new InputStreamReader(System.in));
			
			loginPhase();
			
			while (true) {
				
				optionPhase();
				
				startPhase();
				
				while (true) { // gamePhase
					
					try {
						client.read(Tuple.NEWROUND);
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					
					if (gameOver)
						break;

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

					// Answer question
					String answer = "";
					
					while (!hasAnswer) {
						try {
							answer = scan.readLine();
							client.putread(Tuple.ANSWER, answer);
							hasAnswer = true;
						} catch(IOException e) {
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
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loginPhase() {
		boolean userLoggedIn = false;
		
		System.out.println("Login - enter username: ");
		String name = "";
		
		while (!userLoggedIn) {
			try {
				name = scan.readLine();
				client.putread(Tuple.LOGIN, name);
				userLoggedIn = true;
			} catch (IOException e) {
				System.out.println("Error scanning line.");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	private void optionPhase() {
		boolean hasOption = false,
				hasRequestedNewGame = false, 
				hasJoinedGame = false;
		
		System.out.println("Options: \n Join game \n Create game \n Show games \n Write your choice:");
		String option = "";
		
		while(!hasOption) {
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

		switch (option) {
		case "show games":
			try {
				client.read(Tuple.SHOWGAMES);
				optionPhase();
			} catch (Exception e) {
				e.getMessage();
			}
			break;
			
		case "create game":
			
			System.out.println("Write the name of the game:");
			gameName = getGameName();
			System.out.println("Write the maximum number of players:");
			gameSize = getInteger();
			System.out.println("Write the number of rounds:");
			gameLength = getInteger();
			
			ArrayList<Object> data = new ArrayList<Object>();
			data.add(gameName);
			data.add(gameSize);
			data.add(gameLength);
			
			while (!hasRequestedNewGame) {
				try {
					client.putread(Tuple.CREATEGAME, data);
					hasRequestedNewGame = true;
				} catch (Exception e) {
					System.out.println(e.getMessage());
					System.out.println("Write new game name:");
					gameName = getGameName();
					data.set(0, gameName);
				}
			}
			
		case "join game":
			while (!hasJoinedGame) {
				try {
					client.putread(Tuple.JOINGAME, gameName);
					hasJoinedGame = true;
				} catch (Exception e) {
					System.out.println(e.getMessage());
					System.out.println("Write new game name:");
					gameName = getGameName();
				}
			}
			break;
			
		} // End switch (option)
	}
	
	private void startPhase() {
		boolean hasRequestedStartGame = false;		
		
		System.out.println("When you are ready to begin the game please enter: Start");
		String start = "";
		
		while (!hasRequestedStartGame) {
			try {
				start = scan.readLine().toLowerCase();
				if (start.equals("start")) {
					System.out.println("Start requested.");
					hasRequestedStartGame = true;
				} else
					System.out.println("Incorrect input. To begin game enter: Start");
			} catch (IOException e) {
				System.out.println("Error reading line.");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		gameOver = false;
	}
	
	private String getGameName() {
		boolean hasGameName = false;
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

	private void put(int command, Object data) { // Sends data
		Tuple tuple = new Tuple(command);
		tuple.put(data);
		try {
			sOutput.writeObject(tuple);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void read(int command) throws Exception { // Requests a response
		Tuple tuple = new Tuple(command);
		try {
			sOutput.writeObject(tuple);
			listenFromServer(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void putread(int command, Object data) throws Exception { // Sends data and waits for response
		Tuple tuple = new Tuple(command);
		tuple.put(data);
		try {
			sOutput.writeObject(tuple);
			listenFromServer(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void listenFromServer(int cmdSent) throws Exception {
		try {
			Tuple tuple = (Tuple) sInput.readObject();
			
			int cmdReceived = tuple.getCommand();
			
			if (cmdReceived == cmdSent || cmdReceived == Tuple.ERROR) {
				switch (cmdReceived) {
				case Tuple.SHOWGAMES:
					printGames((List<?>) tuple.getData());
					break;
				case Tuple.NEWROUND: // Server returns the game's status
					newRound((boolean) tuple.getData());
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
					System.out.println((String) tuple.getData());
					break;
				}
			} else {
				System.out.println((String) tuple.getData());
				listenFromServer(cmdSent);
			}
			
			
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(ClassNotFoundException e2) {}
	}
	
	private void printGames(List<?> gameNames) {
		if (gameNames.isEmpty())
			System.out.println("No available games.");
		else
			for (Object gameName : gameNames)
				System.out.println((String) gameName);
	}
	
	private void newRound(boolean hasQuestion) {
		if (hasQuestion)
			System.out.println("Next Question:");
		else {
			System.out.println("Game over.");
			gameOver = true;
		}
	}
	
	private void printChoices(List<?> choices) {
		int i = 0;
		for (Object choice : choices) {
			System.out.println(++i + ": " + (String) choice);
		}
		players = choices.size();
	}

	private void printScores(HashMap<?,?> scores) {
		System.out.println("Users with their corresponding score:");
		
		for (Entry<?, ?> entry : scores.entrySet())
			System.out.println((String) entry.getKey() + ": " + (int) entry.getValue());
	}
}
