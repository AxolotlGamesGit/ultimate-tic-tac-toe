package main;

import java.util.Arrays;

public class SmallBoard {
  public enum Player {
    NONE,
    X,
    O
  }
  
  public Player[][] Board;

  public SmallBoard() {
    Board = new Player[3][3];
    for (int _row = 0; _row < 3; _row++) {
      Arrays.fill(Board[_row],Player.NONE);
    }
  }
  
  public SmallBoard(Player[][] _board) {
    Board = _board;
  }
  
  public static String getString(Player _player) {
    switch(_player) {
      case X:
        return "x";
      case O:
        return "o";
      case NONE:
        return "-";
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
    }
    return Player.NONE;
  }
  
  public void move(int _row, int _col, Player _player) throws IllegalArgumentException {
    if (_row <= -1  ||  _row >= 3  ||
            _col <= -1  ||  _col >= 3) {
      throw new IndexOutOfBoundsException();
    }
    if (Board[_row][_col] == Player.NONE) {
      Board[_row][_col] = _player;
    }
    else {
      throw new IllegalArgumentException("Square occupied");
    }
  }
  
  public void undo(int _row, int _col) throws IllegalArgumentException {
    if (_row <= -1  ||  _row >= 3  ||
            _col <= -1  ||  _col >= 3) {
      throw new IndexOutOfBoundsException();
    }
    if (Board[_row][_col] != Player.NONE) {
      Board[_row][_col] = Player.NONE;
    }
    else {
      throw new IllegalArgumentException("Can't undo: no player on that square");
    }
  }
  
  public Player getWinner() {
    for (int i = 0; i < 3; i++) {
      if (Board[i][0] != Player.NONE  &&  Board[i][0] == Board[i][1]  &&  Board[i][1] == Board[i][2]) {
        return Board[i][0];
      }
      if (Board[0][i] != Player.NONE  &&  Board[0][i] == Board[1][i]  &&  Board[1][i] == Board[2][i]) {
        return Board[0][i];
      }
    }
    if (Board[1][1] != Player.NONE  &&  Board[0][0] == Board[1][1]  &&  Board[1][1] == Board[2][2]) {
      return Board[1][1];
    }
    if (Board[1][1] != Player.NONE  &&  Board[0][2] == Board[1][1]  &&  Board[1][1] == Board[2][0]) {
      return Board[1][1];
    }
    return Player.NONE;
  }
  
  public boolean isFull() {
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        if (Board[_row][_col] == Player.NONE) {
          return false;
        }
      }
    }
    return true;
  }
  
  public boolean isFinished() {
    return isFull()  ||  getWinner() != Player.NONE;
  }

  public boolean wouldWin(int _row, int _col, Player _player) {
    if (Board[_row][_col] != Player.NONE) {
      return false;
    }
    if (isFinished()) {
      return false;
    }
    boolean _result = false;
    move(_row, _col, _player);
    if (getWinner() != Player.NONE) {
      _result = true;
    }
    undo(_row, _col);

    return _result;
  }

  public int getSquareCount(Player _player) {
    int _result = 0;
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        if (Board[_row][_col] == _player) {
          _result++;
        }
      }
    }

    return _result;
  }

  public int getWinningSquareCount(Player _player) {
    if (isFinished()) {
      return 0;
    }
    int _result = 0;
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        if (Board[_row][_col] == Player.NONE) {
          if (wouldWin(_row, _col, _player)) {
            _result++;
          }
        }
      }
    }
    return _result;
  }
  
  public void print() {
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
          System.out.print(getString(Board[_row][_col]) + " ");
      }
      System.out.println("");
    }
  }
}