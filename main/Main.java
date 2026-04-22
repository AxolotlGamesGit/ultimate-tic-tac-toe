package main;

import java.util.Scanner;

import main.SmallBoard.Player;

public class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

    BigBoard board = new BigBoard();
    board.print();

		int bigSquare = -1;
		int smallSquare = -1;
		boolean moveAnywhere = true;
		String token = "";
		while (!checkToken(token, "close")) {
			token = scanner.nextLine().trim().toLowerCase();
		// 	token = "move";
		  if (checkToken(token, "undo")) {
		    if (bigSquare == -1) {
		      System.out.println("Can't undo (max 1 turn undo)");
		    }
		    else {
		      board.undo1d(bigSquare, smallSquare, moveAnywhere);
		      bigSquare = -1;
          smallSquare = -1;
          moveAnywhere = true;
          board.print();
		    }
		    continue;
		  }

			if (checkToken(token, "clear")) {
		    board = new BigBoard();
				board.print();
		    continue;
		  }
		  
			if (checkToken(token, "print")) {
		    board.print();
		    continue;
		  }

      if (checkToken(token, "eval-o")) {
		    Node _node = new Node(BigBoard.copy(board));
				System.out.println(_node.eval(true, Player.O));
				board.print();
		    continue;
		  }

      if (checkToken(token, "eval-x")) {
		    Node _node = new Node(BigBoard.copy(board));
				System.out.println(_node.eval(true, Player.X));
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
				System.out.println(_node.minimax(2,true,-1000000,100000,board.Turn));
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
				int _depth = 10;
				switch (token) {
					case "move-small":
						_depth -= 1;
						break;
					case "move-smaller":
						_depth -= 2;
						break;
					case "move-tiny":
						_depth -= 6;
						break;
					case "move-big":
						_depth+= 1;
						break;
					case "move-bigger":
						_depth += 2;
						break;
					case "move-huge":
						_depth += 5;
						break;
				}
				int[] _bestMove = _node.getBestMove(_depth);
				board.move1d(_bestMove[0],_bestMove[1]);
				board.print();
		    continue;
		  }
		  
		  try {
  		  if (token.length() >= 4  &&  token.charAt(2) == '-') {
  		    bigSquare = getSquare(token.substring(0,2));
  		    smallSquare = getSquare(token.substring(3,token.length()));
  		  }
  		  else if (token.length() >= 3  &&  token.charAt(1) == '-') {
  		    bigSquare = getSquare(token.substring(0,1));
  		    smallSquare = getSquare(token.substring(2,token.length()));
  		  }
  		  else if (token.length() == 2) {
  		    bigSquare = -1;
  		    smallSquare = getSquare(token.substring(0,2));
  		  }
  		  else if (token.length() == 1) {
  		    bigSquare = -1;
  		    smallSquare = getSquare(token.substring(0,1));
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
  		  if (bigSquare == -1) {
  		    bigSquare = board.BigRow*3 + board.BigCol;
  		  }
  		  board.move1d(bigSquare, smallSquare);
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
	
	private static int getSquare(String _square) {
	  switch(_square.trim().toLowerCase()) {
	    case "nw":
	      return 0;
			case "n":
	      return 1;
			case "ne":
	      return 2;
			case "w":
	      return 3;
			case "c":
	      return 4;
			case "e":
	      return 5;
			case "sw":
	      return 6;
			case "s":
	      return 7;
			case "se":
	      return 8;
	  }
	  throw new IllegalArgumentException("not a valid square");
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