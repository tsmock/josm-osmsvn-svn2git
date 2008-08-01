package org.openstreetmap.josm.plugins.remotecontrol;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.download.DownloadDialog.DownloadTask;

/**
 * Processes HTTP "remote control" requests.
 */
public class RequestProcessor extends Thread 
{
    /** The socket this processor listens on */
    private Socket request;
  
    /**
     * Constructor
     * 
     * @param request The WMS request
     */
    public RequestProcessor(Socket request) 
    {
        super("RemoteControl request processor");
        this.setDaemon(true);
        this.request = request;
    }
  
    /**
     * Spawns a new thread for the request
     * 
     * @param request The WMS request
     */
    public static void processRequest(Socket request) 
    {
        RequestProcessor processor = new RequestProcessor(request);
        processor.start();
    }  
  
    /**
     * The work is done here.
     */
    public void run() 
    {
        Writer out = null;
        try 
        {            
            OutputStream raw = new BufferedOutputStream( request.getOutputStream());         
            out = new OutputStreamWriter(raw);
            Reader in = new InputStreamReader(new BufferedInputStream(request.getInputStream()), "ASCII");
            
            StringBuffer requestLine = new StringBuffer();
            while (true) 
            {
                int c = in.read();
                if (c == '\r' || c == '\n') break;
                requestLine.append((char) c);
            }
            
            System.out.println("RemoteControl received: " + requestLine);
            String get = requestLine.toString();
            StringTokenizer st = new StringTokenizer(get);
            String method = st.nextToken();
            String url = st.nextToken();

            if( !method.equals("GET") )
            {
                sendNotImplemented(out);
                return;
            }

            st = new StringTokenizer(url, "&?");
            String command = null; 
            HashMap<String,String> args = new HashMap<String,String>();
            while (st.hasMoreTokens())
            {
                String param = st.nextToken();
                if (command == null) 
                {
                    command = param;
                } 
                else
                {
                    int eq = param.indexOf("=");
                    if (eq>-1)
                        args.put(param.substring(0,eq), param.substring(eq+1));
                }
            }
            
            if (command.equals("/load_and_zoom")) {
                DownloadTask osmTask = new DownloadOsmTask();
                if (!(args.containsKey("bottom") && args.containsKey("top") && 
                    args.containsKey("left") && args.containsKey("right"))) {
                    sendError(out);
                    System.out.println("load_and_zoom remote control request must have bottom,top,left,right parameters");
                    return;	
                }
                try {
                    double minlat = Double.parseDouble(args.get("bottom"));
                    double maxlat = Double.parseDouble(args.get("top"));
                    double minlon = Double.parseDouble(args.get("left"));
                    double maxlon = Double.parseDouble(args.get("right"));
                    osmTask.download(Main.main.menu.download, minlat,minlon,maxlat,maxlon);
                }
                catch (Exception ex)
                {
                    sendError(out);
                    System.out.println("RemoteControl: Error parsing load_and_zoom remote control request:");
                    ex.printStackTrace();
                    return;
                }
                if (args.containsKey("select")) {
                    final String selection = args.get("select");
                    Main.worker.execute(new Runnable() {
                        public void run() {
                            HashSet<Long> ways = new HashSet<Long>();
                            HashSet<Long> nodes = new HashSet<Long>();
                            HashSet<Long> relations = new HashSet<Long>();
                            HashSet<OsmPrimitive> newSel = new HashSet<OsmPrimitive>();
                            for (String item : selection.split(",")) {
                                if (item.startsWith("way"))	{
                                    ways.add(Long.parseLong(item.substring(3)));
                                } else if (item.startsWith("node")) {
                                    nodes.add(Long.parseLong(item.substring(4)));
                                } else if (item.startsWith("relation")) {
                                    relations.add(Long.parseLong(item.substring(8)));
                                } else {
                                    System.out.println("RemoteControl: invalid selection '"+item+"' ignored");
                                }
                            }
                            for (Way w : Main.ds.ways) if (ways.contains(w.id)) newSel.add(w);
                            for (Node n : Main.ds.nodes) if (nodes.contains(n.id)) newSel.add(n);
                            for (Relation r : Main.ds.relations) if (relations.contains(r.id)) newSel.add(r);	
                            Main.ds.setSelected(newSel);
                        }
                    });
                };
            }
            sendHeader(out, "200 OK", "text/plain", false);
            out.write("Content-length: 4\r\n");
            out.write("\r\n");
            out.write("OK\r\n");
            out.flush();
        }
        catch (IOException ioe) { }
        catch(Exception e)
        {
            e.printStackTrace();
            try 
            {
                sendError(out);
            } 
            catch (IOException e1) { }
        }
        finally 
        {
            try 
            {
                request.close();        
            }
            catch (IOException e) {} 
        }
    }

    /**
     * Sends a 500 error: server error
     * @param out The writer where the error is written
     * @throws IOException If the error can not be written
     */
    private void sendError(Writer out) throws IOException
    {
        sendHeader(out, "500 Internal Server Error", "text/html", true);
        out.write("<HTML>\r\n");
        out.write("<HEAD><TITLE>Internal Error</TITLE>\r\n");
        out.write("</HEAD>\r\n");
        out.write("<BODY>");
        out.write("<H1>HTTP Error 500: Internal Server Error</h2>\r\n");
        out.write("</BODY></HTML>\r\n");
        out.flush();
    }

    /**
     * Sends a 501 error: not implemented
     * @param out The writer where the error is written
     * @throws IOException If the error can not be written
     */
    private void sendNotImplemented(Writer out) throws IOException
    {
        sendHeader(out, "501 Not Implemented", "text/html", true);
        out.write("<HTML>\r\n");
        out.write("<HEAD><TITLE>Not Implemented</TITLE>\r\n");
        out.write("</HEAD>\r\n");
        out.write("<BODY>");
        out.write("<H1>HTTP Error 501: Not Implemented</h2>\r\n");
        out.write("</BODY></HTML>\r\n");
        out.flush();
    }
    
    /**
     * Send common HTTP headers to the client.
     * 
     * @param out The Writer
     * @param status The status string ("200 OK", "500", etc)
     * @param contentType The content type of the data sent
     * @param endHeaders If true, adds a new line, ending the headers.
     * @throws IOException When error
     */
    private void sendHeader(Writer out, String status, String contentType, boolean endHeaders) throws IOException
    {
        out.write("HTTP/1.1 " + status + "\r\n");
        Date now = new Date();
        out.write("Date: " + now + "\r\n");
        out.write("Server: JOSM RemoteControl\r\n");
        out.write("Content-type: " + contentType + "\r\n");
        if (endHeaders)
            out.write("\r\n");
    }
}
