package org.openstreetmap.josm.plugins.osminspector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ProgressMonitor;

import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.Name;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.identity.FeatureId;
import org.geotools.api.filter.spatial.Intersects;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.style.FeatureTypeStyle;
import org.geotools.api.style.Fill;
import org.geotools.api.style.Graphic;
import org.geotools.api.style.Mark;
import org.geotools.api.style.Rule;
import org.geotools.api.style.Stroke;
import org.geotools.api.style.Style;
import org.geotools.api.style.StyleFactory;
import org.geotools.api.style.Symbolizer;
import org.geotools.data.DataUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.osminspector.gui.OsmInspectorDialog;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

public class OsmInspectorLayer extends Layer {

    private final StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
    private final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
    private final StreamingRenderer renderer;
    private final CoordinateReferenceSystem crsOSMI;
    private GeomType geometryType;
    private String geometryAttributeName;

    private SimpleFeatureSource featureSource;
    private final MapContent content;
    private boolean bIsChanged;

    private final int layerOffset = 1;

    private final ArrayList<GeomType> selectGeomType;
    private final Color[] featureFills = { new Color(255, 0, 0),
            new Color(0, 0, 255), // duplicate ways
            new Color(204, 204, 0), // minor 5
            new Color(255, 230, 128), // minor 2
            new Color(255, 204, 0), // minor 1
            new Color(255, 102, 102), // major 5
            new Color(255, 148, 77), // major 2
            new Color(255, 0, 0), // major 1
            new Color(255, 255, 64), // selected
            new Color(255, 0, 0), new Color(255, 0, 0) };

    /**
     * dialog showing the bug info
     */
    private final OsmInspectorDialog dialog;

    /**
     * supported actions
     */

    // Container for bugs from Osmi
    private List<OSMIFeatureTracker> arrFeatures;
    private final LinkedHashMap<BugInfo, Long> osmiBugInfo;

    public Geometry getOsmBugGeometry(int index) {
        BugInfo[] array = new BugInfo[osmiBugInfo.keySet().size()];
        array = osmiBugInfo.keySet().toArray(array);
        return array[index].getGeom();
    }

    public Map<BugInfo, Long> getOsmiBugInfo() {
        return osmiBugInfo;
    }

    public SimpleFeatureSource getFeatureSource() {
        return featureSource;
    }

    public void setFeatureSource(SimpleFeatureSource featureSource) {
        this.featureSource = featureSource;
    }

    public boolean isbIsChanged() {
        return bIsChanged;
    }

    public void setbIsChanged(boolean bIsChanged) {
        this.bIsChanged = bIsChanged;
    }

    public List<OSMIFeatureTracker> getArrFeatures() {
        return arrFeatures;
    }

    public void setArrFeatures(List<OSMIFeatureTracker> arrFeatures) {
        this.arrFeatures = arrFeatures;
    }

    public BugIndex getOsmiIndex() {
        return osmiIndex;
    }

    // Pointer to prev and next osmi bugs
    private final BugIndex osmiIndex;

    /**
     *
     * The Bug attribute class: hold geom, id and description for that bug
     *
     * @author snikhil
     *
     */
    public static class BugInfo implements Comparable<BugInfo>{

        public Geometry getGeom() {
            return geom;
        }

        public String getDesc() {
            return desc;
        }

        public String getId() {
            return id;
        }

        @Override
        public int hashCode() {
            String fid = attributes.get("FID");
            String hash =  (fid == null || fid.isEmpty()) ? attributes.get("problem_id") : fid;
            return hash.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            String fid = attributes.get("FID");
            String hash =  (fid == null || fid.isEmpty()) ? attributes.get("problem_id") : fid;


            if (obj instanceof BugInfo) {
                BugInfo b = (BugInfo) obj;

                String bfid = b.attributes.get("FID");
                String bhash =  (bfid == null || bfid.isEmpty()) ? b.attributes.get("problem_id") : bfid;
                return hash.equals(bhash);
            }
            return false;
        }

        // private final long bugId; //incremental bugId
        private final long bugId;
        private final Geometry geom;
        private final String desc;
        private final String id;
        private final Name name;
        private final Map<String, String> attributes;

        public BugInfo(SimpleFeature next, long idx)
                throws IndexOutOfBoundsException, ParseException {

            bugId = idx;
            attributes = new HashMap<>();
            Collection<Property> properties = next.getProperties();
            for (Property p : properties) {
                attributes.put(p.getName().toString(), p.getValue().toString());
            }
            this.geom = (Geometry) next.getAttribute(0);
            this.desc = (String) next.getAttribute("error_desc");
            this.id = next.getID();
            name = next.getName();
        }

        @Override
        public String toString() {
            return "BugId_" + bugId + "\n";
        }

        public String getContentString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Layer:").append(name.getLocalPart()).append("\n");
            for (Entry<String, String> next : attributes.entrySet()) {
                sb.append(next.getKey()).append(":").append(next.getValue())
                        .append("\n");
            }
            return sb.toString();
        }

        public long getBugId() {
            return bugId;
        }

        @Override
        public int compareTo(BugInfo o) {
            String fid = attributes.get("FID");
            String hash =  (fid == null || fid.isEmpty()) ? attributes.get("problem_id") : fid;

            String ofid = o.attributes.get("FID");
            String ohash =  (ofid == null || ofid.isEmpty()) ? o.attributes.get("problem_id") : ofid;
            return hash.compareTo(ohash);
        }
    }

    /**
     * Helper class that stores the bug next and prev pointers and can navigate
     * the entire bug list
     *
     * @author snikhil
     *
     */
    public static class BugIndex {
        private int nextIndex;
        private int previousIndex;
        private final ArrayList<BugInfo> osmBugs;

        public BugIndex(Map<BugInfo, Long> bugs) {
            osmBugs = new ArrayList<>(bugs.keySet());
            nextIndex = 0;
            previousIndex = -1;
        }

        public BugIndex(Map<BugInfo, Long> bugs, int n, int p) {
            osmBugs = new ArrayList<>(bugs.keySet());
            nextIndex = n;
            previousIndex = p;
        }

        public void next() {
            previousIndex = nextIndex;
            nextIndex = ++nextIndex % osmBugs.size();
        }

        public void prev() {
            nextIndex = previousIndex;
            previousIndex = previousIndex - 1 < 0 ? osmBugs.size() - 1
                    : --previousIndex;
        }

        public BugInfo getItemPointedByNext() {
            return osmBugs.get(nextIndex);
        }

        public BugInfo getItemPointedByPrev() {
            return osmBugs.get(nextIndex);
        }

        public int indexOf(BugInfo b) {
            return osmBugs.indexOf(b);
        }

        public BugInfo getNext() {
            next();
            return osmBugs.get(nextIndex);
        }

        public BugInfo getPrev() {
            prev();
            return osmBugs.get(nextIndex);
        }

        public List<BugInfo> getBugs() {
            return osmBugs;
        }

        public void append(Map<BugInfo, Long> osmiBugInfo) {
            for (BugInfo next : osmiBugInfo.keySet()) {
                if (!osmBugs.contains(next)) {
                    this.osmBugs.add(next);
                }
            }
        }
    }

    private enum GeomType {
        POINT, LINE, POLYGON
    }

    private static final Color SELECTED_COLOUR = Color.ORANGE;
    private static final float SELECTED_POINT_SIZE = 15.0f;
    private static final float OPACITY = 1.0f;
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 10.0f;

    public OsmInspectorLayer(GeoFabrikWFSClient wfsClient,
            ProgressMonitor monitor) throws NoSuchAuthorityCodeException,
            FactoryException, IOException, IndexOutOfBoundsException,
            ParseException {
        super("OsmInspector");

        arrFeatures = new ArrayList<>();
        osmiBugInfo = new LinkedHashMap<>();
        selectGeomType = new ArrayList<>();

        // Step 3 - discovery; enhance to iterate over all types with bounds

        String[] typeNames = wfsClient.getTypeNames();
        renderer = new StreamingRenderer();
        CRS.decode(ProjectionRegistry.getProjection().toCode());
        crsOSMI = CRS.decode("EPSG:4326");
        content = new MapContent();
        content.getViewport().setCoordinateReferenceSystem(crsOSMI);

        selectGeomType.add(GeomType.POINT);
        for (int idx = 1; idx < typeNames.length; ++idx) {
            String typeName = typeNames[idx];
            Set<FeatureId> selectedFeatures = new HashSet<>();

            FeatureCollection<SimpleFeatureType, SimpleFeature> features = wfsClient
                    .getFeatures(typeName, monitor);
            setGeometry(selectGeomType, typeName);

            Logging.info("Osm Inspector Features size: " + features.size());
            Style style = createDefaultStyle(idx, selectedFeatures);

            OSMIFeatureTracker tracker = new OSMIFeatureTracker(features);
            arrFeatures.add(tracker);
            FeatureIterator<SimpleFeature> it = tracker.getFeatures().features();

            while (it.hasNext()) {
                BugInfo theInfo = new BugInfo(it.next(), osmiBugInfo.size());
                osmiBugInfo.put(theInfo, theInfo.bugId);
            }

            content.addLayer(new FeatureLayer(tracker.getFeatures(), style));
        }

        osmiIndex = new BugIndex(osmiBugInfo);
        content.setTitle("Osm Inspector Errors");
        renderer.setMapContent(content);
        bIsChanged = true;

        // finally initialize the dialog
        dialog = new OsmInspectorDialog(this);
        this.updateView();
    }

    public void loadFeatures(GeoFabrikWFSClient wfsClient)
            throws NoSuchAuthorityCodeException, FactoryException, IOException,
            IndexOutOfBoundsException, NoSuchElementException, ParseException {
        String[] typeNames = wfsClient.getTypeNames();

        content.layers().clear();
        selectGeomType.clear();
        selectGeomType.add(GeomType.POINT);

        ProgressMonitor monitor = new ProgressMonitor(MainApplication.getMap().mapView,
                "Loading features", "", 0, 100);

        for (int idx = 1; idx < typeNames.length; ++idx) {
            String typeName = typeNames[idx];
            Set<FeatureId> selectedFeatures = new HashSet<>();

            monitor.setProgress(100 / typeNames.length * idx);
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = wfsClient
                    .getFeatures(typeName, monitor);
            setGeometry(selectGeomType, typeName);

            Logging.info("Osm Inspector Features size: " + features.size());

            OSMIFeatureTracker tracker = arrFeatures.get(idx - layerOffset);
            tracker.mergeFeatures(features);

            FeatureIterator<SimpleFeature> it = tracker.getFeatures().features();

            while (it.hasNext()) {
                BugInfo theInfo = new BugInfo(it.next(), osmiBugInfo.size());
                if (!osmiBugInfo.containsKey(theInfo)) {
                    osmiBugInfo.put(theInfo, theInfo.bugId);
                }
            }

            Style style = createDefaultStyle(idx, selectedFeatures);
            content.addLayer(new FeatureLayer(tracker.getFeatures(), style));
        }

        osmiIndex.append(osmiBugInfo);


        monitor.setProgress(100);
        monitor.close();
        bIsChanged = true;
        //dialog.updateDialog(this);
        dialog.refreshModel();
        //dialog.updateNextPrevAction(this);

        this.updateView();
    }

    private Style createDefaultStyle(int idx, Set<FeatureId> IDs) {
        Color fillColor = featureFills[idx];

        Rule selectedRule = createRule(SELECTED_COLOUR, SELECTED_COLOUR, true);
        selectedRule.setFilter(ff.id(IDs));

        Rule rule = createRule(fillColor, fillColor, false);
        rule.setElseFilter(true);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(selectedRule);
        fts.rules().add(rule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }

    private Rule createRule(Color outlineColor, Color fillColor,
            boolean bSelected) {
        Symbolizer symbolizer = null;
        Fill fill = null;
        Stroke stroke = sf.createStroke(ff.literal(outlineColor),
                ff.literal(LINE_WIDTH));

        switch (geometryType) {
        case POLYGON:
            fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
            symbolizer = sf.createPolygonSymbolizer(stroke, fill,
                    geometryAttributeName);
            break;

        case LINE:
            symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
            break;

        case POINT:
            fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));

            Mark mark = sf.getTriangleMark();
            mark.setFill(fill);
            mark.setStroke(stroke);

            Graphic graphic = sf.createDefaultGraphic();
            graphic.graphicalSymbols().clear();
            graphic.graphicalSymbols().add(mark);
            graphic.setSize(ff.literal(bSelected ? SELECTED_POINT_SIZE
                    : POINT_SIZE));

            symbolizer = sf.createPointSymbolizer(graphic,
                    geometryAttributeName);
        }

        Rule rule = sf.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }

    private void setGeometry(ArrayList<GeomType> selectedTypes, String typename) {
        Logging.info("Passed type is" + typename);
        if (typename.compareTo("duplicate_ways") == 0) {
            geometryType = GeomType.LINE;
        } else
            geometryType = GeomType.POINT;

        selectedTypes.add(geometryType);
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("layer/osmdata_small");
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[] {};
    }

    @Override
    public String getToolTipText() {
        return org.openstreetmap.josm.tools.I18n.tr("OsmInspector");
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {
        return;
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds box) {
        LatLon min = box.getMin();
        LatLon max = box.getMax();

        Envelope envelope2 = new Envelope(Math.min(min.lat(), max.lat()),
                Math.max(min.lat(), max.lat()), Math.min(min.lon(), max.lon()),
                Math.max(min.lon(), max.lon()));

        ReferencedEnvelope mapArea = new ReferencedEnvelope(envelope2, crsOSMI);

        renderer.setInteractive(false);
        renderer.paint(g, mv.getBounds(), mapArea);
        bIsChanged = false;
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
    }

    public boolean isChanged() {
        return bIsChanged;
    }

    public void updateView() {
        this.dialog.revalidate();
        invalidate();
    }

    public void selectFeatures(int x, int y) {
        int pixelDelta = 2;
        LatLon clickUL = MainApplication.getMap().mapView.getLatLon(x - pixelDelta, y
                - pixelDelta);
        LatLon clickLR = MainApplication.getMap().mapView.getLatLon(x + pixelDelta, y
                + pixelDelta);

        Envelope envelope = new Envelope(
                Math.min(clickUL.lon(), clickLR.lon()), Math.max(clickUL.lon(),
                        clickLR.lon()), Math.min(clickUL.lat(), clickLR.lat()),
                Math.max(clickUL.lat(), clickLR.lat()));

        ReferencedEnvelope mapArea = new ReferencedEnvelope(envelope, crsOSMI);

        Intersects filter = ff.intersects(ff.property("msGeometry"),
                ff.literal(mapArea));
        //
        // Select features in all layers
        //
        content.layers().clear();

        // Iterate through features and build a list that intersects the above
        // envelope
        for (int idx = 0; idx < arrFeatures.size(); ++idx) {
            OSMIFeatureTracker tracker = arrFeatures.get(idx);
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = tracker
                    .getFeatures();

            SimpleFeatureSource tempfs = DataUtilities.source(features);
            FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures;

            try {
                selectedFeatures = tempfs.getFeatures(filter);
                Set<FeatureId> IDs = new HashSet<>();

                try (FeatureIterator<SimpleFeature> iter = selectedFeatures.features()) {
    
                    Logging.info("Selected features " + selectedFeatures.size());
    
                    while (iter.hasNext()) {
                        SimpleFeature feature = iter.next();
                        IDs.add(feature.getIdentifier());
                    }
                }

                geometryType = selectGeomType.get(idx + layerOffset);
                Style style = createDefaultStyle(idx + layerOffset, IDs);
                content.addLayer(new FeatureLayer(features, style));
            } catch (IOException e) {
                Logging.error(e);
            }
        }

        bIsChanged = true;
    }

    public void selectFeatures(LatLon center) {
        Point point = MainApplication.getMap().mapView.getPoint(center);
        selectFeatures(point.x, point.y);
    }

    public void setOsmiIndex(int firstIndex) {
        osmiIndex.nextIndex = firstIndex;
        osmiIndex.previousIndex = firstIndex - 1 >= 0 ? firstIndex - 1
                : osmiBugInfo.size() - 1;
    }
}
