package main;

public class Debug {
  public int EvalCount = 0;
  public int MinimaxCount = 0;
  
  private Debug instance;
  
  public Debug() {

  }

  public Debug getInstance() {
    if (instance == null) {
      instance = new Debug();
    }
    return instance;
  }
}