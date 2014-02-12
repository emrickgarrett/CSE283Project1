import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;


public class BattleshipServer {
	
	//Init Package scope variables
	ServerSocket serverSocket = null;
	Socket client = null;
	final static int PORT = 32100;
	DataOutputStream dos = null;
	DataInputStream dis = null;
	boolean inProgress = true;
	
	//Game Variables
	private int maxGuesses = 25;
	
	
	//Defines the Board size and board creation
	private int row = 10;
	private int col = 10;
	private int[] board = new int[row*col];
	
	
	//Enum for GameStatus
	public enum GameStatus{
		
		CONTINUE(10),
		CLIENT_WON(20),
		CLIENT_LOST(30),
		ILLEGAL_MOVE(40);
		
		final int id;
		GameStatus(int id){
			this.id = id;
		}
		
	}
	
	//Enum for MoveStatus
	public enum MoveStatus{
		
		MISS(10),
		HIT(20),
		SINK(30),
		ILLEGAL_MOVE(40);
		
		final int id;
		MoveStatus(int id){
			this.id = id;
		}
		
	}
	
	
	
	
	public BattleshipServer(){
		
		initServerSocket();
		printServerInfo();
		gameLoop();
		
	}
	
	private void gameLoop(){
		
		while(true){
			
			initGameBoard();
			listenForClient();
			createStreams();
			
			
			//Loop for playing the game!
			while(inProgress){
				listenForGuess();
			}
			closeStreams();
			inProgress = true;
		}
		
	}
	
	private void listenForGuess(){
		
		int guess = -1;
		
		try{
			guess = dis.readInt();
		}catch(Exception ex){
			System.err.println("Error reading in the Guess!");
			ex.printStackTrace();
		}
		
		//If the user is quitting the game
		if(guess == -1){
			inProgress = false;
		}
		
		else if(guess >= 100 || guess <0){
			sendResponse(MoveStatus.ILLEGAL_MOVE.id, GameStatus.ILLEGAL_MOVE.id);
			inProgress = false;
			return;
		}
		
		//The user made a guess that can be applied to the game!
		else{
			int y = guess/col;
			int x = guess%row;
			
			registerHit(x, y);
			
			//Test to make sure it is printing out the correct input
			//System.out.print("X: " + x + " Y: " + y);
		}
		
	}
	
	public void registerHit(int x, int y){
		
	}
	
	private void sendResponse(int moveStatus, int gameStatus){
		
		try{
			dos.writeInt(moveStatus);
			dos.writeInt(gameStatus);
		}catch(IOException ex){
			System.err.println("Error sending the status");
			ex.printStackTrace();
		}
	}
	
	private void closeStreams(){
		
		try{
			dos.close();
			dis.close();
			client.close();
		}catch(IOException ex){
			System.err.println("Error closing your streams");
			ex.printStackTrace();
		}
		
		dos = null;
		dis = null;
		client = null;
	}
	
	private void listenForClient(){
		
		try {
			client = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Error Accepting Socket Connection");
			e.printStackTrace();
		}
		
	}
	
	private void createStreams(){
		
		try{
		dos = new DataOutputStream(client.getOutputStream());
		dis = new DataInputStream(client.getInputStream());
		}catch(IOException ex){
			System.err.println("Error creating Input/Output Streams");
			ex.printStackTrace();
		}
	}
	
	private void initGameBoard(){
		
		for(int i = 0; i < row*col; i++){
			board[i] = 0;
		}
		
		
		placeShips();
	}
	
	private void placeShips(){
		//Need to place 3 ships (Destroyer, Battleship, and Cruiser?)
		Random randSmall = new Random(2);
		
		System.out.println("RandBig: " + ((int)(Math.random()*100)+1) + " " + ((int)(Math.random()*100)+1));
		System.out.println("RandSmall: " + ((int)(Math.random()*2)+1) + " " + ((int)(Math.random()*2)+1));
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
	
	/**
	 * 
	 * Method to store result, main reason is to "remind" one of the numbers/meanings.
	 * Not necessary to use as we have access to the board
	 * 
	 * @param x : X location
	 * @param y : Y location
	 * @param result : Result to store (0 = Blank, 1 = Hit, 2 = Miss)
	 */
	private void storeResult(int x, int y, int result){
		board[row * x + y] = result;
	}
	
	private String getResult(int x, int y){
		switch(board[row * x + y]){
		case 0 : return "_";
		case 1 : return "H";
		case 2 : return "M";
		default: return "_";
		}
	}

	public static void main(String[] args) {
		
		new BattleshipServer();

	}

}
