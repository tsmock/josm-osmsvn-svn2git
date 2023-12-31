// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.List;
import java.util.Locale;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.ILatLon;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Pair;

/**
 * The Class OsmUtils provides some utilities not provided by the OSM data framework.
 */
public final class OsmUtils {

    /**
     * Instantiates a new osm utils.
     */
    private OsmUtils() {}

    /** The cached locale. */
    private static String cachedLocale = null;

    /**
     * Gets the minimum distance of single coordinate to a way.
     *
     * @param coor the coordinate to get the minimum distance for.
     * @param w the w the way to compare against
     * @return the minimum distance between the given coordinate and the way
     */
    public static double getMinimumDistanceToWay(LatLon coor, Way w) {
        if (coor == null || w == null)
            return Double.POSITIVE_INFINITY;

        double minDist = Double.MAX_VALUE;
        List<Pair<Node, Node>> x = w.getNodePairs(true);

        for (Pair<Node, Node> pair : x) {
            ILatLon ap = pair.a;
            ILatLon bp = pair.b;

            double dist = findMinimum(ap, bp, coor);
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
    }

    /**
     * Find the minimum distance between a point and two way coordinates recursively.
     *
     * @param a the a the first way point coordinate
     * @param b the b the second way point coordinate
     * @param c the c the node coordinate
     * @return the double the minimum distance in m of the way and the node
     */
    private static double findMinimum(ILatLon a, ILatLon b, ILatLon c) {
        CheckParameterUtil.ensureParameterNotNull(c, "c");
        CheckParameterUtil.ensureParameterNotNull(b, "b");
        CheckParameterUtil.ensureParameterNotNull(a, "a");

        LatLon mid = new LatLon((a.lat() + b.lat()) / 2, (a.lon() + b.lon()) / 2);

        double ac = a.greatCircleDistance(c);
        double bc = b.greatCircleDistance(c);
        double mc = mid.greatCircleDistance(c);

        double min = Math.min(Math.min(ac, mc), bc);


        if (min < 5.0) { // close enough?
            return min;
        }

        if (mc < ac && mc < bc) {
            // mid point has lower distance than a and b
            if (ac > bc) { // recurse
                return findMinimum(b, mid, c);
            } else {
                return findMinimum(a, mid, c);
            }
        } else { // mid point is not closer than a or b
            return Math.min(ac, bc);
        }
    }

    /**
     * Checks, if the given address has a relation hosting the address values. This method looks
     * for a relation of type 'associatedStreet' and checks the members for address values, if present.
     * If the member has address values, this methods sets the derived properties of the address
     * node accordingly.
     *
     * @param address The address to check.
     * @return true, if an associated relation has been found.
     */
    public static boolean getValuesFromRelation(OSMAddress address) {
        if (address == null) {
            return false;
        }

        boolean hasValuesFromRel = false; /* true, if we applied some address props from the relation */
        OsmPrimitive addrNode = address.getOsmObject();

        // check all referrers of the node
        for (OsmPrimitive osm : addrNode.getReferrers()) {
            if (osm instanceof Relation) {
                Relation r = (Relation) osm;
                // Relation has the right type?
                if (!TagUtils.isAssociatedStreetRelation(r)) continue;

                // check for 'street' members
                for (RelationMember rm : r.getMembers()) {
                    if (TagUtils.isStreetMember(rm)) {
                        OsmPrimitive street = rm.getMember();
                        if (TagUtils.hasHighwayTag(street)) {
                            String streetName = TagUtils.getNameValue(street);
                            if (!StringUtils.isNullOrEmpty(streetName)) {
                                // street name found -> set property
                                address.setDerivedValue(TagConstants.ADDR_STREET_TAG, streetName);
                                hasValuesFromRel = true;
                                break;
                            } // else: Street has no name: Ooops
                        } // else: Street member, but no highway tag: Ooops
                    }
                }

                // Check for other address properties
                if (TagUtils.hasAddrCityTag(r)) { // city
                    address.setDerivedValue(TagConstants.ADDR_CITY_TAG, TagUtils.getAddrCityValue(r));
                    hasValuesFromRel = true;
                }
                if (TagUtils.hasAddrCountryTag(r)) { // country
                    address.setDerivedValue(TagConstants.ADDR_COUNTRY_TAG, TagUtils.getAddrCountryValue(r));
                    hasValuesFromRel = true;
                }
                if (TagUtils.hasAddrPostcodeTag(r)) { // postcode
                    address.setDerivedValue(TagConstants.ADDR_POSTCODE_TAG, TagUtils.getAddrPostcodeValue(r));
                    hasValuesFromRel = true;
                }
            }
        }
        return hasValuesFromRel;
    }

    /**
     * Gets the tag values from an address interpolation ref, if present.
     *
     * @param address The address
     * @return true, if house numbers are given via address interpolation; otherwise false.
     */
    public static boolean getValuesFromAddressInterpolation(OSMAddress address) {
        if (address == null) return false;

        OsmPrimitive osmAddr = address.getOsmObject();

        for (OsmPrimitive osm : osmAddr.getReferrers()) {
            if (osm instanceof Way) {
                Way w = (Way) osm;
                if (TagUtils.hasAddrInterpolationTag(w)) {
                    applyDerivedValue(address, w, TagConstants.ADDR_POSTCODE_TAG);
                    applyDerivedValue(address, w, TagConstants.ADDR_CITY_TAG);
                    applyDerivedValue(address, w, TagConstants.ADDR_COUNTRY_TAG);
                    applyDerivedValue(address, w, TagConstants.ADDR_STREET_TAG);
                    applyDerivedValue(address, w, TagConstants.ADDR_STATE_TAG);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the local code as string.
     *
     * @return the string representation of the local.
     */
    public static String getLocale() {
        // Check if user could prefer imperial system
        if (cachedLocale == null) {
            Locale l = Locale.getDefault();
            cachedLocale = l.toString();
        }
        return cachedLocale;
    }

    /**
     * Zooms to the given addresses.
     *
     * @param addressList the address list
     */
    public static void zoomAddresses(List<OSMAddress> addressList) {
        CheckParameterUtil.ensureParameterNotNull(addressList, "addressList");

        if (!MainApplication.isDisplayingMapView()) return;   // nothing to do
        if (addressList.size() == 0) return;                  // dto.

        // compute bounding box
        BoundingXYVisitor bbox = new BoundingXYVisitor();
        for (OSMAddress source : addressList) {
            OsmPrimitive osm = source.getOsmObject();
            Bounds b = new Bounds(osm.getBBox().getTopLeft(), osm.getBBox().getBottomRight());
            bbox.visit(b);
        }

        if (bbox.getBounds() != null) {
            //  zoom to calculated bounding box
            MainApplication.getMap().mapView.zoomTo(bbox.getBounds());
        }
    }

    /**
     * Helper method to set a derived value of an address node.
     *
     * @param address The address to change
     * @param w the way containing the tag for the derived value.
     * @param tag the tag to set as applied value.
     */
    private static void applyDerivedValue(OSMAddress address, Way w, String tag) {
        CheckParameterUtil.ensureParameterNotNull(address, "address");
        CheckParameterUtil.ensureParameterNotNull(w, "way");

        if (!address.hasTag(tag) && TagUtils.hasTag(w, tag)) {
            address.setDerivedValue(tag, w.get(tag));
        }
    }
}
