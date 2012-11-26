package uk.ac.manchester.cs.snee.manager.planner.costbenifitmodel.model.energy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import uk.ac.manchester.cs.snee.common.SNEEConfigurationException;
import uk.ac.manchester.cs.snee.compiler.OptimizationException;
import uk.ac.manchester.cs.snee.compiler.costmodels.avroracosts.AlphaBetaExpression;
import uk.ac.manchester.cs.snee.compiler.costmodels.avroracosts.AvroraCostExpressions;
import uk.ac.manchester.cs.snee.compiler.costmodels.avroracosts.AvroraCostParameters;
import uk.ac.manchester.cs.snee.compiler.iot.AgendaIOT;
import uk.ac.manchester.cs.snee.compiler.iot.InstanceExchangePart;
import uk.ac.manchester.cs.snee.compiler.iot.InstanceFragment;
import uk.ac.manchester.cs.snee.compiler.iot.InstanceFragmentTask;
import uk.ac.manchester.cs.snee.compiler.queryplan.CommunicationTask;
import uk.ac.manchester.cs.snee.compiler.queryplan.Fragment;
import uk.ac.manchester.cs.snee.compiler.queryplan.FragmentTask;
import uk.ac.manchester.cs.snee.compiler.queryplan.RadioOnTask;
import uk.ac.manchester.cs.snee.compiler.queryplan.SleepTask;
import uk.ac.manchester.cs.snee.compiler.queryplan.Task;
import uk.ac.manchester.cs.snee.manager.planner.costbenifitmodel.model.channel.ChannelModel;
import uk.ac.manchester.cs.snee.manager.planner.unreliablechannels.LogicalOverlayNetworkHierarchy;
import uk.ac.manchester.cs.snee.manager.planner.unreliablechannels.UnreliableChannelAgenda;
import uk.ac.manchester.cs.snee.metadata.CostParameters;
import uk.ac.manchester.cs.snee.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.snee.metadata.schema.TypeMappingException;
import uk.ac.manchester.cs.snee.metadata.source.sensornet.Site;
import uk.ac.manchester.cs.snee.metadata.source.sensornet.Topology;
import uk.ac.manchester.cs.snee.operators.sensornet.SensornetAcquireOperator;


public class SiteOverlayRobustEnergyModel extends SiteOverlayEnergyModel
{ 
  
   private ChannelModel channelModel = null;
   public SiteOverlayRobustEnergyModel(UnreliableChannelAgenda agenda,
                                      LogicalOverlayNetworkHierarchy overlayNetwork,
                                      ChannelModel channelModel, ArrayList<String> failedNodes,
                                      Topology network, CostParameters costs)
   throws SNEEConfigurationException, IOException,
   OptimizationException, SchemaMetadataException, TypeMappingException
   {
     super(agenda, overlayNetwork);
     this.channelModel = channelModel;
     channelModel.runModel(failedNodes, agenda.getIOT());
   }
   
   /**
    * Returns the total site energy in Joules according to model.
    * @param site
    * @return
    * @throws TypeMappingException 
    * @throws SchemaMetadataException 
    * @throws OptimizationException 
   * @throws SNEEConfigurationException 
    */
   public double getSiteEnergyConsumption(Site site) 
   throws OptimizationException, SchemaMetadataException, 
   TypeMappingException, SNEEConfigurationException 
   {
     double sumEnergy = 0;
     long cpuActiveTimeBms = 0;
     double sensorEnergy = 0;
    
     Site agendaSite =  this.agenda.getSiteByID(site);
     ArrayList<Task> siteTasks = this.agenda.getTasks().get(agendaSite);
     //not within the QEP. so no cost
     if(siteTasks == null)
     {
       sumEnergy += getCPUEnergy(0);
       return sumEnergy;
     }
     for (int i=0; i<siteTasks.size(); i++) 
     {
       Task t = siteTasks.get(i);
       if (t instanceof SleepTask) 
       {
         continue;
       }
       
       cpuActiveTimeBms += t.getDuration();
       if (t instanceof FragmentTask) 
       {
         if(!t.isRan())
         { 
           cpuActiveTimeBms -= t.getDuration();
         }
         else
         {
           FragmentTask ft = (FragmentTask)t;
           Fragment f = ft.getFragment();
           if (f.containsOperatorType(SensornetAcquireOperator.class)) 
             sensorEnergy += AvroraCostParameters.getSensorEnergyCost();
           sumEnergy += sensorEnergy;
         }
       }
       else if(t instanceof InstanceFragmentTask)
       {
         if(!t.isRan())
         { 
           cpuActiveTimeBms -= t.getDuration();
         }
         else
         {
           InstanceFragmentTask ft = (InstanceFragmentTask)t;
           InstanceFragment f = ft.getFragment();
           if (f.containsOperatorType(SensornetAcquireOperator.class)) 
             sensorEnergy += AvroraCostParameters.getSensorEnergyCost();
           sumEnergy += sensorEnergy;
         }
       }
       else if (t instanceof CommunicationTask)
       {
         CommunicationTask ct = (CommunicationTask)t;
         
         if(!t.isRan())
           cpuActiveTimeBms -= t.getDuration();
         else
         {
           sumEnergy += getRadioEnergy(ct);
         }
       }
       else if (t instanceof RadioOnTask) 
       {
          double taskDuration = AgendaIOT.bmsToMs(t.getDuration())/1000.0;
          double radioRXAmp = AvroraCostParameters.getRadioReceiveAmpere(); 
          double voltage = AvroraCostParameters.VOLTAGE;
          double taskEnergy = taskDuration * radioRXAmp * voltage;        
          sumEnergy += taskEnergy;
       }
     }
     sumEnergy += getCPUEnergy(cpuActiveTimeBms);
     return sumEnergy;
   } 
   
   /**
    * Returns radio energy for communication tasks (J) for agenda according to model.
    * Excludes radio switch on.
    * @param ct
    * @return
    * @throws TypeMappingException 
    * @throws SchemaMetadataException 
    * @throws OptimizationException 
    */
   public double getRadioEnergy(CommunicationTask ct) 
   throws OptimizationException, SchemaMetadataException, 
   TypeMappingException 
   {
     int index = this.agenda.getStartTimes().indexOf(ct.getStartTime());
     Task t = this.agenda.getTask(this.agenda.getStartTimes().get(index + 1), ct.getSite());
     double taskDuration = 0.0;
     
     if(t == null)
       taskDuration = AgendaIOT.bmsToMs(ct.getDuration() + 
                                        CommunicationTask.getRadioOffOverhead(
                                            this.agenda.getCostParameters()))/1000.0;
     else
       taskDuration = AgendaIOT.bmsToMs(ct.getDuration())/1000.0;
     
     double voltage = AvroraCostParameters.VOLTAGE;
     
     double radioRXAmp = AvroraCostParameters.getRadioReceiveAmpere();
     if (ct.getMode()==CommunicationTask.RECEIVE) 
     {
       double taskEnergy = taskDuration*radioRXAmp*voltage; 
       return taskEnergy;
     }
     Site sender = ct.getSourceNode();
     Site receiver = ct.getDestNode();
     int txPower = (int)agenda.getIOT().getRT().getRadioLink(sender, receiver).getEnergyCost();
     double radioTXAmp = AvroraCostParameters.getTXAmpere(txPower);
     
     HashSet<InstanceExchangePart> exchComps = ct.getInstanceExchangeComponents();
     AvroraCostExpressions  costExpressions = 
       new AvroraCostExpressions(agenda.getIOT().getDAF(), agenda.getCostParameters(), agenda);
     AlphaBetaExpression txTimeExpr = null;
     if(exchComps == null)
     {
       txTimeExpr = AlphaBetaExpression.multiplyBy(new AlphaBetaExpression(ct.getMaxPacektsTransmitted()),  
                                                   AvroraCostParameters.PACKETTRANSMIT);
     }
     else
     {
       txTimeExpr = AlphaBetaExpression.multiplyBy(costExpressions.getPacketsSent(exchComps, true),
                                                   AvroraCostParameters.PACKETTRANSMIT);
     }
     double txTime = (txTimeExpr.evaluate(agenda.getAcquisitionInterval_bms(), 
                                          agenda.getBufferingFactor()))/1000.0;
     double rxTime = taskDuration-txTime;
     assert(rxTime>=0);
     
     double txEnergy = txTime*radioTXAmp*voltage; 
     double rxEnergy = rxTime*radioRXAmp*voltage; 
     return (txEnergy+rxEnergy); 
   }
   
   
   
   /**
    * Return the CPU energy cost for an agenda, in Joules.
    * @param cpuActiveTimeBms
   * @param siteTasks 
    * @return
    */
   private double getCPUEnergy(long cpuActiveTimeBms) 
   {
     double agendaLength = AgendaIOT.bmsToMs(agenda.getLength_bms(false))/1000.0; //bms to ms to s
     double cpuActiveTime = AgendaIOT.bmsToMs(cpuActiveTimeBms)/1000.0; //bms to ms to s
     double cpuSleepTime = agendaLength - cpuActiveTime; // s
     double voltage = AvroraCostParameters.VOLTAGE;
     double activeCurrent = AvroraCostParameters.CPUACTIVEAMPERE;
     double sleepCurrent = AvroraCostParameters.CPUPOWERSAVEAMPERE;
     //double sleepCurrent =  AvroraCostParameters.CPUIDLEAMPERE;
     
     double cpuActiveEnergy = cpuActiveTime * activeCurrent * voltage; //J
     double cpuSleepEnergy = cpuSleepTime * sleepCurrent * voltage; //J
     //return cpuActiveEnergy;
     return cpuActiveEnergy + cpuSleepEnergy;
   }

   /**
    * Evaluates the energy cost of a packets transmission between 2 nodes (includes radio on cost)
    * @param task
    * @param packets
    * @return
    * @throws OptimizationException
    * @throws SchemaMetadataException
    * @throws TypeMappingException
    */
   public double evaluateCommunicationTask(CommunicationTask task, double packets)
   throws OptimizationException, SchemaMetadataException, 
   TypeMappingException 
   {
     double sumEnergy = 0;
     long cpuActiveTimeBms = task.getDuration();
     double RadioEnergy = getRadioEnergy(task, packets);
     sumEnergy += RadioEnergy; 
     sumEnergy += getCPUEnergyNoAgenda(cpuActiveTimeBms);
     double taskDuration = AgendaIOT.bmsToMs(task.getDuration())/1000.0;
     double radioRXAmp = AvroraCostParameters.getRadioReceiveAmpere(); 
     double voltage = AvroraCostParameters.VOLTAGE;
     double taskEnergy = taskDuration * radioRXAmp * voltage;     
     taskEnergy = taskEnergy /1000; // mJ -> J
     sumEnergy += taskEnergy;
     return sumEnergy;
   }
   
   /**
    * Return the CPU energy cost for an task, in Joules.
    * @param cpuActiveTimeBms
    * @return
    */
   private double getCPUEnergyNoAgenda(long cpuActiveTimeBms)
   {
     double cpuActiveTime = AgendaIOT.bmsToMs(cpuActiveTimeBms)/1000.0; //bms to ms to s
     double voltage = AvroraCostParameters.VOLTAGE;
     double activeCurrent = AvroraCostParameters.CPUACTIVEAMPERE;
     double cpuActiveEnergy = cpuActiveTime * activeCurrent * voltage; //J
     return cpuActiveEnergy;
   }

   /**
    * Returns radio energy for communication tasks (J) for agenda according to model.
    * Excludes radio switch on.
    * @param ct
    * @return
    * @throws TypeMappingException 
    * @throws SchemaMetadataException 
    * @throws OptimizationException 
    */
  public double getRadioEnergy(CommunicationTask ct, double packets) 
  throws OptimizationException, SchemaMetadataException, 
  TypeMappingException 
  {
     double taskDuration = AgendaIOT.bmsToMs(ct.getDuration())/1000.0;
     double voltage = AvroraCostParameters.VOLTAGE;
     
     double radioRXAmp = AvroraCostParameters.getRadioReceiveAmpere();
     if (ct.getMode()==CommunicationTask.RECEIVE) 
     {
       double taskEnergy = taskDuration*radioRXAmp*voltage; 
       return taskEnergy;
     }
     Site sender = ct.getSourceNode();
     Site receiver = (Site)sender.getOutput(0);
     int txPower = (int)agenda.getIOT().getRT().getRadioLink(sender, receiver).getEnergyCost();
     double radioTXAmp = AvroraCostParameters.getTXAmpere(txPower);
     
     AvroraCostExpressions  costExpressions = 
       new AvroraCostExpressions(agenda.getIOT().getDAF(), agenda.getCostParameters(), agenda);
     AlphaBetaExpression txTimeExpr = 
         AlphaBetaExpression.multiplyBy( costExpressions.getPacketsSent(packets, true),
                                         AvroraCostParameters.PACKETTRANSMIT);
     double txTime = (txTimeExpr.evaluate())/1000.0;
     double rxTime = taskDuration-txTime;
     assert(rxTime>=0);
     
     double txEnergy = txTime*radioTXAmp*voltage; 
     double rxEnergy = rxTime*radioRXAmp*voltage; 
     return (txEnergy+rxEnergy); 
  }
}