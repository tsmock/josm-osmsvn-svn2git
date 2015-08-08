package org.openstreetmap.josm.plugins.imagerycache;

import java.io.File;
import java.util.Map;

import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.josm.data.imagery.CachedTileLoaderFactory;
import org.openstreetmap.josm.data.imagery.TileLoaderFactory;
import org.openstreetmap.josm.gui.layer.TMSLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * @author Alexei Kasatkin
 */
public class ImageryCachePlugin extends Plugin {
    
    TileLoaderFactory factory = new TileLoaderFactory() {
        @Override
        public OsmTileLoader makeTileLoader(TileLoaderListener listener, Map<String, String> headers) {
            String cachePath = CachedTileLoaderFactory.PROP_TILECACHE_DIR.get();
            try {
                new File(cachePath).mkdirs();
            } catch (Exception e) {
                cachePath=".";
            }
            
            if (cachePath != null && !cachePath.isEmpty()) {
                return new OsmDBTilesLoader(listener, new File(cachePath), headers);
            }
            return null;
        }

        @Override
        public TileLoader makeTileLoader(TileLoaderListener listener) {
            return makeTileLoader(listener, null);
        }
    };

    public ImageryCachePlugin(PluginInformation info) {
        super(info);
        TMSLayer.setTileLoaderFactory(factory);
    }
    
    public static void main(String[] args) {
        System.out.println("Debugging code for ImageryAdjust plugin");
    }
}
