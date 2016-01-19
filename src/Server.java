
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Server {
	
	//Fields
	private int port = 1500; //the port number to listen for connections
	private ServerSocket serverSocket; //the socket used by the server
	private ArrayList<ClientThread> threadList = new ArrayList<ClientThread>(); //a list of currently active threads and tasks. Allows to close threads and sockets.
	
	private QuestionDB questionsDatabase = new QuestionDB(); //Question database
	private HashMap<User, ClientThread> clientList = new HashMap<User, ClientThread>(); //List of users, their active game and their clientthread.
	private HashMap<String, Game> gameList = new HashMap<String, Game>(); //List of games, key is name of the game as String. Should be written and read as file?
	
	private ObserverThread observer;
	private boolean hasObserver;
	
	//Constructor
	public Server(boolean hasObserver) {
		this.hasObserver = hasObserver;
		if (hasObserver){
			observer = new ObserverThread();
			observer.start();
		}
	}
	
	//Methods
	//Methods for server management
	public void startServer() {
		//create socket server and wait for connection requests on port
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException("Failed to open port " + port, e);
		}
		try {
			//infinite loop to wait for connections
			while(true) {
				Socket socket = serverSocket.accept();  	//wait for connection to accept
				synchronized (threadList) {
					ClientThread thread = new ClientThread(socket); //make a new ClientThread to handle connection
					threadList.add(thread);
					thread.start();
					if (hasObserver) System.out.println("Connected to client: "+socket.getInetAddress().getHostAddress());
				}
			}
		} catch (SocketException e) { //Exception thrown if server socket is closed while Socket.accept() is running.
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Something went horribly wrong.", e);
		} finally {
			System.out.println("Server is closing...");
			stopServer();
			System.out.println("Server closed.");
		}
	}
	
	public void stopServer() { //Stops the server and closes sockets. Call with GUI thread.
		try {
			if (!serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {}
		synchronized(threadList){
			for (ClientThread thread : threadList){
				if (thread != null)
					thread.close();
			}
		}
	}
	
	private void remove(ClientThread clientThread) { //Remove finished thread from threadlist.
		synchronized (threadList) {
			threadList.remove(clientThread);
		}
	}
	
	public static void main(String[] args) {
		//create a server and start it
		Server server = new Server(true);
		server.startServer();
	}
	
	private void loginUser(ClientThread thread, String name) throws Exception { //Checks if entered name is allowed. Client-side?
		synchronized (clientList) {
			
			if (name.length() == 0)
				throw new Exception("The name entered is blank.");
			if (name.length() > 10)
				throw new Exception("Name must be 10 characters or less.");
			if (!alphabeticName(name))
				throw new Exception("Name must contain alphabetic characters only.");
			if (userExists(name))
				throw new Exception("A user with the entered name already exists.");
			
			thread.user = new User(name);
			addUser(thread);
			sendStatus(thread, Tuple.LOGIN, "User created.");
		}
	}
	
	private boolean alphabeticName(String name) {
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (!Character.isAlphabetic(ch))
				return false;
		} return true;
	}

	private boolean userExists(String name) {
		for (User user : clientList.keySet())
			if (name.equals(user.getName()))
				return true;
		
		return false;
	}
	
	private void addUser(ClientThread thread){ //Add user as an active client.
		clientList.put(thread.user, thread);
		if (hasObserver) System.out.println("Client added: "+thread.user.getName());
	}
	
	private void removeUser(User user) { //Removes user from clientlist. Quit game. Username is freed.
		Game game;
		if ((game = user.getGame()) != null)
			game.removeUser(user);
		clientList.remove(user);
		if (hasObserver) System.out.println("Client removed: "+user.getName());
	}
	
	//Methods for managing games
	private void showGames(ClientThread thread) {
		ArrayList<String> availableGames = new ArrayList<String>();
		
		for (Map.Entry<String, Game> entry : gameList.entrySet()) 
			if (!entry.getValue().getGameStarted())
				availableGames.add(entry.getKey());
		
		Tuple tuple = new Tuple(Tuple.SHOWGAMES);
		tuple.put(availableGames);
		thread.sendData(tuple);
	}
	
	private void createGame(ClientThread thread, ArrayList<?> data) throws Exception{ //Add a game to collection.		
		synchronized (gameList) {

			String name = (String) data.get(0);
			int size = (int) data.get(1);
			int length = (int) data.get(2);
			
			List<Question> questions = questionsDatabase.getQuestions(length);
			
			if (gameExists(name))
				throw new Exception("Game with that name already exists.");
			
			Game game = new Game(this, questions, name, size);
			gameList.put(name, game);
			sendStatus(thread, Tuple.CREATEGAME, "Game created.");
		}
	}
	
	private void joinGame(ClientThread thread, String name) throws Exception { //Add new user/player to game.
		if (!gameExists(name))
			throw new Exception("Game with that name doesn't exist.");
		
		User user = thread.user;
		Game game = gameList.get(name);
		List<User> users = game.getUsers();
		
		if (!users.isEmpty()) {
			Tuple tuple = new Tuple(Tuple.MESSAGE);
			tuple.put(user.getName() + " joined the game.");
			sendToAll(users, tuple);
		}
		
		game.addUser(user);
		user.setGame(game);
		sendStatus(thread, Tuple.JOINGAME, "Joined game.");
		game.readyStatus();
	}
	
	void removeGame(String gameName){
		synchronized(gameList){
			gameList.remove(gameName);
		}
		if (hasObserver) System.out.println("Game removed: "+gameName);
	}
	
	private boolean gameExists(String name) {
		return gameList.containsKey(name);
	}
	
	private void requestStart(User user, ArrayList<?> data) {
		boolean hasRequestedStart = (boolean) data.get(0);
		String msg = (String) data.get(1);
		Game game = user.getGame();
		game.requestStart(user, hasRequestedStart, msg);
	}

	private void requestQuestion(User user) throws Exception {
		Game game = user.getGame();
		game.requestQuestion();
	}

	private void requestChoices(User user) {
		Game game = user.getGame();
		game.requestChoices();
	}
	
	private void requestScore(User user) {
		Game game = user.getGame();
		game.requestScores();
	}
	
	private void addAnswer(ClientThread thread, String answer) throws Exception {
		User user = thread.user;
		Game game = user.getGame();
		game.addAnswer(user, answer);
		sendStatus(thread, Tuple.ANSWER, "Answer recieved.");

	}
	
	private void addChoice(User user, int choice) { //User chooses an answer they believe is the correct answer.
		Game game = user.getGame();
		game.addChoice(user, choice - 1);
	}
	
	private void sendStatus(ClientThread thread, int command, String status) {
		Tuple tuple = new Tuple(command);
		tuple.put(status);
		thread.sendData(tuple);
	}
	
	void sendToAll(List<User> users, Tuple data){ //Send the given data to all users on the given list.
		for (User user : users) {
			ClientThread thread = clientList.get(user);
			if (null != thread)
				thread.sendData(data);
		}	
	}
	
	class ClientThread extends Thread { //Threads used to perform tasks for individual clients
		//Fields
		private Socket socket; //the client's connection
		private ObjectInputStream sInput; //input from client
		private ObjectOutputStream sOutput; //output to client
		private User user;
		private Tuple tuple; //current task

		//Constructor
		ClientThread(Socket socket) {
			this.socket = socket;
			//Creating datastreams to transfer data between server and client
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				this.socket.setSoTimeout(3000);
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
				
				int command = tuple.getCommand();
				
				try {
					switch (command) { //Decode command switch
						case Tuple.LOGIN:
							loginUser(this, (String) tuple.getData()); // Throws exception
							if (hasObserver) System.out.println(socket.getLocalAddress().getHostAddress()+" logged in as "+user.getName());
							break;
						case Tuple.SHOWGAMES:
							showGames(this);
							if (hasObserver) System.out.println(socket.getLocalAddress().getHostAddress()+" : "+user.getName()+" requested game list.");
							break;
						case Tuple.CREATEGAME: //Creates a new game
							createGame(this, (ArrayList<?>) tuple.getData());
							if (hasObserver) System.out.println(socket.getLocalAddress().getHostAddress()+" : "+user.getName()+" created a new game.");
							break;
						case Tuple.JOINGAME: //Add user to an active game
							joinGame(this, (String) tuple.getData()); // Throws exception
							if (hasObserver) System.out.println(socket.getLocalAddress().getHostAddress()+" : "+user.getName()+" joined game "+((String) tuple.getData()));
							break;
						case Tuple.STARTGAME:
							requestStart(user, (ArrayList<?>) tuple.getData());
							break;
						case Tuple.QUESTION:
							requestQuestion(user);
							break;
						case Tuple.ANSWER:
							addAnswer(this, (String) tuple.getData()); // Throws exception
							break;
						case Tuple.CHOICES:
							requestChoices(user);
							break;
						case Tuple.CHOOSE:
							addChoice(user, (int) tuple.getData());
							break;
						case Tuple.SCORES:
							requestScore(user);
							break;
						default:
							System.err.println(socket.getInetAddress().getHostAddress()+": Unknown Command received. Closed connection.");
							close();
							break;
					}
				} catch (Exception e) {
					Tuple tuple = new Tuple(Tuple.ERROR);
					tuple.put(e);
					sendData(tuple);
				}
			}
			close(); //Closes all streams. DO NOT REMOVE.
		}
		
		private boolean sendData(Tuple tuple){ //Sends object to connected client.
			try {
				sOutput.writeObject(tuple);
				return true;
			} catch (IOException e) {
				System.err.println("Error sending data to client.");
				close();
			}
			return false;
		}
		
		private boolean readData(){ //Reads object from connected client.
			boolean dataReceived = false;
			while (dataReceived){
				try {
					try {
						tuple = (Tuple) sInput.readObject();
						dataReceived = true;
						return true;
					} catch (SocketTimeoutException e){
						sendData(new Tuple(Tuple.HANDSHAKE));
					}
				} catch (ClassNotFoundException e) {
					System.err.println("Invalid data from client '"+socket.getInetAddress()+"', connection closed.");
					close();
				} catch (IOException e) {
					System.err.println("Connection with client '"+socket.getInetAddress()+"' interrupted.");
					close();
				}	
			}
			return false;
		}
		
		private void close() { //Try to close everything in this thread. Should always be called before thread runs out.
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
			remove(this); //Removes this ClientThread from Server's threadlist.
		}
	}
	
	class ObserverThread extends Thread { //Basic GUI thread for server, used for troubleshooting and observing. Should not be used to configure the server in any way.
		//Constructor
		ObserverThread(){}
		
		//Methods
		public void run(){ //Reads input from console (System.in) to receive commands.
			BufferedReader cInput = new BufferedReader(new InputStreamReader(System.in));
			
			boolean quitting = false;
			while (!quitting){
				try {
					String tokens[];
					do {
						tokens = cInput.readLine().split("[^a-zA-Z]+");
					} while (1 > tokens.length);
					
					switch(tokens[0].toLowerCase()){
					case "clients": //Prints all active clients in the clientList.
					case "client":
						printClients();
						break;
					case "threads":
					case "thread":
						printThreads();
						break;
					case "games":
					case "game":
						printGames();
						break;
					case "quit":
					case "exit":
						quitting = true;
						stopServer();
						break;
					default:
						System.out.println("Type to list:");
						System.out.println("Clients");
						System.out.println("Games");
						System.out.println("Threads");
						System.out.println();
						System.out.println("Type 'quit' to shutdown the server.");
						break;
					}
					
				} catch (IOException e) {
					System.err.println("Something went wrong with Observer.");
					e.printStackTrace();
				}
			}
		}

		private void printGames() { //Prints all active games in the gameList.
			System.out.println("List of active games:");
			Iterator<Entry<String, Game>> iterator = gameList.entrySet().iterator();
			int index = 0;
			if (!iterator.hasNext())
				System.out.println("No currently active games");
			while (iterator.hasNext()){
				System.out.println(index++ +" : "+iterator.next().getKey());
			}
			System.out.println();
		}

		private void printThreads() { //Prints all active clientThreads in the threadList.
			System.out.println("List of active client threads:");
			Iterator<ClientThread> iterator = threadList.iterator();
			int index = 0;
			if (!iterator.hasNext())
				System.out.println("No currently active client threads");
			while (iterator.hasNext()){
				System.out.println(index++ +" : "+iterator.next().socket.getInetAddress().getHostAddress());
			}
			System.out.println();
		}

		private void printClients() { //Prints all active clients in the clientList.
			System.out.println("List of active clients:");
			Iterator<Entry<User, ClientThread>> iterator = clientList.entrySet().iterator();
			int index = 0;
			if (!iterator.hasNext())
				System.out.println("No currently active clients");
			while (iterator.hasNext()){
				Entry<User, ClientThread> currentEntry = iterator.next();
				System.out.println(index++ +" : "+currentEntry.getValue().socket.getInetAddress().getHostAddress()+":"+currentEntry.getKey().getName());
			}
			System.out.println();
		}
	}
}

