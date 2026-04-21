package main;

import java.util.ArrayList;
import java.util.Collections;

import main.SmallBoard.Player;

public class Node {
  private BigBoard state;
  private ArrayList<Integer> bigSquares;
  private ArrayList<Integer> smallSquares;
  private ArrayList<Node> children;
  private boolean moveAnywhere;
  
  public Node(BigBoard _state) {
    state = _state;
    bigSquares = new ArrayList<Integer>();
    smallSquares = new ArrayList<Integer>();
    children = new ArrayList<Node>();
    moveAnywhere = state.BigRow == -1;
  }
  
  public void explore(boolean _print) {
    moveAnywhere = state.BigRow == -1;
    if (moveAnywhere) {
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
      if (_print) {
        System.out.println(bigSquares.get(i) + " " + smallSquares.get(i));
      }
      children.add(new Node(state));
    }
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
          _squareMult = 1.4;
        }
        if (Math.max(_bigRow, _bigCol) == 2  &&  Math.min(_bigRow, _bigCol) == 0) {
          _squareMult = 1.2;
        }
        if (state.Wins.wouldWin(_bigRow, _bigCol, _player)) {
          _squareMult *= 1.75;
        }
        if (state.Wins.wouldWin(_bigRow, _bigCol, SmallBoard.opposite(_player))) {
          _squareMult *= 1.5;
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
    _boardValue += state.Wins.getWinningSquareCount(_player) * 20;
    _boardValue -= state.Wins.getWinningSquareCount(SmallBoard.opposite(_player)) * 20;
    _result += _boardValue;
    if (_print) {
      System.out.println("");
      System.out.println("Big board: " + _boardValue);
    }

    double _outcomeValue = 0.;
    if (state.Wins.isFinished()) {
      switch (state.Wins.getWinner()) {
        case X:
          _outcomeValue = 1000;
          break;
        case O:
          _outcomeValue = -1000;
          break;
        case NONE:
          _outcomeValue = 10;
          break;
      }
    }
    _result += _outcomeValue;
    if (_print) {
      System.out.println("");
      System.out.println("Outcome: " + _outcomeValue);
    }

    // state.print();
    // System.out.println(_result);
    // System.out.println();
    return _result;
  }

  public double minimax(int _depth, boolean _isMaximizing, double _alpha, double _beta, Player _player) {
    if (_depth == 1  ||  state.isFinished()) {
      if (state.isFinished()) {
        state.print();
        return eval(false, _player);
      }
      else {
        return eval(false, _player);
      }
    }
    if (children.size() == 0) {
      explore(false);
    }
    if (_isMaximizing) {
      double _maxEval = Double.NEGATIVE_INFINITY;
      double _newAlpha = _alpha;
      for (int i = 0; i < children.size(); i++) {
        state.move1d(bigSquares.get(i), smallSquares.get(i));
        double _eval = children.get(i).minimax(_depth - 1, !_isMaximizing, _newAlpha, _beta, _player);
        state.undo1d(bigSquares.get(i), smallSquares.get(i), moveAnywhere);
        _maxEval = Math.max(_eval, _maxEval);
        _newAlpha = Math.max(_newAlpha, _eval);
        if (_beta < _newAlpha) {
          break;
        }
      }
      return _maxEval;
    }
    else {
      double _minEval = Double.POSITIVE_INFINITY;
      double _newBeta = _beta;
      for (int i = 0; i < children.size(); i++) {
        state.move1d(bigSquares.get(i), smallSquares.get(i));
        double _eval = children.get(i).minimax(_depth - 1, !_isMaximizing, _newBeta, _beta, _player);
        state.undo1d(bigSquares.get(i), smallSquares.get(i), moveAnywhere);
        _minEval = Math.min(_eval, _minEval);
        _newBeta = Math.min(_newBeta, _eval);
        if (_newBeta < _alpha) {
          break;
        }
      }
      return _minEval;
    }
  }

  public int[] getBestMove(boolean _isMaximizing, int _depth) {
    if (children.size() == 0) {
      explore(false);
    }
    int _bestMoveIndex = 0;
    double _bestMoveEval = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < children.size(); i++) {
      state.move1d(bigSquares.get(i), smallSquares.get(i));
      double _eval = (_isMaximizing ? 1. : -1.) * children.get(i).minimax(_depth, !_isMaximizing, _isMaximizing ? _bestMoveEval : Double.NEGATIVE_INFINITY, !_isMaximizing ? -_bestMoveEval : Double.POSITIVE_INFINITY, SmallBoard.opposite(state.Turn));
      System.out.println(bigSquares.get(i) + " " + smallSquares.get(i) + " " + _eval);
      state.undo1d(bigSquares.get(i), smallSquares.get(i), moveAnywhere);
      if (_eval > _bestMoveEval) {
        _bestMoveIndex = i;
        _bestMoveEval = _eval;
      }
    }

    return new int[]{bigSquares.get(_bestMoveIndex), smallSquares.get(_bestMoveIndex)};
  }
}