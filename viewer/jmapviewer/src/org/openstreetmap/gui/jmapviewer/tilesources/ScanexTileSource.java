// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

import java.util.Random;

import org.openstreetmap.gui.jmapviewer.OsmMercator;

/**
 * This tilesource uses different to OsmMercator projection.
 *
 * Earth is assumed an ellipsoid in this projection, unlike
 * sphere in OsmMercator, so latitude calculation differs a lot.
 *
 * The longitude calculation is the same as in OsmMercator,
 * we inherit it from AbstractTMSTileSource.
 *
 * TODO: correct getDistance() method.
 */
public class ScanexTileSource extends TMSTileSource {
    private static final String DEFAULT_URL = "http://maps.kosmosnimki.ru";
    private static final int DEFAULT_MAXZOOM = 14;
    private static final String API_KEY = "4018C5A9AECAD8868ED5DEB2E41D09F7";

    // Latitude to Y and back calculations.

    /** radius of Earth at equator, m */
    private static double RADIUS_E = 6378137;
    /** equator length, m */
    private static double EQUATOR = 40075016.68557849;
    /** eccentricity of Earth's ellipsoid */
    private static double E = 0.0818191908426;

    private enum ScanexLayer {
        IRS("irs", "/TileSender.ashx?ModeKey=tile&MapName=F7B8CF651682420FA1749D894C8AD0F6&LayerName=BAC78D764F0443BD9AF93E7A998C9F5B"),
        SPOT("spot", "/TileSender.ashx?ModeKey=tile&MapName=F7B8CF651682420FA1749D894C8AD0F6&LayerName=F51CE95441284AF6B2FC319B609C7DEC");

        private String name;
        private String uri;

        ScanexLayer(String name, String uri) {
            this.name = name;
            this.uri = uri;
        }

        public String getName() {
            return name;
        }

        public String getUri() {
            return uri;
        }
    }

    /** IRS by default */
    private ScanexLayer layer = ScanexLayer.IRS;

    /** cached latitude used in {@link #tileYToLat(double, int)} */
    private double cachedLat;

    /**
     * Constructs a new {@code ScanexTileSource}.
     * @param info tile source info
     */
    public ScanexTileSource(TileSourceInfo info) {
        super(info);
        String url = info.getUrl();

        for (ScanexLayer slayer : ScanexLayer.values()) {
            if (url.equalsIgnoreCase(slayer.getName())) {
                this.layer = slayer;
                // Override baseUrl and maxZoom in base class.
                this.baseUrl = DEFAULT_URL;
                if (maxZoom == 0)
                    this.maxZoom = DEFAULT_MAXZOOM;
                break;
            }
        }
    }

    @Override
    public String getExtension() {
        return "jpeg";
    }

    @Override
    public String getTilePath(int zoom, int tilex, int tiley) {
        int tmp = (int) Math.pow(2.0, zoom - 1);

        tilex = tilex - tmp;
        tiley = tmp - tiley - 1;

        return this.layer.getUri() + "&apikey=" + API_KEY + "&x=" + tilex + "&y=" + tiley + "&z=" + zoom;
    }

    /*
     * To solve inverse formula latitude = f(y) we use
     * Newton's method. We cache previous calculated latitude,
     * because new one is usually close to the old one. In case
     * if solution gets out of bounds, we reset to a new random value.
     */
    private double tileYToLat(double y, int zoom) {
        double lat0;
        double lat = cachedLat;
        do {
            lat0 = lat;
            lat = lat - Math.toDegrees(nextTerm(Math.toRadians(lat), y, zoom));
            if (lat > OsmMercator.MAX_LAT || lat < OsmMercator.MIN_LAT) {
                Random r = new Random();
                lat = OsmMercator.MIN_LAT +
                  r.nextInt((int) (OsmMercator.MAX_LAT - OsmMercator.MIN_LAT));
            }
        } while (Math.abs(lat0 - lat) > 0.000001);

        cachedLat = lat;

        return lat;
    }

    /* Next term in Newton's polynomial */
    private static double nextTerm(double lat, double y, int zoom) {
        double sinl = Math.sin(lat);
        double cosl = Math.cos(lat);

        zoom = (int) Math.pow(2.0, zoom - 1);
        double ec = Math.exp((1 - y/zoom)*Math.PI);

        double f = Math.tan(Math.PI/4+lat/2) -
            ec * Math.pow(Math.tan(Math.PI/4 + Math.asin(E * sinl)/2), E);
        double df = 1/(1 - sinl) - ec * E * cosl/((1 - E * sinl) *
            (Math.sqrt(1 - E * E * sinl * sinl)));

        return f/df;
    }
}
