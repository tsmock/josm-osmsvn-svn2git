// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.measurement;
/// @author Raphael Mack <ramack@raphael-mack.de>

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.ILatLon;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.IGpxTrack;
import org.openstreetmap.josm.data.gpx.IGpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This is a layer that draws a grid
 */
public class MeasurementLayer extends Layer {

    public MeasurementLayer(String name) {
        super(name);
    }

    private static Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(MeasurementPlugin.class.getResource("/images/measurement.png")));
    private Collection<WayPoint> points = new ArrayList<>(32);

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getToolTipText() {
        return tr("Layer to make measurements");
    }

    @Override
    public boolean isMergable(Layer other) {
        //return other instanceof MeasurementLayer;
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {
        // TODO: nyi - doubts about how this should be done are around. Ideas?
    }

    @Override
    public void paint(Graphics2D g, final MapView mv, Bounds bounds) {
        g.setColor(Color.green);
        Point l = null;
        for(WayPoint p:points){
            Point pnt = mv.getPoint(p.getCoor());
            if (l != null){
                g.drawLine(l.x, l.y, pnt.x, pnt.y);
            }
            g.drawOval(pnt.x - 2, pnt.y - 2, 4, 4);
            l = pnt;
        }
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        // nothing to do here
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[]{
                LayerListDialog.getInstance().createShowHideLayerAction(),
                // TODO: implement new JMenuItem(new LayerListDialog.DeleteLayerAction(this)),
                SeparatorLayerAction.INSTANCE,
                new GPXLayerImportAction(this),
                SeparatorLayerAction.INSTANCE,
                new LayerListPopup.InfoAction(this)};
    }

    public void removeLastPoint(){
        WayPoint l = null;
        for(WayPoint p:points) l = p;
        if(l != null) points.remove(l);
        recalculate();
    }

    public void mouseClicked(MouseEvent e){
        if (e.getButton() != MouseEvent.BUTTON1) return;

        LatLon coor = MainApplication.getMap().mapView.getLatLon(e.getX(), e.getY());
        points.add(new WayPoint(coor));

        recalculate();
    }

    public void reset(){
        points.clear();
        recalculate();
    }

    private void recalculate(){
        double pathLength = 0.0, segLength = 0.0; // in meters
        WayPoint last = null;

        for(WayPoint p : points){
            if(last != null){
                segLength = calcDistance(last, p);
                pathLength += segLength;
            }
            last = p;
        }
        if (MeasurementPlugin.measurementDialog != null) {
            MeasurementPlugin.measurementDialog.pathLengthLabel.setText(NavigatableComponent.getDistText(pathLength));
        }
        if (MainApplication.getMap().mapMode instanceof MeasurementMode) {
            MainApplication.getMap().statusLine.setDist(pathLength);
        }
        invalidate();
    }

    /*
     * Use an equal area sinusoidal projection to improve accuracy and so we can still use normal polygon area calculation
     * https://stackoverflow.com/questions/4681737/how-to-calculate-the-area-of-a-polygon-on-the-earths-surface-using-python
     */
    public static double calcX(ILatLon p1){
        return p1.lat() * Math.PI * 6367000 / 180;
    }

    public static double calcY(ILatLon p1){
        return p1.lon() * ( Math.PI * 6367000 / 180) * Math.cos(p1.lat() * Math.PI / 180);
    }

    public static double calcDistance(WayPoint p1, WayPoint p2){
        return p1.greatCircleDistance(p2);
    }

    public static double angleBetween(WayPoint p1, WayPoint p2){
        return angleBetween(p1.getCoor(), p2.getCoor());
    }

    public static double angleBetween(ILatLon p1, ILatLon p2){
        double lat1, lon1, lat2, lon2;
        double dlon;

        lat1 = p1.lat() * Math.PI / 180.0;
        lon1 = p1.lon() * Math.PI / 180.0;
        lat2 = p2.lat() * Math.PI / 180.0;
        lon2 = p2.lon() * Math.PI / 180.0;

        dlon = lon2 - lon1;
        double coslat2 = Math.cos(lat2);

        return (180 * Math.atan2(coslat2 * Math.sin(dlon),
                (Math.cos(lat1) * Math.sin(lat2)
                        -
                        Math.sin(lat1) * coslat2 * Math.cos(dlon)))) / Math.PI;
    }

    public static double oldAngleBetween(LatLon p1, LatLon p2){
        double lat1, lon1, lat2, lon2;
        double dlon, dlat;
        double heading;

        lat1 = p1.lat() * Math.PI / 180.0;
        lon1 = p1.lon() * Math.PI / 180.0;
        lat2 = p2.lat() * Math.PI / 180.0;
        lon2 = p2.lon() * Math.PI / 180.0;

        dlon = lon2 - lon1;
        dlat = lat2 - lat1;

        double a = (Math.pow(Math.sin(dlat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2), 2));
        double d = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        heading = Math.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(d))
                / (Math.sin(d) * Math.cos(lat1)));
        if (Math.sin(lon2 - lon1) < 0) {
            heading = 2 * Math.PI - heading;
        }

        return heading * 180 / Math.PI;
    }

    private class GPXLayerImportAction extends AbstractAction {

        /**
         * The data model for the list component.
         */
        private DefaultListModel<GpxLayer> model = new DefaultListModel<>();

        /**
         * @param layer the targeting measurement layer
         */
        public GPXLayerImportAction(MeasurementLayer layer) {
            super(tr("Import path from GPX layer"), ImageProvider.get("dialogs", "edit")); // TODO: find better image
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Box panel = Box.createVerticalBox();
            final JList<GpxLayer> layerList = new JList<>(model);
            Collection<Layer> data = MainApplication.getLayerManager().getLayers();
            Layer lastLayer = null;
            int layerCnt = 0;

            for (Layer l : data) {
                if (l instanceof GpxLayer) {
                    model.addElement((GpxLayer) l);
                    lastLayer = l;
                    layerCnt++;
                }
            }
            if (layerCnt == 1) {
                layerList.setSelectedValue(lastLayer, true);
            }
            if (layerCnt > 0) {
                layerList.setCellRenderer(new DefaultListCellRenderer(){
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        Layer layer = (Layer)value;
                        JLabel label = (JLabel)super.getListCellRendererComponent(list,
                                layer.getName(), index, isSelected, cellHasFocus);
                        Icon icon = layer.getIcon();
                        label.setIcon(icon);
                        label.setToolTipText(layer.getToolTipText());
                        return label;
                    }
                });

                JCheckBox dropFirst = new JCheckBox(tr("Drop existing path"));

                panel.add(layerList);
                panel.add(dropFirst);

                final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION){
                    @Override
                    public void selectInitialValue() {
                        layerList.requestFocusInWindow();
                    }
                };
                final JDialog dlg = optionPane.createDialog(MainApplication.getMainFrame(), tr("Import path from GPX layer"));
                dlg.setVisible(true);

                Object answer = optionPane.getValue();
                if (answer == null || answer == JOptionPane.UNINITIALIZED_VALUE ||
                        (answer instanceof Integer && (Integer)answer != JOptionPane.OK_OPTION)) {
                    return;
                }

                GpxLayer gpx = layerList.getSelectedValue();
                if (dropFirst.isSelected()) {
                    points = new ArrayList<>(32);
                }

                for (IGpxTrack trk : gpx.data.tracks) {
                    for (IGpxTrackSegment trkseg : trk.getSegments()) {
                        for(WayPoint p: trkseg.getWayPoints()){
                            points.add(p);
                        }
                    }
                }
                recalculate();
            } else{
                // TODO: register a listener and show menu entry only if gps layers are available
                // no gps layer
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),tr("No GPX data layer found."));
            }
        }
    }
}
