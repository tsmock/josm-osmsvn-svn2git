// License: GPL. For details, see LICENSE file.
package com.innovant.josm.jrt.core;

import org.jgrapht.Graph;
import org.jgrapht.graph.GraphDelegator;
import org.openstreetmap.josm.data.osm.Node;

import com.innovant.josm.jrt.core.RoutingGraph.RouteType;
import com.innovant.josm.jrt.osm.OsmEdge;

/**
 * @author jose
 *
 */
public class RoutingGraphDelegator extends GraphDelegator<Node, OsmEdge> {
    /**
     *
     */
    private RouteType routeType;

    public RoutingGraphDelegator(Graph<Node, OsmEdge> arg0) {
        super(arg0);
    }

    public RouteType getRouteType() {
        return routeType;
    }

    public void setRouteType(RouteType routeType) {
        this.routeType = routeType;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public double getEdgeWeight(OsmEdge edge) {
        double weight = Double.MAX_VALUE;

        if (routeType == RouteType.SHORTEST) weight = edge.getLength();
        if (routeType == RouteType.FASTEST) weight = edge.getLength() / edge.getSpeed();
        // Return the time spent to traverse the way
        return weight;
    }

}
