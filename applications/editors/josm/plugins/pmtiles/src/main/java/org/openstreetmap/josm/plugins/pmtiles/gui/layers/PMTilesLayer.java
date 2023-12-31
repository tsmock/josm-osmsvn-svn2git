// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pmtiles.gui.layers;

import static org.openstreetmap.josm.tools.Utils.getSystemProperty;

import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.plugins.pmtiles.data.imagery.PMTilesImageryInfo;
import org.openstreetmap.josm.tools.TextUtils;
import org.openstreetmap.josm.tools.Utils;

/**
 * A common interface for layers using PMTiles as a source
 */
interface PMTilesLayer {

    /**
     * Returns imagery info.
     * @return imagery info
     */
    PMTilesImageryInfo getInfo();

    /**
     * Get the source tag for the layer
     * @return The source tag
     */
    default String getChangesetSourceTag() {
        final var sb = new StringBuilder();
        final var info = getInfo();
        if (info.hasAttribution()) {
            sb.append(getInfo().getAttributionText(0, null, null)
                    .replaceAll("<a [^>]*>|</a>", "")
                    .replaceAll("  +", " "));
        }
        if (info.getName() != null) {
            if (!sb.isEmpty()) {
                sb.append(" - ");
            }
            sb.append(info.getName());
        }
        if (sb.isEmpty()) {
            final var location = info.header().location().toString();
            if (Utils.isLocalUrl(location)) {
                final String userName = getSystemProperty("user.name");
                final String userNameAlt = "<user.name>";
                sb.append(location.replace(userName, userNameAlt));
            } else {
                sb.append(TextUtils.stripUrl(location));
            }
        }
        return sb.toString();
    }

    /**
     * Visits the content bounds of this layer. The behavior of this method depends on the layer,
     * but each implementation should attempt to cover the relevant content of the layer in this method.
     * @param v The visitor that gets notified about the contents of this layer.
     * @see org.openstreetmap.josm.gui.layer.Layer#visitBoundingBox
     */
    default void visitBoundingBox(BoundingXYVisitor v) {
        v.visit(this.getInfo().getBounds());
    }
}
