package main;

import java.util.ArrayList;

public class SmallBoard {
  private int board;
  private int lookupsComplete = 0;

  private static final int[] WINNERS = new int[4^9];
  private static final int[] WIN_SQUARE_COUNTS = new int[(4^9)*2];
  // private static final double[] SMALL_EVALS = new double[3^9];

  static {
    SmallBoard _board = new SmallBoard(0, 0);
    for (int i = 0; i < (4^9); i++) {
      _board.board = i;
      WINNERS[i] = _board.getWinner();
    }
    _board = new SmallBoard(0, 1);
    for (int i = 0; i < (4^9); i++) {
      _board.board = i;
      WIN_SQUARE_COUNTS[i] = _board.getWinSquareCount(Constants.X);
      WIN_SQUARE_COUNTS[i + 4^9] = _board.getWinSquareCount(Constants.O);
    }
  }

  public SmallBoard() {
    board = 0;
    lookupsComplete = 2;
  }
  
  public SmallBoard(int _board) {
    board = _board;
  }
  
  public SmallBoard(int _board, int _lookupsComplete) {
    board = _board;
    lookupsComplete = _lookupsComplete;
  }
  
  public static String getString(int _player) {
    switch(_player) {
      case Constants.X:
        return "x";
      case Constants.O:
        return "o";
      case Constants.NONE:
        return "-";
      case Constants.TIE:
        return "/";
    }
    return " ";
  }

  public static int opposite(int _player) {
    switch(_player) {
      case Constants.X:
        return Constants.O;
      case Constants.O:
        return Constants.X;
      case Constants.NONE:
        return Constants.NONE;
      case Constants.TIE:
        return Constants.TIE;
    }
    return Constants.NONE;
  }

  public int getSquare(int _square) {
    return (board >> 2*_square)&3;
  }

  public int getSquare2d(int _row, int _col) {
    return getSquare(_row*3+_col);
  }
  
  public void setSquare(int _square, int _player) {
    board = (board & (~(3<<2*_square))) | (_player<<2*_square);
  }

  public void setSquare2d(int _row, int _col, int _player) {
    setSquare(_row*3+_col, _player);
  }
  
  public void move(int _square, int _player) throws IllegalArgumentException {
    if (_square <= -1  ||  _square >= 9) {
      throw new IndexOutOfBoundsException();
    }
    if (getSquare(_square) == Constants.NONE  ||  _player == Constants.NONE) {
      setSquare(_square, _player);
    }
    else {
      throw new IllegalArgumentException("Square occupied: " + _square);
    }
  }
  
  public void move2d(int _row, int _col, int _player) throws IllegalArgumentException {
    move(_row*3+_col, _player);
  }
  
  public void undo(int _square) throws IllegalArgumentException {
    if (_square <= -1  ||  _square >= 9) {
      throw new IndexOutOfBoundsException();
    }
    if (getSquare(_square) != Constants.NONE) {
      setSquare(_square, Constants.NONE);
    }
    else {
      throw new IllegalArgumentException("Can't undo: no player on that square " + _square);
    }
  }
  
  public void undo2d(int _row, int _col) throws IllegalArgumentException {
    undo(_row*3+_col);
  }
  
  public int getWinner() {
    if (lookupsComplete >= 1) {
      if (board < (4^9)) {
        return WINNERS[board];
      }
    }
    int[][] _winPatterns = new int[][] {{0, 1, 2},
                                          {3, 4, 5},
                                          {6, 7, 8},
                                          {0, 3, 6},
                                          {1, 4, 7},
                                          {2, 5, 8},
                                          {0, 4, 8},
                                          {2, 4, 6}};
    for (int i = 0; i < 8; i++) {
      if (getSquare(_winPatterns[i][0]) != Constants.NONE  &&  
          getSquare(_winPatterns[i][0]) != Constants.TIE  &&
          getSquare(_winPatterns[i][0]) == getSquare(_winPatterns[i][1])  &&
          getSquare(_winPatterns[i][1]) == getSquare(_winPatterns[i][2])) {
        return getSquare(_winPatterns[i][0]);
      }
    }
    if (isFull()) {
      return Constants.TIE;
    }
    return Constants.NONE;
  }
  
  public boolean isFull() {
    for (int i = 0; i < 9; i++) {
      if (getSquare(i) == Constants.NONE) {
        return false;
      }
    }
    return true;
  }
  
  public boolean isFinished() {
    return getWinner() != Constants.NONE;
  }

  public boolean wouldWin(int _row, int _col, int _player) {
    if (getSquare2d(_row,_col) != Constants.NONE) {
      return false;
    }
    if (isFinished()) {
      return false;
    }
    boolean _result = false;
    setSquare2d(_row, _col, _player);
    if (getWinner() != Constants.NONE  &&  getWinner() != Constants.TIE) {
      _result = true;
    }
    setSquare2d(_row, _col, Constants.NONE);

    return _result;
  }

  public int getSquareCount(int _player) {
    int _result = 0;
    for (int i = 0; i < 9; i++) {
      if (getSquare(i) == _player) {
        _result++;
      }
    }

    return _result;
  }

  public int getWinSquareCount(int _player) {
    if (isFinished()  ||  _player == Constants.NONE  ||  _player == Constants.TIE) {
      return 0;
    }
    if (lookupsComplete >= 2) {
      if (board < (4^9)) {
        return WIN_SQUARE_COUNTS[board + ((_player-1)*(4^9))];
      }
    }
    int _result = 0;
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        if (getSquare2d(_row,_col) == Constants.NONE) {
          if (wouldWin(_row, _col, _player)) {
            _result++;
          }
        }
      }
    }
    return _result;
  }

  public ArrayList<Integer> getOrderedMoves(int _player) {
    ArrayList<Integer> _result = new ArrayList<Integer>();
    for (int i = 0; i < 9; i++) {
      if (getSquare(i) == Constants.NONE) {
        _result.add(i);
      }
    }

    int i = 0;
    ArrayList<Integer> _newResult = new ArrayList<Integer>();
    while (i < _result.size()) {
      if (wouldWin(_result.get(i)/3, _result.get(i)%3, _player)) {
        _newResult.add(_result.get(i));
        _result.remove(i);
        continue;
      }
      i++;
    }
    _result.addAll(_newResult);
    
    return _result;
  }

  // public double eval(int _player) {
  //   double _result = 0.;
  //   int _enemy = opposite(_player);
  //   int _playerWinSquares = getWinSquareCount(_player);
  //   int _enemyWinSquares = getWinSquareCount(_enemy);
  //   _result += 1. * _playerWinSquares;
  //   _result -= 1. * _enemyWinSquares;
  //   for (int _square = 0; _square < 9; _square++) {
  //     if (getSquare(_square) == Constants.NONE) {
  //       move(_square,_player);
  //       _result += 0.1 * (getWinSquareCount(_player) - _playerWinSquares);
  //       undo(_square);
  //       move(_square,_enemy);
  //       _result -= 0.1 * (getWinSquareCount(_enemy) - _enemyWinSquares);
  //       undo(_square);
  //     }
  //   }
  //   return _result;
  // }
  
  public void print() {
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        System.out.print(getString(getSquare2d(_row,_col)) + " ");
      }
      System.out.println("");
    }
  }
}