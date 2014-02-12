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
	
	public enum SHIP{
		
		BATTLESHIP(4),
		CRUISER(3),
		DESTROYER(2);
		
		int locations[];
		int hits[];
		boolean sank;
		
		SHIP(int size){
			this.locations = new int[size];
			this.hits = new int[size];
			this.sank = false;
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
	
	/**
	 * 
	 * Place the ships, can be easily modified to place more.
	 * 
	 * It first picks a random spot on the board, and then decides if it wants to place
	 * it horizontally or vertically. It will then skim the board in that direction to see if
	 * their is room, and if there is, place it. If it cannot place it there or there isn't room,
	 * it will pick another random spot and restart.
	 * 
	 */
	private void placeShips(){
		//Need to place 3 ships (Destroyer, Battleship, and Cruiser?)
		
		for(int i = 4; i > 1; i--){
			
			
			SHIP ship = null;
			//Determine which Ship it should use
			switch(i){
			case 4:
				ship = SHIP.BATTLESHIP;
				break;
			case 3:
				ship = SHIP.CRUISER;
				break;
			case 2:
				ship = SHIP.DESTROYER;
				break;
			}
			
			
			/** Start Placement **/
			
			int locationX = (int)(Math.random()*10+1);
			int locationY = (int)(Math.random()*10+1);
			int direction = (int)(Math.random()*2+1);
			
			boolean shouldPlace = true;
			
			for(int j = 0; j < i; j++){
				
				switch(direction){
				case 1: // Horizontal
					if(locationX+j >= 10 || (locationX+j)*row+locationY >= 100 || board[(locationX+j)*row+locationY] != 0){
						shouldPlace = false;
						j = i;
						i++;
					}
					break;
				case 2: // Vertical
					if(locationY+j >= 10 || locationX*row+(locationY+j) >= 100 || board[locationX*row+(locationY+j)] != 0){
						shouldPlace = false;
						j = i;
						i++;
					}
					break;
				}
				
				
			}
			if(shouldPlace)
				for(int j = 0; j < i; j++){
					switch(direction){
					case 1: // Horizontal
						board[(locationX+j)*row + locationY] = i;
						ship.locations[j] = (locationX+j)*row + locationY;
						break;
					case 2: // Vertical
						board[locationX*row + (locationY+j)] = i;
						ship.locations[j] = locationX*row + (locationY+j);
						break;
					}
			/** DEBUGGING **/
			//if(shouldPlace)
			//System.out.println("Placed ship of size: " + ship.locations.length);
			
		}
		
			
		}
		//Print the board now that all the ships have been placed
		printBoard();
	
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
	
/****************Game Loop Methods Below ******************/
	
	/**
	 * Prints the board below, slightly different than what we had before, I wanted to 
	 * give the user an easier way to identify the rows, and make the board a lot easier to see.
	 */
	private void printBoard(){
		
		for(int y = 0; y < col+1; y++){
			for(int x = 0; x < row+1; x++){
				if(x == 0 && y == 0){
					System.out.print("#");
				}
				else if(x > 0 && x < row+1 && y == 0){
					System.out.print(x-1);
				}
				else if(y > 0 && y < col+1 && x == 0){
					System.out.print(y-1);
				}
				else {
					System.out.print(board[(x-1)*row+(y-1)]);
				}
				
				System.out.print("|\t");
			}
			System.out.println();
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
