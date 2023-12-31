/**
 * License: GPL. Copyright 2008. Martin Garbe (leo at running-sheep dot com)
 */
package org.openstreetmap.josm.plugins.editgpx;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxData;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxTrack;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxTrackSegment;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxWayPoint;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * The layer for editing GPX data
 */
public class EditGpxLayer extends Layer {

    private static final Icon icon = new ImageProvider("editgpx_layer").get();
    /** The data that is being edited */
    public final EditGpxData data;
    private final GPXLayerImportAction layerImport;

    /**
     * Constructs a new {@code EditGpxLayer}.
     * @param gpxData edit gpx data
     */
    public EditGpxLayer(EditGpxData gpxData) {
        super(tr("EditGpx"));
        data = gpxData;
        layerImport = new GPXLayerImportAction(data);
    }

    /**
     * check if dataSet is empty
     * if so show import dialog to user
     */
    public void initializeImport() {
        if (data.isEmpty()) {
            layerImport.activateImport();
        }
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[] {
                LayerListDialog.getInstance().createShowHideLayerAction(),
                LayerListDialog.getInstance().createDeleteLayerAction(),
                SeparatorLayerAction.INSTANCE,
                layerImport,
                new ConvertToGpxLayerAction(),
                new ConvertToAnonTimeGpxLayerAction(),
                SeparatorLayerAction.INSTANCE,
                new LayerListPopup.InfoAction(this)};
    }

    @Override
    public String getToolTipText() {
        return tr("Layer for editing GPX tracks");
    }

    @Override
    public boolean isMergable(Layer other) {
        // TODO
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {
        // TODO
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {
        g.setColor(Color.yellow);

        //don't iterate through dataSet whiling making changes
        synchronized(layerImport.importing) {
            Projection projection = ProjectionRegistry.getProjection();
            for (EditGpxTrack track: data.getTracks()) {
                for (EditGpxTrackSegment segment: track.getSegments()) {
                    for (EditGpxWayPoint wayPoint: segment.getWayPoints()) {
                        if (!wayPoint.isDeleted()) {
                            Point pnt = mv.getPoint(wayPoint.getCoor().getEastNorth(projection));
                            g.drawOval(pnt.x - 2, pnt.y - 2, 4, 4);
                        }
                    }
                }
            }
        }
    }

    public void reset(){
        //TODO implement a reset
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        // TODO Auto-generated method stub
    }

    /**
     * convert a DataSet to GPX
     *
     * @param anonTime If true set all time and date in GPX to 01/01/1970 00:00 ?
     * @return GpxData
     */
    private GpxData toGpxData(boolean anonTime) {
        return data.createGpxData(anonTime);
    }

    /**
     * Context item "Convert to GPX layer"
     */
    public class ConvertToGpxLayerAction extends AbstractAction {
        /**
         * Constructs a new {@code ConvertToGpxLayerAction}.
         */
        public ConvertToGpxLayerAction() {
            super(tr("Convert to GPX layer"), ImageProvider.get("converttogpx"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (MainApplication.getMap().mapMode instanceof EditGpxMode && !MainApplication.getMap().selectSelectTool(false)) {
                MainApplication.getMap().selectZoomTool(false); // Select tool might not be support of active layer, zoom is always supported
            }
            MainApplication.getLayerManager().addLayer(new GpxLayer(toGpxData(false), tr("Converted from: {0}", getName())));
            MainApplication.getLayerManager().removeLayer(EditGpxLayer.this);
        }
    }

    /**
     * Context item "Convert to GPX layer with anonymised time"
     */
    public class ConvertToAnonTimeGpxLayerAction extends AbstractAction {
        /**
         * Constructs a new {@code ConvertToAnonTimeGpxLayerAction}.
         */
        public ConvertToAnonTimeGpxLayerAction() {
            super(tr("Convert to GPX layer with anonymised time"), ImageProvider.get("converttogpx"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (MainApplication.getMap().mapMode instanceof EditGpxMode && !MainApplication.getMap().selectSelectTool(false)) {
                MainApplication.getMap().selectZoomTool(false); // Select tool might not be support of active layer, zoom is always supported
            }
            MainApplication.getLayerManager().addLayer(new GpxLayer(toGpxData(true), tr("Converted from: {0}", getName())));
            MainApplication.getLayerManager().removeLayer(EditGpxLayer.this);
        }
    }
}
