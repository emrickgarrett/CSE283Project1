import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class BattleshipClient {
	
	//Networking Variables
	Socket server;
	DataOutputStream dos = null;
	DataInputStream dis = null;
	InetAddress sIP = null;
	
	//Board Variables
	int row = 10;
	int col = 10;
	int board[];
	
	//Boolean to remain true while the game is in progress
	boolean inProgress = true;
	
	//Scanner to get user input
	Scanner scan;
	
	
	
	
	public BattleshipClient(){
		
		//Scanner is initialized in getServerIp
		getServerIP();
		establishConnection();
		createStreams();
		initBoard();
		
		//Game Loop methods
		while(inProgress){
			printBoard();
			getTurn();
			while(true);
		}
		
		closeStreams();
	}
	
	/****************Game Loop Methods Below ******************/
	
	/**
	 * Prints the board below, slightly different than what we had before, I wanted to 
	 * give the user an easier way to identify the rows, and make the board a lot easier to see.
	 */
	private void printBoard(){
		
		board[row*2 + 2] = 1;
		board[row*5 + 4] = 2;
		board[row*7 + 9] = 2;
		
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
				else if(board[row*(x-1) + (y-1)] == 0){
					System.out.print("_");
				}
				else if(board[row*(x-1) + (y-1)] == 1){
					System.out.print("H");
				}
				else if(board[row*(x-1) + (y-1)] == 2){
					System.out.print("M");
				}
				
				System.out.print("|\t");
			}
			System.out.println();
		}
		
	}
	
	private void getTurn(){
		System.out.println("‘M’ indicates “miss.” ‘H’ indicates “hit.” " +
				"Enter your Move (enter negative number to quit)");
		
		System.out.print("\nSelected row: ");
		int t_row = scan.nextInt();
		System.out.print("Selected Column: ");
		int t_col = scan.nextInt();
		System.out.println();
		
		//If either number is negative, quit
		if(t_row < 0 || t_col < 0)
			quitGame();
		
		//If number was positive, send guess and get response
		sendGuess(t_row, t_col);
		getResponse(t_row, t_col);
	}
	
	private void sendGuess(int x, int y){
		
		try{
			dos.writeInt(row*x+y);
		}catch(IOException ex){
			System.err.println("Error sending your guess to the server");
			ex.printStackTrace();
		}
	}
	
	private void getResponse(int t_row, int t_col){
		int moveStatus = 0;
		int gameStatus = 0;
		
		try{
			moveStatus = dis.readInt();
			gameStatus = dis.readInt();
		}catch(IOException ex){
			System.err.println("Error getting status from the server!");
			ex.printStackTrace();
		}
		
		switch(moveStatus){
		case 10: 
			board[row*t_row + t_col] = 2;
			break;
		case 20:
			board[row*t_row + t_col] = 1;
			break;
		case 30: 
			//Sink
			break;
		case 40:
			//Illegal Move
			break;
		}
		
		switch(gameStatus){
		case 10:
			//Continue
			break;
		case 20:
			//Game over, Player won
			break;
		case 30:
			//Game over, Player lost
			break;
		case 40:
			//Illegal Move, you dun goofed
			break;
		}
	}
	
	private void quitGame(){
		System.out.println("Thank you for playing, Come again!");
		System.exit(0);
	}
	
	
	/**************** Board Methods Below *********************/
	
	
	private void initBoard(){
		board = new int[row*col];
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
	
	
	/********************** Networking Methods Below *********************/
	
	private void closeStreams(){
		try{
			dos.close();
			dis.close();
			server.close();
			scan.close();
		}catch(IOException ex){
			System.err.println("Error closing your streams");
			ex.printStackTrace();
		}
	}
	
	private void createStreams(){
		
		try {
			dos = new DataOutputStream(server.getOutputStream());
		} catch (IOException e1) {
			System.err.println("Error creating Output Stream");
			e1.printStackTrace();
		}
		
		
		try {
			dis = new DataInputStream(server.getInputStream());
		} catch (IOException e) {
			System.err.println("Error creating Input Stream");
			e.printStackTrace();
		}
		
	}
	
	private void establishConnection(){
		
		try {
			server = new Socket(sIP, BattleshipServer.PORT);
		} catch (IOException e) {
			System.err.println("Error Creating Socket for the Server");
			e.printStackTrace();
		}
	}
	
	private void getServerIP(){
		
		scan = new Scanner(System.in);
		
		System.out.print("Please enter the server's IP address: ");
		
		try {
			sIP = InetAddress.getByName(scan.next()+ "");
		} catch (UnknownHostException e) {
			System.err.println("Could not resolve the Inet Address");
			e.printStackTrace();
		}
		
		System.out.println();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new BattleshipClient();
	}

}
