package tuplespacepro;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	
	//Fields
	private static int clientCount; //number of clients connected, used to identify each client
	private ArrayList<ClientThread> al; //an arrayList to keep the list of the clients
	private int port = 1500; //the port number to listen for connections
	
	//Constructor
	public Server() {
		al = new ArrayList<ClientThread>();
	}
	
	//Methods
	public void start() {
		//create socket server and wait for connection requests on port
		try 
		{
			//the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			//infinite loop to wait for connections
			while(true) 
			{
				System.out.println("Server waiting for Clients on port " + port);
				
				Socket socket = serverSocket.accept();  	//accept connection
				ClientThread t = new ClientThread(socket);  //make a thread for it
				al.add(t);									//save the thread in the list
				t.start();
				System.out.println("New User connected!");
			}
		}
		catch (IOException e) {
            e.printStackTrace();
		}
	}		
    
	private synchronized void broadcast(String message) { //Used to broadcast a message to all Clients
		//loop through clients to send message through output datastreams, remove users if it fails (disconnected).
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			//try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(message)) {
				al.remove(i);
				System.out.println("Disconnected Client.");
			}
		}
	}
	
	public static void main(String[] args) {
		//create a server and start it
		Server server = new Server();
		server.start();
	}
	
	
	
	class ClientThread extends Thread { //Thread used to read messages from each client
		//Fields
		Socket socket; //the clients connection
		ObjectInputStream sInput; //input for client
		ObjectOutputStream sOutput; //output for client
		
		int id; //unique id

		//Constructor
		ClientThread(Socket socket) {
			//a unique id
			id = ++clientCount;
			this.socket = socket;
			//Creating datastreams to transfer data between server and client
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
			}
			catch (IOException e) {
				return;
			}
		}

		//Methods
		public void run() { //will run forever
			while(true) {
				//read a message
				try {
					String message = (String) sInput.readObject();
					//broadcast message to all clients
					broadcast(message);
				}
				catch (Exception e) {
					break;
				}
			}
		}

		private boolean writeMsg(String msg) { //write a message to the Client output stream if it is still connected.
			//if Client is still connected send the message to it, else return false to disconnect client
			if(socket.isConnected()) {
				//write the message to the stream
				try {
					sOutput.writeObject(msg);
				}
				catch(IOException e) { //IO error
					System.out.println("Error sending message");
				}
				return true;
			}
			else {
				return false;
			}
		}
	}
}

