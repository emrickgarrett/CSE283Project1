import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class BattleshipServer {
	
	//Init Package scope variables
	ServerSocket serverSocket = null;
	Socket client = null;
	final static int PORT = 32100;
	DataOutputStream dos = null;
	DataInputStream dis = null;
	
	//Game Variables
	private int maxGuesses = 25;
	
	
	//Defines the Board size and board creation
	private int row = 10;
	private int col = 10;
	private int[][] board = new int[row][col];
	
	
	//Enum for GameStatus
	private enum GameStatus{
		
		CONTINUE(10),
		CLIENT_WON(20),
		CLIENT_LOST(30),
		ILLEGAL_MOVE(40);
		
		int id;
		GameStatus(int id){
			this.id = id;
		}
		
	}
	
	//Enum for MoveStatus
	private enum MoveStatus{
		
		MISS(10),
		HIT(20),
		SINK(30),
		ILLEGAL_MOVE(40);
		
		int id;
		MoveStatus(int id){
			this.id = id;
		}
		
	}
	
	
	
	
	public BattleshipServer(){
		
		initServerSocket();
		printServerInfo();
		
	}
	
	public void printServerInfo(){
		// Display contact information.
		try {
			System.out.println( 
					"Number Server standing by to accept Clients:"			
							+ "\nIP Address: " + InetAddress.getLocalHost() 
							+ "\nPort: " + serverSocket.getLocalPort() 
							+ "\n\n" );
		} catch (UnknownHostException e) {
			System.err.println("Couldn't get local IP");
			e.printStackTrace();
		}	
	}
	
	private void initServerSocket(){
		//Init the server socket
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.err.println("Error Creating Server Socket");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		new BattleshipServer();

	}

}
