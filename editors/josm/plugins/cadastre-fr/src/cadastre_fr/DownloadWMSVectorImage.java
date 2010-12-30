// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;

public class DownloadWMSVectorImage extends PleaseWaitRunnable {

    private WMSLayer wmsLayer;
    private Bounds bounds;
    private CadastreGrabber grabber = CadastrePlugin.cadastreGrabber;
    private static String errorMessage;

    public DownloadWMSVectorImage(WMSLayer wmsLayer, Bounds bounds) {
        super(tr("Downloading {0}", wmsLayer.getName()));

        this.wmsLayer = wmsLayer;
        this.bounds = bounds;
    }

    @Override
    public void realRun() throws IOException {
        progressMonitor.indeterminateSubTask(tr("Contacting WMS Server..."));
        errorMessage = null;
        try {
            if (grabber.getWmsInterface().retrieveInterface(wmsLayer)) {
                if (wmsLayer.images.isEmpty()) {
                    // first time we grab an image for this layer
                    if (CacheControl.cacheEnabled) {
                        if (wmsLayer.getCacheControl().loadCacheIfExist()) {
                            Main.map.mapView.zoomTo(wmsLayer.getCommuneBBox().toBounds());
                            //Main.map.mapView.repaint();
                            return;
                        }
                    }
                    if (wmsLayer.isRaster()) {
                        // set raster image commune bounding box based on current view (before adjustment)
                        JOptionPane.showMessageDialog(Main.parent,tr("This commune is not vectorized.\nPlease use the other menu entry to georeference a \"Plan image\""));
                        Main.main.removeLayer(wmsLayer);
                        wmsLayer = null;
                        return;
                    } else {
                        // set vectorized commune bounding box by opening the standard web window
                        grabber.getWmsInterface().retrieveCommuneBBox(wmsLayer);
                    }
                }
                // grab new images from wms server into active layer
                wmsLayer.grab(grabber, bounds);
            }
        } catch (DuplicateLayerException e) {
            // we tried to grab onto a duplicated layer (removed)
            System.err.println("removed a duplicated layer");
        } catch (WMSException e) {
            errorMessage = e.getMessage();
            grabber.getWmsInterface().resetCookie();
        }
    }

    @Override
    protected void cancel() {
        grabber.getWmsInterface().cancel();
        if (wmsLayer != null)
            wmsLayer.cancelled = true;
    }

    @Override
    protected void finish() {
    }

    public static void download(WMSLayer wmsLayer) {
        MapView mv = Main.map.mapView;
        Bounds bounds = new Bounds(mv.getLatLon(0, mv.getHeight()), mv.getLatLon(mv.getWidth(), 0));

        Main.worker.execute(new DownloadWMSVectorImage(wmsLayer, bounds));
        if (errorMessage != null)
            JOptionPane.showMessageDialog(Main.parent, errorMessage);
    }
}
