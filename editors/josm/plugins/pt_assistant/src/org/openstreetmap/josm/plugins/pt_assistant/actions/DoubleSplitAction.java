// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JoinNodeWayAction;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.command.SplitWayCommand;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.ILatLon;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.AbstractMapViewPaintable;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Pair;
import org.openstreetmap.josm.tools.RightAndLefthandTraffic;

/**
 * The DoubleSplitAction is a mapmode that allows users to add a bus_bay,a
 * bridge or a tunnel .
 *
 * @author Biswesh
 */
public class DoubleSplitAction extends MapMode implements KeyListener {

    private static final String MAP_MODE_NAME = "Double Split";

    private transient Set<OsmPrimitive> newHighlights = new HashSet<>();
    private transient Set<OsmPrimitive> oldHighlights = new HashSet<>();
    private List<Node> atNodes = new ArrayList<>();
    private final DoubleSplitLayer temporaryLayer = new DoubleSplitLayer();
    ILatLon Pos1 = null;
    ILatLon Pos2 = null;
    Way SegWay1 = null;
    Way SegWay2 = null;
    Way affected;
    Way previousAffectedWay;

    private final Cursor cursorJoinNode;
    private final Cursor cursorJoinWay;

    /**
     * Creates a new DoubleSplitAction
     */
    public DoubleSplitAction() {
        super(tr(MAP_MODE_NAME), "logo_double_split", tr(MAP_MODE_NAME), null, getCursor());
        cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
        cursorJoinWay = ImageProvider.getCursor("crosshair", "joinway");

    }

    private static Cursor getCursor() {
        Cursor cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        return cursor;
    }

    @Override
    public void enterMode() {
        super.enterMode();
        MainApplication.getMap().mapView.addMouseListener(this);
        MainApplication.getMap().mapView.addMouseMotionListener(this);
        MainApplication.getMap().mapView.addKeyListener(this);
        MainApplication.getMap().mapView.addTemporaryLayer(temporaryLayer);
    }

    @Override
    public void exitMode() {
        reset();
        super.exitMode();
        MainApplication.getMap().mapView.removeMouseListener(this);
        MainApplication.getMap().mapView.removeMouseMotionListener(this);
        MainApplication.getMap().mapView.removeKeyListener(this);
        MainApplication.getMap().mapView.removeTemporaryLayer(temporaryLayer);
        resetLayer();
    }

    private void reset() {
        try {
            atNodes.clear();
            updateHighlights();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("reset");
    }

    private void resetLayer() {
        Pos1 = null;
        Pos2 = null;
        SegWay1 = null;
        SegWay2 = null;
        temporaryLayer.invalidate();
    }

    // check if both the nodes are starting and ending nodes of same way
    private boolean startEndPoints(List<Command> commandList) {

        try {
            for (Way way : atNodes.get(0).getParentWays()) {
                if (atNodes.get(1).getParentWays().contains(way)) {
                    if (way.isFirstLastNode(atNodes.get(0)) && way.isFirstLastNode(atNodes.get(1))) {
                        List<TagMap> affectedKeysList = new ArrayList<>();
                        affectedKeysList.add(way.getKeys());
                        newHighlights.add(way);
                        dialogBox(3, null, way, way, commandList);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean firstNodeIsConnectionNode(Node node, Way affected, Way previousAffectedWay) {
        if (node.isConnectionNode()) {
            if (node.getParentWays().contains(affected))
                previousAffectedWay = affected;
            else {
                return true;
            }
        }
        return false;
    }

    private boolean secondNodeIsConnectionNode(Node node, Way previousAffectedWay) {
        if (atNodes.get(1).isConnectionNode()) {
            if (atNodes.get(1).getParentWays().contains(previousAffectedWay))
                return false;
            else {
                return true;
            }
        }
        return false;
    }

    private Node checkCommonNode(Way affected, Way previousAffectedWay) {

        // check if they have any common node
        List<Node> presentNodeList = affected.getNodes();
        for (Node previousNode : previousAffectedWay.getNodes()) {
            if (presentNodeList.contains(previousNode)) {
                return previousNode;
            }
        }
        return null;
    }

    private void removeFirstNode() {

        atNodes.get(0).setDeleted(true);
        atNodes.get(1).setDeleted(true);
        Pos1 = Pos2;
        Pos2 = null;
        SegWay1 = SegWay2;
        SegWay2 = null;
        reset();
    }

    private void action() {

        List<Command> commandList = new ArrayList<>();

        // check if the user has selected an existing node, or a new one
        Point curP1 = MainApplication.getMap().mapView.getPoint(Pos1);
        ILatLon P1 = new LatLon(curP1.getX(), curP1.getY());
        Node node1 = createNode(P1, commandList);
        if (node1 == null) {
            resetLayer();
            reset();
            return;
        }

        Point curP2 = MainApplication.getMap().mapView.getPoint(Pos2);
        ILatLon P2 = new LatLon(curP2.getX(), curP2.getY());
        Node node2 = createNode(P2, commandList);
        if (node2 == null) {
            node1.setDeleted(true);
            Pos2 = null;
            SegWay2 = null;
            reset();
            return;
        }

        if (node1.equals(node2)) {
            resetLayer();
            System.out.println("same");
            return;
        }

        atNodes.add(node1);
        atNodes.add(node2);

        previousAffectedWay = SegWay1;
        affected = SegWay2;

        if (affected == null || previousAffectedWay == null) {
            node1.setDeleted(true);
            node2.setDeleted(true);
            resetLayer();
            return;
        }

        // if both the nodes are starting and ending points of the same way
        // we don't split the way, just add new key-value to the way
        boolean areStartEndPoints = startEndPoints(commandList);
        if (areStartEndPoints) {
            resetLayer();
            return;
        }

        // if first node is a connection node
        boolean isConnectionNode = firstNodeIsConnectionNode(atNodes.get(0), affected, previousAffectedWay);
        if (isConnectionNode) {
            resetLayer();
            return;
        }

        // if second node is a connection node
        isConnectionNode = secondNodeIsConnectionNode(atNodes.get(1), previousAffectedWay);
        if (isConnectionNode) {
            resetLayer();
            return;
        } else {
            if (atNodes.get(1).isConnectionNode()) {
                affected = previousAffectedWay;
            }
        }

        // if both the nodes are not on same way and don't have any common node then
        // make second node as first node
        Node commonNode = null;
        boolean twoWaysWithCommonNode = false;
        if (previousAffectedWay != affected) {
            commonNode = checkCommonNode(affected, previousAffectedWay);
            if (commonNode == null) {
                removeFirstNode();
                return;
            } else {
                twoWaysWithCommonNode = true;
            }
        }

        if (twoWaysWithCommonNode) {
            dialogBox(1, commonNode, affected, previousAffectedWay, commandList);
        } else {
            dialogBox(2, commonNode, affected, previousAffectedWay, commandList);
        }
    }

    // calls the dialog box
    private void dialogBox(int type, Node commonNode, Way affected, Way previousAffectedWay,
            List<Command> commandList) {

        final ExtendedDialog dialog = new SelectFromOptionDialog(type, commonNode, affected, previousAffectedWay,
                commandList, atNodes);
        dialog.setModal(false);
        dialog.showDialog();
        return; // splitting is performed in SegmentToKeepSelectionDialog.buttonAction()

    }

    // create a node in the position that the user has marked
    private Node createNode(ILatLon Pos, List<Command> commandList) {
        Boolean newNode = false;
        Node newStopPos;

        Point p = new Point();
        p.setLocation(Pos.lat(), Pos.lon());

        Node n = MainApplication.getMap().mapView.getNearestNode(p, OsmPrimitive::isUsable);
        if (n == null) {
            newNode = true;
            newStopPos = new Node(MainApplication.getMap().mapView.getLatLon(Pos.lat(), Pos.lon()));
        } else {
            newStopPos = new Node(n);
        }

        if (newNode) {
            commandList.add(new AddCommand(getLayerManager().getEditDataSet(), newStopPos));
        } else {
            commandList.add(new ChangeCommand(n, newStopPos));
            MainApplication.getLayerManager().getEditLayer().data.setSelected(newStopPos);
            newStopPos = n;
        }

        return newStopPos;
    }

    /*
     * the function works if we have both nodes on a single way. It adds the nodes
     * in the undoRedo, splits the ways if required, collects the existing keys of
     * the ways and sends to addTags function
     */
    private void addKeys(Way affected, List<Command> commandList, JComboBox<String> keys, JComboBox<String> values) {
        List<TagMap> affectedKeysList = new ArrayList<>();
        Way selectedWay = null;

        MainApplication.undoRedo.add(new SequenceCommand("Add Nodes", commandList));
        commandList.clear();

        addParentWay(atNodes.get(0));
        addParentWay(atNodes.get(1));

        SplitWayCommand result = SplitWayCommand.split(affected, atNodes, Collections.emptyList());
        if (result == null) {
            MainApplication.undoRedo.undo();
            resetLayer();
            return;
        }

        commandList.add(result);
        MainApplication.undoRedo.add(new SequenceCommand("Split Way", commandList));

        // Find the middle way after split
        List<Way> affectedWayList = result.getNewWays();
        affectedWayList.add(result.getOriginalWay());

        for (Way way : affectedWayList) {
            if (atNodes.contains(way.firstNode()) && atNodes.contains(way.lastNode())) {
                selectedWay = way;
                break;
            }
        }

        // add the existing keys of the selected way to affectedKeysList
        if (selectedWay != null) {
            affectedKeysList.add(affected.getKeys());
            addTags(affectedKeysList, Arrays.asList(selectedWay), keys, values, 0);
        } else {
            MainApplication.undoRedo.undo();
            MainApplication.undoRedo.undo();
            resetLayer();
            return;
        }
    }

    // this function is called when both nodes are in 2 adjacent ways
    private void addKeysOnBothWays(Node commonNode, Way affected, Way previousAffectedWay, List<Command> commandList,
            JComboBox<String> keys, JComboBox<String> values) {
        List<TagMap> affectedKeysList = new ArrayList<>();

        MainApplication.undoRedo.add(new SequenceCommand("Add Nodes", commandList));
        commandList.clear();

        // join newly created nodes to parent ways
        addParentWay(atNodes.get(0));
        addParentWay(atNodes.get(1));

        List<Node> nodelist1 = Arrays.asList(atNodes.get(0), commonNode);
        List<Node> nodelist2 = Arrays.asList(atNodes.get(1), commonNode);

        // required to be added to newly split way
        affectedKeysList.add(previousAffectedWay.getKeys());
        affectedKeysList.add(affected.getKeys());

        // split both the ways separately
        SplitWayCommand result1 = SplitWayCommand.split(previousAffectedWay, nodelist1, Collections.emptyList());
        SplitWayCommand result2 = SplitWayCommand.split(affected, nodelist2, Collections.emptyList());

        if (result1 != null)
            commandList.add(result1);
        else {
            MainApplication.undoRedo.undo();
            resetLayer();
            return;
        }
        if (result2 != null)
            commandList.add(result2);
        else {
            MainApplication.undoRedo.undo();
            resetLayer();
            return;
        }

        MainApplication.undoRedo.add(new SequenceCommand("Split Way", commandList));

        // add newly split way to relations
        List<Relation> referrers1 = OsmPrimitive.getFilteredList(previousAffectedWay.getReferrers(), Relation.class);
        referrers1.removeIf(r -> !RouteUtils.isPTRoute(r));

        int Index1 = getIndex(previousAffectedWay, referrers1, previousAffectedWay);

        List<Relation> referrers2 = OsmPrimitive.getFilteredList(affected.getReferrers(), Relation.class);
        referrers2.removeIf(r -> !RouteUtils.isPTRoute(r));

        int Index2 = getIndex(affected, referrers2, previousAffectedWay);

        Way way1 = null, way2 = null;

        // Find middle way which is a part of both the ways, so find the 2 ways which
        // together would form middle way
        boolean isOriginalWay = true; // we check both the original way and new ways
        for (Way way : result1.getNewWays()) {
            checkMembership(way, referrers1, Index1);
            if (way.containsNode(commonNode) && way.containsNode(atNodes.get(0))) {
                way1 = way;
                isOriginalWay = false;
                break;
            }
        }

        checkMembership(result1.getOriginalWay(), referrers1, Index1);

        if (isOriginalWay) {
            Way way = result1.getOriginalWay();
            if (way.containsNode(commonNode) && way.containsNode(atNodes.get(0))) {
                way1 = way;
            }
        }

        // now do for 2nd way
        isOriginalWay = true;

        for (Way way : result2.getNewWays()) {
            checkMembership(way, referrers2, Index2);
            if (way.containsNode(commonNode) && way.containsNode(atNodes.get(1))) {
                way2 = way;
                isOriginalWay = false;
                break;
            }
        }

        checkMembership(result2.getOriginalWay(), referrers2, Index2);

        if (isOriginalWay) {
            Way way = result2.getOriginalWay();
            if (way.containsNode(commonNode) && way.containsNode(atNodes.get(1))) {
                way2 = way;
            }
        }

        if (way1 != null && way2 != null) {
            List<Way> selectedWays = Arrays.asList(way1, way2);
            addTags(affectedKeysList, selectedWays, keys, values, 1);
        } else {
            MainApplication.undoRedo.undo();
            MainApplication.undoRedo.undo();
            resetLayer();
        }
    }

    // this function is called when both nodes are starting and ending points of
    // same way, we dont split anything here
    private void addKeysWhenStartEndPoint(Way affected, List<Command> commandList, JComboBox<String> keys,
            JComboBox<String> values) {
        List<TagMap> affectedKeysList = new ArrayList<>();
        Way selectedWay = affected;

        addParentWay(atNodes.get(0));
        addParentWay(atNodes.get(1));

        if (selectedWay != null) {
            affectedKeysList.add(affected.getKeys());
            addTags(affectedKeysList, Arrays.asList(selectedWay), keys, values, 2);
        } else {
            resetLayer();
        }
    }

    // join the node to the way only if the node is new
    private void addParentWay(Node node) {
        if (node.getParentWays().size() == 0) {
            MainApplication.getLayerManager().getEditLayer().data.setSelected(node);
            JoinNodeWayAction joinNodeWayAction = JoinNodeWayAction.createMoveNodeOntoWayAction();
            joinNodeWayAction.actionPerformed(null);
        }
    }

    // check if a way is present in a relation, if not then add it
    private void checkMembership(Way way, List<Relation> referrers, int Index) {
        for (Relation r : referrers) {
            boolean isMember = false;
            for (RelationMember rm : r.getMembers()) {
                if (rm.getType() == OsmPrimitiveType.WAY) {
                    if (rm.getWay().equals(way)) {
                        isMember = true;
                    }
                }
            }
            if (!isMember) {
                r.addMember(new RelationMember("", way));
            }
        }
    }

    // get index where the way is present in a relation
    private int getIndex(Way way, List<Relation> referrers, Way previousAffectedWay) {
        int Index = -1;
        for (Relation r : referrers) {
            for (int i = 0; i < r.getMembers().size(); i++) {
                if (r.getMembers().get(i).isWay() && r.getMembers().get(i).getWay().equals(previousAffectedWay)) {
                    Index = i;
                }
            }
        }
        return Index;
    }

    // take key value pair from the dialog box and add it to the existing ways
    private void addTags(List<TagMap> affectedKeysList, List<Way> selectedWay, JComboBox<String> keys,
            JComboBox<String> values, int type) {
        TagMap newKeys1 = affectedKeysList.get(0);
        String prevValue = null;

        if (keys.getSelectedItem() == "bridge") {
            newKeys1.put(keys.getSelectedItem().toString(), values.getSelectedItem().toString());
            newKeys1.put("layer", "1");
        } else if (keys.getSelectedItem() == "tunnel") {
            newKeys1.put(keys.getSelectedItem().toString(), values.getSelectedItem().toString());
            if (!values.getSelectedItem().toString().equals("building_passage"))
                newKeys1.put("layer", "-1");
        } else if (keys.getSelectedItem() == "traffic_calming") {
            newKeys1.put(keys.getSelectedItem().toString(), values.getSelectedItem().toString());
            newKeys1.put("maxspeed", "30");
        } else {
            if (newKeys1.containsKey("bus_bay")) {
                prevValue = newKeys1.get("bus_bay");
                newKeys1.put(keys.getSelectedItem().toString(), "both");
            } else {
                newKeys1.put(keys.getSelectedItem().toString(), values.getSelectedItem().toString());
            }
        }

        if (affectedKeysList.size() == 2) {
            TagMap newKeys2 = affectedKeysList.get(1);

            if (keys.getSelectedItem() == "bridge") {
                newKeys2.put(keys.getSelectedItem().toString(), values.getSelectedItem().toString());
                newKeys2.put("layer", "1");
            } else if (keys.getSelectedItem() == "tunnel") {
                newKeys2.put(keys.getSelectedItem().toString(), values.getSelectedItem().toString());
                if (!values.getSelectedItem().toString().equals("building_passage"))
                    newKeys2.put("layer", "-1");
            } else if (keys.getSelectedItem() == "traffic_calming") {
                newKeys2.put(keys.getSelectedItem().toString(), values.getSelectedItem().toString());
                newKeys2.put("maxspeed", "30");
            } else {
                if (newKeys2.containsKey("bus_bay")) {
                    prevValue = newKeys2.get("bus_bay");
                    newKeys2.put(keys.getSelectedItem().toString(), "both");
                    if (values.getSelectedItem().equals("left") && prevValue.equals("left"))
                        newKeys1.put("bus_bay", "right");
                    else if (values.getSelectedItem().equals("right") && prevValue.equals("right"))
                        newKeys1.put("bus_bay", "left");
                } else {
                    if (newKeys1.get("bus_bay").equals("both")) {
                        if (values.getSelectedItem().equals("left") && prevValue.equals("left"))
                            newKeys2.put("bus_bay", "right");
                        else if (values.getSelectedItem().equals("right") && prevValue.equals("right"))
                            newKeys2.put("bus_bay", "left");
                        else
                            newKeys2.put(keys.getSelectedItem().toString(), values.getSelectedItem().toString());
                    } else {
                        newKeys2.put(keys.getSelectedItem().toString(), values.getSelectedItem().toString());
                    }
                }
            }
            MainApplication.undoRedo
                    .add(new ChangePropertyCommand(Collections.singleton(selectedWay.get(1)), newKeys2));
        }
        MainApplication.undoRedo.add(new ChangePropertyCommand(Collections.singleton(selectedWay.get(0)), newKeys1));
        resetLayer();
    }

    // to find if there is any highway, railway, waterway crossing the way
    private void findIntersection(Set<Way> newWays) {
        try {
            DataSet ds = getLayerManager().getEditDataSet();
            addWaysIntersectingWays(ds.getWays(), Arrays.asList(previousAffectedWay, affected), newWays);
            Node n1 = previousAffectedWay.firstNode();
            Node n2 = previousAffectedWay.lastNode();
            Node n3 = affected.firstNode();
            Node n4 = affected.lastNode();
            List<Way> waysToBeRemoved = new ArrayList<>();
            for (Way way : newWays) {
                int count = 0;
                if (way.containsNode(n1))
                    count++;
                if (way.containsNode(n2))
                    count++;
                if (!previousAffectedWay.equals(affected)) {
                    if (way.containsNode(n3))
                        count++;
                    if (way.containsNode(n4))
                        count++;
                }
                if (count == 1) {
                    waysToBeRemoved.add(way);
                } else {
                    if (!way.hasKey("highway") && !way.hasKey("waterway") && !way.hasKey("railway")) {
                        waysToBeRemoved.add(way);
                    }
                }
            }
            newWays.removeAll(waysToBeRemoved);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addWaysIntersectingWay(Collection<Way> ways, Way w, Set<Way> newWays) {
        List<Pair<Node, Node>> nodePairs = w.getNodePairs(false);
        for (Way anyway : ways) {
            if (Objects.equals(anyway, w))
                continue;
            if (newWays.contains(anyway))
                continue;
            List<Pair<Node, Node>> nodePairs2 = anyway.getNodePairs(false);
            loop: for (Pair<Node, Node> p1 : nodePairs) {
                for (Pair<Node, Node> p2 : nodePairs2) {
                    if (null != Geometry.getSegmentSegmentIntersection(p1.a.getEastNorth(), p1.b.getEastNorth(),
                            p2.a.getEastNorth(), p2.b.getEastNorth())) {
                        newWays.add(anyway);
                        break loop;
                    }
                }
            }
        }
    }

    void addWaysIntersectingWays(Collection<Way> allWays, Collection<Way> initWays, Set<Way> newWays) {
        for (Way w : initWays) {
            addWaysIntersectingWay(allWays, w, newWays);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        // while the mouse is moving, surroundings are checked
        // if anything is found, it will be highlighted.
        // priority is given to nodes
        Cursor newCurs = getCursor();

        Node n = MainApplication.getMap().mapView.getNearestNode(e.getPoint(), OsmPrimitive::isUsable);
        if (n != null) {
            newHighlights.add(n);
            newCurs = cursorJoinNode;
        } else {
            List<WaySegment> wss = MainApplication.getMap().mapView.getNearestWaySegments(e.getPoint(),
                    OsmPrimitive::isSelectable);

            if (!wss.isEmpty()) {
                for (WaySegment ws : wss) {
                    newHighlights.add(ws.way);
                }
                newCurs = cursorJoinWay;
            }
        }

        MainApplication.getMap().mapView.setCursor(newCurs);
        updateHighlights();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (Pos1 == null) {
            SegWay1 = MainApplication.getMap().mapView.getNearestWay(e.getPoint(), OsmPrimitive::isSelectable);
            if (SegWay1 != null) {
                Pos1 = MainApplication.getMap().mapView.getLatLon(e.getX(), e.getY());
            }
        } else if (Pos2 == null) {
            LatLon tempPos = MainApplication.getMap().mapView.getLatLon(e.getX(), e.getY());
            if (Pos1.lat() != tempPos.lat() || Pos1.lon() != tempPos.lon()) {
                SegWay2 = MainApplication.getMap().mapView.getNearestWay(e.getPoint(), OsmPrimitive::isSelectable);
                if (SegWay2 != null) {
                    Pos2 = MainApplication.getMap().mapView.getLatLon(e.getX(), e.getY());
                }
            }
        }

        if (Pos2 != null) {
            reset();
            action();
        }
    }

    // turn off what has been highlighted on last mouse move and highlight what has
    // to be highlighted now
    private void updateHighlights() {
        if (oldHighlights == null && newHighlights == null) {
            return;
        }

        if (oldHighlights.isEmpty() && newHighlights.isEmpty()) {
            return;
        }

        for (OsmPrimitive osm : oldHighlights) {
            osm.setHighlighted(false);
        }

        for (OsmPrimitive osm : newHighlights) {
            osm.setHighlighted(true);
        }

        MainApplication.getLayerManager().getEditLayer().invalidate();

        oldHighlights.clear();
        oldHighlights.addAll(newHighlights);
        newHighlights.clear();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("keyTyped");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("keyPressed");
        boolean z = e.getKeyCode() == KeyEvent.VK_Z;
        updateKeyModifiers(e);
        if (z) {
            if (Pos1 != null && Pos2 == null) {
                Pos1 = null;
                SegWay1 = null;
                temporaryLayer.invalidate();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println("keyReleased");
    }

    // A dialogBox to query whether to select bus_bay, tunnel or bridge.

    private class SelectFromOptionDialog extends ExtendedDialog {
        Way affected, previousAffectedWay;
        private JComboBox<String> keys;
        private JComboBox<String> values;
        private int type;
        private List<Command> commandList;
        private Node commonNode;
        private boolean rightHandTraffic;

        SelectFromOptionDialog(int type, Node commonNode, Way affected, Way previousAffectedWay,
                List<Command> commandList, List<Node> atNode) {
            super(Main.parent, tr("What do you want the segment to be?"), new String[] { tr("Ok"), tr("Cancel") },
                    true);
            this.affected = affected;
            this.previousAffectedWay = previousAffectedWay;
            this.commandList = commandList;
            this.type = type;
            this.commonNode = commonNode;

            rightHandTraffic = true;
            for (Node n : atNodes) {
                if (!RightAndLefthandTraffic.isRightHandTraffic(n.getCoor())) {
                    rightHandTraffic = false;
                    break;
                }
            }

            setButtonIcons("ok", "cancel");
            setCancelButton(2);
            configureContextsensitiveHelp("/Dialog/AddValue", true /* show help button */);

            final JPanel pane = new JPanel(new GridBagLayout());
            pane.add(new JLabel("Select the appropriate option"), GBC.eol().fill(GBC.HORIZONTAL));

            keys = new JComboBox<>();
            values = new JComboBox<>();
            keys.setEditable(true);
            Set<Way> newWays = new HashSet<>();
            findIntersection(newWays);

            if (previousAffectedWay.hasKey("waterway") || affected.hasKey("waterway")) {
                setOptionsWithTunnel();
            } else if (previousAffectedWay.hasKey("bus_bay") || affected.hasKey("bus_bay")) {
                setOptionsWithBusBay();
            } else if (newWays != null && newWays.size() != 0) {
                setOptionsWithBridge();
            } else {
                setOptionsWithBusBay();
            }

            pane.add(keys, GBC.eop().fill(GBC.HORIZONTAL));
            pane.add(values, GBC.eop().fill(GBC.HORIZONTAL));

            setContent(pane, false);
            setDefaultCloseOperation(HIDE_ON_CLOSE);
        }

        private void setOptionsWithBusBay() {
            keys.setModel(
                    new DefaultComboBoxModel<>(new String[] { "bus_bay", "bridge", "tunnel", "traffic_calming" }));

            if (affected.hasTag("bus_bay", "right") || previousAffectedWay.hasTag("bus_bay", "right")) {
                values.setModel(new DefaultComboBoxModel<>(new String[] { "left", "right", "both" }));
            } else if (affected.hasTag("bus_bay", "left") || previousAffectedWay.hasTag("bus_bay", "left")) {
                values.setModel(new DefaultComboBoxModel<>(new String[] { "right", "left", "both" }));
            } else if (rightHandTraffic) {
                values.setModel(new DefaultComboBoxModel<>(new String[] { "right", "left", "both" }));
            } else {
                values.setModel(new DefaultComboBoxModel<>(new String[] { "left", "right", "both" }));
            }

            // below code changes the list in values on the basis of key
            keys.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ("bus_bay".equals(keys.getSelectedItem())) {
                        values.setModel(new DefaultComboBoxModel<>(new String[] { "both", "right", "left" }));
                        if (rightHandTraffic)
                            values.setModel(new DefaultComboBoxModel<>(new String[] { "right", "left", "both" }));
                        else
                            values.setModel(new DefaultComboBoxModel<>(new String[] { "left", "right", "both" }));
                    } else if ("bridge".equals(keys.getSelectedItem())) {
                        values.setModel(new DefaultComboBoxModel<>(new String[] { "yes" }));
                    } else if ("tunnel".equals(keys.getSelectedItem())) {
                        if (previousAffectedWay.hasKey("waterway") || affected.hasKey("waterway"))
                            values.setModel(
                                    new DefaultComboBoxModel<>(new String[] { "culvert", "yes", "building_passage" }));
                        else
                            values.setModel(
                                    new DefaultComboBoxModel<>(new String[] { "yes", "culvert", "building_passage" }));
                    } else if ("traffic_calming".equals(keys.getSelectedItem())) {
                        values.setModel(new DefaultComboBoxModel<>(new String[] { "table" }));
                    }
                }
            });
        }

        private void setOptionsWithTunnel() {
            keys.setModel(
                    new DefaultComboBoxModel<>(new String[] { "tunnel", "bridge", "bus_bay", "traffic_calming" }));

            if (previousAffectedWay.hasKey("waterway") || affected.hasKey("waterway"))
                values.setModel(new DefaultComboBoxModel<>(new String[] { "culvert", "yes", "building_passage" }));
            else
                values.setModel(new DefaultComboBoxModel<>(new String[] { "yes", "culvert", "building_passage" }));

            // below code changes the list in values on the basis of key
            keys.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ("tunnel".equals(keys.getSelectedItem())) {
                        if (previousAffectedWay.hasKey("waterway") || affected.hasKey("waterway"))
                            values.setModel(
                                    new DefaultComboBoxModel<>(new String[] { "culvert", "yes", "building_passage" }));
                        else
                            values.setModel(
                                    new DefaultComboBoxModel<>(new String[] { "yes", "culvert", "building_passage" }));
                    } else if ("bus_bay".equals(keys.getSelectedItem())) {
                        values.setModel(new DefaultComboBoxModel<>(new String[] { "both", "right", "left" }));
                        if (rightHandTraffic)
                            values.setModel(new DefaultComboBoxModel<>(new String[] { "right", "left", "both" }));
                        else
                            values.setModel(new DefaultComboBoxModel<>(new String[] { "left", "right", "both" }));
                    } else if ("bridge".equals(keys.getSelectedItem())) {
                        values.setModel(new DefaultComboBoxModel<>(new String[] { "yes" }));
                    } else if ("traffic_calming".equals(keys.getSelectedItem())) {
                        values.setModel(new DefaultComboBoxModel<>(new String[] { "table" }));
                    }
                }
            });
        }

        private void setOptionsWithBridge() {
            keys.setModel(
                    new DefaultComboBoxModel<>(new String[] { "bridge", "bus_bay", "tunnel", "traffic_calming" }));

            values.setModel(new DefaultComboBoxModel<>(new String[] { "yes" }));

            // below code changes the list in values on the basis of key
            keys.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ("tunnel".equals(keys.getSelectedItem())) {
                        if (previousAffectedWay.hasKey("waterway") || affected.hasKey("waterway"))
                            values.setModel(
                                    new DefaultComboBoxModel<>(new String[] { "culvert", "yes", "building_passage" }));
                        else
                            values.setModel(
                                    new DefaultComboBoxModel<>(new String[] { "yes", "culvert", "building_passage" }));
                    } else if ("bus_bay".equals(keys.getSelectedItem())) {
                        values.setModel(new DefaultComboBoxModel<>(new String[] { "both", "right", "left" }));
                        if (rightHandTraffic)
                            values.setModel(new DefaultComboBoxModel<>(new String[] { "right", "left", "both" }));
                        else
                            values.setModel(new DefaultComboBoxModel<>(new String[] { "left", "right", "both" }));
                    } else if ("bridge".equals(keys.getSelectedItem())) {
                        values.setModel(new DefaultComboBoxModel<>(new String[] { "yes" }));
                    } else if ("traffic_calming".equals(keys.getSelectedItem())) {
                        values.setModel(new DefaultComboBoxModel<>(new String[] { "table" }));
                    }
                }
            });
        }

        @Override
        protected void buttonAction(int buttonIndex, ActionEvent evt) {
            super.buttonAction(buttonIndex, evt);
            toggleSaveState(); // necessary since #showDialog() does not handle it due to the non-modal dialog

            if (getValue() == 1) {
                if (this.type == 1) {
                    addKeysOnBothWays(this.commonNode, this.affected, this.previousAffectedWay, this.commandList, keys,
                            values);
                } else if (this.type == 2) {
                    addKeys(this.affected, this.commandList, keys, values);
                } else if (this.type == 3) {
                    addKeysWhenStartEndPoint(this.affected, this.commandList, keys, values);
                }

            } else if (getValue() != 3) {
                resetLayer();
            }
        }

    }

    private class DoubleSplitLayer extends AbstractMapViewPaintable {
        @Override
        public void paint(Graphics2D g, MapView mv, Bounds bbox) {
            if (Pos1 != null) {
                Point curP1 = MainApplication.getMap().mapView.getPoint(Pos1);
                CheckParameterUtil.ensureParameterNotNull(mv, "mv");
                g.setColor(Color.RED);
                g.fill(new Rectangle.Double(curP1.x - 3, curP1.y - 3, 6, 6));
            }
            if (Pos2 != null) {
                Point curP2 = MainApplication.getMap().mapView.getPoint(Pos2);
                CheckParameterUtil.ensureParameterNotNull(mv, "mv");
                g.setColor(Color.RED);
                g.fill(new Rectangle.Double(curP2.x - 3, curP2.y - 3, 6, 6));
            }
        }
    }

}
