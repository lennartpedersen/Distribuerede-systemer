
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class Server {
	
	//Fields
	private int port = 1500; //the port number to listen for connections
	private ServerSocket serverSocket; //the socket used by the server
	private ArrayList<ClientThread> threadlist = new ArrayList<ClientThread>(); //a list of currently active threads and tasks. Allows to close threads and sockets.
	
	public QuestionDB questionsDatabase = new QuestionDB(); //Question database
	public HashMap<User, ClientThread> clientList = new HashMap<User, ClientThread>(); //List of users, their active game and their clientthread.
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
				ClientThread thread = new ClientThread(socket); //make a new ClientThread to handle connection
				threadlist.add(thread);
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
	
	public boolean isUserAllowed(String name) throws Exception { //Checks if entered name is allowed. Client-side?
		if (name.length() == 0)
			throw new Exception("The name entered is blank.");
		if (name.length() > 10)
			throw new Exception("Name must be 10 characters or less.");
		if (!alphabeticName(name))
			throw new Exception("Name must contain alphabetic characters only.");
		if (nameExists(name))
			throw new Exception("A user with the entered name already exists.");
		return true;
	}
	
	public void addUser(User user, ClientThread clientThread){ //Add user as an active client.
		clientList.put(user, clientThread);
	}
	
	public void removeUser(User user) { //Removes user from clientlist. Quit game. Username is freed.
		clientList.remove(user);
	}
	
	public boolean alphabeticName(String name) {
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (!Character.isAlphabetic(ch))
				return false;
		} return true;
	}

	public boolean nameExists(String name) {
		Iterator<Entry<User, ClientThread>> iterator = clientList.entrySet().iterator();
		while (iterator.hasNext())
			if (name.equals(iterator.next().getKey().getName())) 
				return true;		
		return false;
	}
	
	public boolean gameExists(String name) {
		return gameList.containsKey(name);
	}
	
	//Methods for managing games
	public void newGame(String name, List<Question> questions, int size) throws Exception{ //Add a game to collection.
		if (gameExists(name))
			throw new Exception("Game with that name already exists.");
		Game game = new Game(this, questions, size);
		gameList.put(name, game);
	}
	
	public void addUserToGame(String gamename, User user) throws Exception{ //Add new user/player to game.
		if (!gameExists(gamename))
			throw new Exception("Game with that name doesn't exist.");
		Game game = gameList.get(gamename);
		//Add user to the game
		game.addUser(user);
		//Add user to clientList, for keeping track of current active game for user
		user.setGame(game);
	}
	
	public void chooseAnswer(User user, int choice) throws Exception { //User chooses an answer they believe is the correct answer.
		Game game = user.getGame();
		if (game == null)
			throw new Exception("You must join a game before you can be choose an answer.");
		String answer = game.getListOfAnswers().get(choice);
		game.addChoice(user, answer);
	}
	
	public void sendToAll(List<User> list, Tuple data){ //Send the given data to all users on the given list.
		Iterator<User> iterator = list.iterator();
		while(iterator.hasNext()){
			ClientThread thread = clientList.get(iterator.next());
			if (thread != null)
				thread.sendData(data);
		}	
	}
	
	public void requestStartGame(User user) throws Exception { //Request the start of the game tied to given user.
		Game game = user.getGame();
		if (game == null)
			throw new Exception("You must join a game before you can be ready.");
		game.requestStartGame();
	}

	public void requestQuestion(User user) throws Exception {
		Game game = user.getGame();
		if (game == null)
			throw new Exception("You must join a game before you can be choose an answer.");
		game.requestQuestion();
	}

	public void requestChoices(User user) throws Exception {
		Game game = user.getGame();
		if (game == null)
			throw new Exception("You must join a game before you can be choose an answer.");
		game.requestChoices();
	}
	
	public void addAnswer(User user, String answer) throws Exception {
		Game game = user.getGame();
		if (game == null)
			throw new Exception("You must join a game before you can be choose an answer.");
		game.addAnswer(user, answer);
	}
	
	class ClientThread extends Thread { //Threads used to perform tasks for individual clients
		//Fields
		Socket socket; //the client's connection
		ObjectInputStream sInput; //input from client
		ObjectOutputStream sOutput; //output to client
		User user;
		Tuple tuple; //current task

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
				
				try {
					String name;
					switch (tuple.getCommand()){ //Decode command switch
						case Tuple.LOGIN:
							name = (String) tuple.get(0);
							if (isUserAllowed(name)) {
								user = new User(name);
								addUser(user, this);
								sendStatus("User created.");
							}
							break;
						case Tuple.REQUESTNEWGAME: //Creates a new game
							name = (String) tuple.get(0);
							int size = (int) tuple.get(1);
							int length = (int) tuple.get(2);
							List<Question> questions = questionsDatabase.getQuestions(length);
							newGame(name, questions, size);
							sendStatus("Game created.");
							break;
						case Tuple.JOINGAME: //Add user to an active game
							name = (String) tuple.get(0);
							addUserToGame(name, user);
							sendStatus("Joined game.");
							break;
						case Tuple.REQUESTSTARTGAME:
							requestStartGame(user);
							sendStatus("Start requested.");
							break;
						case Tuple.CHOOSE:
							int choice = (int) tuple.get(0);
							chooseAnswer(user, choice);
							break;
						case Tuple.ANSWER:
							String answer = (String) tuple.get(0);
							addAnswer(user, answer);
							sendStatus("Answer recieved.");
							break;
						case Tuple.QUESTION:
							requestQuestion(user);
							break;
						case Tuple.CHOICES:
							requestChoices(user);
							break;
							//Add new command here.
							/*
						case "":
							
							break;
							 */
						default:
							System.err.println("You are an idiot Thomas. You forgot a command! Unknown Command. Closed connection.");
							close();
							break;
					}
				} catch (Exception e) {
					Tuple tuple = new Tuple(Tuple.ERROR);
					tuple.put(e);
					sendData(tuple);
				}
			}
			remove(this); //Removes this ClientThread from Server's threadlist.
			close(); //Closes all streams. DO NOT REMOVE.
		}
		
		private void sendStatus(String status) {
			Tuple tuple = new Tuple(-1);
			tuple.put(status);
			sendData(tuple);
		}
		
		public boolean sendData(Object object){ //Sends object to connected client.
			try {
				sOutput.writeObject(object);
				return true;
			} catch (IOException e) {
				System.err.println("Error sending data to client.");
				close();
			}
			return false;
		}
		
		public boolean readData(){ //Reads object from connected client.
			try {
				tuple = (Tuple) sInput.readObject();
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
			removeUser(user);
		}
	}
}

