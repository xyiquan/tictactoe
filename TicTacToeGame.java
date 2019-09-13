package tictactoe;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Xing Yiquan
 */
// Class where the main game logic of TicTacToe is implemented.
public class TicTacToeGame {

	// Messages to display after player move
	private final static String MOVE_ALREADY_MADE = "Sorry this square has been taken, please try again:";
	private final static String MOVE_MUST_BE_A_VALID_NUMBER = "Sorry your move must be a number from 1 to 9. Try again:";
	private final static String GAME_WELOME = "Welcome to the TicTacToe game! To win the game be the first to get 3 in a row.";
	private final static String GAME_WON = "Congratulations, you have won the game!";
	private final static String GAME_NO_WINNERS = "Sorry there are no winners for this game.";
	private final static String GAME_NEW_STARTING = "Ok, starting new game...\n\n";
	private final static String GAME_END = "Thank you for playing!\n";
	private final static String PLS_MAKE_A_MOVE = "Please make a move by entering a number from 1 to 9.";
	private final static String PLS_PLAY_AGAIN = "Would you like to play again? (Y/N)";
	private final static String PLS_REPLY_Y_OR_N = "Sorry, please reply with Y or N:";

	public TicTacToeGame() {
	}

	public static void main(String[] args) {

		// Register the players with their mark (i.e. symbol) that will appear on the
		// TicTacToe board.
		TicTacToePlayerRoster roster = new TicTacToePlayerRoster();
		roster.addPlayer("O");
		roster.addPlayer("X");

		// Init resources and variables
		Scanner scan = openIO();
		boolean isStartNewGame = true; // tracks if player wants to play another game

		while (isStartNewGame) {

			// Reset game state when starting a new game
			isStartNewGame = false;
			TicTacToePlayer currentPlayer = roster.getNextPlayer();
			TicTacToeBoard board = new TicTacToeBoard();

			// Prompt player to make first move
			displayMessage(GAME_WELOME);
			board.displayState();
			displayMessage("Player " + currentPlayer.getMark() + "> " + PLS_MAKE_A_MOVE);

			// Keep prompting players to make their next move and update game board as long
			// as moves are still available.
			while (true) {

				int playerPickedSquareNum = 0;

				// Checks player input is a number from 1-9 and corresponding square is not
				// taken. If not, ask player to pick another number/square.
				while (true) {
					if (scan.hasNextInt()) {

						// Square number current player picked for next move.
						playerPickedSquareNum = scan.nextInt();

						// Check number input is from 1 to 9
						if (playerPickedSquareNum < 1 || playerPickedSquareNum > 9) {
							// scan.next();// clear the current invalid input
							displayMessage(MOVE_MUST_BE_A_VALID_NUMBER);
							continue;
						}

						// Check square is not taken
						if (board.isSquareTaken(playerPickedSquareNum)) {
							// If square is taken, prompt user to choose another square
							displayMessage(MOVE_ALREADY_MADE);
							continue;
						}

						// Case player input is valid number and square is not taken
						break;

					} else {
						scan.next();// clear the current invalid non-number input
						displayMessage(MOVE_MUST_BE_A_VALID_NUMBER);
						continue;
					}
				}

				// Since input is valid and square not taken, update board that square is taken
				// by current player
				board.updateSquareNumTakenByPlayer(playerPickedSquareNum, currentPlayer.getMark());

				// Check after this move if the game has been won
				boolean isGameWon = board.isGameWon();

				// Check after this move is there are any squares left for next move
				boolean isNoMoreMoves = board.isAllSquaresTaken();

				// Case game is won by current player, so game ends
				if (isGameWon) {
					currentPlayer.incrementWins();// record win for current player
					board.displayState();
					displayMessage(GAME_WON);
					break;
				}

				// Case game it not won but there are no more squares left, so game ends
				if (!isGameWon && isNoMoreMoves) {
					board.displayState();
					displayMessage(GAME_NO_WINNERS);
					break;
				}

				// Case game is not won and there are still squares left, continue next move
				currentPlayer = roster.getNextPlayer();
				board.displayState();
				displayMessage("Player " + currentPlayer.getMark() + "> " + PLS_MAKE_A_MOVE);
			}

			// Prompts players if they want to play another game
			displayMessage(PLS_PLAY_AGAIN);

			// Checks player input for (Y)es or (N)o reply
			while (true) {
				if (scan.hasNext()) {
					String playerPlayAgain = scan.next();

					// Case player enters Y/y, start new game.
					if (playerPlayAgain.equalsIgnoreCase("Y")) {
						displayMessage(GAME_NEW_STARTING);
						isStartNewGame = true;
						break;
					}

					// Case player enters N/n, end game.
					if (playerPlayAgain.equalsIgnoreCase("N")) {
						displayMessage(GAME_END);
						isStartNewGame = false;
						break;
					}
					displayMessage(PLS_REPLY_Y_OR_N);
				}
			}
		}

		// Display win record for all players
		roster.displayWins();

		// Close the IO when game finishes
		closeIO(scan);
	}

	// Helper method to print message after each player move
	private static void displayMessage(String msg) {
		System.out.println(msg);
	}

	// Helper method to open IO
	private static Scanner openIO() {
		return new Scanner(System.in);
	}

	// Helper method to close IO
	private static void closeIO(Scanner scan) {
		// Close the IO when game finishes
		if (scan != null) {
			scan.close();
		}
	}

}

/*
 * Class to keep track of the state of game board. It also has helper functions
 * to check if game is won and if there are still squares left not taken.
 */
class TicTacToeBoard {

	// Board is represented by array
	private TicTacToeBoardSquare[] board = new TicTacToeBoardSquare[9];

	public TicTacToeBoard() {
		for (int n = 1; n < 10; n++) {
			// Create squares 1 to 9 on the board
			board[n - 1] = new TicTacToeBoardSquare(n);
		}
	}

	/**
	 * Display the state of the TicTacToe game board
	 */
	protected void displayState() {
		System.out.println("+-+-+-+");
		System.out.println("|" + board[0] + "|" + board[1] + "|" + board[2] + "|");
		System.out.println("+-+-+-+");
		System.out.println("|" + board[3] + "|" + board[4] + "|" + board[5] + "|");
		System.out.println("+-+-+-+");
		System.out.println("|" + board[6] + "|" + board[7] + "|" + board[8] + "|");
		System.out.println("+-+-+-+");
	}

	protected boolean isSquareTaken(int squareNum) {
		return board[squareNum - 1].isTaken();
	}

	protected void updateSquareNumTakenByPlayer(int squareNum, String playerName) {
		board[squareNum - 1].setSquareState(playerName);
	}

	protected boolean isGameWon() {
		if (isRow1Win() || isRow2Win() || isRow3Win() || isCol1Win() || isCol2Win() || isCol3Win() || isDiagonal1Win()
				|| isDiagonal2Win()) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean isAllSquaresTaken() {
		// Check if all squares on the board are taken.
		for (TicTacToeBoardSquare square : board) {
			// As long as 1 square is not taken, game is not completed.
			if (!square.isTaken()) {
				return false;
			}
		}
		// Case all squares are taken as loop above does not return.
		return true;
	}

	private boolean isThreeSquaresInARow(TicTacToeBoardSquare square1, TicTacToeBoardSquare square2,
			TicTacToeBoardSquare square3) {
		// Check if square1 = square2 = square3
		if (square1.getSquareState().equals(square2.getSquareState())
				&& square2.getSquareState().equals(square3.getSquareState())) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isRow1Win() {
		return isThreeSquaresInARow(board[0], board[1], board[2]);
	}

	private boolean isRow2Win() {
		return isThreeSquaresInARow(board[3], board[4], board[5]);
	}

	private boolean isRow3Win() {
		return isThreeSquaresInARow(board[6], board[7], board[8]);
	}

	private boolean isCol1Win() {
		return isThreeSquaresInARow(board[0], board[3], board[6]);
	}

	private boolean isCol2Win() {
		return isThreeSquaresInARow(board[1], board[4], board[7]);
	}

	private boolean isCol3Win() {
		return isThreeSquaresInARow(board[2], board[5], board[8]);
	}

	private boolean isDiagonal1Win() {
		return isThreeSquaresInARow(board[0], board[4], board[8]);
	}

	private boolean isDiagonal2Win() {
		return isThreeSquaresInARow(board[2], board[4], board[6]);
	}
}

// Class to keep track of the state of each square on the game board.
// Taken - is true if a player takes the square.
// SquareState - Initially this is the square number, as a player takes this
// square the player mark replaces the square number
class TicTacToeBoardSquare {

	private boolean taken = false;
	private String squareState = "";

	protected TicTacToeBoardSquare(int squareNumber) {
		taken = false;
		squareState = "" + squareNumber;
	}

	protected String getSquareState() {
		return squareState;
	}

	protected void setTaken(boolean moved) {
		this.taken = moved;
	}

	protected void setSquareState(String playerName) {
		this.squareState = playerName;
		this.taken = true;
	}

	protected boolean isTaken() {
		return taken;
	}

	public String toString() {
		return squareState;
	}
}

/*
 * Class for keeping track of player's mark (i.e. X or O) and win record
 */
class TicTacToePlayer {

	private String mark = "";
	private int numWins = 0;

	protected TicTacToePlayer(String name) {
		this.mark = name;
	}

	protected int getNumWins() {
		return numWins;
	}

	protected void incrementWins() {
		numWins = numWins + 1;
	}

	protected String getMark() {
		return mark;
	}
}

/*
 * Class for handling: (1) which players are playing the game, (2) which player
 * goes in the next move, (3) and number of wins for each player.
 */
class TicTacToePlayerRoster {
	private ArrayList<TicTacToePlayer> playerRoster;

	protected TicTacToePlayerRoster() {
		playerRoster = new ArrayList<TicTacToePlayer>();
	}

	protected void addPlayer(String name) {
		TicTacToePlayer player = new TicTacToePlayer(name);
		playerRoster.add(player);
	}

	protected TicTacToePlayer getNextPlayer() {
		TicTacToePlayer currentPlayer = playerRoster.remove(0);
		playerRoster.add(currentPlayer);
		return currentPlayer;
	}

	protected void displayWins() {
		for (TicTacToePlayer player : playerRoster) {
			System.out.println(player.getMark() + " won " + player.getNumWins() + " time(s).");
		}
	}
}