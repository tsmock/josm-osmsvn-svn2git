// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.commandline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.IGpxTrack;
import org.openstreetmap.josm.data.gpx.IGpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.BBox;

public class GpxFilter {
    private BBox bbox;
    private final GpxData data;

    public GpxFilter() {
        bbox = new BBox(0.0, 0.0, 0.0, 0.0);
        data = new GpxData();
    }

    public void initBboxFilter(BBox bbox) {
        this.bbox = bbox;
    }

    public void addGpxData(GpxData data) {
        Collection<Collection<WayPoint>> currentTrack;
        Collection<WayPoint> currentSegment;
        for (IGpxTrack track : data.tracks) {
            currentTrack = new ArrayList<>();
            for (IGpxTrackSegment segment : track.getSegments()) {
                currentSegment = new ArrayList<>();
                for (WayPoint wp : segment.getWayPoints()) {
                    if (bbox.bounds(wp.getCoor())) {
                        currentSegment.add(wp);
                    } else {
                        if (currentSegment.size() > 1) {
                            currentTrack.add(currentSegment);
                            currentSegment = new ArrayList<>();
                        }
                    }
                }
                if (currentSegment.size() > 1) {
                    currentTrack.add(currentSegment);
                    currentSegment = new ArrayList<>();
                }
            }
            this.data.tracks.add(new GpxTrack(currentTrack, Collections.<String, Object>emptyMap()));
        }
    }

    public GpxData getGpxData() {
        return data;
    }
}
