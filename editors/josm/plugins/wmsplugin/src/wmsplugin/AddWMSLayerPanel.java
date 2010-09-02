package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.bbox.SlippyMapBBoxChooser;
import org.openstreetmap.josm.tools.GBC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AddWMSLayerPanel extends JPanel {
    private List<LayerDetails> selectedLayers;
    private URL serviceUrl;
    private LayerDetails selectedLayer;

    private JTextField menuName;
    private JTextArea resultingLayerField;
    private MutableTreeNode treeRootNode;
    private DefaultTreeModel treeData;
    private JTree layerTree;
    private JButton showBoundsButton;

    private boolean previouslyShownUnsupportedCrsError = false;

    private static final Map<String, Projection> supportedProjections = new HashMap<String, Projection>();
    static {
        for (Projection proj : Projection.allProjections) {
            supportedProjections.put(proj.toCode().trim().toUpperCase(), proj);
        }
    }

    public AddWMSLayerPanel() {
        JPanel wmsFetchPanel = new JPanel(new GridBagLayout());
        menuName = new JTextField(40);
        menuName.setText(tr("Unnamed WMS Layer"));
        final JTextArea serviceUrl = new JTextArea(3, 40);
        serviceUrl.setLineWrap(true);
        serviceUrl.setText("http://sample.com/wms?");
        wmsFetchPanel.add(new JLabel(tr("Menu Name")), GBC.std().insets(0,0,5,0));
        wmsFetchPanel.add(menuName, GBC.eop().insets(5,0,0,0).fill(GridBagConstraints.HORIZONTAL));
        wmsFetchPanel.add(new JLabel(tr("Service URL")), GBC.std().insets(0,0,5,0));
        JScrollPane scrollPane = new JScrollPane(serviceUrl,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        wmsFetchPanel.add(scrollPane, GBC.eop().insets(5,0,0,0));
        JButton getLayersButton = new JButton(tr("Get Layers"));
        getLayersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Cursor beforeCursor = getCursor();
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    attemptGetCapabilities(serviceUrl.getText());
                } finally {
                    setCursor(beforeCursor);
                }
            }
        });
        wmsFetchPanel.add(getLayersButton, GBC.eop().anchor(GridBagConstraints.EAST));

        treeRootNode = new DefaultMutableTreeNode();
        treeData = new DefaultTreeModel(treeRootNode);
        layerTree = new JTree(treeData);
        layerTree.setCellRenderer(new LayerTreeCellRenderer());
        layerTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                TreePath[] selectionRows = layerTree.getSelectionPaths();
                if(selectionRows == null) {
                    showBoundsButton.setEnabled(false);
                    selectedLayer = null;
                    return;
                }

                selectedLayers = new LinkedList<LayerDetails>();
                for (TreePath i : selectionRows) {
                    Object userObject = ((DefaultMutableTreeNode) i.getLastPathComponent()).getUserObject();
                    if(userObject instanceof LayerDetails) {
                        LayerDetails detail = (LayerDetails) userObject;
                        if(!detail.isSupported()) {
                            layerTree.removeSelectionPath(i);
                            if(!previouslyShownUnsupportedCrsError) {
                                JOptionPane.showMessageDialog(null, tr("That layer does not support any of JOSM's projections,\n" +
                                "so you can not use it. This message will not show again."),
                                tr("WMS Error"), JOptionPane.ERROR_MESSAGE);
                                previouslyShownUnsupportedCrsError = true;
                            }
                        } else if(detail.ident != null) {
                            selectedLayers.add(detail);
                        }
                    }
                }

                if (!selectedLayers.isEmpty()) {
                    resultingLayerField.setText(buildGetMapUrl());

                    if(selectedLayers.size() == 1) {
                        showBoundsButton.setEnabled(true);
                        selectedLayer = selectedLayers.get(0);
                    }
                } else {
                    showBoundsButton.setEnabled(false);
                    selectedLayer = null;
                }
            }
        });
        wmsFetchPanel.add(new JScrollPane(layerTree), GBC.eop().insets(5,0,0,0).fill(GridBagConstraints.HORIZONTAL));

        JPanel layerManipulationButtons = new JPanel();
        showBoundsButton = new JButton(tr("Show Bounds"));
        showBoundsButton.setEnabled(false);
        showBoundsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(selectedLayer.bounds != null) {
                    SlippyMapBBoxChooser mapPanel = new SlippyMapBBoxChooser();
                    mapPanel.setBoundingBox(selectedLayer.bounds);
                    JOptionPane.showMessageDialog(null, mapPanel, tr("Show Bounds"), JOptionPane.PLAIN_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, tr("No bounding box was found for this layer."),
                            tr("WMS Error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        layerManipulationButtons.add(showBoundsButton);

        wmsFetchPanel.add(layerManipulationButtons, GBC.eol().insets(0,0,5,0));
        wmsFetchPanel.add(new JLabel(tr("WMS URL")), GBC.std().insets(0,0,5,0));
        resultingLayerField = new JTextArea(3, 40);
        resultingLayerField.setLineWrap(true);
        wmsFetchPanel.add(new JScrollPane(resultingLayerField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), GBC.eop().insets(5,0,0,0).fill(GridBagConstraints.HORIZONTAL));

        add(wmsFetchPanel);
    }

    private String buildRootUrl() {
        StringBuilder a = new StringBuilder(serviceUrl.getProtocol());
        a.append("://");
        a.append(serviceUrl.getHost());
        if(serviceUrl.getPort() != -1) {
            a.append(":");
            a.append(serviceUrl.getPort());
        }
        a.append(serviceUrl.getPath());
        a.append("?");
        return a.toString();
    }

    private String buildGetMapUrl() {
        StringBuilder a = new StringBuilder();
        a.append(buildRootUrl());
        a.append("FORMAT=image/jpeg&VERSION=1.1.1&SERVICE=WMS&REQUEST=GetMap&Layers=");
        a.append(commaSepLayerList());
        a.append("&");

        return a.toString();
    }

    private String commaSepLayerList() {
        StringBuilder b = new StringBuilder();

        Iterator<LayerDetails> iterator = selectedLayers.iterator();
        while (iterator.hasNext()) {
            LayerDetails layerDetails = iterator.next();
            b.append(layerDetails.ident);
            if(iterator.hasNext()) {
                b.append(",");
            }
        }

        return b.toString();
    }

    private void attemptGetCapabilities(String serviceUrlStr) {
        serviceUrl = null;
        try {
            if (serviceUrlStr.endsWith("?")) {
                // It's the root of the URL. We need to append the GetCapabilities
                // query parameters
                serviceUrl = new URL(serviceUrlStr + "VERSION=1.1.1&SERVICE=WMS&REQUEST=GetCapabilities");
            } else if (serviceUrlStr.toLowerCase().contains("getcapabilities")) {
                // The URL already contains the GetCapabilities query parameters
                serviceUrl = new URL(serviceUrlStr);
            } else {
                JOptionPane.showMessageDialog(this, tr("Invalid service URL."),
                        tr("WMS Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (HeadlessException e) {
            return;
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(this, tr("Invalid service URL."),
                    tr("WMS Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String incomingData;
        try {
            URLConnection openConnection = serviceUrl.openConnection();
            InputStream inputStream = openConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder ba = new StringBuilder();
            while((line = br.readLine()) != null) {
                ba.append(line);
                ba.append("\n");
            }
            incomingData = ba.toString();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, tr("Could not retrieve WMS layer list."),
                    tr("WMS Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        Document document;
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(incomingData)));
        } catch (ParserConfigurationException e) {
            JOptionPane.showMessageDialog(this, tr("Could not parse WMS layer list."),
                    tr("WMS Error"), JOptionPane.ERROR_MESSAGE);
            return;
        } catch (SAXException e) {
            JOptionPane.showMessageDialog(this, tr("Could not parse WMS layer list."),
                    tr("WMS Error"), JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, tr("Could not parse WMS layer list."),
                    tr("WMS Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        treeRootNode.setUserObject(serviceUrl.getHost());
        Element capabilityElem = getChild(document.getDocumentElement(), "Capability");
        List<Element> children = getChildren(capabilityElem, "Layer");
        List<LayerDetails> layers = parseLayers(children);

        updateTreeList(layers);
    }

    private void updateTreeList(List<LayerDetails> layers) {
        addLayersToTreeData(treeRootNode, layers);
        layerTree.expandRow(0);
    }

    private void addLayersToTreeData(MutableTreeNode parent, List<LayerDetails> layers) {
        for (LayerDetails layerDetails : layers) {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(layerDetails);
            addLayersToTreeData(treeNode, layerDetails.children);
            treeData.insertNodeInto(treeNode, parent, 0);
        }
    }

    private List<LayerDetails> parseLayers(List<Element> children) {
        List<LayerDetails> details = new LinkedList<LayerDetails>();
        for (Element element : children) {
            details.add(parseLayer(element));
        }
        return details;
    }

    private LayerDetails parseLayer(Element element) {
        String name = getChildContent(element, "Title", null, null);
        String ident = getChildContent(element, "Name", null, null);

        boolean josmSupportsThisLayer = false;
        List<String> crsList = new LinkedList<String>();
        // I think CRS and SRS are the same at this point
        List<Element> crsChildren = getChildren(element, "CRS");
        crsChildren.addAll(getChildren(element, "SRS"));
        for (Element child : crsChildren) {
            String crs = (String) getContent(child);
            if(crs != null) {
                String upperCase = crs.trim().toUpperCase();
                crsList.add(upperCase);
                josmSupportsThisLayer |= supportedProjections.containsKey(upperCase);
            }
        }

        Bounds bounds = null;
        Element bboxElem = getChild(element, "EX_GeographicBoundingBox");
        if(bboxElem != null) {
            // Attempt to use EX_GeographicBoundingBox for bounding box
            double left = Double.parseDouble(getChildContent(bboxElem, "westBoundLongitude", null, null));
            double top = Double.parseDouble(getChildContent(bboxElem, "northBoundLatitude", null, null));
            double right = Double.parseDouble(getChildContent(bboxElem, "eastBoundLongitude", null, null));
            double bot = Double.parseDouble(getChildContent(bboxElem, "southBoundLatitude", null, null));
            bounds = new Bounds(bot, left, top, right);
        } else {
            // If that's not available, try LatLonBoundingBox
            bboxElem = getChild(element, "LatLonBoundingBox");
            if(bboxElem != null) {
                double left = Double.parseDouble(bboxElem.getAttribute("minx"));
                double top = Double.parseDouble(bboxElem.getAttribute("maxy"));
                double right = Double.parseDouble(bboxElem.getAttribute("maxx"));
                double bot = Double.parseDouble(bboxElem.getAttribute("miny"));
                bounds = new Bounds(bot, left, top, right);
            }
        }

        List<Element> layerChildren = getChildren(element, "Layer");
        List<LayerDetails> childLayers = parseLayers(layerChildren);

        return new LayerDetails(name, ident, crsList, josmSupportsThisLayer, bounds, childLayers);
    }

    public String getUrlName() {
        return menuName.getText();
    }

    public String getUrl() {
        return resultingLayerField.getText();
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Test");
        f.setContentPane(new AddWMSLayerPanel());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }

    private static String getChildContent(Element parent, String name, String missing, String empty) {
        Element child = getChild(parent, name);
        if (child == null) {
            return missing;
        } else {
            String content = (String) getContent(child);
            return (content != null) ? content : empty;
        }
    }

    private static Object getContent(Element element) {
        NodeList nl = element.getChildNodes();
        StringBuffer content = new StringBuffer();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                return node;
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                content.append(node.getNodeValue());
                break;
            }
        }
        return content.toString().trim();
    }

    private static List<Element> getChildren(Element parent, String name) {
        List<Element> retVal = new LinkedList<Element>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element && name.equals(child.getNodeName())) {
                retVal.add((Element) child);
            }
        }
        return retVal;
    }

    private static Element getChild(Element parent, String name) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element && name.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }

    class LayerDetails {

        private String name;
        private String ident;
        private List<String> crsList;
        private List<LayerDetails> children;
        private Bounds bounds;
        private boolean supported;

        public LayerDetails(String name, String ident, List<String> crsList,
                boolean supportedLayer, Bounds bounds,
                List<LayerDetails> childLayers) {
            this.name = name;
            this.ident = ident;
            this.crsList = crsList;
            this.supported = supportedLayer;
            this.children = childLayers;
            this.bounds = bounds;
        }

        public boolean isSupported() {
            return this.supported;
        }

        @Override
        public String toString() {
            if(this.name == null || this.name.isEmpty()) {
                return this.ident;
            } else {
                return this.name;
            }
        }

    }

    class LayerTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                    row, hasFocus);
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof LayerDetails) {
                LayerDetails layer = (LayerDetails) userObject;
                setEnabled(layer.isSupported());
            }
            return this;
        }
    }

}
