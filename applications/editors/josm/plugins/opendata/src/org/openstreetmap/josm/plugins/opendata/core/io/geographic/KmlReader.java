// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.UTFInputStreamReader;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.ProjectionPatterns;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.date.DateUtils;

public class KmlReader extends AbstractReader {

    // CHECKSTYLE.OFF: SingleSpaceSeparator
    public static final String KML_PLACEMARK   = "Placemark";
    public static final String KML_NAME        = "name";
    public static final String KML_COLOR       = "color";
    public static final String KML_SIMPLE_DATA = "SimpleData";
    public static final String KML_LINE_STRING = "LineString";
    public static final String KML_POINT       = "Point";
    public static final String KML_POLYGON     = "Polygon";
    public static final String KML_OUTER_BOUND = "outerBoundaryIs";
    public static final String KML_INNER_BOUND = "innerBoundaryIs";
    public static final String KML_LINEAR_RING = "LinearRing";
    public static final String KML_COORDINATES = "coordinates";
    public static final String KML_WHEN = "when";

    public static final String KML_EXT_TRACK = "Track";
    public static final String KML_EXT_COORD = "coord";
    public static final String KML_EXT_LANG = "lang";
    // CHECKSTYLE.ON: SingleSpaceSeparator

    public static final Pattern COLOR_PATTERN = Pattern.compile("\\p{XDigit}{8}");
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s", Pattern.UNICODE_CHARACTER_CLASS);

    private final XMLStreamReader parser;
    private final Map<LatLon, Node> nodes = new HashMap<>();

    public KmlReader(XMLStreamReader parser) {
        this.parser = parser;
    }

    public static DataSet parseDataSet(InputStream in, ProgressMonitor instance)
            throws IOException, XMLStreamException, FactoryConfigurationError {
        try (InputStreamReader ir = UTFInputStreamReader.create(in)) {
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(ir);
            return new KmlReader(parser).parseDoc();
        }
    }

    @Override
    protected DataSet doParseDataSet(InputStream source,
            ProgressMonitor progressMonitor) throws IllegalDataException {
        return null;
    }

    private DataSet parseDoc() throws XMLStreamException {
        DataSet ds = new DataSet();
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT && parser.getLocalName().equals(KML_PLACEMARK)) {
                parsePlaceMark(ds);
            }
        }
        return ds;
    }

    private static boolean keyIsIgnored(String key) {
        for (ProjectionPatterns pp : OdConstants.PROJECTIONS) {
            if (pp.getXPattern().matcher(key).matches() || pp.getYPattern().matcher(key).matches()) {
                return true;
            }
        }
        return false;
    }

    private void parsePlaceMark(DataSet ds) throws XMLStreamException {
        List<OsmPrimitive> list = new ArrayList<>();
        long when = 0;
        Way way = null;
        List<Node> wayNodes = null;
        Node node = null;
        Relation relation = null;
        String role = "";
        String previousName = null;
        Map<String, String> tags = new HashMap<>();
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.getLocalName().equals(KML_COLOR)) {
                    String s = parser.getElementText();
                    if (COLOR_PATTERN.matcher(s).matches()) {
                        // KML color format is aabbggrr, convert it to OSM (web) format: #rrggbb
                        tags.put(KML_COLOR, '#'+s.substring(6, 8)+s.substring(4, 6)+s.substring(2, 4));
                    }
                } else if (parser.getLocalName().equals(KML_NAME)) {
                    try {
                        tags.put(KML_NAME, parser.getElementText());
                    } catch (XMLStreamException e) {
                        Logging.trace(e);
                    }
                } else if (parser.getLocalName().equals(KML_SIMPLE_DATA)) {
                    String key = parser.getAttributeValue(null, "name");
                    if (!keyIsIgnored(key)) {
                        tags.put(key, parser.getElementText());
                    }
                } else if (parser.getLocalName().equals(KML_POLYGON)) {
                    relation = new Relation();
                    ds.addPrimitive(relation);
                    relation.put("type", "multipolygon");
                    list.add(relation);
                } else if (parser.getLocalName().equals(KML_OUTER_BOUND)) {
                    role = "outer";
                } else if (parser.getLocalName().equals(KML_INNER_BOUND)) {
                    role = "inner";
                } else if (parser.getLocalName().equals(KML_LINEAR_RING)) {
                    if (relation != null) {
                        way = new Way();
                        ds.addPrimitive(way);
                        wayNodes = new ArrayList<>();
                        relation.addMember(new RelationMember(role, way));
                    }
                } else if (parser.getLocalName().equals(KML_LINE_STRING) || parser.getLocalName().equals(KML_EXT_TRACK)) {
                    way = new Way();
                    ds.addPrimitive(way);
                    wayNodes = new ArrayList<>();
                    list.add(way);
                } else if (parser.getLocalName().equals(KML_COORDINATES)) {
                    String[] tab = SPACE_PATTERN.split(parser.getElementText().trim());
                    for (String s : tab) {
                        node = parseNode(ds, wayNodes, node, s.split(","));
                    }
                } else if (parser.getLocalName().equals(KML_EXT_COORD)) {
                    node = parseNode(ds, wayNodes, node, SPACE_PATTERN.split(parser.getElementText().trim()));
                    if (node != null && when > 0) {
                        node.setRawTimestamp((int) when);
                    }
                } else if (parser.getLocalName().equals(KML_WHEN)) {
                    when = DateUtils.tsFromString(parser.getElementText().trim());
                } else if (parser.getLocalName().equals(KML_EXT_LANG) && KML_NAME.equals(previousName)) {
                    tags.put(KML_NAME, parser.getElementText());
                }
                previousName = parser.getLocalName();
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (parser.getLocalName().equals(KML_PLACEMARK)) {
                    break;
                } else if (parser.getLocalName().equals(KML_POINT)) {
                    list.add(node);
                } else if (parser.getLocalName().equals(KML_LINE_STRING)
                        || parser.getLocalName().equals(KML_EXT_TRACK)
                        || parser.getLocalName().equals(KML_LINEAR_RING)) {
                    if (way != null && wayNodes != null)
                        way.setNodes(wayNodes);
                    wayNodes = new ArrayList<>();
                }
            }
        }
        ds.update(() -> {
            for (OsmPrimitive p : list) {
                p.putAll(tags);
            }
        });
    }

    private Node parseNode(DataSet ds, List<Node> wayNodes, Node node, String[] values) {
        if (values.length >= 2) {
            LatLon ll = new LatLon(Double.parseDouble(values[1]), Double.parseDouble(values[0])).getRoundedToOsmPrecision();
            node = nodes.computeIfAbsent(ll, latLon -> {
                Node tNode = new Node(latLon);
                ds.addPrimitive(tNode);
                if (values.length > 2 && !"0".equals(values[2])) {
                    tNode.put("ele", values[2]);
                }
                return tNode;
            });
            if (wayNodes != null) {
                wayNodes.add(node);
            }
        }
        return node;
    }
}
