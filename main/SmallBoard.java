package main;

import java.util.ArrayList;

public class SmallBoard {
  public enum Player {
    NONE(0),
    X(1),
    O(2),
    TIE(3);

    public final int Value;

    Player(int _value) {
      this.Value = _value;
    }
    
    public static Player fromInt(int _value) {
      switch (_value) {
        case 0:
          return NONE;
        case 1:
          return X;
        case 2:
          return O;
        case 3:
          return TIE;
      }
      return NONE;
    }
  }
  
  private int board;

  public SmallBoard() {
    board = 0;
  }
  
  public SmallBoard(int _board) {
    board = _board;
  }
  
  public static String getString(Player _player) {
    switch(_player) {
      case X:
        return "x";
      case O:
        return "o";
      case NONE:
        return "-";
      case TIE:
        return "/";
    }
    return " ";
  }

  public static Player opposite(Player _player) {
    switch(_player) {
      case X:
        return Player.O;
      case O:
        return Player.X;
      case NONE:
        return Player.NONE;
      case TIE:
        return Player.TIE;
    }
    return Player.NONE;
  }

  public Player getSquare(int _square) {
    return Player.fromInt((board >> 2*_square)&3);
  }

  public Player getSquare2d(int _row, int _col) {
    return getSquare(_row*3+_col);
  }
  
  public void setSquare(int _square, Player _player) {
    board = (board & (~(3<<2*_square))) | (_player.Value<<2*_square);
  }

  public void setSquare2d(int _row, int _col, Player _player) {
    setSquare(_row*3+_col, _player);
  }
  
  public void move(int _square, Player _player) throws IllegalArgumentException {
    if (_square <= -1  ||  _square >= 9) {
      throw new IndexOutOfBoundsException();
    }
    if (getSquare(_square) == Player.NONE  ||  _player == Player.NONE) {
      setSquare(_square, _player);
    }
    else {
      throw new IllegalArgumentException("Square occupied: " + _square);
    }
  }
  
  public void move2d(int _row, int _col, Player _player) throws IllegalArgumentException {
    move(_row*3+_col, _player);
  }
  
  public void undo(int _square) throws IllegalArgumentException {
    if (_square <= -1  ||  _square >= 9) {
      throw new IndexOutOfBoundsException();
    }
    if (getSquare(_square) != Player.NONE) {
      setSquare(_square, Player.NONE);
    }
    else {
      throw new IllegalArgumentException("Can't undo: no player on that square " + _square);
    }
  }
  
  public void undo2d(int _row, int _col) throws IllegalArgumentException {
    undo(_row*3+_col);
  }
  
  public Player getWinner() {
    int[][] _winPatterns = new int[][] {{0, 1, 2},
                                          {3, 4, 5},
                                          {6, 7, 8},
                                          {0, 3, 6},
                                          {1, 4, 7},
                                          {2, 5, 8},
                                          {0, 4, 8},
                                          {2, 4, 6}};
    for (int i = 0; i < 8; i++) {
      if (getSquare(_winPatterns[i][0]) != Player.NONE  &&  
          getSquare(_winPatterns[i][0]) != Player.TIE  &&
          getSquare(_winPatterns[i][0]) == getSquare(_winPatterns[i][1])  &&
          getSquare(_winPatterns[i][1]) == getSquare(_winPatterns[i][2])) {
        return getSquare(_winPatterns[i][0]);
      }
    }
    if (isFull()) {
      return Player.TIE;
    }
    return Player.NONE;
  }
  
  public boolean isFull() {
    for (int i = 0; i < 9; i++) {
      if (getSquare(i) == Player.NONE) {
        return false;
      }
    }
    return true;
  }
  
  public boolean isFinished() {
    return getWinner() != Player.NONE;
  }

  public boolean wouldWin(int _row, int _col, Player _player) {
    if (getSquare2d(_row,_col) != Player.NONE) {
      return false;
    }
    if (isFinished()) {
      return false;
    }
    boolean _result = false;
    setSquare2d(_row, _col, _player);
    if (getWinner() != Player.NONE  &&  getWinner() != Player.TIE) {
      _result = true;
    }
    setSquare2d(_row, _col, Player.NONE);

    return _result;
  }

  public int getSquareCount(Player _player) {
    int _result = 0;
    for (int i = 0; i < 9; i++) {
      if (getSquare(i) == _player) {
        _result++;
      }
    }

    return _result;
  }

  public int getWinSquareCount(Player _player) {
    if (isFinished()) {
      return 0;
    }
    int _result = 0;
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        if (getSquare2d(_row,_col) == Player.NONE) {
          if (wouldWin(_row, _col, _player)) {
            _result++;
          }
        }
      }
    }
    return _result;
  }

  public ArrayList<Integer> getOrderedMoves(Player _player) {
    ArrayList<Integer> _result = new ArrayList<Integer>();
    for (int i = 0; i < 9; i++) {
      if (getSquare(i) == Player.NONE) {
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

  public double eval(Player _player) {
    double _result = 0.;
    Player _enemy = opposite(_player);
    int _playerWinSquares = getWinSquareCount(_player);
    int _enemyWinSquares = getWinSquareCount(_enemy);
    _result += 1. * _playerWinSquares;
    _result -= 1. * _enemyWinSquares;
    for (int _square = 0; _square < 9; _square++) {
      if (getSquare(_square) == Player.NONE) {
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
  
  public void print() {
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        System.out.print(getString(getSquare2d(_row,_col)) + " ");
      }
      System.out.println("");
    }
  }
}