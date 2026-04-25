package main;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

import main.SmallBoard.Player;

public class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

    BigBoard board = new BigBoard();
    board.print();

		Stack<Integer> bigSquares = new Stack<Integer>();
		Stack<Integer> smallSquares = new Stack<Integer>();
		Stack<Boolean> moveAnywhere = new Stack<Boolean>();
		String token = "";
		int xDepth = 5;
		int oDepth = 5;
		while (!checkToken(token, "close")) {
		  int _depth = 8;
		  if ((board.Turn == Player.X  &&  xDepth == -1)  ||  
		          (board.Turn == Player.O  &&  oDepth == -1)  ||
		          (board.Turn == Player.NONE)) {
			  token = scanner.nextLine().trim().toLowerCase();
		  }
		  else {
			  token = "move";
			  if (board.Turn == Player.X) {
			    _depth = xDepth;
			  }
			  else {
			    _depth = oDepth;
			  }
		  }
		  
		  if (checkToken(token, "undo")) {
		    if (!bigSquares.empty()) {
    	    board.undo1d(bigSquares.pop(), smallSquares.pop(), moveAnywhere.pop());
    	    if (_depth != 1  &&  !bigSquares.empty()) {
		        board.undo1d(bigSquares.pop(), smallSquares.pop(), moveAnywhere.pop());
    	    }
          board.print();
		    }
		    else {
		      System.out.println("Can't undo");
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
				System.out.println(_node.minimax(_depth,true,-1000000,100000,board.Turn));
				board.print();
		    continue;
		  }

			if (checkToken(token, "moves")) {
		    // Node _node = new Node(BigBoard.copy(board));
				// _node.explore(board.Turn,true);
				ArrayList<Integer> _moves = board.Board[board.BigRow][board.BigCol].getOrderedMoves(board.Turn);
				for (int i = 0; i < _moves.size(); i++) {
					System.out.println(_moves.get(i));
				}
				board.print();
		    continue;
		  }
		  
			if (checkToken(token, "stats")) {
				Debug _debug = Debug.getInstance();
				System.out.println("Eval calls: " + _debug.EvalCount);
        System.out.println("Minimax calls: " + _debug.MinimaxCount);
        System.out.println("Average # of children: " + (double)_debug.ChildCount / (double)_debug.NodeCount);
			}

		  if (checkToken(token, "move")) {
		    Node _node = new Node(BigBoard.copy(board));
				switch (token) {
					case "move-small":
						_depth -= 1;
						break;
					case "move-smaller":
						_depth -= 2;
						break;
					case "move-tiny":
						_depth -= 5;
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
				System.out.println("Thinking (depth " + _depth + ")");
        // Debug.getInstance().resetStats();
				int[] _bestMove = _node.getBestMove(_depth);
        // Debug _debug = Debug.getInstance();
        // System.out.println("Eval calls: " + _debug.EvalCount);
        // System.out.println("Minimax calls: " + _debug.MinimaxCount);
        // System.out.println("Average # of children: " + (double)_debug.ChildCount / (double)_debug.NodeCount);
				bigSquares.push(_bestMove[0]);
				smallSquares.push(_bestMove[1]);
				moveAnywhere.push(board.BigRow == -1);
				board.move1d(_bestMove[0],_bestMove[1]);
				board.print();
				continue;
		  }
		  
		  try {
  		  if (token.length() >= 4  &&  token.charAt(2) == '-') {
  		    bigSquares.push(getSquare(token.substring(0,2)));
  		    smallSquares.push(getSquare(token.substring(3,token.length())));
  		  }
  		  else if (token.length() >= 3  &&  token.charAt(1) == '-') {
  		    bigSquares.push(getSquare(token.substring(0,1)));
  		    smallSquares.push(getSquare(token.substring(2,token.length())));
  		  }
  		  else if (token.length() == 2) {
  		    bigSquares.push(-1);
  		    smallSquares.push(getSquare(token.substring(0,2)));
  		  }
  		  else if (token.length() == 1) {
  		    bigSquares.push(-1);
  		    smallSquares.push(getSquare(token.substring(0,1)));
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
  		  if (bigSquares.peek() == -1) {
  		    bigSquares.pop();
  		    bigSquares.push(board.BigRow*3 + board.BigCol);
  		  }
  		  board.move1d(bigSquares.peek(), smallSquares.peek());
		  }
		  catch (Exception e) {
		    System.out.println("Illegal move");
		    System.out.println(e.getMessage());
		    continue;
		  }
		  
		  board.print();
		  moveAnywhere.push(_tempMoveAnywhere);
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

	@SuppressWarnings("unused")
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
	
	@SuppressWarnings("unused")
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