// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.tools.Logging;

/**
 * Grab the SVG administrative boundaries of the active commune layer (cadastre),
 * isolate the SVG path of the concerned commune (other municipalities are also
 * downloaded in the SVG data), convert to OSM nodes and way plus simplify.
 * Thanks to Frederic Rodrigo for his help.
 */
public class DownloadSVGTask extends PleaseWaitRunnable {

    private WMSLayer wmsLayer;
    private CadastreInterface wmsInterface;
    private String svg;
    private EastNorthBound viewBox;
    private static String errorMessage;

    /**
     * Constructs a new {@code DownloadSVGTask}.
     */
    public DownloadSVGTask(WMSLayer wmsLayer) {
        super(tr("Downloading {0}", wmsLayer.getName()));

        this.wmsLayer = wmsLayer;
        this.wmsInterface = wmsLayer.grabber.getWmsInterface();
    }

    @Override
    public void realRun() throws IOException, OsmTransferException {
        progressMonitor.indeterminateSubTask(tr("Contacting WMS Server..."));
        errorMessage = null;
        try {
            if (wmsInterface.retrieveInterface(wmsLayer)) {
                svg = grabBoundary(wmsLayer.getCommuneBBox());
                if (svg == null)
                    return;
                progressMonitor.indeterminateSubTask(tr("Extract SVG ViewBox..."));
                getViewBox(svg);
                if (viewBox == null)
                    return;
                progressMonitor.indeterminateSubTask(tr("Extract best fitting boundary..."));
                createWay(svg);
            }
        } catch (DuplicateLayerException e) {
            Logging.warn("removed a duplicated layer");
        } catch (WMSException e) {
            errorMessage = e.getMessage();
            wmsLayer.grabber.getWmsInterface().resetCookie();
        }
    }

    @Override
    protected void cancel() {
        wmsLayer.grabber.getWmsInterface().cancel();
    }

    @Override
    protected void finish() {
        // Do nothing
    }

    private boolean getViewBox(String svg) {
        double[] box = new SVGParser().getViewBox(svg);
        if (box != null) {
            viewBox = new EastNorthBound(new EastNorth(box[0], box[1]),
                    new EastNorth(box[0]+box[2], box[1]+box[3]));
            return true;
        }
        Logging.warn("Unable to parse SVG data (viewBox)");
        return false;
    }

    /**
     *  The svg contains more than one commune boundary defined by path elements. So detect
     *  which path element is the best fitting to the viewBox and convert it to OSM objects
     */
    private void createWay(String svg) {
        String[] SVGpaths = new SVGParser().getClosedPaths(svg);
        ArrayList<Double> fitViewBox = new ArrayList<>();
        ArrayList<ArrayList<EastNorth>> eastNorths = new ArrayList<>();
        for (int i = 0; i < SVGpaths.length; i++) {
            ArrayList<EastNorth> eastNorth = new ArrayList<>();
            fitViewBox.add(createNodes(SVGpaths[i], eastNorth));
            eastNorths.add(eastNorth);
        }
        // the smallest fitViewBox indicates the best fitting path in viewBox
        Double min = Collections.min(fitViewBox);
        int bestPath = fitViewBox.indexOf(min);
        List<Node> nodeList = new ArrayList<>();
        for (EastNorth eastNorth : eastNorths.get(bestPath)) {
            nodeList.add(new Node(ProjectionRegistry.getProjection().eastNorth2latlon(eastNorth)));
        }
        Way wayToAdd = new Way();
        DataSet ds = OsmDataManager.getInstance().getEditDataSet();
        Collection<Command> cmds = new LinkedList<>();
        for (Node node : nodeList) {
            cmds.add(new AddCommand(ds, node));
            wayToAdd.addNode(node);
        }
        wayToAdd.addNode(wayToAdd.getNode(0)); // close the circle

        cmds.add(new AddCommand(ds, wayToAdd));
        UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Create boundary"), cmds));
        MainApplication.getMap().repaint();
    }

    private double createNodes(String SVGpath, ArrayList<EastNorth> eastNorth) {
        // looks like "M981283.38 368690.15l143.81 72.46 155.86 ..."
        String[] coor = SVGpath.split("[MlZ ]"); //coor[1] is x, coor[2] is y
        double dx = Double.parseDouble(coor[1]);
        double dy = Double.parseDouble(coor[2]);
        double minY = Double.MAX_VALUE;
        double minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxX = Double.MIN_VALUE;
        for (int i = 3; i < coor.length; i += 2) {
            double east = dx += Double.parseDouble(coor[i]);
            double north = dy += Double.parseDouble(coor[i+1]);
            eastNorth.add(new EastNorth(east, north));
            minX = minX > east ? east : minX;
            minY = minY > north ? north : minY;
            maxX = maxX < east ? east : maxX;
            maxY = maxY < north ? north : maxY;
        }
        // flip the image (svg using a reversed Y coordinate system)
        double pivot = viewBox.min.getY() + (viewBox.max.getY() - viewBox.min.getY()) / 2;
        for (int i = 0; i < eastNorth.size(); i++) {
            EastNorth en = eastNorth.get(i);
            eastNorth.set(i, new EastNorth(en.east(), 2 * pivot - en.north()));
        }
        return Math.abs(minX - viewBox.min.getX())+Math.abs(maxX - viewBox.max.getX())
        +Math.abs(minY - viewBox.min.getY())+Math.abs(maxY - viewBox.max.getY());
    }

    private String grabBoundary(EastNorthBound bbox) throws IOException, OsmTransferException {
        try {
            return grabSVG(getURLsvg(bbox));
        } catch (MalformedURLException e) {
            throw (IOException) new IOException(tr("CadastreGrabber: Illegal url.")).initCause(e);
        }
    }

    private static URL getURLsvg(EastNorthBound bbox) throws MalformedURLException {
        String str = CadastreInterface.BASE_URL+"/scpc/wms?version=1.1&request=GetMap";
        str += "&layers=";
        str += "CDIF:COMMUNE";
        str += "&format=image/svg";
        str += "&bbox="+bbox.min.east()+",";
        str += bbox.min.north() + ",";
        str += bbox.max.east() + ",";
        str += bbox.max.north();
        str += "&width="+CadastrePlugin.imageWidth+"&height="+CadastrePlugin.imageHeight;
        str += "&styles=";
        str += "COMMUNE_90";
        Logging.info("URL="+str);
        return new URL(str.replace(" ", "%20"));
    }

    private String grabSVG(URL url) throws IOException, OsmTransferException {
        File file = new File(CadastrePlugin.cacheDir + "boundary.svg");
        String svg = "";
        try (InputStream is = wmsInterface.getContent(url)) {
            if (file.exists())
                file.delete();
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true));
                 InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {
                String line;
                while (null != (line = br.readLine())) {
                    line += "\n";
                    bos.write(line.getBytes(StandardCharsets.UTF_8));
                    svg += line;
                }
            }
        } catch (IOException e) {
            Logging.error(e);
        }
        return svg;
    }

    public static void download(WMSLayer wmsLayer) {
        if (!CadastrePlugin.autoSourcing) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    tr("Please, enable auto-sourcing and check cadastre millesime."));
            return;
        }
        MainApplication.worker.execute(new DownloadSVGTask(wmsLayer));
        if (errorMessage != null)
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), errorMessage);
    }
}
