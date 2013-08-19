/**
 * 
 */
package org.openstreetmap.gui.jmapviewer.tilesources;

import java.awt.Image;

import org.openstreetmap.gui.jmapviewer.Coordinate;

public abstract class AbstractOsmTileSource extends AbstractTMSTileSource {
    
    /**
     * The OSM attribution. Must be always in line with <a href="http://www.openstreetmap.org/copyright/en">http://www.openstreetmap.org/copyright/en</a>
     */
    public static final String DEFAULT_OSM_ATTRIBUTION = "\u00a9 OpenStreetMap contributors";
    
    public AbstractOsmTileSource(String name, String base_url) {
        super(name, base_url);
    }

    public int getMaxZoom() {
        return 18;
    }

    @Override
    public boolean requiresAttribution() {
        return true;
    }

    @Override
    public String getAttributionText(int zoom, Coordinate topLeft, Coordinate botRight) {
        return DEFAULT_OSM_ATTRIBUTION;
    }

    @Override
    public String getAttributionLinkURL() {
        return "http://openstreetmap.org/";
    }

    @Override
    public Image getAttributionImage() {
        return null;
    }

    @Override
    public String getAttributionImageURL() {
        return null;
    }

    @Override
    public String getTermsOfUseText() {
        return null;
    }

    @Override
    public String getTermsOfUseURL() {
        return "http://www.openstreetmap.org/copyright";
    }
}
