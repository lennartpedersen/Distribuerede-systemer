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
import java.util.Map;

public class Client  {
	//Fields
	private ObjectInputStream sInput; //Datastream for receiving data from socket
	private ObjectOutputStream sOutput; //Datastream for sending data to socket
	private Socket socket; //The connection to the server
	
	private InetAddress server; //Server address.
	private int port = 1500; //Server port.
	private User user;
	private static Client client;
	private boolean gameStarted = false;
	private BufferedReader scan;

	//Constructor
	public Client() throws UnknownHostException {
		server = InetAddress.getByName("127.0.0.1");
	}
	
	//Methods
	public void start() {
		//try to connect to the server.
		try {
			socket = new Socket(server, port);
			// creating data streams to receive and send data.
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());

			//create the Thread to listen for messages from the server 
			new ListenFromServer().start();
			//success! the client is connected!
			
			//create reader for messages from user
			scan = new BufferedReader(new InputStreamReader(System.in));
			//loop forever for messages typed by the user
			while(true) {
				startLoginPhase();
				
				optionPhase();
				
				client.put(requestStartGame());
				
				// Sets gameStarted to true when all players are ready
				client.read(phase());
				
				while (gameStarted) {
					// Get question from server
					client.read(question());

					// Answer question
					// System.out.println("Enter answer:"); send from server pls
					String answer = scan.readLine();
					client.put(answer(answer));

					// Get choices from server
					client.read(choices());

					// Pick choice
					// System.out.println("Choose an answer by its index:");
					// send from server pls
					answer = scan.readLine();
					client.put(choose(Integer.parseInt(answer)));

					// If game is done, set gameStarted to false
					client.read(phase());

				}
				
				client.read(scores());
				
				//System.out.println("Game over."); //Display final scores?
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void optionPhase() {
		try {
			System.out.println("Options: \n Join game \n Host game \n Write your choice:");
			String option = scan.readLine().toLowerCase();
			while (!option.equals("join game") && !option.equals("host game")) {
				System.out.println("Incorrect input. Please try again");
				option = scan.readLine().toLowerCase();
			}
			
			String gameName = "";
			int gameSize = 0;
			int gameLength = 0;
			

			System.out.println("Write the name of the game:");
			gameName = scan.readLine().toLowerCase();
			
			if (option.equals("host game")) {
				System.out.println("Write the maximum number of players:");
				gameSize = getInteger();
				System.out.println("Write the number of rounds:");
				gameLength = getInteger();
				client.put(requestNewGame(gameName, gameSize, gameLength));
			}
			
			client.put(joinGame(gameName, user)); 

			// Players can put a start-game request - if all players have
			// requested this then the game starts.
			System.out.println("When you are ready to begin the game please enter: Start");
			String start = scan.readLine().toLowerCase();
			while (!start.equals("start")) {
				System.out.println("Input not correct. To begin game enter: Start");
				start = scan.readLine();
			}
		} catch (IOException e) {
			System.out.println("Error reading line.");
		}
	}
	
	private int getInteger() {
		String readline;
		int integer = 0;
		boolean isInt = false;
		
		while (!isInt) {
			try {
				readline = scan.readLine();
				integer = Integer.parseInt(readline);
				isInt = true;
			} catch (IOException e2) {
				System.out.println("Error reading line.");
			} catch (NumberFormatException e) {
				System.out.println("Please enter an integer.");
			}
		}
		return integer;
	}

	void put(Tuple tuple) { //To send a message to the server
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
	
	public void startLoginPhase() {
		System.out.println("Login - enter username: ");
		String name;
		try {
			name = scan.readLine();
			client.put(login(name));
		} catch (IOException e) {
			System.out.println("Error scanning line.");
		}
	}

	public void read(Tuple tuple) { //For get-commands: get questions, get answers.
		try {
			sOutput.writeObject(tuple);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	class ListenFromServer extends Thread { //ListenServer is a Thread that listens for messages from the server and displays them real-time in the console as they are received.
		public void run() {
			while(true) {
				try {
					Tuple tuple = (Tuple) sInput.readObject();
					//Analyze object and do task.
					
					switch (tuple.getCommand()) {
					case 0:
						System.out.println((String) tuple.get(0));
					case 1: // Server returns the name
						user = new User((String) tuple.get(0));
						break;
					case 6: // Server returns the question
						System.out.println(tuple.get(0));
						break;
					case 8: // Server returns choices as List<String>
						printChoices((List<?>) tuple.get(0));
						break;
					case 9: // Server returns the game's phase
						int phase = (int) tuple.get(0);
						if (phase == 3)
							gameStarted = false;
						else if (phase >= 0)
							gameStarted = true;
						break;
					case 10: // Server returns scores as HashMap<User, Integer>
						printScores((HashMap<?, ?>) tuple.get(0));
						break;
					}
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				catch(ClassNotFoundException e2) {}
			}
		}
		
		private void printChoices(List<?> choices) {
			int i = 0;
			for (Object choice : choices) {
				System.out.println(++i + ": " + (String) choice);
			}
		}

		private void printScores(HashMap<?, ?> hashMap) {
			System.out.println("Users with their corresponding score:");
			for (Map.Entry<?, ?> user : hashMap.entrySet()) 
				System.out.println(((User) user).getName() + " " + (int) user.getValue());
		}
	}
	
	private Tuple login(String name) {
		Tuple tuple = new Tuple(1);
		tuple.put(name);
		return tuple;
	}
	
	private Tuple requestNewGame(String name, int size, int length) {
		Tuple tuple = new Tuple(2);
		tuple.put(name);
		tuple.put(size);
		tuple.put(length);
		return tuple;
	}
	
	private Tuple joinGame(String name, User user) {
		Tuple tuple = new Tuple(3);
		tuple.put(name);
		tuple.put(user);
		return tuple;
	}
	
	private Tuple answer(String answer) {
		Tuple tuple = new Tuple(4);
		tuple.put(answer);
		return tuple;
	}
	
	private Tuple choose(int choice) {
		Tuple tuple = new Tuple(5);
		tuple.put(choice);
		return tuple;
	}
	
	private Tuple question() {
		Tuple tuple = new Tuple(6);
		return tuple;
	}
	
	private Tuple requestStartGame() {
		Tuple tuple = new Tuple(7);
		return tuple;
	}
	
	private Tuple choices() {
		Tuple tuple = new Tuple(8);
		return tuple;
	}
	
	private Tuple phase() {
		Tuple tuple = new Tuple(9);
		return tuple;
	}
	
	private Tuple scores() {
		Tuple tuple = new Tuple(10);
		return tuple;
	}
}
