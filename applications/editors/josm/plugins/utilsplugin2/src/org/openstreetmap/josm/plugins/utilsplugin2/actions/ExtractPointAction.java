// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Extracts node from its ways.
 */
public class ExtractPointAction extends JosmAction {

    /**
     * Constructs a new {@code ExtractPointAction}.
     */
    public ExtractPointAction() {
        super(tr("Extract node"), "extnode",
                tr("Extracts node from a way"),
                Shortcut.registerShortcut("tools:extnode", tr("More tools: {0}", "Extract node"),
                        KeyEvent.VK_J, Shortcut.ALT_SHIFT), true);
        putValue("help", ht("/Action/ExtractNode"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getEditDataSet();
        Collection<Node> selectedNodes = ds.getSelectedNodes();
        if (selectedNodes.size() != 1) {
            new Notification(tr("This tool extracts node from its ways and requires single node to be selected."))
            .setIcon(JOptionPane.WARNING_MESSAGE).show();
            return;
        }
        final boolean ok = checkAndConfirmOutlyingOperation("extnode",
                tr("Extract node confirmation"),
                tr("You are about to extract a node which can have other referrers not yet downloaded."
                        + "<br>"
                        + "This can cause problems because other objects (that you do not see) might use them."
                        + "<br>"
                        + "Do you really want to extract?"),
                "", // incomplete node should not happen
                selectedNodes, null);
        if (!ok) {
            return;
        }

        Node nd = selectedNodes.iterator().next();
        Node ndCopy = new Node(nd.getCoor());
        List<Command> cmds = new LinkedList<>();

        Point p = MainApplication.getMap().mapView.getMousePosition();
        if (p != null)
            cmds.add(new MoveCommand(nd, MainApplication.getMap().mapView.getLatLon(p.x, p.y)));
        List<OsmPrimitive> refs = nd.getReferrers();
        cmds.add(new AddCommand(ds, ndCopy));

        for (OsmPrimitive pr: refs) {
            if (pr instanceof Way) {
                Way w = (Way) pr;
                List<Node> nodes = w.getNodes();
                int idx = nodes.indexOf(nd);
                nodes.set(idx, ndCopy); // replace node with its copy
                cmds.add(new ChangeNodesCommand(w, nodes));
            }
        }
        if (cmds.size() > 1) UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Extract node from line"), cmds));
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
        setEnabled(selection.size() == 1);
    }
}
