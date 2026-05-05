package main;

import java.util.ArrayList;
import java.util.Arrays;

public class SmallBoard {
  private int board;
  private int lookupsComplete = 0;

  private static final int[] WINNERS = new int[1 << 18];
  private static final int[] WIN_SQUARE_COUNTS = new int[1 << 19];
  private static final double[] SMALL_EVALS = new double[1 << 19];
  private static final double[] BIG_EVALS = new double[1 << 19];
  private static final double[][][] BIG_SQUARE_MULTS = new double[1 << 19][3][3];

  static {
    SmallBoard _board = new SmallBoard(0, 0);
    for (int i = 0; i < (1 << 18); i++) {
      _board.board = i;
      WINNERS[i] = _board.getWinner();
    }
    _board = new SmallBoard(0, 1);
    for (int i = 0; i < (1 << 18); i++) {
      _board.board = i;
      WIN_SQUARE_COUNTS[i] = _board.getWinSquareCount(Constants.X);
      WIN_SQUARE_COUNTS[i + (1 << 18)] = _board.getWinSquareCount(Constants.O);
    }
    _board = new SmallBoard(0, 2);
    for (int i = 0; i < (1 << 18); i++) {
      _board.board = i;
      SMALL_EVALS[i] = _board.smallEval(Constants.X);
      SMALL_EVALS[i + (1 << 18)] = _board.smallEval(Constants.O);
      BIG_EVALS[i] = _board.bigEval(Constants.X);
      BIG_EVALS[i + (1 << 18)] = _board.bigEval(Constants.O);
      BIG_SQUARE_MULTS[i] = _board.getSquareMults(Constants.X);
      BIG_SQUARE_MULTS[i + (1 << 18)] = _board.getSquareMults(Constants.O);
    }
  }

  public SmallBoard() {
    board = 0;
    lookupsComplete = 5;
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
      if (board < (1 << 18)) {
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
      if (board < (1 << 18)) {
        return WIN_SQUARE_COUNTS[board + ((_player-1)*(1 << 18))];
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

  public double smallEval(int _player) {
    if (lookupsComplete >= 3) {
      if (board < (1 << 18)) {
        return SMALL_EVALS[board + ((_player-1)*(1 << 18))];
      }
    }

    double _result = 0.;
    int _enemy = opposite(_player);
    int _playerWinSquares = getWinSquareCount(_player);
    int _enemyWinSquares = getWinSquareCount(_enemy);
    _result += 1. * _playerWinSquares;
    _result -= 1. * _enemyWinSquares;
    for (int _square = 0; _square < 9; _square++) {
      if (getSquare(_square) == Constants.NONE) {
        move(_square,_player);
        _result += 0.1 * (getWinSquareCount(_player) - _playerWinSquares);
        undo(_square);
        move(_square,_enemy);
        _result -= 0.1 * (getWinSquareCount(_enemy) - _enemyWinSquares);
        undo(_square);
      }
    }
    return _result;
  }

  public double bigEval(int _player) {
    if (lookupsComplete >= 4) {
      if (board < (1 << 18)) {
        return BIG_EVALS[board + ((_player-1)*(1 << 18))];
      }
    }

    double _result = 0.;
    int _enemy = opposite(_player);
    int _playerWinSquares = getWinSquareCount(_player);
    int _enemyWinSquares = getWinSquareCount(_enemy);
    _result += 20. * _playerWinSquares;
    _result -= 20. * _enemyWinSquares;
    for (int _square = 0; _square < 9; _square++) {
      if (getSquare(_square) == Constants.NONE) {
        move(_square,_player);
        _result += 2. * (getWinSquareCount(_player) - _playerWinSquares);
        undo(_square);
        move(_square,_enemy);
        _result -= 2. * (getWinSquareCount(_enemy) - _enemyWinSquares);
        undo(_square);
      }
    }
    return _result;
  }

  public double[][] getSquareMults(int _player) {
    if (lookupsComplete >= 5) {
      if (board < (1 << 18)) {
        return BIG_SQUARE_MULTS[board + ((_player-1)*(1 << 18))];
      }
    }

    double[][] _result = new double[3][3];
    for (int _row = 0; _row < 3; _row++) {
      Arrays.fill(_result[_row],0.);
    }
    
    // With an empty big board, use preset values
    if (getSquareCount(Constants.NONE) == 9) {
      for (int _row = 0; _row < 3; _row++) {
        for (int _col = 0; _col < 3; _col++) {
          if (_row == 1  &&  _col == 1) {
            _result[_row][_col] = 1.6;
          }
          else if (Math.abs(_row - _col) != 1) {
            _result[_row][_col] = 1.3;
          }
          else {
            _result[_row][_col] = 1.;
          }
        }
      }
    }
    // If there is something on the big board, use overcomplicated method
    else {
      // Count the % of possible 3 in a rows each big square is in
      int[] _playerPotentialWinSquares = new int[9];
      Arrays.fill(_playerPotentialWinSquares,0);
      int[] _enemyPotentialWinSquares = new int[9];
      Arrays.fill(_enemyPotentialWinSquares,0);
      int[][] _winPatterns = new int[][] {{0, 1, 2},
                                          {3, 4, 5},
                                          {6, 7, 8},
                                          {0, 3, 6},
                                          {1, 4, 7},
                                          {2, 5, 8},
                                          {0, 4, 8},
                                          {2, 4, 6}};
      PATTERN: for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 3; j++) {
          if (getSquare(_winPatterns[i][j]) == Constants.TIE) {
            continue PATTERN;
          }
          if (getSquare(_winPatterns[i][j]) == SmallBoard.opposite(_player)) {
            break;
          }
          if (j == 2) {
            _playerPotentialWinSquares[_winPatterns[i][0]]++;
            _playerPotentialWinSquares[_winPatterns[i][1]]++;
            _playerPotentialWinSquares[_winPatterns[i][2]]++;
          }
        }
        for (int j = 0; j < 3; j++) {
          if (getSquare(_winPatterns[i][j]) == _player) {
            break;
          }
          if (j == 2) {
            _enemyPotentialWinSquares[_winPatterns[i][0]]++;
            _enemyPotentialWinSquares[_winPatterns[i][1]]++;
            _enemyPotentialWinSquares[_winPatterns[i][2]]++;
          }
        }
      }
  
      // Calculate the multiplier for each big square based on % of possible wins, # of win squares created and if it wins
      int _playerWinSquares = getWinSquareCount(_player);
      int _enemyWinSquares = getWinSquareCount(SmallBoard.opposite(_player));
      for (int _row = 0; _row < 3; _row++) {
        for (int _col = 0; _col < 3; _col++) {
          if (getSquare2d(_row,_col) != Constants.NONE) {
            _result[_row][_col] = -1.;
            continue;
          }
          if (wouldWin(_row,_col,_player)) {
            _result[_row][_col] += 2.;
          }
          if (wouldWin(_row,_col,SmallBoard.opposite(_player))) {
            _result[_row][_col] += 2.;
          }
          if (_result[_row][_col] == 0.) {
            setSquare2d(_row, _col, _player);
            _result[_row][_col] += 0.5 * (getWinSquareCount(_player) - _playerWinSquares);
            setSquare2d(_row, _col, opposite(_player));
            _result[_row][_col] += 0.5 * (getWinSquareCount(SmallBoard.opposite(_player)) - _enemyWinSquares);
            setSquare2d(_row, _col, Constants.NONE);
          }
        }
      }
    }

    return _result;
  }
  
  public void print() {
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        System.out.print(getString(getSquare2d(_row,_col)) + " ");
      }
      System.out.println("");
    }
  }
}