package public_transport;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class TrackStoplistSortCommand extends Command
{
  private TrackStoplistTableModel stoplistTM = null;
  private Vector< Vector< Object > > tableDataModel = null;
  private Vector< Node > nodes = null;
  private Vector< Integer > workingLines = null;
  private int insPos;
  private String stopwatchStart;
  
  public TrackStoplistSortCommand(StopImporterAction controller)
  {
    stoplistTM = controller.getCurrentTrack().stoplistTM;
    workingLines = new Vector< Integer >();
    insPos = controller.getDialog().getStoplistTable().getSelectedRow();
    stopwatchStart = controller.getCurrentTrack().stopwatchStart;
    
    // use either selected lines or all lines if no line is selected
    int[] selectedLines = controller.getDialog().getStoplistTable().getSelectedRows();
    if (selectedLines.length > 0)
    {
      for (int i = 0; i < selectedLines.length; ++i)
    workingLines.add(selectedLines[i]);
    }
    else
    {
      for (int i = 0; i < stoplistTM.getRowCount(); ++i)
    workingLines.add(new Integer(i));
    }
  }
  
  @SuppressWarnings("unchecked")
  public boolean executeCommand()
  {
    tableDataModel = (Vector< Vector< Object > >)stoplistTM.getDataVector()
    .clone();
    nodes = (Vector< Node >)stoplistTM.nodes.clone();
    
    Vector< NodeSortEntry > nodesToSort = new Vector< NodeSortEntry >();
    for (int i = workingLines.size()-1; i >= 0; --i)
    {
      int j = workingLines.elementAt(i).intValue();
      nodesToSort.add(new NodeSortEntry
      (stoplistTM.nodes.elementAt(j), (String)stoplistTM.getValueAt(j, 0),
        (String)stoplistTM.getValueAt(j, 1),
         StopImporterDialog.parseTime(stopwatchStart)));
      stoplistTM.nodes.removeElementAt(j);
      stoplistTM.removeRow(j);
    }
    
    Collections.sort(nodesToSort);
    
    int insPos = this.insPos;
    Iterator< NodeSortEntry > iter = nodesToSort.iterator();
    while (iter.hasNext())
    {
      NodeSortEntry nse = iter.next();
      stoplistTM.insertRow(insPos, nse.node, nse.time, nse.name);
      if (insPos >= 0)
    ++insPos;
    }
    return true;
  }
  
  public void undoCommand()
  {
    stoplistTM.setDataVector(tableDataModel);
    stoplistTM.nodes = nodes;
  }
  
  public void fillModifiedData
    (Collection< OsmPrimitive > modified, Collection< OsmPrimitive > deleted,
     Collection< OsmPrimitive > added)
  {
  }
  
  public MutableTreeNode description()
  {
    return new DefaultMutableTreeNode("public_transport.TrackStoplist.Sort");
  }
  
  private class NodeSortEntry implements Comparable< NodeSortEntry >
  {
    public Node node = null;
    public String time = null;
    public String name = null;
    public double startTime = 0;
    
    public NodeSortEntry(Node node, String time, String name, double startTime)
    {
      this.node = node;
      this.time = time;
      this.name = name;
    }
    
    public int compareTo(NodeSortEntry nse)
    {
      double time = StopImporterDialog.parseTime(this.time);
      if (time - startTime > 12*60*60)
    time -= 24*60*60;
      
      double nseTime = StopImporterDialog.parseTime(nse.time);
      if (nseTime - startTime > 12*60*60)
    nseTime -= 24*60*60;
      
      if (time < nseTime)
    return -1;
      else if (time > nseTime)
    return 1;
      else
    return 0;
    }
  };
};
