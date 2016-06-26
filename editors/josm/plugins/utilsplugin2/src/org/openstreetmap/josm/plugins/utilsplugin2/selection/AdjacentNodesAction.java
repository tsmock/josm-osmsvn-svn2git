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
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Extends current selection
 */
public class AdjacentNodesAction extends JosmAction {

    public static final boolean treeMode = false;

    public AdjacentNodesAction() {
        super(tr("Adjacent nodes"), "adjnodes", tr("Select adjacent nodes"),
                Shortcut.registerShortcut("tools:adjnodes", tr("Tool: {0}", "Adjacent nodes"),
                        KeyEvent.VK_E, Shortcut.DIRECT), true);
        putValue("help", ht("/Action/AdjacentNodes"));
    }

    private Set<Way> activeWays = new HashSet<>();

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getEditDataSet();
        Collection<OsmPrimitive> selection = ds.getSelected();
        Set<Node> selectedNodes = OsmPrimitive.getFilteredSet(selection, Node.class);

        Set<Way> selectedWays = OsmPrimitive.getFilteredSet(ds.getSelected(), Way.class);

        // if no nodes and no ways are selected, do nothing
        if (selectedNodes.isEmpty() && selectedWays.isEmpty()) return;

        if (selectedWays.isEmpty()) {
            // if one node is selected, used ways connected to it to extend selecteons
            // activeWays are remembered for next extend action (!!!)

            // FIXME: some strange behaviour is possible if user delete some of these way
            // how to clear activeWays during such user actions? Do not know
            if (selectedNodes.size() == 1) {
                activeWays.clear();
                //                System.out.println("Cleared active ways");
            }
        } else {
            // use only ways that were selected for adding nodes
            activeWays = selectedWays;
        }

        // selecting nodes of selected ways
        if (selectedNodes.isEmpty()) {
            HashSet<Node> newNodes = new HashSet<>();
            NodeWayUtils.addNodesConnectedToWays(selectedWays, newNodes);
            activeWays.clear();
            ds.setSelected(newNodes);
            return;
        }

        if (activeWays.isEmpty()) {
            NodeWayUtils.addWaysConnectedToNodes(selectedNodes, activeWays);
        }

        Set<Node> newNodes = new HashSet<>();
        for (Node node: selectedNodes) {
            for (Way w: activeWays) {
                NodeWayUtils.addNeighbours(w, node, newNodes);
            }
        }

        // select only newly found nodes
        newNodes.removeAll(selectedNodes);

        // enable branching on next call of this function
        // if no new nodes were found, next search will include all touched ways
        if (newNodes.isEmpty()) {
            activeWays.clear();
        }

        ds.addSelected(newNodes);
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (selection == null) {
            setEnabled(false);
            return;
        }
        setEnabled(!selection.isEmpty());
    }
}
