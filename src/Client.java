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
	private static Client client;
	private boolean gameStarted = true;
	private BufferedReader scan;

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
			//loop forever for messages typed by the user
			while(true) {
				startLoginPhase();
				
				optionPhase();
				
				while (gameStarted) {
					// Get question from server
					client.read(question());

					// Answer question
					System.out.println("Enter answer:"); //send from server pls
					String answer = scan.readLine();
					client.put(answer(answer));

					// Get choices from server
					client.read(choices());

					// Pick choice
					System.out.println("Choose an answer by its number:");
					int choice = getInteger();
					client.put(choose(choice));

					// If game is done, set gameStarted to false
					client.read(phase());

				}
				
				client.read(scores());
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void startLoginPhase() {
		System.out.println("Login - enter username: ");
		String name;
		try {
			name = scan.readLine();
			client.put(login(name));
			listenFromServer();
		} catch (IOException e) {
			System.out.println("Error scanning line.");
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
				client.read(requestNewGame(gameName, gameSize, gameLength));
			}
			
			client.read(joinGame(gameName)); 
			// Players can put a start-game request - if all players have
			// requested this then the game starts.
			System.out.println("When you are ready to begin the game please enter: Start");
			String start = scan.readLine().toLowerCase();
			while (!start.equals("start")) {
				System.out.println("Input not correct. To begin game enter: Start");
				start = scan.readLine();
			}
			
			client.read(requestStartGame());
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

	private void read(Tuple tuple) { //For get-commands: get questions, get answers.
		try {
			sOutput.writeObject(tuple);
			listenFromServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void listenFromServer() {
		try {
			Tuple tuple = (Tuple) sInput.readObject();
			
			switch (tuple.getCommand()) {
			case Tuple.ERROR:
				System.out.println(((Exception) tuple.get(0)).getMessage());
				break;
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
	}

	private void printScores(HashMap<?, ?> hashMap) {
		System.out.println("Users with their corresponding score:");
		for (Map.Entry<?, ?> user : hashMap.entrySet()) 
			System.out.println(((User) user).getName() + " " + (int) user.getValue());
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
	
	private Tuple joinGame(String name) {
		Tuple tuple = new Tuple(3);
		tuple.put(name);
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
