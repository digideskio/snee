package uk.ac.manchester.snee.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;

import uk.ac.manchester.cs.snee.compiler.OptimizationException;
import uk.ac.manchester.cs.snee.compiler.costmodels.cardinalitymodel.CardinalityEstimatedCostModel;
import uk.ac.manchester.cs.snee.manager.AutonomicManagerImpl;
import uk.ac.manchester.cs.snee.manager.common.Adaptation;

public class Plotter implements Serializable
{

  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = -2846057863098603796L;
  
  private File outputFolder = null;
  private int queryID = 1;
  private String sep = System.getProperty("file.separator");
  private File energyPlotFile = null;
  private double energyYMax = 0;
  private File timePlotFile = null;
  private double timeYMax = 0;
  private File qepCostPlotFile = null;
  private double qepYMax = 0;
  private File lifetimePlotFile = null;
  private double lifetimeYMax = 0;
  private File cyclesBurnedPlotFile = null;
  private double cyclesBurnedYMax = 0;
  private File cyclesMissedPlotFile = null;
  private double cyclesMissedYMax = 0;
  private File cyclesLeftPlotFile = null;
  private double cyclesLeftYMax = 0;
  private File tuplesLeftPlotFile = null;
  private double tuplesLeftYMax = 0;
  private File tuplesMissedPlotFile = null;
  private double tuplesMissedYMax = 0;
  private File tuplesBurnedPlotFile = null;
  private double tuplesBurnedYMax = 0;
  private int numberOfAdaptations = 0;
  private BufferedWriter energyWriter;
  private BufferedWriter timeWriter;
  private BufferedWriter qepWriter;
  private BufferedWriter lifetimeWriter;
  private BufferedWriter cyclesBurnedWriter;
  private BufferedWriter cyclesMissedWriter;
  private BufferedWriter cyclesLeftWriter;
  private BufferedWriter tuplesLeftWriter;
  private BufferedWriter tuplesMissedWriter;
  private BufferedWriter tuplesBurnedWriter;
  
  
  
  public Plotter (File outputFolder, AutonomicManagerImpl manager) 
  throws IOException
  {
    this.outputFolder = outputFolder;
    this.outputFolder = new File("plots");
    if(this.outputFolder.exists())
    {
      manager.deleteFileContents(outputFolder);
      this.outputFolder.mkdir();
    }
    else
      this.outputFolder.mkdir();
    
    energyPlotFile = new File(this.outputFolder.toString() + sep + "energyPlot.tex");
    timePlotFile = new File(this.outputFolder.toString() + sep + "timePlot.tex");
    qepCostPlotFile = new File(this.outputFolder.toString() + sep + "qepPlot.tex");
    lifetimePlotFile = new File(this.outputFolder.toString() + sep + "lifetimePlot.tex");
    cyclesBurnedPlotFile = new File(this.outputFolder.toString() + sep + "cyclesBurnedPlot.tex");
    cyclesMissedPlotFile = new File(this.outputFolder.toString() + sep + "cyclesMissedPlot.tex");
    cyclesLeftPlotFile = new File(this.outputFolder.toString() + sep + "cyclesLeftPlot.tex");
    tuplesLeftPlotFile = new File(this.outputFolder.toString() + sep + "tuplesLeftPlot.tex");
    tuplesMissedPlotFile = new File(this.outputFolder.toString() + sep + "tuplesMissedPlot.tex");
    tuplesBurnedPlotFile = new File(this.outputFolder.toString() + sep + "tuplesBurnedPlot.tex");
    if(energyPlotFile.exists())
    {
      energyPlotFile.delete();
    }
    if(timePlotFile.exists())
    {
      timePlotFile.delete();
    }
    if(qepCostPlotFile.exists())
    {
      qepCostPlotFile.delete();
    }
    if(lifetimePlotFile.exists())
    {
      lifetimePlotFile.delete();
    }
    if(cyclesBurnedPlotFile.exists())
    {
      cyclesBurnedPlotFile.delete();
    }
    if(cyclesMissedPlotFile.exists())
    {
      cyclesMissedPlotFile.delete();
    }
    if(cyclesLeftPlotFile.exists())
    {
      cyclesLeftPlotFile.delete();
    }
    if(tuplesLeftPlotFile.exists())
    {
      tuplesLeftPlotFile.delete();
    }
    if(tuplesMissedPlotFile.exists())
    {
      tuplesMissedPlotFile.delete();
    }
    if(tuplesBurnedPlotFile.exists())
    {
      tuplesBurnedPlotFile.delete();
    }
    
    energyWriter = new BufferedWriter(new FileWriter(energyPlotFile, true));
    timeWriter = new BufferedWriter(new FileWriter(timePlotFile, true));
    qepWriter = new BufferedWriter(new FileWriter(qepCostPlotFile, true));
    lifetimeWriter = new BufferedWriter(new FileWriter(lifetimePlotFile));
    cyclesBurnedWriter =  new BufferedWriter(new FileWriter(cyclesBurnedPlotFile, true));
    cyclesMissedWriter = new BufferedWriter(new FileWriter(cyclesMissedPlotFile, true));
    cyclesLeftWriter = new BufferedWriter(new FileWriter(cyclesLeftPlotFile, true));
    tuplesLeftWriter = new BufferedWriter(new FileWriter(tuplesLeftPlotFile, true));
    tuplesMissedWriter = new BufferedWriter(new FileWriter(tuplesMissedPlotFile, true));
    tuplesBurnedWriter = new BufferedWriter(new FileWriter(tuplesBurnedPlotFile, true));
  }
  
  public Plotter (File outputFolder) 
  throws IOException
  {
    this.outputFolder = outputFolder;
    this.outputFolder = new File("plots");
    if(this.outputFolder.exists())
    {
      deleteFileContents(outputFolder);
      this.outputFolder.mkdir();
    }
    else
      this.outputFolder.mkdir();
    
    energyPlotFile = new File(this.outputFolder.toString() + sep + "energyPlot.tex");
    timePlotFile = new File(this.outputFolder.toString() + sep + "timePlot.tex");
    qepCostPlotFile = new File(this.outputFolder.toString() + sep + "qepPlot.tex");
    lifetimePlotFile = new File(this.outputFolder.toString() + sep + "lifetimePlot.tex");
    cyclesBurnedPlotFile = new File(this.outputFolder.toString() + sep + "cyclesBurnedPlot.tex");
    cyclesMissedPlotFile = new File(this.outputFolder.toString() + sep + "cyclesMissedPlot.tex");
    cyclesLeftPlotFile = new File(this.outputFolder.toString() + sep + "cyclesLeftPlot.tex");
    tuplesLeftPlotFile = new File(this.outputFolder.toString() + sep + "tuplesLeftPlot.tex");
    tuplesMissedPlotFile = new File(this.outputFolder.toString() + sep + "tuplesMissedPlot.tex");
    tuplesBurnedPlotFile = new File(this.outputFolder.toString() + sep + "tuplesBurnedPlot.tex");
    if(energyPlotFile.exists())
    {
      energyPlotFile.delete();
    }
    if(timePlotFile.exists())
    {
      timePlotFile.delete();
    }
    if(qepCostPlotFile.exists())
    {
      qepCostPlotFile.delete();
    }
    if(lifetimePlotFile.exists())
    {
      lifetimePlotFile.delete();
    }
    if(cyclesBurnedPlotFile.exists())
    {
      cyclesBurnedPlotFile.delete();
    }
    if(cyclesMissedPlotFile.exists())
    {
      cyclesMissedPlotFile.delete();
    }
    if(cyclesLeftPlotFile.exists())
    {
      cyclesLeftPlotFile.delete();
    }
    if(tuplesLeftPlotFile.exists())
    {
      tuplesLeftPlotFile.delete();
    }
    if(tuplesMissedPlotFile.exists())
    {
      tuplesMissedPlotFile.delete();
    }
    if(tuplesBurnedPlotFile.exists())
    {
      tuplesBurnedPlotFile.delete();
    }
    
    energyWriter = new BufferedWriter(new FileWriter(energyPlotFile, true));
    timeWriter = new BufferedWriter(new FileWriter(timePlotFile, true));
    qepWriter = new BufferedWriter(new FileWriter(qepCostPlotFile, true));
    lifetimeWriter = new BufferedWriter(new FileWriter(lifetimePlotFile));
    cyclesBurnedWriter =  new BufferedWriter(new FileWriter(cyclesBurnedPlotFile, true));
    cyclesMissedWriter = new BufferedWriter(new FileWriter(cyclesMissedPlotFile, true));
    cyclesLeftWriter = new BufferedWriter(new FileWriter(cyclesLeftPlotFile, true));
    tuplesLeftWriter = new BufferedWriter(new FileWriter(tuplesLeftPlotFile, true));
    tuplesMissedWriter = new BufferedWriter(new FileWriter(tuplesMissedPlotFile, true));
    tuplesBurnedWriter = new BufferedWriter(new FileWriter(tuplesBurnedPlotFile, true));
  }
  
  
  
  public void plot(Adaptation global, Adaptation partial, Adaptation local) 
  throws 
  IOException, OptimizationException
  {
    DecimalFormat df = new DecimalFormat("#.#####");
    
    energyWriter.write(queryID + " ");
    if(global != null)
    {
      energyWriter.write(df.format(global.getEnergyCost()) + " ");
      energyYMax = Math.max(energyYMax, global.getEnergyCost());
      numberOfAdaptations++;
    }
    if(partial != null)
    {
      energyWriter.write(df.format(partial.getEnergyCost()) + " ");
      energyYMax = Math.max(energyYMax, partial.getEnergyCost());
      numberOfAdaptations++;
    }
    if(local != null)
    {
      energyWriter.write(df.format(local.getEnergyCost()) + " ");
      energyYMax = Math.max(energyYMax, local.getEnergyCost());
      numberOfAdaptations++;
    }
    energyWriter.write("\n");
    
    timeWriter.write(queryID + " ");
    if(global != null)
    {
      timeWriter.write(df.format(global.getTimeCost() / 1000) + " ");
      timeYMax = Math.max(timeYMax, global.getTimeCost() / 1000);
    }
    if(partial != null)
    {
      timeWriter.write(df.format(partial.getTimeCost() / 1000) + " ");
      timeYMax = Math.max(timeYMax, partial.getTimeCost() / 1000);
    }
    if(local != null)
    {
      timeWriter.write(df.format(local.getTimeCost() / 1000) + " ");
      timeYMax = Math.max(timeYMax, local.getTimeCost() / 1000);
    }
    timeWriter.write("\n");
    
    qepWriter.write(queryID + " ");
    if(global != null)
    {
      qepWriter.write(df.format(global.getRuntimeCost()) + " ");
      qepYMax = Math.max(qepYMax, global.getRuntimeCost());
    }
    if(partial != null)
    {
      qepWriter.write(df.format(partial.getRuntimeCost()) + " ");
      qepYMax = Math.max(qepYMax, partial.getRuntimeCost());
    }
    if(local != null)
    {
      qepWriter.write(df.format(local.getRuntimeCost()) + " ");
      qepYMax = Math.max(qepYMax, local.getRuntimeCost());
    }
    qepWriter.write("\n");
    
    lifetimeWriter.write(queryID + " ");
    if(global != null)
    {
      lifetimeWriter.write(df.format(global.getLifetimeEstimate() / 1000) + " ");
      lifetimeYMax = Math.max(lifetimeYMax, global.getLifetimeEstimate() / 1000);
    }
    if(partial != null)
    {
      lifetimeWriter.write(df.format(partial.getLifetimeEstimate() / 1000) + " ");
      lifetimeYMax = Math.max(lifetimeYMax, partial.getLifetimeEstimate() / 1000);
    }
    if(local != null)
    {
      lifetimeWriter.write(df.format(local.getLifetimeEstimate() / 1000) + " ");
      lifetimeYMax = Math.max(lifetimeYMax, local.getLifetimeEstimate() / 1000);
    }
    lifetimeWriter.write("\n");
    
    cyclesBurnedWriter.write(queryID + " ");
    if(global != null)
    {
      cyclesBurnedWriter.write(df.format(global.getEnergyCost() / global.getRuntimeCost()) + " ");
      cyclesBurnedYMax = Math.max(cyclesBurnedYMax, global.getEnergyCost() / global.getRuntimeCost());
    }
    if(partial != null)
    {
      cyclesBurnedWriter.write(df.format(partial.getEnergyCost() / partial.getRuntimeCost()) + " ");
      cyclesBurnedYMax = Math.max(cyclesBurnedYMax, partial.getEnergyCost() / partial.getRuntimeCost());
    }
    if(local != null)
    {
      cyclesBurnedWriter.write(df.format(local.getEnergyCost() / local.getRuntimeCost()) + " ");
      cyclesBurnedYMax = Math.max(cyclesBurnedYMax, local.getEnergyCost() / local.getRuntimeCost());
    }
    cyclesBurnedWriter.write("\n");   
    
    cyclesMissedWriter.write(queryID + " ");
    if(global != null)
    {
      cyclesMissedWriter.write(df.format(global.getTimeCost() / global.getNewQep().getAgendaIOT().getLength_bms(false)) + " ");
      cyclesMissedYMax = Math.max(cyclesMissedYMax, global.getTimeCost() / global.getNewQep().getAgendaIOT().getLength_bms(false));
    }
    if(partial != null)
    {
      cyclesMissedWriter.write(df.format(partial.getTimeCost() / partial.getNewQep().getAgendaIOT().getLength_bms(false)) + " ");
      cyclesMissedYMax = Math.max(cyclesMissedYMax, partial.getTimeCost() / partial.getNewQep().getAgendaIOT().getLength_bms(false));
    }
    if(local != null)
    {
      cyclesMissedWriter.write(df.format(local.getTimeCost() / local.getNewQep().getAgendaIOT().getLength_bms(false)) + " ");
      cyclesMissedYMax = Math.max(cyclesMissedYMax, local.getTimeCost() / local.getNewQep().getAgendaIOT().getLength_bms(false));
    }
    cyclesMissedWriter.write("\n");   

    cyclesLeftWriter.write(queryID + " ");
    if(global != null)
    {
      cyclesLeftWriter.write(df.format(global.getLifetimeEstimate() / global.getNewQep().getAgendaIOT().getLength_bms(false)) + " ");
      cyclesLeftYMax = Math.max(cyclesLeftYMax, global.getLifetimeEstimate() / global.getNewQep().getAgendaIOT().getLength_bms(false));
    }
    if(partial != null)
    {
      cyclesLeftWriter.write(df.format(partial.getLifetimeEstimate() / partial.getNewQep().getAgendaIOT().getLength_bms(false)) + " ");
      cyclesLeftYMax = Math.max(cyclesLeftYMax, partial.getLifetimeEstimate() / partial.getNewQep().getAgendaIOT().getLength_bms(false));
    }
    if(local != null)
    {
      cyclesLeftWriter.write(df.format(local.getLifetimeEstimate() / local.getNewQep().getAgendaIOT().getLength_bms(false)) + " ");
      cyclesLeftYMax = Math.max(cyclesLeftYMax, local.getLifetimeEstimate() / local.getNewQep().getAgendaIOT().getLength_bms(false));
    }
    cyclesLeftWriter.write("\n"); 

    float globalTupleCount = 0;
    float partialTupleCount = 0;
    float localTupleCount = 0;
    if(global != null)
    {
      CardinalityEstimatedCostModel tupleModel = new CardinalityEstimatedCostModel(global.getNewQep());
      tupleModel.runModel();
      globalTupleCount = tupleModel.returnAgendaExecutionResult();
    }
    if(partial != null)
    {
      CardinalityEstimatedCostModel tupleModel = new CardinalityEstimatedCostModel(partial.getNewQep());
      tupleModel.runModel();
      partialTupleCount = tupleModel.returnAgendaExecutionResult();
    }
    if(local != null)
    {
      CardinalityEstimatedCostModel tupleModel = new CardinalityEstimatedCostModel(local.getNewQep());
      tupleModel.runModel();
      localTupleCount = tupleModel.returnAgendaExecutionResult();
    }
    
    tuplesLeftWriter.write(queryID + " ");
    if(global != null)
    {
      tuplesLeftWriter.write(df.format(globalTupleCount * (global.getLifetimeEstimate() / global.getNewQep().getAgendaIOT().getLength_bms(false))) + " ");
      tuplesLeftYMax = Math.max(tuplesLeftYMax, globalTupleCount * (global.getLifetimeEstimate() / global.getNewQep().getAgendaIOT().getLength_bms(false)));
    }
    if(partial != null)
    {
      tuplesLeftWriter.write(df.format(partialTupleCount * (partial.getLifetimeEstimate() / partial.getNewQep().getAgendaIOT().getLength_bms(false))) + " ");
      tuplesLeftYMax = Math.max(tuplesLeftYMax, partialTupleCount * (partial.getLifetimeEstimate() / partial.getNewQep().getAgendaIOT().getLength_bms(false)));
    }
    if(local != null)
    {
      tuplesLeftWriter.write(df.format(localTupleCount * (local.getLifetimeEstimate() / local.getNewQep().getAgendaIOT().getLength_bms(false))) + " ");
      tuplesLeftYMax = Math.max(tuplesLeftYMax, localTupleCount * (local.getLifetimeEstimate() / local.getNewQep().getAgendaIOT().getLength_bms(false)));
    }
    tuplesLeftWriter.write("\n"); 
    
    tuplesMissedWriter.write(queryID + " ");
    if(global != null)
    {
      tuplesMissedWriter.write(df.format((global.getTimeCost() / global.getNewQep().getAgendaIOT().getLength_bms(false)) * globalTupleCount) + " ");
      tuplesMissedYMax = Math.max(tuplesMissedYMax, (global.getTimeCost() / global.getNewQep().getAgendaIOT().getLength_bms(false)) * globalTupleCount);
    }
    if(partial != null)
    {
      tuplesMissedWriter.write(df.format((partial.getTimeCost() / partial.getNewQep().getAgendaIOT().getLength_bms(false)) * partialTupleCount) + " ");
      tuplesMissedYMax = Math.max(tuplesMissedYMax, (partial.getTimeCost() / partial.getNewQep().getAgendaIOT().getLength_bms(false)) * partialTupleCount);
    }
    if(local != null)
    {
      tuplesMissedWriter.write(df.format((local.getTimeCost() / local.getNewQep().getAgendaIOT().getLength_bms(false)) * localTupleCount) + " ");
      tuplesMissedYMax = Math.max(tuplesMissedYMax, (local.getTimeCost() / local.getNewQep().getAgendaIOT().getLength_bms(false)) * localTupleCount);
    }
    tuplesMissedWriter.write("\n"); 
    
    tuplesBurnedWriter.write(queryID + " ");
    if(global != null)
    {
      tuplesBurnedWriter.write(df.format((global.getEnergyCost() / global.getRuntimeCost()) * globalTupleCount) + " ");
      tuplesBurnedYMax =  Math.max(tuplesBurnedYMax, (global.getEnergyCost() / global.getRuntimeCost()) * globalTupleCount);
    }
    if(partial != null)
    {
      tuplesBurnedWriter.write(df.format((partial.getEnergyCost() / partial.getRuntimeCost()) * partialTupleCount) + " ");
      tuplesBurnedYMax =  Math.max(tuplesBurnedYMax, (partial.getEnergyCost() / partial.getRuntimeCost()) * partialTupleCount);
    }
    if(local != null)
    {
      tuplesBurnedWriter.write(df.format((local.getEnergyCost() / local.getRuntimeCost()) * localTupleCount) + " ");
      tuplesBurnedYMax =  Math.max(tuplesBurnedYMax, (local.getEnergyCost() / local.getRuntimeCost()) * localTupleCount);
    }
    tuplesBurnedWriter.write("\n"); 
    
    energyWriter.flush();
    timeWriter.flush(); 
    qepWriter.flush(); 
    lifetimeWriter.flush();
    cyclesBurnedWriter.flush();
    cyclesMissedWriter.flush(); 
    cyclesLeftWriter.flush();
    tuplesLeftWriter.flush(); 
    tuplesMissedWriter.flush();
    tuplesBurnedWriter.flush();
    
    try
    {
      if(numberOfAdaptations == 3)
      {
        Runtime rt = Runtime.getRuntime();
        File bash = new File("src/main/resources/bashScript/plotGnuplot3");
        String location = bash.getAbsolutePath();
        String [] commandArg = new String[9];
        commandArg[0] = location;
        commandArg[1] = "topologies";
        commandArg[2] = "adaptation_cost_(J)";
        commandArg[3] = new Integer(queryID+1).toString();
        commandArg[4] = new Double(this.energyYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "energy";
        commandArg[6] = energyPlotFile.getAbsolutePath();
        commandArg[7] = "global";
        commandArg[8] = "partial";
        rt.exec(commandArg);
        //time
        commandArg[2] = "adaptation_cost_(1000_s)";
        commandArg[4] = new Double(this.timeYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "time";
        commandArg[6] = timePlotFile.getAbsolutePath();
        rt.exec(commandArg);
        //qep
        commandArg[2] = "qep_cost_(J)";
        commandArg[4] = new Double(this.qepYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "qep";
        commandArg[6] = qepCostPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        //lifetime
        commandArg[2] = "lifetime_(1000_s)";
        commandArg[4] = new Double(this.lifetimeYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "lifetime";
        commandArg[6] = lifetimePlotFile.getAbsolutePath();
        rt.exec(commandArg);
        //cycles burned
        commandArg[2] = "cycles_burned";
        commandArg[4] = new Double(this.cyclesBurnedYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "cyclesburned";
        commandArg[6] = cyclesBurnedPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        //cycles missed
        commandArg[2] = "cycles_missed";
        commandArg[4] = new Double(this.cyclesMissedYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "cyclesMissed";
        commandArg[6] = cyclesMissedPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        // cycles left
        commandArg[2] = "cycles_left";
        commandArg[4] = new Double(this.cyclesLeftYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "cyclesleft";
        commandArg[6] = cyclesLeftPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        // tuples left        
        commandArg[2] = "tuples_left";
        commandArg[4] = new Double(this.tuplesLeftYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "tuplesleft";
        commandArg[6] = tuplesLeftPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        // tuples missed
        commandArg[2] = "tuples_missed";
        commandArg[4] = new Double(this.tuplesMissedYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "tuplesMissed";
        commandArg[6] = tuplesMissedPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        // tuples burned
        commandArg[2] = "tuples_burned";
        commandArg[4] = new Double(this.tuplesBurnedYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "tuplesBurned";
        commandArg[6] = tuplesBurnedPlotFile.getAbsolutePath();
        rt.exec(commandArg);
      }
      else
      {
        //energy
        Runtime rt = Runtime.getRuntime();
        File bash = new File("src/main/resources/bashScript/plotGnuplot2");
        String location = bash.getAbsolutePath();
        String [] commandArg = new String[10];
        commandArg[0] = location;
        commandArg[1] = "topologies";
        commandArg[2] = "adaptation_cost_(J)";
        commandArg[3] = new Integer(queryID+1).toString();
        commandArg[4] = new Double(this.energyYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "energy";
        commandArg[6] = energyPlotFile.getAbsolutePath();
        commandArg[7] = "global";
        commandArg[8] = "partial";
        commandArg[9] = "local";
        rt.exec(commandArg);
        //time
        commandArg[2] = "adaptation_cost_(1000 s)";
        commandArg[4] = new Double(this.timeYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "time";
        commandArg[6] = timePlotFile.getAbsolutePath();
        rt.exec(commandArg);
        //qep
        commandArg[2] = "qep_cost_(J)";
        commandArg[4] = new Double(this.qepYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "qep";
        commandArg[6] = qepCostPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        //lifetime
        commandArg[2] = "lifetime (1000 s)";
        commandArg[4] = new Double(this.lifetimeYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "lifetime";
        commandArg[6] = lifetimePlotFile.getAbsolutePath();
        rt.exec(commandArg);
        //cycles burned
        commandArg[2] = "cycles_burned";
        commandArg[4] = new Double(this.cyclesBurnedYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "cyclesburned";
        commandArg[6] = cyclesBurnedPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        //cycles missed
        commandArg[2] = "cycles_missed";
        commandArg[4] = new Double(this.cyclesMissedYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "cyclesMissed";
        commandArg[6] = cyclesMissedPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        // cycles left
        commandArg[2] = "cycles_left";
        commandArg[4] = new Double(this.cyclesLeftYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "cyclesleft";
        commandArg[6] = cyclesLeftPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        // tuples left        
        commandArg[2] = "tuples_left";
        commandArg[4] = new Double(this.tuplesLeftYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "tuplesleft";
        commandArg[6] = tuplesLeftPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        // tuples missed
        commandArg[2] = "tuples_missed";
        commandArg[4] = new Double(this.tuplesMissedYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "tuplesMissed";
        commandArg[6] = tuplesMissedPlotFile.getAbsolutePath();
        rt.exec(commandArg);
        // tuples burned
        commandArg[2] = "tuples_burned";
        commandArg[4] = new Double(this.tuplesBurnedYMax).toString();
        commandArg[5] = outputFolder.getAbsolutePath() + sep + "tuplesBurned";
        commandArg[6] = tuplesBurnedPlotFile.getAbsolutePath();
        rt.exec(commandArg);
      }
      queryID ++;
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
      e.printStackTrace();
      System.exit(0);
    }
  }
  
  private static void deleteFileContents(File firstOutputFolder)
  {
    if(firstOutputFolder.exists())
    {
      File[] contents = firstOutputFolder.listFiles();
      for(int index = 0; index < contents.length; index++)
      {
        File delete = contents[index];
        if(delete.isDirectory())
          if(delete != null && delete.listFiles().length > 0)
            deleteFileContents(delete);
          else
            delete.delete();
        else
          delete.delete();
      }
    }
    else
    {
      firstOutputFolder.mkdir();
    }  
  }
  
}
