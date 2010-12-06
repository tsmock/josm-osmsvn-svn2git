package org.openstreetmap.josm.plugins.imagery.wms;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.io.CacheFiles;
import org.openstreetmap.josm.plugins.imagery.ImageryInfo.ImageryType;
import org.openstreetmap.josm.plugins.imagery.wms.io.WMSLayerExporter;
import org.openstreetmap.josm.plugins.imagery.wms.io.WMSLayerImporter;

// WMSPlugin-specific functions
public class WMSAdapter {
    CacheFiles cache = new CacheFiles("wmsplugin");

    public final StringProperty PROP_BROWSER = new StringProperty("imagery.wms.browser", "webkit-image {0}");
    public final IntegerProperty PROP_SIMULTANEOUS_CONNECTIONS = new IntegerProperty("imagery.wms.simultaneousConnections", 3);
    public final BooleanProperty PROP_OVERLAP = new BooleanProperty("imagery.wms.overlap", false);
    public final IntegerProperty PROP_OVERLAP_EAST = new IntegerProperty("imagery.wms.overlapEast", 14);
    public final IntegerProperty PROP_OVERLAP_NORTH = new IntegerProperty("imagery.wms.overlapNorth", 4);

    protected void initExporterAndImporter() {
        ExtensionFileFilter.exporters.add(new WMSLayerExporter());
        ExtensionFileFilter.importers.add(new WMSLayerImporter());
    }

    public WMSAdapter() {
        cache.setExpire(CacheFiles.EXPIRE_MONTHLY, false);
        cache.setMaxSize(70, false);
        initExporterAndImporter();
    }

    public Grabber getGrabber(MapView mv, WMSLayer layer){
        if(layer.getInfo().getImageryType() == ImageryType.HTML)
            return new HTMLGrabber(mv, layer, cache);
        else if(layer.getInfo().getImageryType() == ImageryType.WMS)
            return new WMSGrabber(mv, layer, cache);
        else throw new IllegalStateException("WMSAdapter.getGrabber() called for non-WMS layer type");
    }

}
