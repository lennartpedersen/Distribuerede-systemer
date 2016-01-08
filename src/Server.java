
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
	
	//Fields
	private int port = 1500; //the port number to listen for connections
	private ServerSocket serverSocket; //the socket used by the server
	private ArrayList<ClientThread> threadlist = new ArrayList<ClientThread>(); //a list of currently active threads and tasks. Allows to close threads and sockets.
	
	public QuestionDB questionsDatabase = new QuestionDB(); //Question database
	public ArrayList<User> clientList = new ArrayList<User>(); //List of users and active game. Should be written and read as file?
	public HashMap<String, Game> gameList = new HashMap<String, Game>(); //List of games, key is name of the game as String. Should be written and read as file?
	
	//Constructor
	public Server() {}
	
	//Methods
	//Methods for server management
	public void start() {
		//create socket server and wait for connection requests on port
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException("Failed to open port "+port, e);
		}
		try {
			//infinite loop to wait for connections
			while(true) {
				Socket socket = serverSocket.accept();  	//wait for connection to accept
				ClientThread thread = new ClientThread(socket);
				threadlist.add(thread);		//make a new ClientThread to handle connection
				thread.start();
			}
		} catch (SocketException e) { //Exception thrown if server socket is closed while Socket.accept() is running.
			System.err.println("Server is closing...");
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() throws IOException{ //Stops the server and closes sockets. Call with GUI thread.
		serverSocket.close();
		for (ClientThread thread : threadlist){
			thread.close();
		}
		System.err.println("Server closed.");
	}
	
	public void remove(ClientThread clientThread) { //Remove finished thread from threadlist.
		threadlist.remove(clientThread);
	}
	
	public static void main(String[] args) {
		//create a server and start it
		Server server = new Server();
		server.start();
	}
	
	public void newUser(String name) throws Exception {
		if (name.length() == 0)
			throw new Exception("The name entered is blank.");
		if (name.length() > 10)
			throw new Exception("Name must be 10 characters or less.");
		if (!alphabeticName(name))
			throw new Exception("Name must contain alphabetic characters only.");
		if (nameExists(name))
			throw new Exception("A user with the entered name already exists.");
		clientList.add(new User(name));
	}
	
	public boolean alphabeticName(String name) {
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (!Character.isAlphabetic(ch))
				return false;
		} return true;
	}

	public boolean nameExists(String name) {
		for (User user : clientList)
			if (name.equals(user.getName())) 
				return true;		
		return false;
	}
	
	//Methods for managing games
	public void newGame(String name, Game game){ //Add a game to collection.
		//TODO Check for duplicates before putting. Respond to duplicates.
		gameList.put(name, game);
	}
	
	public void addUser(String gamename, User user){ //Add new user/player to game.
		//TODO Check if game exists, respond to client that requested the operation.
		Game game = gameList.get(gamename);
		//Add user to the game
		game.addUser(user);
		//Add user to clientList, for keeping track of current active game for user
		user.setGame(game);
		clientList.add(user);
	}
	
	public void startGame(String gamename){ //Start a specified game.
		//TODO Check if game exists, respond.
		gameList.get(gamename).beginGame();
	}
	
	public void getQuestion(String gamename){ //Return current question for specified game.
		//TODO
		gameList.get(gamename).getCurrentQuestion();
	}
	
	public void evaluateChoices(){ //No idea what this does...
		//TODO
	}
	
	class ClientThread extends Thread { //Threads used to perform tasks for individual clients
		//Fields
		Socket socket; //the client's connection
		ObjectInputStream sInput; //input from client
		ObjectOutputStream sOutput; //output to client
		Command task; //current task

		//Constructor
		ClientThread(Socket socket) {
			this.socket = socket;
			//Creating datastreams to transfer data between server and client
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
			}
			catch (IOException e) {
				System.err.println("Connection to Client '"+socket.getInetAddress()+"' failed.");
				close();
			}
		}

		//Methods
		public void run() { //Decode command object and perform necessary tasks.
			while (readData()){ //Read Command object from inputstream. Decode command only if data reading is successful.
				//Decode Commmand object and perform task.
				String command = task.getCommand();
				switch (command){ //Decode command switch
					case "requestgame": //Creates a new game
						//TODO Ping Pong with Client in case of error.
						ArrayList<User> users = new ArrayList<User>();
						users.add(task.getUser());
						ArrayList<Question> questions = questionsDatabase.getQuestions(int); //TODO Need number of questions for game.
						newGame(task.getGameName(), new Game(users, questions, task.getGameSize()));
						break;
					case "joingame": //Add user to an active game
						addUser(task.getGameName(), task.getUser());
						break;
					//Add new command here.
					/*
					case "":
						
						break;
					case "":
						
						break;
					case "":
						
						break;
					*/
					default:
						System.err.println("You are an idiot Thomas. You forgot a command! Unknown Command. Closed connection.");
						close();
						break;
				}
			}
			remove(this); //Removes this ClientThread from Server's threadlist.
			close(); //Closes all streams. DO NOT REMOVE.
		}
		
		public boolean sendData(Object data){ //Sends object to connected client.
			try {
				sOutput.writeObject(data);
				return true;
			} catch (IOException e) {
				System.err.println("Error sending data to client.");
				close();
			}
			return false;
		}
		
		public boolean readData(){ //Reads object from connected client.
			try {
				task = (Command) sInput.readObject();
				return true;
			} catch (ClassNotFoundException e) {
				System.err.println("Invalid data from client '"+socket.getInetAddress()+"', connection closed.");
				close();
			} catch (IOException e) {
				System.err.println("Connection with client '"+socket.getInetAddress()+"' interrupted.");
				close();
			}
			return false;
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

}

