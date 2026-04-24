package main;

public class Debug {
  public int EvalCount = 0;
  public int MinimaxCount = 0;
  public int NodeCount = 0;
  public int ChildCount = 0;
  
  private static Debug instance;
  
  public Debug() {

  }

  public static Debug getInstance() {
    if (instance == null) {
      instance = new Debug();
    }
    return instance;
  }

  public void resetStats() {
    instance.EvalCount = 0;
    instance.MinimaxCount = 0;
    instance.NodeCount = 0;
    instance.ChildCount = 0;
  }
}