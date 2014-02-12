import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;


/**
 * 
 * @author emrickgj
 *
 *
 * The BattleshipServer is the Server end of the project. It handles the locations of the ship, 
 * takes in guesses from a client, tells if it was a hit or not, and basically keeps track of the entire
 * game. Upon the game ending, restarts and listens for another client.
 */
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
	
	
	/**
	 * 
	 * @author emrickgj
	 *
	 * Enum type to store the different GameStatus' and their values
	 */
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
	
	/**
	 * 
	 * @author emrickgj
	 *
	 *
	 * Enum type to store the different MoveStatus' and their values
	 */
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
	
	/**
	 * 
	 * @author emrickgj
	 *
	 * Enum type to store the Ships, their locations, their hits, and whether or not they have sunk
	 */
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
	
	
	
	/**
	 * Constructor for the Server, inits the server socket, prints out the server info, and starts the game loop
	 */
	public BattleshipServer(){
		
		initServerSocket();
		printServerInfo();
		gameLoop();
		
	}
	
	
	/**
	 * Loop that runs the Server end of the Battleship game
	 */
	private void gameLoop(){
		
		while(true){
			
			initGameBoard();
			listenForClient();
			createStreams();
			
			
			//Loop for playing the game with a client!
			while(inProgress){
				listenForGuess();
			}
			closeStreams();
			inProgress = true;
		}
		
	}
	
	/**
	 * Listens for a guess from the user
	 */
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
	
	/**
	 * Register a hit on said location
	 * @param x : The x location
	 * @param y : The y location
	 */
	public void registerHit(int x, int y){
		sendResponse(MoveStatus.HIT.id, GameStatus.CONTINUE.id);
	}
	
	/**
	 * Send a response to the client
	 * @param moveStatus : The move status
	 * @param gameStatus : The game status
	 */
	private void sendResponse(int moveStatus, int gameStatus){
		
		try{
			dos.writeInt(moveStatus);
			dos.writeInt(gameStatus);
		}catch(IOException ex){
			System.err.println("Error sending the status");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Close the streams with the client
	 */
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
	
	/**
	 * Listens for the client and accepts the incoming connection
	 */
	private void listenForClient(){
		
		try {
			client = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Error Accepting Socket Connection");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Creates all the streams for the server to the client
	 */
	private void createStreams(){
		
		try{
		dos = new DataOutputStream(client.getOutputStream());
		dis = new DataInputStream(client.getInputStream());
		}catch(IOException ex){
			System.err.println("Error creating Input/Output Streams");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Initialized the game board and places the new pieces
	 */
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
		
		//Loop starts at 4 (BattleShip) and ends at 2 (Cruiser). Uses this value to also place them on board
		for(int i = 4; i > 1; i--){
			
			//Uses this variable to modify our ships down below
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
			
			//Should it place the ship, or restart the search
			boolean shouldPlace = true;
			
			//Loop to find an acceptable location
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
			
			//If it was an acceptable spot, add it!
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
			
			}//End inner loop
			
		}//End outer loop
		
		
		
		//Print the board now that all the ships have been placed
		printBoard();
	
	}
	
	/**
	 * Print out the servers info to the console, launches on startup of the server
	 */
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
				else if(board[(x-1)*row+(y-1)] != 0){
					System.out.print(board[(x-1)*row+(y-1)]);//Print out the value here
				}else{
					System.out.print("_");
				}
				
				System.out.print("|\t");
			}
			System.out.println();
		}
		
	}
	
	/**
	 * Initializes the server socket to listen on the selected port
	 */
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
     * Main Method
     * @param args
     */
	public static void main(String[] args) {
		
		new BattleshipServer();

	}

}
