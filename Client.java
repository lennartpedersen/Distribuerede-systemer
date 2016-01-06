package tuplespacepro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client  {
	//Fields
	private ObjectInputStream sInput; //Datastream for receiving data from socket
	private ObjectOutputStream sOutput; //Datastream for sending data to socket
	private Socket socket; //The connection to the server
	
	private InetAddress server; //Server address.
	private int port = 1500; //Server port.

	//Constructor
	Client() throws UnknownHostException {
		//server = InetAddress.getByName("2.106.191.230");
		server = InetAddress.getByName("10.16.165.221");

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
	
	void sendMessage(String msg) { //To send a message to the server
		try {
			sOutput.writeObject(msg);
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
			String msg = scan.readLine();
			client.sendMessage(msg);
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
}
