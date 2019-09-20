package tictactoe;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Xing Yiquan
 */

/*
 * This implementation of TicTacToe game is able to: [1] Scale board size to n
 * by n (where n is BOARD_SIZE), [2] configure number of game squares in a
 * row/column/diagonal needed to win the game (configured via WIN_ROWS), and [3]
 * support more than 2 players by adding to the player roster (add display
 * marker to PLAYERS). The win condition checking (isGameWon() method in
 * TicTacToeGame) is also optimized such that whenever a player makes a move,
 * only n squares in every direction around that move is checked, bounded by the
 * edges of the board (instead of checking all rows, columns, diagonals on the
 * board).
 */

// Class where the main game logic of TicTacToe is implemented.
public class TicTacToeGame {

	// Configurable Game Defaults
	private final static int BOARD_SIZE = 3; // n by n board size
	private final static int WIN_ROWS = 3; // number of squares in a row needed to win
	private final static String[] PLAYERS = { "O", "X" }; // register display markers of all players

	// Messages to display after player move
	private final static String MOVE_ALREADY_MADE = "Sorry this square has been taken, please try again:";
	private final static String MOVE_MUST_BE_A_VALID_NUMBER = "Sorry your move must be a number from %1$d to %2$d. Try again:";
	private final static String GAME_WELOME = "Welcome to the TicTacToe game! To win the game be the first to get 3 in a row.";
	private final static String GAME_WON = "Congratulations, you have won the game!";
	private final static String GAME_NO_WINNERS = "Sorry there are no moves left. This game ends in a draw!";
	private final static String GAME_NEW_STARTING = "Ok, starting new game...\n\n";
	private final static String GAME_END = "Thank you for playing!\n";
	private final static String PLS_MAKE_A_MOVE = "Player %1$s > Please make a move by entering a number from %2$d to %3$d:";
	private final static String PLS_PLAY_AGAIN = "Would you like to play again? (Y/N)";
	private final static String PLS_REPLY_Y_OR_N = "Sorry, please reply with Y or N:";

	public TicTacToeGame() {
	}

	public static void main(String[] args) {

		// Register the players with their mark (i.e. symbol) that will appear on the
		// TicTacToe board.
		TicTacToePlayerRoster roster = new TicTacToePlayerRoster();
		for (String player : PLAYERS) {
			roster.addPlayer(player);
		}

		// Init resources and variables
		Scanner scan = openIO();
		boolean isStartNewGame = true; // tracks if player wants to play another game

		while (isStartNewGame) {

			// Reset game state when starting a new game
			isStartNewGame = false;
			TicTacToePlayer currentPlayer = roster.getNextPlayer();
			TicTacToeBoard board = new TicTacToeBoard(BOARD_SIZE, WIN_ROWS);

			// Prompt player to make first move
			displayMessage(GAME_WELOME);
			board.displayState();
			displayMessage(PLS_MAKE_A_MOVE, currentPlayer.getMark().toString(), board.getFirstSquareNum(),
					board.getLastSquareNum());

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
						if (playerPickedSquareNum < 1 || playerPickedSquareNum > board.getLastSquareNum()) {
							displayMessage(MOVE_MUST_BE_A_VALID_NUMBER, board.getFirstSquareNum(),
									board.getLastSquareNum());
							continue;
						}

						// Check square is not taken
						if (board.isSquareTaken(playerPickedSquareNum)) {
							// If square is taken, prompt user to choose another square
							displayMessage(MOVE_ALREADY_MADE);
							continue;
						}

						// Case player input is valid number and square is not taken, continue with game
						break;

					} else {
						scan.next();// clear the current invalid non-number input
						displayMessage(MOVE_MUST_BE_A_VALID_NUMBER, board.getFirstSquareNum(),
								board.getLastSquareNum());
						continue;
					}
				}

				// Since input is valid and square not taken, update board that square is taken
				// by current player
				board.updateSquareTakenByPlayer(playerPickedSquareNum, currentPlayer);

				// Check after this move if the game has been won
				boolean isGameWon = isGameWon(playerPickedSquareNum, currentPlayer, board);

				// Check after this move is there are any squares left for next move
				boolean isNoMoreMoves = board.isAllSquaresTaken();

				// Case game is won by current player, so game ends
				if (isGameWon) {
					currentPlayer.incrementScore();// record win for current player
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
				displayMessage(PLS_MAKE_A_MOVE, currentPlayer.getMark().toString(), board.getFirstSquareNum(),
						board.getLastSquareNum());
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

	// Check if the current square taken by current player wins the game. Does this
	// by checking around the current square for N (no of squares in a row required
	// to win) adjacent squares in every direction, counting all squares (in each
	// direction) that is connected to the current square.
	private static boolean isGameWon(int currentSquareNum, TicTacToePlayer currentPlayer, TicTacToeBoard board) {

		// Check Column along square taken by current player
		int num_cols_in_a_row = board.checkColsAlongCurrentSquare(currentSquareNum, currentPlayer);
		if (num_cols_in_a_row >= WIN_ROWS) {
			return true;
		}

		// Check Rows
		int num_rows_in_a_row = board.checkRowsAlongCurrentSquare(currentSquareNum, currentPlayer);
		if (num_rows_in_a_row >= WIN_ROWS) {
			return true;
		}

		// Check \ Diagonals
		int num_diagonal_1_in_a_row = board.checkDiagonal1AlongCurrentSquare(currentSquareNum, currentPlayer);
		if (num_diagonal_1_in_a_row >= WIN_ROWS) {
			return true;
		}

		// Check / diagonals
		int num_diagonal_2_in_a_row = board.checkDiagonal2AlongCurrentSquare(currentSquareNum, currentPlayer);
		if (num_diagonal_2_in_a_row >= WIN_ROWS) {
			return true;
		}

		// Case no win conditions met
		return false;
	}

	// Helper method to print message after each player move
	private static void displayMessage(String msg) {
		System.out.println(msg);
	}

	// Helper method to print message after each player move
	private static void displayMessage(String msg, String str1, int num1, int num2) {
		msg = String.format(msg, str1, num1, num2);
		displayMessage(msg);
	}

	// Helper method to print message after each player move
	private static void displayMessage(String msg, int num1, int num2) {
		msg = String.format(msg, num1, num2);
		displayMessage(msg);
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

	// Error messages
	private static final String ERROR_BOARD_SIZE = "Board size must be a number greater than zero.";
	private static final String ERROR_WIN_SIZE = "Win size must be a number greater than zero and not more than board size.";

	private int boardSize = 0;
	private int winGameSize = 0;
	private int firstSquareNum = 1;
	private int lastSquareNum = 1;

	private TicTacToeBoardSquare[][] board;

	public TicTacToeBoard() {
		this(3, 3);
	}

	public TicTacToeBoard(int size) {
		this(size, 3);
	}

	public TicTacToeBoard(int size, int win) {
		// Ensure valid board size and win condition
		if (size < 1) {
			throw new IllegalArgumentException(ERROR_BOARD_SIZE);
		}
		if (win < 1 || win > size) {
			throw new IllegalArgumentException(ERROR_WIN_SIZE);
		}

		boardSize = size; // size (n) of the n x n board
		winGameSize = win; // Number of squares in a row to win
		board = new TicTacToeBoardSquare[boardSize][boardSize];
		initializeSquaresOnBoard();// create all the squares on the board
	}

	private void initializeSquaresOnBoard() {
		// Square number to display for each square
		int squareNumber = 0;
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				squareNumber += 1;// increment square number for next square
				// Create size x size squares on the board
				board[row][col] = new TicTacToeBoardSquare(squareNumber);
			}
		}
		lastSquareNum = squareNumber;
	}

	/**
	 * Display the state of the TicTacToe game board
	 */
	protected void displayState() {

		String border = "+";
		int maxSquareLength = String.valueOf(lastSquareNum).length();
		String squareBorder = String.format("%1$" + maxSquareLength + "s", "").replace(' ', '-') + "+";
		for (int n = 0; n < boardSize; n++) {
			border += squareBorder;
		}

		System.out.println(border);

		for (int row = 0; row < boardSize; row++) {
			String row_n = "|";
			for (int col = 0; col < boardSize; col++) {
				// Create squares 1 to 9 on the board
				row_n += String.format("%1$" + maxSquareLength + "s", board[row][col].getSquareState()) + "|";
			}
			System.out.println(row_n);
			System.out.println(border);
		}
	}

	protected int getFirstSquareNum() {
		return firstSquareNum;
	}

	protected int getLastSquareNum() {
		return lastSquareNum;
	}

	protected boolean isSquareTaken(int squareNum) {
		return board[rowNumOfSquare(squareNum)][colNumOfSquare(squareNum)].isTaken();
	}

	// Calculates row number from square number
	// e.g. on a 4x4 board, square 6 is on row 1 [ (6-1) / 4 = 1]
	private int rowNumOfSquare(int squareNum) {
		return (squareNum - 1) / boardSize;
	}

	// Calculates column number from square number
	// e.g. on a 4x4 board, square 6 is on column 1 [ (6-1) mod 4 = 1) ]
	private int colNumOfSquare(int squareNum) {
		return (squareNum - 1) % boardSize;
	}

	// Set square is taken by which player
	protected void updateSquareTakenByPlayer(int squareNum, TicTacToePlayer player) {
		board[rowNumOfSquare(squareNum)][colNumOfSquare(squareNum)].setTaken(player);
	}

	// Checks if all the squares on the board are taken by any player
	protected boolean isAllSquaresTaken() {
		// Check if all squares on the board are taken.
		for (TicTacToeBoardSquare[] row : board) {
			for (TicTacToeBoardSquare square : row) {
				// As long as 1 square is not taken, game is not completed.
				if (!square.isTaken()) {
					return false;
				}
			}
		}
		// Case all squares are taken
		return true;
	}

	protected int checkColsAlongCurrentSquare(int currentSquareNum, TicTacToePlayer player) {
		int startRow = rowNumOfSquare(currentSquareNum);
		int startCol = colNumOfSquare(currentSquareNum);
		return 1 + countAdjacentSquares(startRow, startCol, 0, 1, winGameSize - 1, player)
				+ countAdjacentSquares(startRow, startCol, 0, -1, winGameSize - 1, player);
	}

	protected int checkRowsAlongCurrentSquare(int currentSquareNum, TicTacToePlayer player) {
		int startRow = rowNumOfSquare(currentSquareNum);
		int startCol = colNumOfSquare(currentSquareNum);
		return 1 + countAdjacentSquares(startRow, startCol, 1, 0, winGameSize - 1, player)
				+ countAdjacentSquares(startRow, startCol, -1, 0, winGameSize - 1, player);
	}

	protected int checkDiagonal1AlongCurrentSquare(int currentSquareNum, TicTacToePlayer player) {
		int startRow = rowNumOfSquare(currentSquareNum);
		int startCol = colNumOfSquare(currentSquareNum);
		return 1 + countAdjacentSquares(startRow, startCol, 1, 1, winGameSize - 1, player)
				+ countAdjacentSquares(startRow, startCol, -1, -1, winGameSize - 1, player);
	}

	protected int checkDiagonal2AlongCurrentSquare(int currentSquareNum, TicTacToePlayer player) {
		int startRow = rowNumOfSquare(currentSquareNum);
		int startCol = colNumOfSquare(currentSquareNum);
		return 1 + countAdjacentSquares(startRow, startCol, -1, 1, winGameSize - 1, player)
				+ countAdjacentSquares(startRow, startCol, 1, -1, winGameSize - 1, player);
	}

	private int countAdjacentSquares(int startRow, int startCol, int incrementRow, int incrementCol,
			int squares_in_a_row, TicTacToePlayer player) {

		int adjacentRow = startRow + incrementRow;
		int adjacentCol = startCol + incrementCol;

		if (adjacentRow < 0 || adjacentRow > boardSize - 1 || adjacentCol < 0 || adjacentCol > boardSize - 1
				|| squares_in_a_row <= 0) {
			// Case reached the edge of the board, so do not count
			return 0;
		} else {
			if (board[adjacentRow][adjacentCol].isTakenBy(player)) {
				// Case adjacent square is taken by current player, count plus 1 and check next
				// adjacent square recursively
				return 1 + countAdjacentSquares(adjacentRow, adjacentCol, incrementRow, incrementCol,
						(squares_in_a_row - 1), player);
			} else {
				// Case no adjacent square taken by current player, so do not count
				return 0;
			}
		}
	}
}

//Class to keep track of the state of each square on the game board.
//Taken - is true if a player takes the square.
//SquareState - Initially this is the square number, as a player takes this
//square the player mark replaces the square number
class TicTacToeBoardSquare {

	private int squareNumber = 0;
	private TicTacToePlayer player;

	protected TicTacToeBoardSquare(int num) {
		squareNumber = num;
	}

	protected String getSquareState() {
		if (player == null || player.isEmpty()) {
			return String.valueOf(squareNumber);
		} else {
			return player.getMark().toString();
		}
	}

	protected void setTaken(TicTacToePlayer p) {
		player = p;
	}

	protected boolean isTaken() {
		if (player == null || player.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	protected boolean isTakenBy(TicTacToePlayer p) {
		if (player != null) {
			return player.equals(p);
		} else {
			return false;
		}
	}

	public String toString() {
		return "[" + squareNumber + ":" + player.getMark() + "]";
	}
}

/*
 * Class for keeping track of player's mark (i.e. X or O) and individual score.
 */
class TicTacToePlayer {

	private static final String ERROR_PLAYER_NAME_EMPTY = "Player name must not be empty.";
	private TicTacToeMarker mark;
	private int numWins = 0;

	protected TicTacToePlayer(String marker) {
		if (marker == null || marker.isEmpty()) {
			throw new IllegalArgumentException(ERROR_PLAYER_NAME_EMPTY);
		}
		mark = new TicTacToeMarker(marker);
	}

	protected int getScore() {
		return numWins;
	}

	protected void incrementScore() {
		numWins = numWins + 1;
	}

	protected TicTacToeMarker getMark() {
		return mark;
	}

	protected boolean isEmpty() {
		if (mark == null) {
			return true;
		} else {
			return false;
		}
	}
}

/*
 * Class for Marker placed on the TicTacToe board
 */
class TicTacToeMarker {
	// Error messages
	private static final String ERROR_MARKER_EMPTY = "Player name must not be empty.";
	// The marker to display on TicTacToe board
	private String mark = "";

	protected TicTacToeMarker(String marker) {
		if (marker == null || marker.isEmpty()) {
			throw new IllegalArgumentException(ERROR_MARKER_EMPTY);
		}
		mark = marker;
	}

	protected String getMark() {
		return mark;
	}

	public String toString() {
		return mark;
	}
}

/*
 * Class to track (1) players playing the game, (2) which player goes in the
 * next move, (3) and number of wins for each player.
 */
class TicTacToePlayerRoster {
	// Error messages
	private static final String ERROR_MARKER_EMPTY = "Player name must not be empty.";
	private ArrayList<TicTacToePlayer> playerRoster;

	protected TicTacToePlayerRoster() {
		playerRoster = new ArrayList<TicTacToePlayer>();
	}

	protected void addPlayer(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException(ERROR_MARKER_EMPTY);
		}
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
			System.out.println(player.getMark() + " won " + player.getScore() + " time(s).");
		}
	}
}