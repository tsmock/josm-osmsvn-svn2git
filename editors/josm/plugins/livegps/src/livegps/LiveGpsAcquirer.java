package livegps;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.lang.Float;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;

import org.json.JSONObject;
import org.json.JSONException;

public class LiveGpsAcquirer implements Runnable {
    private String gpsdHost;
    private int gpsdPort;

    private Socket gpsdSocket;
    private BufferedReader gpsdReader;
    private boolean connected = false;
    private boolean shutdownFlag = false;
    private boolean JSONProtocol = true;

    private final List<PropertyChangeListener> propertyChangeListener = new ArrayList<PropertyChangeListener>();
    private PropertyChangeEvent lastStatusEvent;
    private PropertyChangeEvent lastDataEvent;

    /**
     * Constructor, initializes the configurable settings.
     */
    public LiveGpsAcquirer() {
        super();

        gpsdHost = Main.pref.get("livegps.gpsd.host", "localhost");
        gpsdPort = Main.pref.getInteger("livegps.gpsd.port", 2947);
        // put the settings back in to the preferences, makes keys appear.
        Main.pref.put("livegps.gpsd.host", gpsdHost);
        Main.pref.putInteger("livegps.gpsd.port", gpsdPort);
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
    public void fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus status,
            String statusMessage) {
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
        PropertyChangeEvent event = new PropertyChangeEvent(this, "gpsdata",
                oldData, newData);

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

    public void run() {
        LiveGpsData oldGpsData = null;
        LiveGpsData gpsData = null;

        shutdownFlag = false;
        while (!shutdownFlag) {

            try {
                if (!connected)
                    connect();

                if (connected) {
                    String line;

                    // <FIXXME date="23.06.2007" author="cdaller">
                    // TODO this read is blocking if gps is connected but has no
                    // fix, so gpsd does not send positions
                    line = gpsdReader.readLine();
                    // </FIXXME>
                    if (line == null)
                        break;

                    if (JSONProtocol == true)
                        gpsData = ParseJSON(line);
                    else
                        gpsData = ParseOld(line);

                    if (gpsData == null)
                        continue;

                    fireGpsDataChangeEvent(oldGpsData, gpsData);
                    oldGpsData = gpsData;
                } else {
                    fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.DISCONNECTED, tr("Not connected"));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {}
                }
            } catch (IOException iox) {
                connected = false;
                if (gpsData != null) {
                    gpsData.setFix(false);
                    fireGpsDataChangeEvent(oldGpsData, gpsData);
                }
                fireGpsStatusChangeEvent(
                        LiveGpsStatus.GpsStatus.CONNECTION_FAILED,
                        tr("Connection Failed"));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {} ;
                // send warning to layer
            }
        }

        fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.DISCONNECTED,
                tr("Not connected"));
        if (gpsdSocket != null) {
            try {
                gpsdSocket.close();
                gpsdSocket = null;
                System.out.println("LiveGps: Disconnected from gpsd");
            } catch (Exception e) {
                System.out.println("LiveGps: Unable to close socket; reconnection may not be possible");
            }
        }
    }

    public void shutdown() {
        shutdownFlag = true;
    }

    private void connect() throws IOException {
        JSONObject greeting;
        String line, type, release;

        System.out.println("LiveGps: trying to connect to gpsd at " + gpsdHost + ":" + gpsdPort);
        fireGpsStatusChangeEvent( LiveGpsStatus.GpsStatus.CONNECTING, tr("Connecting"));

        InetAddress[] addrs = InetAddress.getAllByName(gpsdHost);
        for (int i = 0; i < addrs.length && gpsdSocket == null; i++) {
            try {
                gpsdSocket = new Socket(addrs[i], gpsdPort);
                break;
            } catch (Exception e) {
                System.out.println("LiveGps: Could not open connection to gpsd: " + e);
                gpsdSocket = null;
            }
        }

        if (gpsdSocket == null)
            return;

        fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.CONNECTING, tr("Connecting"));

        /*
         * First emit the "w" symbol. The older version will activate, the newer one will ignore it.
         */
        gpsdSocket.getOutputStream().write(new byte[] { 'w', 13, 10 });

        gpsdReader = new BufferedReader(new InputStreamReader(gpsdSocket.getInputStream()));
        line = gpsdReader.readLine();
        if (line == null)
            return;

        try {
            greeting = new JSONObject(line);
            type = greeting.getString("class");
            if (type.equals("VERSION")) {
                release = greeting.getString("release");
                System.out.println("LiveGps: Connected to gpsd " + release);
            } else
                System.out.println("LiveGps: Unexpected JSON in gpsd greeting: " + line);
        } catch (JSONException jex) {
            if (line.startsWith("GPSD,")) {
                connected = true;
                JSONProtocol = false;
                System.out.println("LiveGps: Connected to old gpsd protocol version.");
                fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.CONNECTED, tr("Connected"));
            }
        }

        if (JSONProtocol == true) {
            JSONObject Watch = new JSONObject();
            try { 
                Watch.put("enable", true);
                Watch.put("json", true);
            } catch (JSONException je) {};

            String Request = "?WATCH=" + Watch.toString() + ";\n";
            gpsdSocket.getOutputStream().write(Request.getBytes());

            connected = true;
            fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.CONNECTED, tr("Connected"));
        }
    }

    private LiveGpsData ParseJSON(String line) {
        JSONObject report;
        String type;
        double lat = 0;
        double lon = 0;
        float speed = 0;
        float course = 0;

        try {
            report = new JSONObject(line);
            type = report.getString("class");
        } catch (JSONException jex) {
            System.out.println("LiveGps: line read from gpsd is not a JSON object:" + line);
            return null;
        }
        if (!type.equals("TPV"))
            return null;

        try {
            lat = report.getDouble("lat");
            lon = report.getDouble("lon");
            speed = (new Float(report.getDouble("speed"))).floatValue();
            course = (new Float(report.getDouble("track"))).floatValue();

            return new LiveGpsData(lat, lon, course, speed, true);
        } catch (JSONException je) {}

        return null;
    }

    private LiveGpsData ParseOld(String line) {
        String words[];
        double lat = 0;
        double lon = 0;
        float speed = 0;
        float course = 0;

        words = line.split(",");
        if ((words.length == 0) || (!words[0].equals("GPSD")))
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
                    } catch (NumberFormatException nex) {}
                    return new LiveGpsData(lat, lon, course, speed, true);
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
                    return new LiveGpsData(lat, lon, course, speed, true);
                }
                break;
            default:
                // not interested
            }
        }

        return null;
    }
}
