// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.layers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

public class OdOsmDataLayer extends OsmDataLayer implements OdLayer {

    private final OdDataLayer dataLayer;

    public OdOsmDataLayer(OdDataLayer dataLayer, DataSet data, String name) {
        super(data, name, null);
        this.dataLayer = dataLayer;
    }

    public final void removeForbiddenTags() {
        if (dataLayer != null && dataLayer.handler != null && dataLayer.handler.hasForbiddenTags()) {
            MainApplication.worker.submit(() -> {
                data.clearSelection();
                for (Iterator<OsmPrimitive> it = data.allPrimitives().iterator(); it.hasNext();) {
                    OsmPrimitive p = it.next();
                    if (dataLayer.handler.isForbidden(p)) {
                        data.addSelected(p);

                        List<Node> nodes = null;
                        if (p instanceof Way) {
                            nodes = ((Way) p).getNodes();
                        }
                        if (nodes != null) {
                            for (Node n : nodes) {
                                List<OsmPrimitive> refferingAllowedWays = new ArrayList<>();
                                for (OsmPrimitive referrer : n.getReferrers()) {
                                    if (referrer instanceof Way && !dataLayer.handler.isForbidden(referrer)) {
                                        refferingAllowedWays.add(referrer);
                                    }
                                }

                                if (refferingAllowedWays.isEmpty()) {
                                    data.addSelected(n);
                                }
                            }
                        }
                    }
                }
                Collection<OsmPrimitive> sel = data.getSelected();
                if (!sel.isEmpty()) {
                    MainApplication.getMenu().purge.actionPerformed(null);
                }
            });
        }
    }

    @Override
    public OdDataLayer getDataLayer() {
        return dataLayer;
    }
}
