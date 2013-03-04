package uk.ac.manchester.snee.client.utils;

import java.util.ArrayList;

public class Seed
{
  private ArrayList<String> tuples = new ArrayList<String>();
  private ArrayList<Double> oAverage = new ArrayList<Double>();
  private ArrayList<Double> aAverage = new ArrayList<Double>();
  private Integer max = null;
  
  public Seed()
  {
    
  }

  public void setTuples(ArrayList<String> tuples)
  {
    this.tuples = tuples;
  }

  public ArrayList<String> getTuples()
  {
    return tuples;
  }

  public void setoAverage(ArrayList<Double> oAverage)
  {
    this.oAverage = oAverage;
  }

  public ArrayList<Double> getoAverage()
  {
    return oAverage;
  }

  public void setaAverage(ArrayList<Double> aAverage)
  {
    this.aAverage = aAverage;
  }

  public ArrayList<Double> getaAverage()
  {
    return aAverage;
  }

  public void setMax(Integer max)
  {
    this.max = max;
  }

  public Integer getMax()
  {
    return max;
  }
  
  
}