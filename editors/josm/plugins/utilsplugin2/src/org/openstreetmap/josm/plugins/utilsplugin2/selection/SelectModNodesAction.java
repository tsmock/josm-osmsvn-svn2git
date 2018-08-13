// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Unselects all nodes
 */
public class SelectModNodesAction extends JosmAction {
    private int lastHash;
    private Command lastCmd;

    public SelectModNodesAction() {
        super(tr("Select last modified nodes"), "selmodnodes",
                tr("Select last modified nodes"),
                Shortcut.registerShortcut("tools:selmodnodes", tr("Tool: {0}", "Select last modified nodes"),
                        KeyEvent.VK_Z, Shortcut.SHIFT), true);
        putValue("help", ht("/Action/SelectLastModifiedNodes"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getEditDataSet();
        if (ds != null) {
            Collection<OsmPrimitive> selection = ds.getSelected();
            ds.clearSelection(OsmPrimitive.getFilteredSet(selection, Node.class));
            Command cmd = null;

            if (UndoRedoHandler.getInstance().commands == null) return;
            int num = UndoRedoHandler.getInstance().commands.size();
            if (num == 0) return;
            int k = 0, idx;
            if (selection != null && !selection.isEmpty() && selection.hashCode() == lastHash) {
                // we are selecting next command in history if nothing is selected
                idx = UndoRedoHandler.getInstance().commands.indexOf(lastCmd);
            } else {
                idx = num;
            }

            Set<Node> nodes = new HashSet<>(10);
            do {  //  select next history element
                if (idx > 0) idx--; else idx = num-1;
                cmd = UndoRedoHandler.getInstance().commands.get(idx);
                Collection<? extends OsmPrimitive> pp = cmd.getParticipatingPrimitives();
                nodes.clear();
                for (OsmPrimitive p : pp) {  // find all affected ways
                    if (p instanceof Node && !p.isDeleted()) nodes.add((Node) p);
                }
                if (!nodes.isEmpty()) {
                    ds.setSelected(nodes);
                    lastCmd = cmd; // remember last used command and last selection
                    lastHash = ds.getSelected().hashCode();
                    return;
                }
                k++;
            } while (k < num); // try to find previous command if this affects nothing
            lastCmd = null; lastHash = 0;
        }
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerManager().getEditDataSet() != null);
    }
}
