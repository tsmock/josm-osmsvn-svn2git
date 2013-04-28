package geochat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.CoordinateFormat;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapView;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * This class holds all the chat data and periodically polls the server.
 * 
 * @author zverik
 */
class ChatServerConnection {
    private int userId;
    private String userName;
    private static ChatServerConnection instance;
    private Set<ChatServerConnectionListener> listeners;
    private LogRequest requestThread;

    private ChatServerConnection() {
        userId = 0;
        userName = null;
        listeners = new HashSet<ChatServerConnectionListener>();
        requestThread = new LogRequest();
        new Thread(requestThread).start();
        checkLogin();
    }
    
    public static ChatServerConnection getInstance() {
        if( instance == null )
            instance = new ChatServerConnection();
        return instance;
    }

    public void addListener( ChatServerConnectionListener listener ) {
        listeners.add(listener);
    }

    public void removeListener( ChatServerConnectionListener listener ) {
        listeners.remove(listener);
    }

    public boolean isActive() {
        return isLoggedIn() && getPosition() != null;
    }

    public boolean isLoggedIn() {
        return userId > 0;
    }

    public String getUserName() {
        return userName;
    }

    /**
     * Test that userId is still active, log out otherwise.
     */
    public void checkLogin() {
        final int uid = Main.pref.getInteger("geochar.lastuid", 0);
        if( uid <= 0 ) return;
        String query = "whoami&uid=" + uid;
        JsonQueryUtil.queryAsync(query, new JsonQueryCallback() {
            public void processJson( JSONObject json ) {
                if( json != null && json.has("name") ) {
                    login(uid, json.getString("name"));
                }
            }
        });
    }

    public void login( final String userName ) {
        if( userName == null )
            throw new IllegalArgumentException("userName is null");
        LatLon pos = getPosition();
        if( pos == null ) {
            fireLoginFailed("Position is unknown");
            return;
        }
        try {
            String query = "register&lat=" + pos.latToString(CoordinateFormat.DECIMAL_DEGREES)
                    + "&lon=" + pos.lonToString(CoordinateFormat.DECIMAL_DEGREES)
                    + "&name=" + URLEncoder.encode(userName, "UTF8");
            JsonQueryUtil.queryAsync(query, new JsonQueryCallback() {
                public void processJson( JSONObject json ) {
                    if( json == null )
                        fireLoginFailed(tr("Could not get server response, check logs"));
                    else if( json.has("error") )
                        fireLoginFailed(tr("Failed to login as {0}:", userName) + "\n" + json.getString("error"));
                    else if( !json.has("uid") )
                        fireLoginFailed(tr("The server did not return user ID"));
                    else {
                        login(json.getInt("uid"), userName);
                    }
                }
            });
        } catch( UnsupportedEncodingException e ) {
            // wut
        }
    }

    private void login( int userId, String userName ) {
        this.userId = userId;
        this.userName = userName;
        Main.pref.putInteger("geochat.lastuid", userId);
        for( ChatServerConnectionListener listener : listeners )
            listener.loggedIn(userName);
    }

    private void fireLoginFailed( String reason ) {
        for( ChatServerConnectionListener listener : listeners )
            listener.notLoggedIn(reason);
    }

    /**
     * Unregister the current user.
     */
    public void logout() {
        if( !isLoggedIn() )
            return;
        String query = "logout&uid=" + userId;
        JsonQueryUtil.queryAsync(query, new JsonQueryCallback() {
            public void processJson( JSONObject json ) {
                if( json != null && json.has("message") ) {
                    ChatServerConnection.this.userId = 0;
                    ChatServerConnection.this.userName = null;
                    Main.pref.put("geochat.lastuid", null);
                }
            }
        });
    }

    private void fireMessageFailed( String reason ) {
        for( ChatServerConnectionListener listener : listeners )
            listener.messageSendFailed(reason);
    }

    /**
     * Posts message to the main channel.
     * @param message Message string.
     * @see #postMessage(java.lang.String, java.lang.String)
     */
    public void postMessage( String message ) {
        postMessage(message, null);
    }

    /**
     * Posts message to the main channel or to a specific user.
     * Calls listener on fail.
     * @param message Message string.
     * @param targetUser null if sending to everyone, name of user otherwise.
     */
    public void postMessage( String message, String targetUser ) {
        if( !isLoggedIn() ) {
            fireMessageFailed("Not logged in");
            return;
        }
        LatLon pos = getPosition();
        if( pos == null ) {
            fireMessageFailed("Position is unknown");
            return;
        }
        try {
            String query = "post&lat=" + pos.latToString(CoordinateFormat.DECIMAL_DEGREES)
                    + "&lon=" + pos.lonToString(CoordinateFormat.DECIMAL_DEGREES)
                    + "&uid=" + userId
                    + "&message=" + URLEncoder.encode(message, "UTF8");
            if( targetUser != null && targetUser.length() > 0)
                    query += "&to=" + URLEncoder.encode(targetUser, "UTF8");
            JsonQueryUtil.queryAsync(query, new JsonQueryCallback() {
                public void processJson( JSONObject json ) {
                    if( json == null )
                        fireMessageFailed(tr("Could not get server response, check logs"));
                    else if( json.has("error") )
                        fireMessageFailed(tr("Failed to send message:") + "\n" + json.getString("error"));
                }
            });
        } catch( UnsupportedEncodingException e ) {
            // wut
        }
    }
    
    /**
     * Returns current coordinates or null if there is no map, or zoom is too low.
     */
    private static LatLon getPosition() {
        if( Main.map == null || Main.map.mapView == null )
            return null;
        if( getCurrentZoom() < 10 )
            return null;
        Projection proj = Main.getProjection();
        return proj.eastNorth2latlon(Main.map.mapView.getCenter());
    }

    // Following three methods were snatched from TMSLayer
    private static double latToTileY( double lat, int zoom ) {
        double l = lat / 180 * Math.PI;
        double pf = Math.log(Math.tan(l) + (1 / Math.cos(l)));
        return Math.pow(2.0, zoom - 1) * (Math.PI - pf) / Math.PI;
    }

    private static double lonToTileX( double lon, int zoom ) {
        return Math.pow(2.0, zoom - 3) * (lon + 180.0) / 45.0;
    }

    public static int getCurrentZoom() {
        if( Main.map == null || Main.map.mapView == null ) {
            return 1;
        }
        MapView mv = Main.map.mapView;
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
        double x1 = lonToTileX(topLeft.lon(), 1);
        double y1 = latToTileY(topLeft.lat(), 1);
        double x2 = lonToTileX(botRight.lon(), 1);
        double y2 = latToTileY(botRight.lat(), 1);

        int screenPixels = mv.getWidth() * mv.getHeight();
        double tilePixels = Math.abs((y2 - y1) * (x2 - x1) * 256 * 256);
        if( screenPixels == 0 || tilePixels == 0 ) {
            return 1;
        }
        double factor = screenPixels / tilePixels;
        double result = Math.log(factor) / Math.log(2) / 2 + 1;
        int intResult = (int)Math.floor(result);
        return intResult;
    }

    private class LogRequest implements Runnable {
        private static final int MAX_JUMP = 20000; // in meters
        private LatLon lastPosition = null;
        private long lastUserId = 0;
        private long lastId = 0;
        private boolean lastStatus = false;
        private boolean stopping = false;

        public void run() {
            int interval = Main.pref.getInteger("geochat.interval", 3);
            while( !stopping ) {
                process();
                try {
                    Thread.sleep(interval * 1000);
                } catch( InterruptedException e ) {
                    stopping = true;
                }
            }
        }

        public void stop() {
            stopping = true;
        }

        public void process() {
            if( !isLoggedIn() ) {
                fireStatusChanged(false);
                return;
            }

            LatLon pos = getPosition();
            if( pos == null ) {
                fireStatusChanged(false);
                return;
            }
            fireStatusChanged(true);
            
            final boolean needReset;
            final boolean needFullReset = lastUserId != userId;
            if( needFullReset || (lastPosition != null && pos.greatCircleDistance(lastPosition) > MAX_JUMP) ) {
                // reset messages
                lastId = 0;
                needReset = true;
            } else
                needReset = false;
            lastUserId = userId;
            lastPosition = pos;

            String query = "get&lat=" + pos.latToString(CoordinateFormat.DECIMAL_DEGREES)
                    + "&lon=" + pos.lonToString(CoordinateFormat.DECIMAL_DEGREES)
                    + "&uid=" + userId + "&last=" + lastId;
            JsonQueryUtil.queryAsync(query, new JsonQueryCallback() {
                public void processJson( JSONObject json ) {
                    if( json == null )
                        fireLoginFailed(tr("Could not get server response, check logs"));
                    else if( json.has("error") )
                        fireLoginFailed(tr("Failed to login as {0}:", userName) + "\n" + json.getString("error"));
                    else {
                        if( json.has("users") ) {
                            Map<String, LatLon> users = parseUsers(json.getJSONArray("users"));
                            for( ChatServerConnectionListener listener : listeners )
                                listener.updateUsers(users);
                        }
                        if( json.has("messages") ) {
                            List<ChatMessage> messages = parseMessages(json.getJSONArray("messages"), false);
                            for( ChatMessage m : messages )
                                if( m.getId() > lastId )
                                    lastId = m.getId();
                            for( ChatServerConnectionListener listener : listeners )
                                listener.receivedMessages(needReset, messages);
                        }
                        if( json.has("private") ) {
                            List<ChatMessage> messages = parseMessages(json.getJSONArray("private"), true);
                            for( ChatMessage m : messages )
                                if( m.getId() > lastId )
                                    lastId = m.getId();
                            for( ChatServerConnectionListener listener : listeners )
                                listener.receivedPrivateMessages(needFullReset, messages);
                        }
                    }
                }
            });
        }

        private List<ChatMessage> parseMessages( JSONArray messages, boolean priv ) {
            List<ChatMessage> result = new ArrayList<ChatMessage>();
            for( int i = 0; i < messages.length(); i++ ) {
                try {
                    JSONObject msg = messages.getJSONObject(i);
                    long id = msg.getLong("id");
                    double lat = msg.getDouble("lat");
                    double lon = msg.getDouble("lon");
                    long timeStamp = msg.getLong("timestamp");
                    String author = msg.getString("author");
                    String message = msg.getString("message");
                    ChatMessage cm = new ChatMessage(id, new LatLon(lat, lon), author, message, new Date(timeStamp));
                    cm.setPrivate(priv);
                    result.add(cm);
                } catch( JSONException e ) {
                    // do nothing, just skip this message
                }
            }
            return result;
        }

        private Map<String, LatLon> parseUsers( JSONArray users ) {
            Map<String, LatLon> result = new HashMap<String, LatLon>();
            for( int i = 0; i < users.length(); i++ ) {
                try {
                    JSONObject user = users.getJSONObject(i);
                    String name = user.getString("user");
                    double lat = user.getDouble("lat");
                    double lon = user.getDouble("lon");
                    result.put(name, new LatLon(lat, lon));
                } catch( JSONException e ) {
                    // do nothing, just skip this user
                }
            }
            return result;
        }

        private void fireStatusChanged( boolean newStatus ) {
            if( newStatus == lastStatus )
                return;
            lastStatus = newStatus;
            for( ChatServerConnectionListener listener : listeners )
                listener.statusChanged(newStatus);
        }
    }
}
