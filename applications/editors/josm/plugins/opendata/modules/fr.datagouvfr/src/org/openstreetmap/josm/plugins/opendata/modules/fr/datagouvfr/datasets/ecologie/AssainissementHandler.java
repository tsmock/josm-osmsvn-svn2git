// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.ecologie;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.DefaultSpreadSheetHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.SpreadSheetReader.CoordinateColumns;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;
import org.openstreetmap.josm.tools.Pair;

public class AssainissementHandler extends DataGouvDataSetHandler {

    public AssainissementHandler() {
        super("assainissement-collectif-30381843");
        setName("Assainissement collectif");
        setSpreadSheetHandler(new InternalOdsHandler());
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsOdsFilename(filename, "Export_ERU_20..");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        // Implemented in InternalOdsHandler.nodesAdded()
    }

    @Override
    public List<Pair<String, URL>> getDataURLs() {
        List<Pair<String, URL>> result = new ArrayList<>();
        try {
            result.add(new Pair<>("Données 2009", new URL("http://www.assainissement.developpement-durable.gouv.fr/telecharger2.php")));
            result.add(new Pair<>("Données 2010", new URL("http://www.assainissement.developpement-durable.gouv.fr/telecharger2_2010.php")));
            result.add(new Pair<>("Données 2011", new URL("http://www.assainissement.developpement-durable.gouv.fr/telecharger2_2011.php")));
            result.add(new Pair<>("Données 2012", new URL("http://www.assainissement.developpement-durable.gouv.fr/telecharger2_2012.php")));
            result.add(new Pair<>("Données 2013", new URL("http://www.assainissement.developpement-durable.gouv.fr/telecharger2_2013.php")));
            result.add(new Pair<>("Données 2014", new URL("http://www.assainissement.developpement-durable.gouv.fr/telecharger2_2014.php")));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    private final class InternalOdsHandler extends DefaultSpreadSheetHandler {

        private Node nodeWithKeys;
        
        private final Set<String> interestingKeys = new HashSet<>();
        
        public InternalOdsHandler() {
            setSheetNumber(1);
        }
        
        private void updateTag(Node node, String odsKey, String osmKey) {
            String value = nodeWithKeys.get(odsKey);
            removeTag(odsKey);
            if (value != null && !value.isEmpty()) {
                addTag(node, osmKey, value);
            }
        }
        
        private void addTag(Node node, String osmKey, String value) {
            node.put(osmKey, value);
            interestingKeys.add(osmKey);
        }
        
        private void removeTag(String odsKey) {
            nodeWithKeys.remove(odsKey);
        }
        
        private void removeUninterestingTags() {
            for (String key : nodeWithKeys.getKeys().keySet()) {
                if (!interestingKeys.contains(key)) {
                    nodeWithKeys.remove(key);
                }
            }
        }

        @Override
        public void nodesAdded(DataSet ds, Map<CoordinateColumns, Node> nodes, String[] header, int lineNumber) {
            Node steuNode = null;
            Node rejetNode = null;
            for (CoordinateColumns c : nodes.keySet()) {
                if (header[c.xCol].contains("STEU") && header[c.yCol].contains("STEU")) {
                    steuNode = nodes.get(c);
                } else if (header[c.xCol].contains("rejet") && header[c.yCol].contains("rejet")) {
                    rejetNode = nodes.get(c);
                } else {
                    System.err.println("Line "+lineNumber+": Unexpected coordinate columns: "+c);
                }
            }
            if (steuNode == null || rejetNode == null) {
                System.err.println("Line "+lineNumber+": 'STEU' or 'rejet' information not found");
                return;
            }
            nodeWithKeys = steuNode.hasKeys() ? steuNode : rejetNode;
            
            addTag(steuNode, "man_made", "wastewater_plant");
            updateTag(steuNode, "Code du STEU", "ref");
            updateTag(steuNode, "Nom du STEU", "name");
            updateTag(steuNode, "Date de mise en service du STEU", "start_date");
            updateTag(steuNode, "Date de mise hors service du STEU", "end_date");
            updateTag(steuNode, "Exploitant", "operator");
            updateTag(steuNode, "Capacité nominale en EH", "capacity");
            updateTag(rejetNode, "Nom du milieu de rejet", "name");
            
            Way pipeline = new Way();
            pipeline.addNode(steuNode);
            pipeline.addNode(rejetNode);
            if (pipeline.getLength() > 1) {
                //pipeline.put("man_made", "pipeline");
                //pipeline.put("type", "water");
                ds.addPrimitive(pipeline);
            }
            
            removeUninterestingTags();
        }
    }
}
