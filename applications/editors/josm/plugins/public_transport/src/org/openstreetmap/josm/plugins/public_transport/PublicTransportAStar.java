// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport;

import java.util.Iterator;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;

public class PublicTransportAStar extends AStarAlgorithm {
    public PublicTransportAStar(Node start, Node end) {
        super(new NodeVertex(start), new NodeVertex(end));
    }

    public static class NodeVertex extends AStarAlgorithm.Vertex {
        public NodeVertex(Node node) {
            this.node = node;
        }

        @Override
        public int compareTo(AStarAlgorithm.Vertex v) {
            return this.node.compareTo(((NodeVertex) v).node);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NodeVertex))
                return false;
            return node.equals(((NodeVertex) o).node);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(node);
        }

        public Node node;
    }

    public static class PartialWayEdge extends AStarAlgorithm.Edge {
        public PartialWayEdge(Way way, int beginIndex, int endIndex) {
            this.way = way;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }

        @Override
        public AStarAlgorithm.Vertex getBegin() {
            return new NodeVertex(way.getNode(beginIndex));
        }

        @Override
        public AStarAlgorithm.Vertex getEnd() {
            return new NodeVertex(way.getNode(endIndex));
        }

        @Override
        public double getLength() {
            int min = beginIndex;
            int max = endIndex;
            if (endIndex < beginIndex) {
                min = endIndex;
                max = beginIndex;
            }

            double totalDistance = 0;
            for (int i = min; i < max; ++i) {
                totalDistance += way.getNode(i).greatCircleDistance(way.getNode(i + 1));
            }
            return totalDistance;
        }

        public Way way;

        public int beginIndex;

        public int endIndex;
    }

    @Override
    public Vector<AStarAlgorithm.Edge> getNeighbors(AStarAlgorithm.Vertex vertex) {
        if (waysPerNode == null) {
            waysPerNode = new TreeMap<>();

            Iterator<Way> iter = MainApplication.getLayerManager().getEditDataSet().getWays().iterator();
            while (iter.hasNext()) {
                Way way = iter.next();

                // Only consider ways that are usable
                if (!way.isUsable())
                    continue;

                // Further tests whether the way is eligible.

                for (int i = 0; i < way.getNodesCount(); ++i) {
                    if (waysPerNode.get(way.getNode(i)) == null)
                        waysPerNode.put(way.getNode(i), new TreeSet<Way>());
                    waysPerNode.get(way.getNode(i)).add(way);
                }
            }
        }

        NodeVertex nodeVertex = (NodeVertex) vertex;
        System.out.println(nodeVertex.node.getUniqueId());

        Vector<AStarAlgorithm.Edge> result = new Vector<>();
        // Determine all ways in which nodeVertex.node is contained.
        Iterator<Way> iter = waysPerNode.get(nodeVertex.node).iterator();
        while (iter.hasNext()) {
            Way way = iter.next();

            // Only consider ways that are usable
            if (!way.isUsable())
                continue;

            // Further tests whether the way is eligible.

            for (int i = 0; i < way.getNodesCount(); ++i) {
                if (way.getNode(i).equals(nodeVertex.node)) {
                    if (i > 0)
                        result.add(new PartialWayEdge(way, i, i - 1));
                    if (i < way.getNodesCount() - 1)
                        result.add(new PartialWayEdge(way, i, i + 1));
                }
            }
        }

        return result;
    }

    TreeMap<Node, TreeSet<Way>> waysPerNode = null;

    @Override
    public double estimateDistance(AStarAlgorithm.Vertex vertex) {
        NodeVertex nodeVertex = (NodeVertex) vertex;
        return ((NodeVertex) super.end).node
                .greatCircleDistance(nodeVertex.node);
    }
}
