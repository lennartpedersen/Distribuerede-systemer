import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
	private boolean questionsReceived = false, scoresReceived = false;

	//Constructor
	public Client() throws UnknownHostException {
		server = InetAddress.getByName("127.0.0.1");
		user = new User("HEY");
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
				while(option.equals("Join game") == false && option.equals("Request game") == false) {
					System.out.println("Incorrect input. Please try again");
					option = scan.readLine();
				}
				System.out.println("Write your arguments(GameName Gamesize) \n (Separate with spaces): \n ");
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
				while(start.equals("Start") == false) {
					System.out.println("Input not correct. To begin game enter: Start");
					start = scan.readLine();
				} 
				client.put(startGameRequest(true));	
				for(int i = 0; i < gameSize; i++) {		
					//Get questions-tuple from server. Wait until questions are received - then prints: type answer
					client.get(getQuestions("Questions"));
					while(!questionsReceived) {
						//wait until questions are received
					}
					questionsReceived = false;
					System.out.println("Type the number of the answer you think is correct");
					//Print questions
					
					
					int choice = Integer.parseInt(scan.readLine());
					client.put(choice); //Sends choice of answer to server. 
					client.get("scores"); //Receives scores as an array. Needs to output scores to the user.
					while(!scoresReceived) {
						//wait until questions are received
					}
					scoresReceived = false;
					
					
					//new round starts - loop to getQuestions in for loop of length = number of Q's
					System.out.println("New round starts");
				}
				System.out.println("Game over."); //Display final scores?
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
			login(name); //TODO Check if username is already taken
		} catch (IOException e) {
			System.out.println("Error scanning line.");
		}
	}

	public void get(Object command) { //For get-commands: get questions, get answers.
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
					Command task = (Command) sInput.readObject();
					//Analyze object and do task.
					if(task.getCommand().equals("Questions")) {
						questionsReceived = true;
						printQuestions();
					} else if(task.getCommand().equals("scores")) {
						scoresReceived = true;
						printScores();
					}
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				catch(ClassNotFoundException e2) {}
			}
		}

		private void printQuestions() {
		// TODO Print Questions received from server
		
	}

		private void printScores() {
			// TODO Print array with scores received from game.
			
		}
	}
	public Object joinGame(String gameName, User user){
		return new Command("joinGame",gameName,user);
	}
	
	public Object requestGame(int gameSize, String gameName) {
		return new Command("requestGame",gameName, gameSize);
	}
	
	public void login(String userName) {
		user.setName(userName);
	}
	
	public Object getQuestions(String string) {
		return new Command(string);
	}
	
	public Object startGameRequest(boolean b) {
		return new Command("startGameRequest",true);
	}
	
	public static Object choiseOfAnswer(int choice) {
		return new Command("choiceOfAnswer", choice);
	}

}
