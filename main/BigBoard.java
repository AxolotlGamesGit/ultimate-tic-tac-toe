package main;

import main.SmallBoard.Player;

public class BigBoard {
  public SmallBoard[][] Board;
  public SmallBoard.Player Turn;
  public SmallBoard Wins;
  public int BigRow;
  public int BigCol;
    
  public BigBoard() {
    Board = new SmallBoard[3][3];
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        Board[_row][_col] = new SmallBoard();
      }
    }
    Wins = new SmallBoard();
    Turn = Player.X;
    BigRow = -1;
    BigCol = -1;
  }
  
  public BigBoard(SmallBoard[][] _board, SmallBoard.Player _turn, int _bigRow, int _bigCol) {
    Board = _board;
    Turn = _turn;
    BigRow = _bigRow;
    BigCol = _bigCol;
    Wins = new SmallBoard();
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        Wins.move(_row,_col,Board[_row][_col].getWinner());
      }
    }
  }
  
  public static BigBoard copy(BigBoard _bigBoard) {
    return new BigBoard(_bigBoard.Board, _bigBoard.Turn, _bigBoard.BigRow, _bigBoard.BigCol);
  }
  
  public void move(int _bigRow, int _bigCol, int _smallRow, int _smallCol) throws IllegalArgumentException {
    if (Turn == Player.NONE) {
      throw new IllegalArgumentException("Game already over");
    }
    if ((_bigRow != BigRow  ||  _bigCol != BigCol)
            &&  (BigRow != -1  &&  !Board[BigRow][BigCol].isFinished())) {
      Node _node = new Node(BigBoard.copy(this));
      _node.explore(true);
      throw new IllegalArgumentException("Can't move in that board: " + _bigRow + " " + _bigCol);
    }
    if (_bigRow == -1  ||  _bigCol == -1) {
      throw new IllegalArgumentException("Must select a big square");
    }
    if (_bigRow <= -1  ||  _bigRow >= 3  ||
            _bigCol <= -1  ||  _bigCol >= 3  ||
            _smallRow <= -1  ||  _smallRow >= 3  ||
            _smallCol <= -1  ||  _smallCol >= 3) {
      throw new IllegalArgumentException("Index out of bounds");
    }
    SmallBoard currentBoard = Board[_bigRow][_bigCol];
    currentBoard.move(_smallRow,_smallCol,Turn);
    if (currentBoard.getWinner() != Player.NONE) {
      Wins.move(_bigRow,_bigCol,currentBoard.getWinner());
    }
    BigRow = _smallRow;
    BigCol = _smallCol;
    if (Board[BigRow][BigCol].isFinished()) {
      BigRow = -1;
      BigCol = -1;
    }
    if (Wins.isFinished()) {
      Turn = Player.NONE;
    }
    if (Turn == Player.X) {
      Turn = Player.O;
    }
    else if (Turn == Player.O) {
      Turn = Player.X;
    }
  }
  
  public void undo(int _bigRow, int _bigCol, int _smallRow, int _smallCol, boolean _moveAnywhere) throws IllegalArgumentException {
    if (_bigRow <= -1  ||  _bigRow >= 3  ||
            _bigCol <= -1  ||  _bigCol >= 3  ||
            _smallRow <= -1  ||  _smallRow >= 3  ||
            _smallCol <= -1  ||  _smallCol >= 3) {
      throw new IllegalArgumentException("Index out of bounds");
    }
    SmallBoard currentBoard = Board[_bigRow][_bigCol];
    if (Turn == Player.X) {
      Turn = Player.O;
    }
    else if (Turn == Player.O) {
      Turn = Player.X;
    }
    if (Turn == Player.NONE) {
      Turn = currentBoard.Board[_smallRow][_smallCol];
    }
    currentBoard.undo(_smallRow,_smallCol);
    Wins.Board[_bigRow][_bigCol] = Player.NONE;
    BigRow = _moveAnywhere ? -1 : _bigRow;
    BigCol = _moveAnywhere ? -1 : _bigCol;
  }
  
  public void move1d(int _bigSquare, int _smallSquare) throws IllegalArgumentException {
    move(_bigSquare/3,_bigSquare%3,_smallSquare/3,_smallSquare%3);
  }
  
  public void undo1d(int _bigSquare, int _smallSquare, boolean _moveAnywhere) throws IllegalArgumentException {
    undo(_bigSquare/3,_bigSquare%3,_smallSquare/3,_smallSquare%3,_moveAnywhere);
  }
  
  public void move(int _row, int _col) throws IllegalArgumentException {
    move(BigRow, BigCol, _row, _col);
  }

  public boolean isFinished() {
    if (Wins.isFinished()) {
      return true;
    }
    for (int _row = 0; _row < 3; _row++) {
      for (int _col = 0; _col < 3; _col++) {
        if (!Board[_row][_col].isFinished()) {
          return false;
        }
      }
    }
    return true;
  }
  
  public void print() {
    for (int _bigRow = 0; _bigRow < 3; _bigRow++) {
      for (int _smallRow = 0; _smallRow < 3; _smallRow++) {
        System.out.print(" ");
        for (int _bigCol = 0; _bigCol < 3; _bigCol++) {
          // Print small board state if there is no winner
          if (!Board[_bigRow][_bigCol].isFinished()) {
            for (int _smallCol = 0; _smallCol < 3; _smallCol++) {
              switch (Board[_bigRow][_bigCol].Board[_smallRow][_smallCol]) {
                case X:
                  System.out.print("x");
                  break;
                case O:
                  System.out.print("o");
                  break;
                case NONE:
                  if ((_bigRow == BigRow  &&  _bigCol == BigCol)  ||  (BigRow == -1)) {
                    System.out.print("~");
                  }
                  else {
                    System.out.print("-");
                  }
                  break;
              }
              if (_smallCol < 2) {
                System.out.print(" ");
              }
            }
          }
          // Print winner as big
          else {
            // X
            switch (Board[_bigRow][_bigCol].getWinner()) {
              case X:
                switch (_smallRow) {
                  case 0:
                    System.out.print(" \\ / ");
                    break;
                  case 1:
                    System.out.print("  X  ");
                    break;
                  case 2:
                    System.out.print(" / \\ ");
                    break;
                }
                break;
              case O:
                switch (_smallRow) {
                  case 0:
                    System.out.print(" /‾\\ ");
                    break;
                  case 1:
                    System.out.print("|   |");
                    break;
                  case 2:
                    System.out.print(" \\_/ ");
                    break;
                }
                break;
              case NONE:
                switch (_smallRow) {
                  case 0:
                    System.out.print("     ");
                    break;
                  case 1:
                    System.out.print("-----");
                    break;
                  case 2:
                    System.out.print("     ");
                    break;
                }
                break;
            }
          }
          if (_bigCol < 2) {
            // System.out.print("  |  ");
            System.out.print(" | ");
          }
        }
        System.out.println("");
      }
      if (_bigRow < 2) {
        System.out.println("-----------------------");
      }
    }
    if (Wins.isFinished()) {
      switch (Wins.getWinner()) {
        case X:
          System.out.println("X WINS!");
          break;
        case O:
          System.out.println("O WINS!");
          break;
        case NONE:
          System.out.println("IT'S A TIE!");
          break;
      }
    } 
  }
}