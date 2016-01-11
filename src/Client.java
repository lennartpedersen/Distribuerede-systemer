import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.ws.handler.MessageContext.Scope;

public class Client  {
	//Fields
	private ObjectInputStream sInput; //Datastream for receiving data from socket
	private ObjectOutputStream sOutput; //Datastream for sending data to socket
	private Socket socket; //The connection to the server
	
	private InetAddress server; //Server address.
	private int port = 1500; //Server port.
	private String gameName;
	private User user;
	private static Client client;
	private boolean gameStarted = false;

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
			
			//create reader for messages from user
			BufferedReader scan = new BufferedReader(new InputStreamReader(System.in));
			//loop forever for messages typed by the user
			while(true) {
				startLoginPhase();
				System.out.println("Options: \n Join game \n Request game \n Write your choice:" );
				String option = scan.readLine();
				while(!option.equals("Join game") && !option.equals("Request game")) {
					System.out.println("Incorrect input. Please try again");
					option = scan.readLine();
				}
				System.out.println("Write your arguments (GameName Gamesize) \n (Separate with spaces): \n ");
				String argument = scan.readLine();
				StringTokenizer st = new StringTokenizer(argument);
				String gameName = "";
				int gameSize = 0;
				while(st.hasMoreTokens()) {
					if(option.equals("Join game")) {
						gameName = st.nextToken();
					} else {
						gameName = st.nextToken();
						gameSize = Integer.parseInt(st.nextToken());
					}
				}
				if(option.equals("Join game")) {
					client.put(joinGame(gameName,user));
					 
				}
				else if(option.equals("Request game")) {
					client.put(requestGame(gameSize,gameName));
					client.put(joinGame(gameName,user)); //Automatically join the game you have created.
				}
				
				
				//Players can put a start-game request - if all players have requested this then the game start.
				System.out.println("When you are ready to begin the game please enter: Start");
				String start = scan.readLine();
				while(!start.equals("Start")) {
					System.out.println("Input not correct. To begin game enter: Start");
					start = scan.readLine();
				} 
				client.put(startGameRequest(true));
				
				// Sets gameStarted to true when all players are ready
				client.read(phase("Phase"));
				
				while (gameStarted) {
					// Get question from server
					client.read(question("Question"));

					// Answer question
					// System.out.println("Enter answer:"); send from server pls
					String answer = scan.readLine();
					client.put(answer(answer));

					// Get choices from server
					client.read(choices("Choices"));

					// Pick choice
					// System.out.println("Choose an answer by its index:");
					// send from server pls
					answer = scan.readLine();
					client.put(choise(Integer.parseInt(answer)));

					// If game is done, set gameStarted to false
					client.read(phase("Phase"));

				}
				
				client.read(scores("Scores"));
				
				//System.out.println("Game over."); //Display final scores?
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		//create the Thread to listen for messages from the server 
		new ListenFromServer().start();
		//success! the client is connected!
	}
	
	void put(Object command) { //To send a message to the server
		try {
			sOutput.writeObject(command);
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
		BufferedReader scan = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Login - enter username: ");
		String name;
		try {
			name = scan.readLine();
			client.put(login(name)); //TODO Check if username is already taken
		} catch (IOException e) {
			System.out.println("Error scanning line.");
		}
	}

	public void read(Object command) { //For get-commands: get questions, get answers.
		try {
			sOutput.writeObject(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	class ListenFromServer extends Thread { //ListenServer is a Thread that listens for messages from the server and displays them real-time in the console as they are received.
		public void run() {
			while(true) {
				try {
					Command tuple = (Command) sInput.readObject();
					//Analyze object and do task.
					
					switch (tuple.get(0)) {
					case "login":
						if (tuple.get(1))
							user = new User(tuple.get(2));
						else
							System.out.println("Username taken.");
						break;
					case "questions":
						System.out.println(tuple.get(1));
						break;
					case "scores":
						printScores(tuple.get(1));
						break;
					case "phase":
						if (tuple.get(1) == 3)
							gameStarted = false;
						else if (tuple.get(1) >= 0)
							gameStarted = true;
						break;
					}
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				catch(ClassNotFoundException e2) {}
			}
		}

		private void printScores(HashMap<User, Integer> scores) {
			System.out.println("Users with their corresponding score:");
			for (Map.Entry<User, Integer> user : scores.entrySet()) 
				System.out.println(((User) user).getName() + " " + user.getValue());
		}
	}
	
	public Object joinGame(String gameName, User user){
		return new Command("joinGame",gameName,user);
	}
	
	public Object requestGame(int gameSize, String gameName) {
		return new Command("requestGame",gameName, gameSize);
	}
	
	public Object login(String userName) {
		return new Command("login", userName);
	}
	
	public Object question(String question) {
		return new Command(question);
	}
	
	public Object startGameRequest(boolean b) {
		return new Command("startGameRequest",true);
	}
	
	public Object choise(int choice) {
		return new Command("choice", choice);
	}
	
	public Object answer(String answer) {
		return new Command("answer", answer);
	}
	
	public Object choices(String choices) {
		return new Command(choices);
	}
	
	public Object phase(String phase) {
		return new Command(phase);
	}
	
	public Object scores(String scores) {
		return new Command(scores);
	}
}
