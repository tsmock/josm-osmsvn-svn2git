// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Add missing nodes at intersections of selected ways.
 */
public class AddIntersectionsAction extends JosmAction {

    /**
     * Constructs a new {@code AddIntersectionsAction}.
     */
    public AddIntersectionsAction() {
        super(tr("Add nodes at intersections"), "addintersect", tr("Add missing nodes at intersections of selected ways."),
                Shortcut.registerShortcut("tools:addintersect", tr("More tools: {0}", tr("Add nodes at intersections")),
                        KeyEvent.VK_I, Shortcut.SHIFT),
                true);
        putValue("help", ht("/Action/AddIntersections"));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (!isEnabled())
            return;
        List<Way> ways = new ArrayList<>(getLayerManager().getEditDataSet().getSelectedWays());
        if (ways.isEmpty()) {
            new Notification(
                    tr("Please select one or more ways with intersections of segments."))
            .setIcon(JOptionPane.INFORMATION_MESSAGE)
            .show();
            return;
        }

        LinkedList<Command> cmds = new LinkedList<>();
        Geometry.addIntersections(ways, false, cmds);
        if (!cmds.isEmpty()) {
            UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Add nodes at intersections"), cmds));
            Set<Node> nodes = new HashSet<>(10);
            // find and select newly added nodes
            for (Command cmd: cmds) if (cmd instanceof AddCommand) {
                Collection<? extends OsmPrimitive> pp = cmd.getParticipatingPrimitives();
                for (OsmPrimitive p : pp) { // find all affected nodes
                    if (p instanceof Node && p.isNew())
                        nodes.add((Node) p);
                }
                if (!nodes.isEmpty()) {
                    getLayerManager().getEditDataSet().setSelected(nodes);
                }
            }
        }
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && !selection.isEmpty());
    }
}
