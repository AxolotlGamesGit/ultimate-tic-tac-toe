package main;

import java.util.Stack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

import main.SmallBoard.Player;

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
  
  public void explore(Player _player, boolean _print) {
    if (state.BigRow == -1) {
      for (int _row = 0; _row < 3; _row++) {
        for (int _col = 0; _col < 3; _col++) {
          SmallBoard _currentBoard = state.Board[_row][_col];
          if (!_currentBoard.isFinished()) {
            ArrayList<Integer> _moves = _currentBoard.getOrderedMoves(_player);
            smallSquares.addAll(_moves);
            bigSquares.addAll(Collections.nCopies(_moves.size(), _row*3+_col));
          }
        }
      }
    }
    else {
      ArrayList<Integer> _moves = state.Board[state.BigRow][state.BigCol].getOrderedMoves(_player);
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

  public double eval(boolean _print, Player _player) {
    // Game outcome
    if (state.isFinished()) {
      Player _winner = state.Wins.getWinner();
      if (_winner == _player) {
        return 1000000. - state.Moves;
      }
      else if (_winner == SmallBoard.opposite(_player)) {
        return -1000000. + state.Moves;
      }
      else {
        return 10.;
      }
    }

    double _result = 0;
    
    // Calculate the multiplier for each big square
    double[][] _squareMults = new double[3][3];
    for (int _row = 0; _row < 3; _row++) {
      Arrays.fill(_squareMults[_row],0.);
    }
    
    int _playerWins = 0;
    int _enemyWins = 0;
    // With an empty big board, use preset values
    if (state.Wins.getSquareCount(Player.NONE) == 9) {
      for (int _row = 0; _row < 3; _row++) {
        for (int _col = 0; _col < 3; _col++) {
          if (_row == 1  &&  _col == 1) {
            _squareMults[_row][_col] = 1.6;
          }
          else if (Math.abs(_row - _col) != 1) {
            _squareMults[_row][_col] = 1.3;
          }
          else {
            _squareMults[_row][_col] = 1.;
          }
        }
      }
    }
    // If there is something on the big board, use overcomplicated method
    else {
      // Count the % of possible 3 in a rows each big square is in
      int[][] _playerPotentialWinSquares = new int[3][3];
      for (int _row = 0; _row < 3; _row++) {
        Arrays.fill(_playerPotentialWinSquares[_row],0);
      }
      int[][] _enemyPotentialWinSquares = new int[3][3];
      for (int _row = 0; _row < 3; _row++) {
        Arrays.fill(_enemyPotentialWinSquares[_row],0);
      }
      ROW: for (int _row = 0; _row < 3; _row++) {
        for (int _col = 0; _col < 3; _col++) {
          if (state.Wins.Board[_row][_col] == SmallBoard.opposite(_player)) {
            continue ROW;
          }
        }
        _playerWins++;
        for (int _col = 0; _col < 3; _col++) {
          _playerPotentialWinSquares[_row][_col]++;
        }
      }
      COL: for (int _col = 0; _col < 3; _col++) {
        for (int _row = 0; _row < 3; _row++) {
          if (state.Wins.Board[_row][_col] == SmallBoard.opposite(_player)) {
            continue COL;
          }
        }
        _playerWins++;
        for (int _row = 0; _row < 3; _row++) {
          _playerPotentialWinSquares[_row][_col]++;
        }
      }
      for (int i = 0; i < 3; i++) {
        if (state.Wins.Board[i][i] == SmallBoard.opposite(_player)) {
          break;
        }
        if (i == 2) {
          _playerWins++;
          for (int j = 0; j < 3; j++) {
            _playerPotentialWinSquares[j][j]++;
          }
        }
      }
      for (int i = 0; i < 3; i++) {
        if (state.Wins.Board[i][2-i] == SmallBoard.opposite(_player)) {
          break;
        }
        if (i == 2) {
          _playerWins++;
          for (int j = 0; j < 3; j++) {
            _playerPotentialWinSquares[j][2-j]++;
          }
        }
      }
      ROW: for (int _row = 0; _row < 3; _row++) {
        for (int _col = 0; _col < 3; _col++) {
          if (state.Wins.Board[_row][_col] == _player) {
            continue ROW;
          }
        }
        _enemyWins++;
        for (int _col = 0; _col < 3; _col++) {
          _enemyPotentialWinSquares[_row][_col]++;
        }
      }
      COL: for (int _col = 0; _col < 3; _col++) {
        for (int _row = 0; _row < 3; _row++) {
          if (state.Wins.Board[_row][_col] == _player) {
            continue COL;
          }
        }
        _enemyWins++;
        for (int _row = 0; _row < 3; _row++) {
          _enemyPotentialWinSquares[_row][_col]++;
        }
      }
      for (int i = 0; i < 3; i++) {
        if (state.Wins.Board[i][i] == _player) {
          break;
        }
        if (i == 2) {
          _enemyWins++;
          for (int j = 0; j < 3; j++) {
            _enemyPotentialWinSquares[j][j]++;
          }
        }
      }
      for (int i = 0; i < 3; i++) {
        if (state.Wins.Board[i][2-i] == _player) {
          break;
        }
        if (i == 2) {
          _enemyWins++;
          for (int j = 0; j < 3; j++) {
            _enemyPotentialWinSquares[j][2-j]++;
          }
        }
      }
  
      // Calculate the multiplier for each big square based on % of possible wins, # of win squares created and if it wins
      int _playerWinSquares = state.Wins.getWinningSquareCount(_player);
      int _enemyWinSquares = state.Wins.getWinningSquareCount(SmallBoard.opposite(_player));
      for (int _row = 0; _row < 3; _row++) {
        for (int _col = 0; _col < 3; _col++) {
          if (state.Board[_row][_col].isFinished()) {
            _squareMults[_row][_col] = -1.;
            continue;
          }
          if (state.Wins.wouldWin(_row,_col,_player)) {
            _squareMults[_row][_col] += 2.;
          }
          if (state.Wins.wouldWin(_row,_col,SmallBoard.opposite(_player))) {
            _squareMults[_row][_col] += 2.;
          }
          if (_squareMults[_row][_col] == 0.) {
            state.Wins.move(_row,_col,_player);
            _squareMults[_row][_col] += 0.5 * (state.Wins.getWinningSquareCount(_player) - _playerWinSquares);
            state.Wins.undo(_row,_col);
            state.Wins.move(_row,_col,SmallBoard.opposite(_player));
            _squareMults[_row][_col] += 0.5 * (state.Wins.getWinningSquareCount(SmallBoard.opposite(_player)) - _enemyWinSquares);
            state.Wins.undo(_row,_col);
          }
          if (_print) {
            System.out.println(_row + " " + _col + " " + _playerWins + " " + _playerPotentialWinSquares[_row][_col] + " " + _enemyWins + " " + _enemyPotentialWinSquares[_row][_col]);
          }
          if (_playerWins != 0) {
            _squareMults[_row][_col] += 1.25 * (double)(_playerPotentialWinSquares[_row][_col]) / ((double)_playerWins);
          }
          if (_enemyWins != 0) {
            _squareMults[_row][_col] += 1.25 * (double)(_enemyPotentialWinSquares[_row][_col]) / ((double)_enemyWins);
          }
        }
      }
    }

    // Calculate the value for each small board using the big square multipliers
    for (int _bigRow = 0; _bigRow < 3; _bigRow++) {
      for (int _bigCol = 0; _bigCol < 3; _bigCol++) {
        if (_squareMults[_bigRow][_bigCol] == -1.) {
          continue;
        }
        double _squareValue = 0.;
        SmallBoard _currentBoard = state.Board[_bigRow][_bigCol];
        _squareValue += 0.2 * _currentBoard.getSquareCount(_player);
        _squareValue -= 0.2 * _currentBoard.getSquareCount(SmallBoard.opposite(_player));
        _squareValue += 1. *  _currentBoard.getWinningSquareCount(_player);
        _squareValue -= 1. * _currentBoard.getWinningSquareCount(SmallBoard.opposite(_player));

        _result += _squareValue * _squareMults[_bigRow][_bigCol];

        if (_print) {
          System.out.println("");
          System.out.println("Big square: " + _bigRow + " " + _bigCol);
          System.out.println("Small board value: " + _squareValue);
          System.out.println("Big square mult: " + _squareMults[_bigRow][_bigCol]);
          System.out.println("Total: " + (_squareValue * _squareMults[_bigRow][_bigCol]));
        }
      }
    }

    // Calculate the value of the overall board state
    double _boardValue = 0.;
    _boardValue += 7 * state.Wins.getSquareCount(_player);
    _boardValue -= 7 * state.Wins.getSquareCount(SmallBoard.opposite(_player));
    _boardValue += 20 * state.Wins.getWinningSquareCount(_player);
    _boardValue -= 20 * state.Wins.getWinningSquareCount(SmallBoard.opposite(_player));
    if (state.BigRow == -1) {
      if (state.Turn == _player) {
        _boardValue += 10;
      }
      else {
         _boardValue -= 10;
      }
    }
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
  
  public double eval2(boolean _print, Player _player) {
    if (state.isFinished()) {
      Player _winner = state.Wins.getWinner();
      if (_winner == _player) {
        return 1000000. - state.Moves;
      }
      else if (_winner == SmallBoard.opposite(_player)) {
        return -1000000. + state.Moves;
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
          _squareMult = 1.4;
        }
        if (Math.abs(_bigRow - _bigCol) != 1) {
          _squareMult = 1.2;
        }
        if (state.Wins.wouldWin(_bigRow, _bigCol, _player)) {
          _squareMult += 1;
        }
        if (state.Wins.wouldWin(_bigRow, _bigCol, SmallBoard.opposite(_player))) {
          _squareMult += 1;
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
      switch (_player) {
        case X:
          return eval2(false, _player);
        case O:
          return eval2(false, _player);
        default:
          return 0;
      }
    }
    if (children.empty()) {
      explore(_player, false);
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
      explore(state.Turn, false);
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