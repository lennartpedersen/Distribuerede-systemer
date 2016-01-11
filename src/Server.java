
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	
	public void addUser(User user){ //Add user as an active client.
		clientList.add(user);
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
		for (User user : clientList)
			if (name.equals(user.getName())) 
				return true;		
		return false;
	}
	
	public boolean gameExists(String name) {
		return gameList.containsKey(name);
	}
	
	//Methods for managing games
	public void newGame(String name, Game game) throws Exception{ //Add a game to collection.
		if (gameExists(name))
			throw new Exception("Game with that name already exists.");
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
		clientList.add(user);
	}
	
	public void startGame(String gamename) throws Exception{ //Start a specified game.
		if (!gameExists(gamename))
			throw new Exception("Game with that name doesn't exist.");
		gameList.get(gamename).beginGame();
	}
	
	public Question getQuestion(String gamename){ //Return current question for specified game.
		return gameList.get(gamename).getCurrentQuestion();
	}
	
	public void evaluateChoices(){ //No idea what this does...
		//TODO
	}
	
	public boolean requestStartGame(User user) throws Exception { //Request the start of the game tied to given user.
		Game game = user.getGame();
		if (game == null)
			throw new Exception("You must join a game before you can be ready.");
		game.addRequest(user);
		return game.isGameReady();
	}
	
	public void chooseAnswer(User user, int choice) throws Exception { //User chooses an answer they believe is the correct answer.
		Game game = user.getGame();
		if (game == null)
			throw new Exception("You must join a game before you can be choose an answer.");
		String answer = game.getListOfAnswers().get(choice);
		game.addChoice(user, answer);
	}
	
	class ClientThread extends Thread { //Threads used to perform tasks for individual clients
		//Fields
		Socket socket; //the client's connection
		ObjectInputStream sInput; //input from client
		ObjectOutputStream sOutput; //output to client
		User user;
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
				try {
					switch (command){ //Decode command switch
						case "login":
							isUserAllowed(task.getUser().getName()); //Check if name already exists. Throws exception currently, why it is not in an if statement.
							clientList.add(user);
							user = task.getUser();
							break;
						case "requestgame": //Creates a new game
							ArrayList<User> users = new ArrayList<User>();
							users.add(task.getUser());
							List<Question> questions = questionsDatabase.getQuestions(task.getGameLength());
							newGame(task.getGameName(), new Game(users, questions, task.getGameSize()));
							break;
						case "joingame": //Add user to an active game
							addUserToGame(task.getGameName(), task.getUser());
							break;
						case "startGameRequest":
							if (requestStartGame(user)){
								sendData(new Command("questions", user.getGame().getCurrentQuestion()));
							}
							break;
						case "choice":
							chooseAnswer(user, task.getChoice());
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
				}catch (Exception e){
					sendData(new Command("Error", e));
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
			removeUser(user);
		}
	}
}

