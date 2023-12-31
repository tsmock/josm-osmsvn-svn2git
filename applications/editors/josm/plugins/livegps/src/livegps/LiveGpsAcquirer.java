// License: Public Domain. For details, see LICENSE file.
package livegps;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

/**
 * Acquires NMEA data from a GPSD
 */
public class LiveGpsAcquirer implements Runnable {
    private final String gpsdHost;
    private final int gpsdPort;

    private Socket gpsdSocket;
    private BufferedReader gpsdReader;
    private boolean connected;
    private boolean shutdownFlag;
    private boolean JSONProtocol = true;
    private long skipTime;
    private int skipNum;

    private final List<PropertyChangeListener> propertyChangeListener = new ArrayList<>();
    private PropertyChangeEvent lastStatusEvent;
    private PropertyChangeEvent lastDataEvent;

    /**
     * Constructor, initializes the configurable settings.
     */
    public LiveGpsAcquirer() {

        gpsdHost = Config.getPref().get(LiveGPSPreferences.C_HOST, LiveGPSPreferences.DEFAULT_HOST);
        gpsdPort = Config.getPref().getInt(LiveGPSPreferences.C_PORT, LiveGPSPreferences.DEFAULT_PORT);
        // put the settings back in to the preferences, makes keys appear.
        Config.getPref().put(LiveGPSPreferences.C_HOST, gpsdHost);
        Config.getPref().putInt(LiveGPSPreferences.C_PORT, gpsdPort);
    }

    /**
     * Adds a property change listener to the acquirer.
     * @param listener the new listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!propertyChangeListener.contains(listener)) {
            propertyChangeListener.add(listener);
        }
    }

    /**
     * Remove a property change listener from the acquirer.
     * @param listener the new listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeListener.contains(listener)) {
            propertyChangeListener.remove(listener);
        }
    }

    /**
     * Fire a gps status change event. Fires events with key "gpsstatus" and a {@link LiveGpsStatus}
     * object as value.
     * The status event may be sent any time.
     * @param status the status.
     * @param statusMessage the status message.
     */
    public void fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus status, String statusMessage) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, "gpsstatus",
                null, new LiveGpsStatus(status, statusMessage));

        if (!event.equals(lastStatusEvent)) {
            firePropertyChangeEvent(event);
            lastStatusEvent = event;
        }
    }

    /**
     * Fire a gps data change event to all listeners. Fires events with key "gpsdata" and a
     * {@link LiveGpsData} object as values.
     * This event is only sent, when the suppressor permits it. This
     * event will cause the UI to re-draw itself, which has some performance penalty,
     * @param oldData the old gps data.
     * @param newData the new gps data.
     */
    public void fireGpsDataChangeEvent(LiveGpsData oldData, LiveGpsData newData) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, "gpsdata", oldData, newData);

        if (!event.equals(lastDataEvent)) {
            firePropertyChangeEvent(event);
            lastDataEvent = event;
        }
    }

    /**
     * Fires the given event to all listeners.
     * @param event the event to fire.
     */
    protected void firePropertyChangeEvent(PropertyChangeEvent event) {
        for (PropertyChangeListener listener : propertyChangeListener) {
            listener.propertyChange(event);
        }
    }

    @Override
    public void run() {
        LiveGpsData oldGpsData = null;
        LiveGpsData gpsData;

        shutdownFlag = false;
        while (!shutdownFlag) {

            while (!connected && !shutdownFlag) {
                try {
                    connect();
                } catch (IOException iox) {
                    Logging.trace(iox);
                    fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.CONNECTION_FAILED, tr("Connection Failed"));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        Logging.trace(interruptedException);
                        Thread.currentThread().interrupt();
                    }
                }
            }
            if (shutdownFlag)
              break;

            assert connected;

            try {
                String line;

                // <FIXXME date="23.06.2007" author="cdaller">
                // TODO this read is blocking if gps is connected but has no
                // fix, so gpsd does not send positions
                line = gpsdReader.readLine();
                // </FIXXME>
                if (line == null)
                    throw new IOException();

                if (JSONProtocol)
                    gpsData = parseJSON(line);
                else
                    gpsData = parseOld(line);

                if (gpsData == null)
                    continue;

                fireGpsDataChangeEvent(oldGpsData, gpsData);
                oldGpsData = gpsData;
            } catch (IOException iox) {
                Logging.log(Logging.LEVEL_WARN, "LiveGps: lost connection to gpsd", iox);
                fireGpsStatusChangeEvent(
                        LiveGpsStatus.GpsStatus.CONNECTION_FAILED,
                        tr("Connection Failed"));
                disconnect();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    Logging.trace(interruptedException);
                    Thread.currentThread().interrupt();
                }
                // send warning to layer
            }
        }

        Logging.info("LiveGps: Disconnected from gpsd");
        fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.DISCONNECTED,
                tr("Not connected"));
        disconnect();
    }

    public void shutdown() {
        Logging.info("LiveGps: Shutdown gpsd");
        shutdownFlag = true;
    }

    private void connect() throws IOException {
        JsonObject greeting;
        String line, type, release;

        long t = System.currentTimeMillis();
        if (skipTime == 0 || t > skipTime) {
            skipTime = 0;
            Logging.info("LiveGps: trying to connect to gpsd at " + gpsdHost + ":"
                + gpsdPort + (skipNum != 0 ? " (skipped " + skipNum + " notices)" : ""));
            skipNum = 0;
        } else {
          ++skipNum;
        }
        fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.CONNECTING, tr("Connecting"));

        InetAddress[] addrs = InetAddress.getAllByName(gpsdHost);
        for (int i = 0; i < addrs.length && gpsdSocket == null; i++) {
            try {
                gpsdSocket = new Socket(addrs[i], gpsdPort);
                break;
            } catch (IOException e) {
                if (skipTime == 0) {
                    Logging.warn("LiveGps: Could not open connection to gpsd ("+addrs[i]+"): " + e);
                }
                gpsdSocket = null;
            }
        }

        if (gpsdSocket == null || !gpsdSocket.isConnected()) {
            if (skipTime == 0)
                skipTime = System.currentTimeMillis()+60000;
            throw new IOException();
        }
        skipTime = 0;
        skipNum = 0;

        /*
         * First emit the "w" symbol. The older version will activate, the newer one will ignore it.
         */
        gpsdSocket.getOutputStream().write(new byte[] {'w', 13, 10});

        gpsdReader = new BufferedReader(new InputStreamReader(gpsdSocket.getInputStream(), StandardCharsets.UTF_8));
        line = gpsdReader.readLine();
        if (line == null)
            return;

        try (JsonReader reader = Json.createReader(new StringReader(line))) {
            greeting = reader.readObject();
            type = greeting.getString("class");
            if ("VERSION".equals(type)) {
                release = greeting.getString("release");
                Logging.info("LiveGps: Connected to gpsd " + release);
            } else
                Logging.info("LiveGps: Unexpected JSON in gpsd greeting: " + line);
        } catch (JsonException jex) {
            if (line.startsWith("GPSD,")) {
                connected = true;
                JSONProtocol = false;
                Logging.info("LiveGps: Connected to old gpsd protocol version.");
                fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.CONNECTED, tr("Connected"));
            }
            Logging.trace(jex);
        }

        if (JSONProtocol) {
            JsonObject watch = Json.createObjectBuilder()
                    .add("enable", true)
                    .add("json", true)
                    .build();

            String request = "?WATCH=" + watch.toString() + ";\n";
            gpsdSocket.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));

            connected = true;
            fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.CONNECTED, tr("Connected"));
        }
    }

    private void disconnect() {
        assert gpsdSocket != null;

        connected = false;

        try {
            gpsdSocket.close();
            gpsdSocket = null;
        } catch (IOException e) {
            Logging.warn("LiveGps: Unable to close socket; reconnection may not be possible");
            Logging.trace(e);
        }
    }

    private static LiveGpsData parseJSON(String line) {
        JsonObject report;
        double lat, lon;
        float speed = 0;
        float course = 0;
        float epx = 0;
        float epy = 0;

        try (JsonReader reader = Json.createReader(new StringReader(line))) {
            report = reader.readObject();
        } catch (JsonException jex) {
            Logging.warn("LiveGps: line read from gpsd is not a JSON object:" + line);
            Logging.trace(jex);
            return null;
        }
        if (!"TPV".equals(report.getString("class")) || report.getInt("mode") < 2)
            return null;

        JsonNumber latJson = report.getJsonNumber("lat");
        JsonNumber lonJson = report.getJsonNumber("lon");
        if (latJson == null || lonJson == null)
            return null;
        lat = latJson.doubleValue();
        lon = lonJson.doubleValue();

        JsonNumber epxJson = report.getJsonNumber("epx");
        JsonNumber epyJson = report.getJsonNumber("epy");
        JsonNumber speedJson = report.getJsonNumber("speed");
        JsonNumber trackJson = report.getJsonNumber("track");

        if (speedJson != null)
            speed = (float) speedJson.doubleValue();
        if (trackJson != null)
            course = (float) trackJson.doubleValue();
        if (epxJson != null)
            epx = (float) epxJson.doubleValue();
        if (epyJson != null)
            epy = (float) epyJson.doubleValue();

        return new LiveGpsData(lat, lon, course, speed, epx, epy);
    }

    private static LiveGpsData parseOld(String line) {
        String[] words;
        double lat;
        double lon;
        float speed = 0;
        float course = 0;

        words = line.split(",");
        if ((words.length == 0) || !"GPSD".equals(words[0]))
            return null;

        for (int i = 1; i < words.length; i++) {
            if ((words[i].length() < 2) || (words[i].charAt(1) != '=')) {
                // unexpected response.
                continue;
            }

            char what = words[i].charAt(0);
            String value = words[i].substring(2);
            switch (what) {
            case 'O':
                // full report, tab delimited.
                String[] status = value.split("\\s+");
                if (status.length >= 5) {
                    lat = Double.parseDouble(status[3]);
                    lon = Double.parseDouble(status[4]);
                    try {
                        speed = Float.parseFloat(status[9]);
                        course = Float.parseFloat(status[8]);
                    } catch (NumberFormatException nex) {
                        Logging.debug(nex);
                    }
                    return new LiveGpsData(lat, lon, course, speed);
                }
                break;
            case 'P':
                // position report, tab delimited.
                String[] pos = value.split("\\s+");
                if (pos.length >= 2) {
                    lat = Double.parseDouble(pos[0]);
                    lon = Double.parseDouble(pos[1]);
                    speed = Float.NaN;
                    course = Float.NaN;
                    return new LiveGpsData(lat, lon, course, speed);
                }
                break;
            default:
                // not interested
            }
        }

        return null;
    }
}
