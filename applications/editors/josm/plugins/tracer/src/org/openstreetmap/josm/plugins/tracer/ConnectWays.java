/**
 * Tracer - plugin for JOSM
 * Jan Bilak, Petr Dlouhý
 * This program is free software and licensed under GPL.
 */

package org.openstreetmap.josm.plugins.tracer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.ILatLon;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Pair;

public class ConnectWays {
    final static double MIN_DISTANCE = 0.000005; //Minimal distance, when nodes are merged
    final static double MIN_DISTANCE_TW = 0.000005; //Minimal distance, when node is connected to other way
    final static double MIN_DISTANCE_SQ = 0.000005; //Minimal distance, when other node is connected this way
    final static double MAX_ANGLE = 30; //Minimal angle, when other node is connected this way

    /**
     * Try connect way to other buidings.
     * @param way Way to connect.
     * @return Commands.
     */
    public static Command connect(Way way) {
        Map<Way, Way> modifiedWays = new HashMap<>();
        LinkedList<Command> cmds = new LinkedList<>();
        Way newWay = new Way(way);
        for (int i = 0; i < way.getNodesCount() - 1; i++) {
            Node n = way.getNode(i);
            System.out.println("-------");
            System.out.println("Node: " + n);
            LatLon ll = n.getCoor();
            BBox bbox = new BBox(
                    n.lon() - MIN_DISTANCE,
                    n.lat() - MIN_DISTANCE,
                    n.lon() + MIN_DISTANCE,
                    n.lat() + MIN_DISTANCE);

            // bude se node slucovat s jinym?
            double minDistanceSq = MIN_DISTANCE;
            List<Node> nodes = MainApplication.getLayerManager().getEditDataSet().searchNodes(bbox);
            Node nearestNode = null;
            for (Node nn : nodes) {
                if (!nn.isUsable() || way.containsNode(nn) || newWay.containsNode(nn) || !isInBuilding(nn)) {
                    continue;
                }
                double dist = nn.getCoor().distance(ll);
                if (dist < minDistanceSq) {
                    minDistanceSq = dist;
                    nearestNode = nn;
                }
            }

            System.out.println("Nearest: " + nearestNode);
            if (nearestNode == null) {
                tryConnectNodeToAnyWay(n, modifiedWays);
            } else {
                cmds.addAll(mergeNodes(n, nearestNode, newWay));
            }
        }

        for(Map.Entry<Way, Way> e : modifiedWays.entrySet()){
            cmds.add(new ChangeCommand(e.getKey(), e.getValue()));
        }

        cmds.addFirst(new ChangeCommand(way, trySplitWayByAnyNodes(newWay)));

        Command cmd = new SequenceCommand(tr("Merge objects nodes"), cmds);
        return cmd;
    }

    /**
     * Merges two nodes
     * @param n1 First node
     * @param n2 Second node
     * @param way Way containing first node
     * @return List of Commands.
     */
    private static List<Command> mergeNodes(Node n1, Node n2, Way way){
        List<Command> cmds = new LinkedList<>();
        cmds.add(new MoveCommand(n2,
                 (n1.getEastNorth().getX() - n2.getEastNorth().getX())/2,
                 (n1.getEastNorth().getY() - n2.getEastNorth().getY())/2
                 ));

        int j = way.getNodes().indexOf(n1);
        way.addNode(j, n2);
        if (j == 0) {
            // first + last point
            way.addNode(way.getNodesCount(), n2);
        }
        way.removeNode(n1);

        cmds.add(new DeleteCommand(n1));
        return cmds;
    }

    /**
     * Try connect node "node" to way of other building.
     *
     * Zkusi zjistit, zda node neni tak blizko nejake usecky existujici budovy,
     * ze by mel byt zacnenen do teto usecky. Pokud ano, provede to.
     *
     * @param node Node to connect.
     * @throws IllegalStateException
     * @throws IndexOutOfBoundsException
     * @return List of Commands.
     */
    private static void tryConnectNodeToAnyWay(Node node, Map<Way, Way> m)
            throws IllegalStateException, IndexOutOfBoundsException {

        LatLon ll = node.getCoor();
        BBox bbox = new BBox(
                node.lon() - MIN_DISTANCE_TW,
                node.lat() - MIN_DISTANCE_TW,
                node.lon() + MIN_DISTANCE_TW,
                node.lat() + MIN_DISTANCE_TW);

        // node nebyl slouceny s jinym
        // hledani pripadne blizke usecky, kam bod pridat
        List<Way> ways = MainApplication.getLayerManager().getEditDataSet().searchWays(bbox);
        double minDist = Double.MAX_VALUE;
        Way nearestWay = null;
        int nearestNodeIndex = 0;
        for (Way ww : ways) {
            if (!ww.isUsable() || ww.containsNode(node) || !isBuilding(ww)) {
                continue;
            }

            if(m.get(ww) != null){
                ww = m.get(ww);
            }

            for (Pair<Node, Node> np : ww.getNodePairs(false)) {
                double dist = TracerGeometry.distanceFromSegment(ll, np.a, np.b);
                if (dist < minDist) {
                    minDist = dist;
                    nearestWay = ww;
                    nearestNodeIndex = ww.getNodes().indexOf(np.a);
                }
            }
        }
        System.out.println("Nearest way: " + nearestWay + " distance: " + minDist);
        if (minDist < MIN_DISTANCE_TW) {
            Way newNWay = new Way(nearestWay);

            newNWay.addNode(nearestNodeIndex + 1, node);
            System.out.println("New way:" + newNWay);

            m.put(nearestWay, newNWay);
        }
    }

    /**
     * Try split way by any existing buiding nodes.
     *
     * Zkusi zjistit zda nejake usecka z way by nemela prochazet nejakym existujicim bodem,
     * ktery je ji velmi blizko. Pokud ano, tak puvodni usecku rozdeli na dve tak, aby
     * prochazela takovym bodem.
     *
     * @param way Way to split.
     * @throws IndexOutOfBoundsException
     * @throws IllegalStateException
     * @return Modified way
     */
    private static Way trySplitWayByAnyNodes(Way way)
            throws IndexOutOfBoundsException, IllegalStateException {

        // projdi kazdou novou usecku a zjisti, zda by nemela vest pres existujici body
        int i = 0;
        while (i < way.getNodesCount()) {
            // usecka n1, n2
            Node n1 = way.getNodes().get(i);
            Node n2 = way.getNodes().get((i + 1) % way.getNodesCount());
            System.out.println(way.getNodes().get(i) + "-----" + way.getNodes().get((i + 1) % way.getNodesCount()));
            double minDistanceSq = MIN_DISTANCE_SQ;
            //double maxAngle = MAX_ANGLE;
            List<Node> nodes = MainApplication.getLayerManager().getEditDataSet().searchNodes(new BBox(
                Math.min(n1.lon(), n2.lon()) - minDistanceSq,
                Math.min(n1.lat(), n2.lat()) - minDistanceSq,
                Math.max(n1.lon(), n2.lon()) + minDistanceSq,
                Math.max(n1.lat(), n2.lat()) + minDistanceSq
            ));
            Node nearestNode = null;
            for (Node nod : nodes) {
                if (!nod.isUsable() || way.containsNode(nod) || !isInBuilding(nod)) {
                    continue;
                }
                double dist = TracerGeometry.distanceFromSegment(nod, n1, n2);
                double angle = TracerGeometry.angleOfLines(n1, nod, nod, n2);
                System.out.println("Angle: " + angle + " distance: " + dist + " Node: " + nod);
                if (!n1.equalsEpsilon(nod, ILatLon.MAX_SERVER_PRECISION)
                 && !n2.equalsEpsilon(nod, ILatLon.MAX_SERVER_PRECISION) && dist < minDistanceSq) { // && Math.abs(angle) < maxAngle) {
                    //maxAngle = angle;
                    nearestNode = nod;
                }
            }
            System.out.println("Nearest_: " + nearestNode);
            System.out.println("");
            if (nearestNode == null) {
                // tato usecka se nerozdeli
                i++;
                continue;
            } else {
                // rozdeleni usecky
                way.addNode(i + 1, nearestNode);
                continue; // i nezvetsuji, treba bude treba rozdelit usecku znovu
            }
        }
        return way;
    }

    private static boolean isInBuilding(Node n) {
        for (OsmPrimitive op : n.getReferrers()) {
            if (op instanceof Way) {
                if (isBuilding((Way) op)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isBuilding(Way w) {
        return (w.getKeys().get("building") == null ? false : w.getKeys().get("building").equals("yes"));
    }
}
