package uk.ac.manchester.cs.snee.manager.planner.successorrelation;

public class StoppingCriteria
{
  private static int numberOfIterationsTillStop = 100;
  
  public static  boolean satisifiesStoppingCriteria(int interations)
  {
    if(interations >= numberOfIterationsTillStop)
      return true;
    else
      return false;
  }
}