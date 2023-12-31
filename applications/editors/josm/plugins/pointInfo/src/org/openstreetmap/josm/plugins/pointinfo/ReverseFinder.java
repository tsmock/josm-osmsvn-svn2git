// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.HttpClient;

/**
 * Class to access Nominatim Reverse Geocoding
 * @author Javier Sánchez Portero
 */
public final class ReverseFinder {

    public static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f";

    private ReverseFinder() {
    }

    /**
     * Performs a Nominatim search.
     * @param pos Coordinates to search
     * @return search results
     * @throws IOException if any IO error occurs.
     */
    public static ReverseRecord queryNominatim(LatLon pos) throws IOException {
        String request = String.format(Locale.ENGLISH, NOMINATIM_URL, pos.lat(), pos.lon());
        String result = HttpClient.create(URI.create(request).toURL()).connect().fetchContent();
        JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8)));
        JsonObject obj = jsonReader.readObject();
        jsonReader.close();
        return new ReverseRecord(obj);
    }
}
