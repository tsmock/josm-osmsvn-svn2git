// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.command.SplitWayCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Splits selected ways on T-intersections. Target points should belong only to one way (except selected one),
 * or two selected ways.
 *
 * @author Zverik
 */
public class SplitOnIntersectionsAction extends JosmAction {
    private static final String TITLE = tr("Split adjacent ways");
    private static final String TOOL_DESC = tr("Split adjacent ways on T-intersections");
    public SplitOnIntersectionsAction() {
        super(TITLE, "dumbutils/splitonintersections", TOOL_DESC,
            Shortcut.registerShortcut("tools:splitonintersections", tr("More tools: {0}", tr("Split adjacent ways")),
            KeyEvent.VK_P, Shortcut.ALT_CTRL_SHIFT),
            true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<Command> list = new ArrayList<>();
        List<Way> selectedWays = new ArrayList<>(getLayerManager().getEditDataSet().getSelectedWays());
        Map<Way, List<Node>> splitWays = new HashMap<>();

        for (Way way : selectedWays) {
            if (way.getNodesCount() > 1 && !way.hasIncompleteNodes() && !way.isClosed())
                for (Node node : new Node[] {way.getNode(0), way.getNode(way.getNodesCount() - 1)}) {
                    List<Way> refs = node.getParentWays();
                    refs.remove(way);
                    if (selectedWays.size() > 1) {
                        // When several ways are selected, split only those among selected
                        Iterator<Way> it = refs.iterator();
                        while (it.hasNext()) {
                            if (!selectedWays.contains(it.next()))
                                it.remove();
                        }
                    }

                    Iterator<Way> it = refs.iterator();
                    while (it.hasNext()) {
                        Way w = it.next();
                        if (w.isDeleted() || w.isIncomplete() || !w.isInnerNode(node))
                            it.remove();
                    }
                    if (refs.size() == 1) {
                        if (splitWays.containsKey(refs.get(0)))
                            splitWays.get(refs.get(0)).add(node);
                        else {
                            List<Node> nodes = new ArrayList<>(1);
                            nodes.add(node);
                            splitWays.put(refs.get(0), nodes);
                        }
                    } else if (refs.size() > 1) {
                        new Notification(
                                tr("There are several ways containing one of the splitting nodes. Select ways participating in this operation.")
                                ).setIcon(JOptionPane.WARNING_MESSAGE).show();
                        return;
                    }
                }
        }

        if (splitWays.isEmpty()) {
            new Notification(tr("The selection cannot be used for action ''{0}''", TOOL_DESC))
                    .setIcon(JOptionPane.WARNING_MESSAGE).show();
            return;
        }

        for (Map.Entry<Way, List<Node>> entry : splitWays.entrySet()) {
            SplitWayCommand split = SplitWayCommand.split(entry.getKey(), entry.getValue(), selectedWays);
            if (split != null) {
                // execute, we need the result, see also #16006
                UndoRedoHandler.getInstance().add(split);
                selectedWays.remove(split.getOriginalWay());
                selectedWays.addAll(split.getNewWays());
                list.add(split);
            }
        }

        if (!list.isEmpty()) {
            if (list.size() > 1) {
                // create a single command for the previously executed commands
                SequenceCommand seq = new SequenceCommand(TITLE, list);
                for (int i = 0; i < list.size(); i++) {
                    UndoRedoHandler.getInstance().undo();
                }
                UndoRedoHandler.getInstance().add(seq);
            }
            getLayerManager().getEditDataSet().clearSelection();
        }
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        boolean ok = false;
        if (selection != null)
            for (OsmPrimitive p : selection) {
                if (p instanceof Way)
                    ok = true;
                else {
                    ok = false;
                    break;
                }
            }
        setEnabled(ok);
    }
}
