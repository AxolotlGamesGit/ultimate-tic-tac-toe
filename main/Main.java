package main;

import java.util.Scanner;

import main.SmallBoard.Player;

public class Main {
  	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

    BigBoard board = new BigBoard();
    board.print();

		int bigRow = -1;
		int bigCol = -1;
		int smallRow = -1;
		int smallCol = -1;
		boolean moveAnywhere = true;
		String token = "";
		while (!checkToken(token, "close")) {
			token = scanner.nextLine().trim().toLowerCase();
		  if (checkToken(token, "undo")) {
		    if (bigRow == -1) {
		      System.out.println("Can't undo (max 1 turn undo)");
		    }
		    else {
		      board.undo(bigRow, bigCol, smallRow, smallCol, moveAnywhere);
		      bigRow = -1;
          bigCol = -1;
          smallRow = -1;
          smallCol = -1;
          moveAnywhere = true;
          board.print();
		    }
		    continue;
		  }

			if (checkToken(token, "clear")) {
		    board = new BigBoard();
		    continue;
		  }
		  
			if (checkToken(token, "print")) {
		    board.print();
		    continue;
		  }

			if (checkToken(token, "eval")) {
		    Node _node = new Node(BigBoard.copy(board));
				System.out.println(_node.eval(true, board.Turn));
				board.print();
		    continue;
		  }

			if (checkToken(token, "minimax")) {
		    Node _node = new Node(BigBoard.copy(board));
				System.out.println(_node.minimax(2,board.Turn == Player.X,-1000000,100000,board.Turn));
				board.print();
		    continue;
		  }

			if (checkToken(token, "moves")) {
		    Node _node = new Node(BigBoard.copy(board));
				_node.explore(true);
		    continue;
		  }

			if (checkToken(token, "move")) {
		    Node _node = new Node(BigBoard.copy(board));
				int[] _bestMove = _node.getBestMove(board.Turn == Player.X, 9);
				board.move1d(_bestMove[0],_bestMove[1]);
				board.print();
		    continue;
		  }
		  
		  try {
  		  if (token.length() >= 4  &&  token.charAt(2) == '-') {
  		    bigRow = getRow(token.substring(0,2));
  		    bigCol = getCol(token.substring(0,2));
  		    smallRow = getRow(token.substring(3,token.length()));
  		    smallCol = getCol(token.substring(3,token.length()));
  		  }
  		  else if (token.length() >= 3  &&  token.charAt(1) == '-') {
  		    bigRow = getRow(token.substring(0,1));
  		    bigCol = getCol(token.substring(0,1));
  		    smallRow = getRow(token.substring(2,token.length()));
  		    smallCol = getCol(token.substring(2,token.length()));
  		  }
  		  else if (token.length() == 2) {
  		    bigRow = -1;
          bigCol = -1;
  		    smallRow = getRow(token.substring(0,2));
  		    smallCol = getCol(token.substring(0,2));
  		  }
  		  else if (token.length() == 1) {
  		    bigRow = -1;
          bigCol = -1;
  		    smallRow = getRow(token.substring(0,1));
  		    smallCol = getCol(token.substring(0,1));
  		  }
  		  else {
  		    System.out.println("Invalid input (check notation)");
  		    continue;
  		  }
		  }
		  catch (Exception e) {
		    System.out.println("Invalid input (check notation)");
		    continue;
		  }
		  
		  boolean _tempMoveAnywhere = board.BigRow == -1;
		  try {
  		  if (bigRow == -1) {
  		    bigRow = board.BigRow;
  		    bigCol = board.BigCol;
  		  }
  		  board.move(bigRow, bigCol, smallRow, smallCol);
		  }
		  catch (Exception e) {
		    System.out.println("Illegal move");
		    System.out.println(e.getMessage());
		    continue;
		  }
		  
		  board.print();
		  moveAnywhere = _tempMoveAnywhere;
		}

		scanner.close();
	}
	
	private static int getRow(String _square) {
	  switch(_square.trim().toLowerCase()) {
	    case "n", "ne", "nw":
	      return 0;
      case "c", "e", "w":
        return 1;
      case "s", "se", "sw":
        return 2;
	  }
	  throw new IllegalArgumentException("not a valid square");
	}
	
	private static int getCol(String _square) {
	  switch(_square.trim().toLowerCase()) {
	    case "w", "nw", "sw":
	      return 0;
      case "c", "n", "s":
        return 1;
      case "e", "ne", "se":
        return 2;
	  }
	  throw new IllegalArgumentException("not a valid square");
	}

	private static boolean checkToken(String _token, String _target) {
	  return _token.length() >= _target.length()  &&  _token.substring(0,_target.length()).compareToIgnoreCase(_target) == 0;
	}
}