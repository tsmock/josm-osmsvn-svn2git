// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.gpx;

import java.util.Collection;

import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxRoute;
import org.openstreetmap.josm.data.gpx.IGpxTrack;
import org.openstreetmap.josm.data.gpx.IGpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * Utility class to apply a visitor on GPX containers (track, route, data).
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 */
public final class GpxIterator {
    /**
     * Static class, no need to instantiate me.
     */
    private GpxIterator() {}

    /**
     * Runs the given visitor on a GPX data instance. If one or both
     * arguments are null, this method will return immediately.
     *
     * @param data
     *            The GPX data instance.
     * @param visitor
     *            The visitor which inspects all GPX entities.
     */
    public static void visit(GpxData data, IGpxVisitor visitor) {
        if (data == null) return;
        if (visitor == null) return;

        if (data.isEmpty()) return;

        visitor.beginWayPoints();
        visitSingleWaypoints(data, visitor);
        visitor.endWayPoints();

        // routes
        if (data.hasRoutePoints()) {
            for (GpxRoute rte : data.routes) {
                visitRoute(visitor, rte);
            }
        }

        // tracks
        for (IGpxTrack trk : data.tracks) {
            visitTrack(visitor, trk);
        }
    }

    /**
     * Visits a single GPX track.
     * @param track The track to visit.
     * @param visitor
     *            The visitor which inspects all GPX entities.
     */
    public static void visit(IGpxTrack track, IGpxVisitor visitor) {
        visitTrack(visitor, track);
    }

    /**
     * Visits a single GPX route.
     * @param route The route to visit.
     * @param visitor
     *            The visitor which inspects all GPX entities.
     */
    public static void visit(GpxRoute route, IGpxVisitor visitor) {
        visitRoute(visitor, route);
    }

    // ---------------------- Helper methods ----------------

    private static void visitTrack(IGpxVisitor visitor, IGpxTrack trk) {
        if (trk == null) return;
        if (visitor == null) return;

        Collection<IGpxTrackSegment> segments = trk.getSegments();

        if (segments != null) {
            visitor.beginTrack(trk);
            // visit all segments
            for (IGpxTrackSegment segment : segments) {
                Collection<WayPoint> waypts = segment.getWayPoints();
                // no visitor here...
                if (waypts == null)
                    continue;

                visitor.beginTrackSegment(trk, segment);

                for (WayPoint wayPoint : waypts) {
                    visitor.visitTrackPoint(wayPoint, trk, segment);
                }

                visitor.endTrackSegment(trk, segment);
            }
            visitor.endTrack(trk);
        }
    }

    private static void visitRoute(IGpxVisitor visitor, GpxRoute route) {
        if (route == null) return;
        if (visitor == null) return;

        visitor.beginWayPoints();
        for (WayPoint wpt : route.routePoints) {
            visitor.visitRoutePoint(wpt, route);
        }
        visitor.endWayPoints();
    }

    private static void visitSingleWaypoints(GpxData data, IGpxVisitor visitor) {
        // isolated way points
        if (data.waypoints != null) { // better with an hasWaypoints method!?
            for (WayPoint wpt : data.waypoints) {
                visitor.visitWayPoint(wpt);
            }
        }
    }
}
