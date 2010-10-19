package org.openstreetmap.josm.plugins.elevation;

import java.util.List;

import org.openstreetmap.josm.data.gpx.WayPoint;

/**
 * This class represents a 'leaf node' in the elevation profile tree. It has no children and ignores
 * the slice size parameter. So no data reduction takes places; every way point is kept.   
 * 
 * ElevationModelSlice is intended to be used internally.
 * @author oliverwieland
 *
 */
public class ElevationProfileLeaf extends ElevationProfileBase {
    /**
     * Creates a name elevation profile leaf with a given set of way points.
     * @param name The name of the profile.
     * @param parent The (optional) parent profile.
     * @param wayPoints The list containing the way points of the profile.
     */
    public ElevationProfileLeaf(String name, IElevationProfile parent, List<WayPoint> wayPoints) {
        super(name, parent, wayPoints, 0);		
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.elevation.IElevationProfile#elevationValueAt(int)
     */
    @Override
    public int elevationValueAt(int i) {
        if (i < 0 || i >= getNumberOfWayPoints()) throw new IndexOutOfBoundsException("Wrong index: " + i);
        return (int)WayPointHelper.getElevation(getWayPoints().get(i));
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.elevation.IElevationProfile#getChildren()
     */
    @Override
    public List<IElevationProfile> getChildren() {
        return null; // we have no children
    }
    
    @Override
    public String toString() {
        return "ElevationModelSlice [avrgEle=" + getAverageHeight() + ", maxEle=" + getMaxHeight()
                + ", minEle=" + getMinHeight() + ", name=" + getName() + ", wayPoints="
                + getNumberOfWayPoints() + "]";
    }
}
