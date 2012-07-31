package uk.ac.manchester.cs.snee.manager.failednodestrategies.localrepairstrategy;

import java.io.File;

import uk.ac.manchester.cs.snee.common.SNEEConfigurationException;
import uk.ac.manchester.cs.snee.compiler.iot.AgendaIOT;
import uk.ac.manchester.cs.snee.compiler.iot.AgendaIOTUtils;
import uk.ac.manchester.cs.snee.compiler.iot.IOT;
import uk.ac.manchester.cs.snee.compiler.queryplan.RT;
import uk.ac.manchester.cs.snee.compiler.queryplan.RTUtils;
import uk.ac.manchester.cs.snee.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.snee.metadata.source.sensornet.TopologyUtils;

/**
 *@author stokesa6
 * class used to add utility functions for failed node framework
 **/
public class LocalRepairStrategyUtils 
{

  private LocalRepairStrategy frameworkPartial;
  private String sep = System.getProperty("file.separator");
  
  public LocalRepairStrategyUtils(LocalRepairStrategy ad)
  {
    this.frameworkPartial = ad;
  }
  
  /**
   * method used to output topology as a dot file.
   * @param string location and name of topology file.
   */
  public void outputTopologyAsDotFile(File outputFolder , String string)
  {
    try
    {
      
      File topFolder = new File(outputFolder.toString() + sep + "Topology");
      topFolder.mkdir();
      new TopologyUtils(frameworkPartial.getWsnTopology()).exportAsDOTFile(topFolder.toString() + string, true);
    }
    catch (SchemaMetadataException e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * method used to output a route as a dot file inside an adaption
   */
  public void outputRouteAsDotFile(File outputFolder , String string, RT route)
  {
    try
    {
      
      File topFolder = new File(outputFolder.toString() + sep + "Route");
      topFolder.mkdir();
      new RTUtils(route).exportAsDOTFile(topFolder.toString() + sep + string);
    }
    catch (SchemaMetadataException e)
    {
      e.printStackTrace();
    }
  }

  public void outputAgendas(AgendaIOT newAgenda, AgendaIOT agenda, IOT oldIOT, IOT newIOT, File outputFolder) throws SNEEConfigurationException
  {
    AgendaIOTUtils oldOutput = new AgendaIOTUtils(agenda, oldIOT, false);
    AgendaIOTUtils output = new AgendaIOTUtils(newAgenda, newIOT, false);
    AgendaIOTUtils oldOutputMS = new AgendaIOTUtils(agenda, oldIOT, true);
    AgendaIOTUtils outputMS = new AgendaIOTUtils(newAgenda, newIOT, true);
    File agendaFolder = new File(outputFolder.toString() + sep + "Agendas");
    agendaFolder.mkdir();
    //bms
    output.generateImage(agendaFolder.toString());
    output.exportAsLatex(agendaFolder.toString() + sep + "newAgendaLatexBMS.tex");
    oldOutput.generateImage(agendaFolder.toString());
    oldOutput.exportAsLatex(agendaFolder.toString() + sep + "oldAgendaLatexBMS.tex");
    //ms
    outputMS.generateImage(agendaFolder.toString());
    outputMS.exportAsLatex(agendaFolder.toString() + sep + "newAgendaLatexMS.tex");
    oldOutputMS.generateImage(agendaFolder.toString());
    oldOutputMS.exportAsLatex(agendaFolder.toString() + sep + "oldAgendaLatexMS.tex");
    
  }
  
}