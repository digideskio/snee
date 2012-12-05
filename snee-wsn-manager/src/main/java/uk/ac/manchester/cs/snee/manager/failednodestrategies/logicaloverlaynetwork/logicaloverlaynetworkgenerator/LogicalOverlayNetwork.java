package uk.ac.manchester.cs.snee.manager.failednodestrategies.logicaloverlaynetwork.logicaloverlaynetworkgenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.rits.cloning.Cloner;

import uk.ac.manchester.cs.snee.common.graph.Node;
import uk.ac.manchester.cs.snee.compiler.costmodels.HashMapList;
import uk.ac.manchester.cs.snee.compiler.queryplan.SensorNetworkQueryPlan;
import uk.ac.manchester.cs.snee.manager.common.RunTimeSite;
import uk.ac.manchester.cs.snee.metadata.source.sensornet.Site;
import uk.ac.manchester.cs.snee.metadata.source.sensornet.Topology;

/**
 * class used to represent a set of nodes used within the local strategy for node failure
 * @author alan
 *
 */
public class LogicalOverlayNetwork implements Serializable
{
  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = -2290495984293624164L;
  
  
  // cluster rep
  protected HashMapList<String, String> clusters = null; 
  private SensorNetworkQueryPlan qep = null;
  private HashMap<String, RunTimeSite> finalRunningSites;
  private String id = "";
  private static Integer idCounter = 1;
  private TreeMap<String, String> logicalNodeIds = new TreeMap<String, String>();
  private String currentNextID ="A";
  private String stringFormat = "";
  /**
   * constructor
   */
  public LogicalOverlayNetwork()
  {
    clusters = new HashMapList<String, String>();
    id = "Overlay" + idCounter.toString();
    idCounter++;
  }
  
  public LogicalOverlayNetwork(HashMapList<String, String> newHashMapList)
  {
    clusters = new HashMapList<String, String>();
    Iterator<String> keys = newHashMapList.keySet().iterator();
    while(keys.hasNext())
    {
      String key = keys.next();
      Iterator<String> eqNodes = newHashMapList.get(key).iterator();
      while(eqNodes.hasNext())
      {
        String eqNode = eqNodes.next();
        this.addClusterNode(key, eqNode);
      }
    }
    generateStringFormat();
    id = "Overlay" + idCounter.toString();
    idCounter++;
  }
  
  public void addClusterNode(String primary, ArrayList<String> equivilentNodes)
  {
    this.clusters.addAll(primary, equivilentNodes);
    addNewID(primary);
    generateStringFormat();
  }
  
  private void addNewID(String primary)
  {
    logicalNodeIds.put(primary, currentNextID);
    String lastCharachter = currentNextID.substring(currentNextID.length() -1);
    int asciiValue = lastCharachter.charAt(0);
    if(asciiValue == 90)
    {
      currentNextID = currentNextID.concat("A");
    }
    else
    {
      currentNextID = currentNextID.substring(0, currentNextID.length() -1);
      currentNextID = currentNextID.concat(Character.toString ((char) (asciiValue + 1)));
    }
  }

  /**
   * searches though the rest of the clusters, looking for the equivilentNode.
   * If none exist, tyhen add, else choose which cluster owns the node
   * @param primary
   * @param equivilentNode
   */
  public void addClusterNode(String primary, String equivilentNode)
  {
    this.clusters.add(primary, equivilentNode);
    generateStringFormat();
  }

  public Set<String> getKeySet()
  {
    return this.clusters.keySet();
  }

  public ArrayList<String> getEquivilentNodes(String key)
  {
    ArrayList<String> cluster = clusters.get(key);
    return cluster;
  }
  
  /**
   * removes the node from all clusters, as once removed, 
   * relation wont be correct for any cluster which contains the node
   * @param removal
   */
  public void removeNode(String removal)
  {
    Set<String> nodeKeys = clusters.keySet();
    Iterator<String> keyIterator = nodeKeys.iterator();
    while(keyIterator.hasNext())
    {
      String key = keyIterator.next();
      clusters.remove(key, removal);
    }
    generateStringFormat();
  }
  
  @Override
  public String toString()
  {
    Iterator<String> keys = this.getKeySet().iterator();
    String output = "";
    while(keys.hasNext())
    {
      String key = keys.next();
      output = output.concat(key + " : " + clusters.get(key).toString() + ":" );
    }
    return output;
  }

  /**
   * checks if any cluster contains the candidate
   * @param candidate
   * @return
   */
  public boolean contains(String candidate)
  {
    Iterator<String> keys = this.getKeySet().iterator();
    boolean found = false;
    while(keys.hasNext() && !found)
    {
      String key = keys.next();
      ArrayList<String> candiates = this.getEquivilentNodes(key);
      if(candiates.contains(candidate))
        found = true;
    }
    return found;
  }

  /**
   * finds the cluster head to which this candidate is associated with
   * @param candidate
   * @return
   */
  public String getClusterHeadFor(String candidate)
  {
    if(this.isClusterHead(candidate))
      return candidate; 
    
    Iterator<String> keys = this.getKeySet().iterator();
    while(keys.hasNext())
    {
      String key = keys.next();
      ArrayList<String> candiates = this.getEquivilentNodes(key);
      if(candiates.contains(candidate))
        return key;
    }
    
    return candidate;
  }

  /**
   * does a simple check to see which candidate has the least energy cost overall in relation to the 
   * parent and children cluster heads.
   * @param key
   * @param children
   * @param parent
   * @param network
   * @return
   */
  public String getReplacement(String key, List<Node> children, Node parent, Topology network)
  {
    Iterator<String> candiates = this.getEquivilentNodes(key).iterator();
    String bestCandiate = null;
    double bestCost = Double.MAX_VALUE;
    while(candiates.hasNext())
    {
      double cost = 0;
      String candiate = candiates.next();
      Site candiateSite = network.getSite(candiate);
      Iterator<Node> childrenIterator = children.iterator();
      while(childrenIterator.hasNext())
      {
        Site child = (Site) childrenIterator.next();
        cost += network.getLinkEnergyCost(child, candiateSite);
      }
      if(network.hasLink(candiateSite, (Site) parent))
      {
        cost += network.getLinkEnergyCost(candiateSite, (Site) parent);
        if(cost < bestCost)
        {
          bestCandiate = candiate;
          bestCost = cost;
        }
      }
    }
    return bestCandiate;
  }

  public void setQep(SensorNetworkQueryPlan qep)
  {
    this.qep = qep;
  }

  public SensorNetworkQueryPlan getQep()
  {
    return qep;
  }
  
  /**
   * updates a cluster by removing head and changing the candidate to the head
   * @param head
   * @param newHead
   */
  public void updateCluster(String head, String newHead)
  {
    ArrayList<String> candidates = clusters.get(head);
    candidates.remove(newHead);
    clusters.remove(head);
    clusters.set(newHead, candidates);
    generateStringFormat();
  }

  private void generateStringFormat()
  {
    ArrayList<String> keys = new ArrayList<String>(this.clusters.keySet());
    Collections.sort(keys);
    Iterator<String> keyIterator = keys.iterator();
    while(keyIterator.hasNext())
    {
      String key = keyIterator.next();
      this.stringFormat = this.stringFormat.concat(key + "[");
      ArrayList<String> cluster = this.clusters.get(key);
      Collections.sort(cluster);
      Iterator<String> clusterIt = cluster.iterator();
      while(clusterIt.hasNext())
      {
        String node = clusterIt.next();
        this.stringFormat = this.stringFormat.concat(node + ",");
      }
      this.stringFormat = this.stringFormat.concat("]");
    }
  }

  /**
   * removes the QEP associated with this logical overlay network
   */
  public void removeQEP()
  {
    this.qep = null;
  }

  /**
   * return a string id for the logical overlay network
   * @return
   */
  public String getId()
  {
    return id;
  }

  /**
   * cloner method for the logical overlay network
   * @return
   */
  public LogicalOverlayNetwork generateClone()
  {
    return new LogicalOverlayNetwork(this.clusters);
  }
  
  /**
   * Comparison helper method.
   */
  public boolean equals(LogicalOverlayNetwork other)
  {
    return this.stringFormat.equals(other.getStringFormat());
  }
  
  public String getStringFormat()
  {
    return this.stringFormat;
  }
  
  /**
   * iterator of all sites within the logical overlay network, including cluster heads.
   * no particular order of cluster heads, but clusters are output together
   * @return
   */
  public Iterator<String> siteIdIterator()
  {
    ArrayList<String> array = new ArrayList<String>();
    Iterator<String> keyIterator = this.clusters.keySet().iterator();
    while(keyIterator.hasNext())
    {
      String key = keyIterator.next();
      array.add(key);
      Iterator<String> candidates = this.clusters.get(key).iterator();
      while(candidates.hasNext())
      {
        String cand = candidates.next();
        array.add(cand);
      }
    }
    //add root
    array.add(this.qep.getRT().getRoot().getID());
    return array.iterator();
  }
  
  public void setFinalEnergyModel(HashMap<String, RunTimeSite> runningSites)
  {
    setFinalRunningSites(runningSites);
  }

  public void setFinalRunningSites(HashMap<String, RunTimeSite> finalRunningSites)
  {
    this.finalRunningSites = finalRunningSites;
  }

  public HashMap<String, RunTimeSite> getFinalRunningSites()
  {
    return finalRunningSites;
  }

  public HashMap<String, RunTimeSite> getCopyOfFinalRunningSites()
  {
    Cloner cloner = new Cloner();
    cloner.dontClone(Logger.class);
    return cloner.deepClone(finalRunningSites);
  }

  public boolean isClusterHead(String sourceID)
  {
    return this.clusters.keySet().contains(sourceID);
  }
  
  public HashMapList<String, String> getClusters()
  {
    return this.clusters;
  }
}
