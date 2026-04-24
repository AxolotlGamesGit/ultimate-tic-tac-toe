package main;

import java.util.Stack;

import main.SmallBoard.Player;

import java.util.ArrayList;
import java.util.Collections;

public class Node {
  private BigBoard state;
  private Stack<Integer> bigSquares;
  private Stack<Integer> smallSquares;
  private Stack<Node> children;

  public Node(BigBoard _state) {
    state = _state;
    bigSquares = new Stack<Integer>();
    smallSquares = new Stack<Integer>();
    children = new Stack<Node>();
  }
  
  public void explore(boolean _print) {
    if (state.BigRow == -1) {
      for (int _row = 0; _row < 3; _row++) {
        for (int _col = 0; _col < 3; _col++) {
          if (!state.Board[_row][_col].isFinished()) {
            ArrayList<Integer> _moves = getMoves(state.Board[_row][_col]);
            smallSquares.addAll(_moves);
            bigSquares.addAll(Collections.nCopies(_moves.size(), _row*3+_col));
          }
        }
      }
    }
    else {
      ArrayList<Integer> _moves = getMoves(state.Board[state.BigRow][state.BigCol]);
      smallSquares.addAll(_moves);
      bigSquares.addAll(Collections.nCopies(_moves.size(), state.BigRow*3+state.BigCol));
    }

    for (int i = 0; i < bigSquares.size(); i++) {
      children.push(new Node(state));
    }
    
    // System.out.println(children.size());
    // System.out.println(bigSquares.size());
    // System.out.println(smallSquares.size());
  }

  private ArrayList<Integer> getMoves(SmallBoard _board) {
    ArrayList<Integer> _result = new ArrayList<Integer>();
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        if (_board.Board[_row][_col] == Player.NONE) {
          _result.add(_row*3 + _col);
        }
      }
    }
    
    return _result;
  }

  public double eval(boolean _print, Player _player) {
    if (state.Wins.isFinished()) {
      Player _winner = state.Wins.getWinner();
      if (_winner == _player) {
        return 1000000.;
      }
      else if (_winner == SmallBoard.opposite(_player)) {
        return -1000000.;
      }
      else {
        return 10.;
      }
    }
    
    double _result = 0.;
    for (int _bigRow = 0; _bigRow < 3; _bigRow++) {
      for (int _bigCol = 0; _bigCol < 3; _bigCol++) {
        double _squareValue = 0.;
        double _squareMult = 1.;
        SmallBoard _currentBoard = state.Board[_bigRow][_bigCol];
        _squareValue += _currentBoard.getSquareCount(_player);
        _squareValue -= _currentBoard.getSquareCount(SmallBoard.opposite(_player));
        _squareValue += _currentBoard.getWinningSquareCount(_player) * 3;
        _squareValue -= _currentBoard.getWinningSquareCount(SmallBoard.opposite(_player)) * 3;

        if (_bigRow == 1  &&  _bigCol == 1) {
          _squareMult = 1.5;
        }
        if (Math.max(_bigRow, _bigCol) == 2  &&  Math.min(_bigRow, _bigCol) == 0) {
          _squareMult = 1.25;
        }
        if (state.Wins.wouldWin(_bigRow, _bigCol, _player)) {
          _squareMult *= 2;
        }
        if (state.Wins.wouldWin(_bigRow, _bigCol, SmallBoard.opposite(_player))) {
          _squareMult *= 2;
        }

        _result += _squareValue * _squareMult;

        if (_print) {
          System.out.println("");
          System.out.println("Big square: " + _bigRow + " " + _bigCol);
          System.out.println("Small board value: " + _squareValue);
          System.out.println("Big square mult: " + _squareMult);
          System.out.println("Total: " + (_squareValue * _squareMult));
        }
      }
    }

    double _boardValue = 0.;
    _boardValue += state.Wins.getSquareCount(_player) * 10;
    _boardValue -= state.Wins.getSquareCount(SmallBoard.opposite(_player)) * 10;
    _boardValue += state.Wins.getWinningSquareCount(_player) * 30;
    _boardValue -= state.Wins.getWinningSquareCount(SmallBoard.opposite(_player)) * 30;
    _result += _boardValue;
    if (_print) {
      System.out.println("");
      System.out.println("Big board: " + _boardValue);
    }

    // state.print();
    // System.out.println(_result);
    // System.out.println();
    return _result;
  }

  public double minimax(int _depth, boolean _isMaximizing, double _alpha, double _beta, Player _player) {
    Debug.getInstance().MinimaxCount++;
    if (_depth <= 1  ||  state.isFinished()) {
      Debug.getInstance().EvalCount++;
      return eval(false, _player);
    }
    if (children.empty()) {
      explore(false);
      Debug.getInstance().NodeCount++;
      Debug.getInstance().ChildCount += children.size();
    }
    if (_isMaximizing) {
      double _maxEval = Double.NEGATIVE_INFINITY;
      double _newAlpha = _alpha;
      while (!children.empty()) {
        boolean _moveAnywhere = state.BigRow == -1;
        state.move1d(bigSquares.peek(), smallSquares.peek());
        double _eval = children.pop().minimax(_depth - 1, false, _newAlpha, _beta, _player);
        state.undo1d(bigSquares.pop(), smallSquares.pop(), _moveAnywhere);
        _maxEval = Math.max(_eval, _maxEval);
        _newAlpha = Math.max(_newAlpha, _eval);
        if (_beta <= _newAlpha) {
          break;
        }
      }
      return _maxEval;
    }
    else {
      double _minEval = Double.POSITIVE_INFINITY;
      double _newBeta = _beta;
      while (!children.empty()) {
        boolean _moveAnywhere = state.BigRow == -1;
        state.move1d(bigSquares.peek(), smallSquares.peek());
        double _eval = children.pop().minimax(_depth - 1, true, _alpha, _newBeta, _player);
        state.undo1d(bigSquares.pop(), smallSquares.pop(), _moveAnywhere);
        _minEval = Math.min(_eval, _minEval);
        _newBeta = Math.min(_newBeta, _eval);
        if (_newBeta <= _alpha) {
          break;
        }
      }
      return _minEval;
    }
  }

  public int[] getBestMove(int _depth) {
    if (children.empty()) {
      explore(false);
    }
    int _bestMoveBigSquare = -1;
    int _bestMoveSmallSquare = -1;
    double _bestMoveEval = Double.NEGATIVE_INFINITY;
    Player _player = state.Turn;
    while(!children.empty()) {
      boolean _moveAnywhere = state.BigRow == -1;
      state.move1d(bigSquares.peek(), smallSquares.peek());
      double _eval = children.pop().minimax(_depth, false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, _player);
      // System.out.println(bigSquares.peek() + " " + smallSquares.peek() + " " + _eval);
      state.undo1d(bigSquares.peek(), smallSquares.peek(), _moveAnywhere);
      if (_eval > _bestMoveEval) {
        _bestMoveBigSquare = bigSquares.peek();
        _bestMoveSmallSquare = smallSquares.peek();
        _bestMoveEval = _eval;
      }
      bigSquares.pop();
      smallSquares.pop();
    }
    System.out.println(_bestMoveBigSquare + "-" + _bestMoveSmallSquare + " " + _bestMoveEval);
    // System.out.println(_bestMoveEval);
    // System.out.println(minimax(_depth+1,true,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,state.Turn));

    return new int[]{_bestMoveBigSquare, _bestMoveSmallSquare};
  }
}