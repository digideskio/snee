package uk.ac.manchester.cs.snee.manager.failednode.alternativerouter;

import java.util.Random;

public enum PenaliseNodeHeuristic
{
  TRUE, FALSE;
  
  public static PenaliseNodeHeuristic RandomEnum()
  { 
    PenaliseNodeHeuristic[] values = (PenaliseNodeHeuristic[]) values();
    return values[new Random().nextInt(values.length)];
  }
}