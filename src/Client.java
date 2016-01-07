import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class Client  {
	//Fields
	private ObjectInputStream sInput; //Datastream for receiving data from socket
	private ObjectOutputStream sOutput; //Datastream for sending data to socket
	private Socket socket; //The connection to the server
	
	private InetAddress server; //Server address.
	private int port = 1500; //Server port.
	private String gameName;
	private User user = new User();

	//Constructor
	Client() throws UnknownHostException {
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
		Client client = new Client();
		//start the connection to the Server.
		client.start();	
		//create reader for messages from user
		BufferedReader scan = new BufferedReader(new InputStreamReader(System.in));
		//loop forever for messages typed by the user
		while(true) {
			System.out.println("Login - enter username: ");
			String name = scan.readLine();
			login(name);
			System.out.println("Options: \n Join game \n Request game \n Write your choice:" );
			String option = scan.readLine();
			System.out.println("Write your arguments: \n (Separate with spaces) \n ");
			String argument = scan.readLine();
			StringTokenizer st = new StringTokenizer(argument);
			String gameName;
			int gameSize;	
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
				get("questions"); //Get questions-tuple from server. Wait until questions are received - then print: type answer 
			}
			else if(option.equals("Request game")) {
				client.put(requestGame(gameSize,gameName));
				client.put(joinGame(gameName,user));
			}
		}
	}

	
	
	class ListenFromServer extends Thread { //ListenServer is a Thread that listens for messages from the server and displays them real-time in the console as they are received.
		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					System.out.println(msg);
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				catch(ClassNotFoundException e2) {}
			}
		}
	}
	public static Object joinGame(String gameName, User user){
		return new Command("joinGame",gameName,user);
	}
	
	public static Object requestGame(int gameSize, String gameName) {
		return new Command("requestGame",gameSize, gameName);
	}
	
	public static void login(String userName) {
		user.setName(userName);
	}
}
