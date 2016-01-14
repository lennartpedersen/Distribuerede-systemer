import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
	private boolean gameStarted;
	private BufferedReader scan;
	private int players = 0;

	//Constructor
	public Client() throws UnknownHostException {
		server = InetAddress.getByName("127.0.0.1");
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
				
				gameStarted = true;
				
				while (gameStarted) {

					boolean hasQuestion = false,
							hasAnswer = false,
							hasChoices = false,
							hasChoice = false,
							hasScores = false;
					
					while (!hasQuestion) {
						try {
							client.read(question());
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
							client.read(answer(answer));
							hasAnswer = true;
						} catch(IOException e) {
							System.out.println("Error reading line.");
						} catch (Exception e) {
							System.out.println(e.getMessage());
							answer = getNewAnswer(answer);
							hasAnswer = true;
						}
					}
					
					
					// Get choices from server
					while (!hasChoices) {
						try {
							client.read(choices());
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
					
					try {
						client.put(choose(choice));
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					
					while (!hasScores) {
						try {
							client.read(scores());
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
		System.out.println("Login - enter username: ");
		String name;
		boolean userLoggedIn = false;
		while (!userLoggedIn) {
			try {
				name = scan.readLine();
				client.read(login(name));
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
				hasJoinedGame = false, 
				hasRequestedStartGame = false;
		
		System.out.println("Options: \n Join game \n Host game \n Write your choice:");
		String option = "";
		while(!hasOption) {
			try {
				option = scan.readLine().toLowerCase();
				if (option.equals("join game") || option.equals("host game"))
					hasOption = true;
				else
					System.out.println("Incorrect input. Try again.");
			} catch (IOException e) {
				System.out.println("Error scanning line.");
			}
		}

		System.out.println("Write the name of the game:");
		String gameName = getGameName();
		int gameSize = 0;
		int gameLength = 0;


		if (option.equals("host game")) {
			System.out.println("Write the maximum number of players:");
			gameSize = getInteger();
			System.out.println("Write the number of rounds:");
			gameLength = getInteger();
			
			while (!hasRequestedNewGame) {
				try {
					client.read(requestNewGame(gameName, gameSize, gameLength));
					hasRequestedNewGame = true;
				} catch (Exception e) {
					System.out.println(e.getMessage());
					System.out.println("Write new game name:");
					gameName = getGameName();
				}
			}
		}
		
		while (!hasJoinedGame) {
			try {
				client.read(joinGame(gameName));
				hasJoinedGame = true;
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println("Write new game name:");
				gameName = getGameName();
			}
		}
		
		// Players can put a start-game request - if all players have
		// requested this then the game starts.
		System.out.println("When you are ready to begin the game please enter: Start");
		String start = "";
		while (!hasRequestedStartGame) {
			try {
				start = scan.readLine().toLowerCase();
				if (start.equals("start")) {
					client.read(requestStartGame());
					hasRequestedStartGame = true;
				} else
					System.out.println("Incorrect input. To begin game enter: Start");
			} catch (IOException e) {
				System.out.println("Incorrect input. Try again.");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
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
				if (newAnswer.contains(answer) || answer.contains(newAnswer))
					System.out.println("Write an incorrect answer.");
				else {
					client.read(answer(newAnswer));
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

	private void put(Tuple tuple) { //To send a message to the server
		try {
			sOutput.writeObject(tuple);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		//create the Client
		client = new Client();
		//start the connection to the Server.
		client.start();	
	}

	private void read(Tuple tuple) throws Exception { //For get-commands: get questions, get answers.
		try {
			sOutput.writeObject(tuple);
			listenFromServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void listenFromServer() throws Exception {
		try {
			Tuple tuple = (Tuple) sInput.readObject();
			
			switch (tuple.getCommand()) {
			case Tuple.ERROR:
				throw ((Exception) tuple.get(0));
			case Tuple.QUESTION: // Server returns the question
				System.out.println((String) tuple.get(0));
				break;
			case Tuple.CHOICES: // Server returns choices as List<String>
				printChoices((List<?>) tuple.get(0));
				break;
			case Tuple.PHASE: // Server returns the game's phase
				int phase = (int) tuple.get(0);
				if (phase == 3)
					gameStarted = false;
				break;
			case Tuple.SCORES: // Server returns scores as HashMap<User, Integer>
				printScores((HashMap<?, ?>) tuple.get(0));
				break;
			default:
				System.out.println((String) tuple.get(0));
				break;
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(ClassNotFoundException e2) {}
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

	private Tuple login(String name) {
		Tuple tuple = new Tuple(Tuple.LOGIN);
		tuple.put(name);
		return tuple;
	}
	
	private Tuple requestNewGame(String name, int size, int length) {
		Tuple tuple = new Tuple(Tuple.REQUESTNEWGAME);
		tuple.put(name);
		tuple.put(size);
		tuple.put(length);
		return tuple;
	}
	
	private Tuple joinGame(String name) {
		Tuple tuple = new Tuple(Tuple.JOINGAME);
		tuple.put(name);
		return tuple;
	}
	
	private Tuple answer(String answer) {
		Tuple tuple = new Tuple(Tuple.ANSWER);
		tuple.put(answer);
		return tuple;
	}
	
	private Tuple choose(int choice) {
		Tuple tuple = new Tuple(Tuple.CHOOSE);
		tuple.put(choice);
		return tuple;
	}
	
	private Tuple question() {
		Tuple tuple = new Tuple(Tuple.QUESTION);
		return tuple;
	}
	
	private Tuple requestStartGame() {
		Tuple tuple = new Tuple(Tuple.REQUESTSTARTGAME);
		return tuple;
	}
	
	private Tuple choices() {
		Tuple tuple = new Tuple(Tuple.CHOICES);
		return tuple;
	}
	
	private Tuple scores() {
		Tuple tuple = new Tuple(Tuple.SCORES);
		return tuple;
	}
}
