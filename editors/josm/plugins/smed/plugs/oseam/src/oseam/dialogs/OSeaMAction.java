package oseam.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;

import oseam.panels.*;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Map;

import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import oseam.Messages;
import oseam.seamarks.SeaMark;
import oseam.seamarks.SeaMark.Obj;
import oseam.seamarks.MarkCard;
import oseam.seamarks.MarkIsol;
import oseam.seamarks.MarkLat;
import oseam.seamarks.MarkLight;
import oseam.seamarks.MarkSpec;
import oseam.seamarks.MarkSaw;
import smed.plug.ifc.SmedPluginManager;

public class OSeaMAction {

    private SmedPluginManager manager;
    public PanelMain panelMain = null;

    public SeaMark mark = null;
    public Node node = null;
    private Collection<? extends OsmPrimitive> Selection = null;

    public SelectionChangedListener SmpListener = new SelectionChangedListener() {
        public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
            Node nextNode = null;
            Selection = newSelection;

            // System.out.println(newSelection);
            for (OsmPrimitive osm : Selection) {
                if (osm instanceof Node) {
                    nextNode = (Node) osm;
                    if (Selection.size() == 1) {
                        if (nextNode.compareTo(node) != 0) {
                            node = nextNode;
                            parseNode();
                        }
                    } else
                        manager.showVisualMessage(Messages.getString("OneNode"));
                }
            }
            if (nextNode == null) {
                node = null;
                mark = null;
                panelMain.clearSelections();
                manager.showVisualMessage(Messages.getString("SelectNode"));
            }
        }
    };

    public OSeaMAction(SmedPluginManager mngr) {

        DataSet.addSelectionListener(SmpListener);
        manager = mngr;
        String str = Main.pref.get("mappaint.style.sources");
        if (!str.contains("dev.openseamap.org")) {
            if (!str.isEmpty())
                str += new String(new char[] { 0x1e });
            Main.pref.put("mappaint.style.sources", str + "http://dev.openseamap.org/josm/seamark_styles.xml");
        }
        str = Main.pref.get("color.background");
        if (str.equals("#000000") || str.isEmpty())
            Main.pref.put("color.background", "#606060");
    }

    public JPanel getOSeaMPanel() {
        if (panelMain == null) {
            panelMain = new PanelMain(this);
            panelMain.setLayout(null);
            panelMain.setSize(new Dimension(400, 360));
        }
        return panelMain;
    }

    private void parseNode() {

        Map<String, String> keys;

        manager.showVisualMessage("");
        mark = null;
        String type = "";
        String str = "";

        keys = node.getKeys();

        if (keys.containsKey("seamark:type"))
            type = keys.get("seamark:type");
        if (type.equals("buoy_lateral")) {
            mark = new MarkLat(this);
            mark.setObject(Obj.BOYLAT);
        } else if (type.equals("beacon_lateral")) {
            mark = new MarkLat(this);
            mark.setObject(Obj.BCNLAT);
        } else if (type.equals("buoy_cardinal")) {
            mark = new MarkCard(this);
            mark.setObject(Obj.BOYCAR);
        } else if (type.equals("beacon_cardinal")) {
            mark = new MarkCard(this);
            mark.setObject(Obj.BCNCAR);
        } else if (type.equals("buoy_safe_water")) {
            mark = new MarkSaw(this);
            mark.setObject(Obj.BOYSAW);
        } else if (type.equals("beacon_safe_water")) {
            mark = new MarkSaw(this);
            mark.setObject(Obj.BCNSAW);
        } else if (type.equals("buoy_special_purpose")) {
            mark = new MarkSpec(this);
            mark.setObject(Obj.BOYSPP);
        } else if (type.equals("beacon_special_purpose")) {
            mark = new MarkSpec(this);
            mark.setObject(Obj.BCNSPP);
        } else if (type.equals("buoy_isolated_danger")) {
            mark = new MarkIsol(this);
            mark.setObject(Obj.BOYISD);
        } else if (type.equals("beacon_isolated_danger")) {
            mark = new MarkIsol(this);
            mark.setObject(Obj.BCNISD);
        } else if (type.equals("landmark") || type.equals("light_vessel") || type.equals("light_major") || type.equals("light_minor")) {
            mark = new MarkLight(this);
            mark.setObject(Obj.LIGHTS);
        } else if (type.equals("light_float")) {
            if (keys.containsKey("seamark:light_float:colour")) {
                str = keys.get("seamark:light_float:colour");
                if (str.equals("red") || str.equals("green") || str.equals("red;green;red") || str.equals("green;red;green")) {
                    mark = new MarkLat(this);
                    mark.setObject(Obj.BOYLAT);
                } else if (str.equals("black;yellow") || str.equals("black;yellow;black") || str.equals("yellow;black")
                        || str.equals("yellow;black;yellow")) {
                    mark = new MarkCard(this);
                    mark.setObject(Obj.BOYCAR);
                } else if (str.equals("black;red;black")) {
                    mark = new MarkIsol(this);
                    mark.setObject(Obj.BOYISD);
                } else if (str.equals("red;white")) {
                    mark = new MarkSaw(this);
                    mark.setObject(Obj.BOYSAW);
                } else if (str.equals("yellow")) {
                    mark = new MarkSpec(this);
                    mark.setObject(Obj.BOYSPP);
                }
            } else if (keys.containsKey("seamark:light_float:topmark:shape")) {
                str = keys.get("seamark:light_float:topmark:shape");
                if (str.equals("cylinder") || str.equals("cone, point up")) {
                    mark = new MarkLat(this);
                    mark.setObject(Obj.BOYLAT);
                }
            } else if (keys.containsKey("seamark:light_float:topmark:colour")) {
                str = keys.get("seamark:light_float:topmark:colour");
                if (str.equals("red") || str.equals("green")) {
                    mark = new MarkLat(this);
                    mark.setObject(Obj.BOYLAT);
                }
            }
        } else if (keys.containsKey("buoy_lateral:category") || keys.containsKey("buoy_lateral:shape") || keys.containsKey("buoy_lateral:colour")) {
            mark = new MarkLat(this);
            mark.setObject(Obj.BOYLAT);
        } else if (keys.containsKey("beacon_lateral:category") || keys.containsKey("beacon_lateral:shape") || keys.containsKey("beacon_lateral:colour")) {
            mark = new MarkLat(this);
            mark.setObject(Obj.BCNLAT);
        } else if (keys.containsKey("buoy_cardinal:category") || keys.containsKey("buoy_cardinal:shape") || keys.containsKey("buoy_cardinal:colour")) {
            mark = new MarkCard(this);
            mark.setObject(Obj.BOYCAR);
        } else if (keys.containsKey("beacon_cardinal:category") || keys.containsKey("beacon_cardinal:shape") || keys.containsKey("beacon_cardinal:colour")) {
            mark = new MarkCard(this);
            mark.setObject(Obj.BCNCAR);
        } else if (keys.containsKey("buoy_isolated_danger:category") || keys.containsKey("buoy_isolated_danger:shape") || keys.containsKey("buoy_isolated_danger:colour")) {
            mark = new MarkIsol(this);
            mark.setObject(Obj.BOYISD);
        } else if (keys.containsKey("beacon_isolated_danger:category") || keys.containsKey("beacon_isolated_danger:shape") || keys.containsKey("beacon_isolated_danger:colour")) {
            mark = new MarkIsol(this);
            mark.setObject(Obj.BCNISD);
        } else if (keys.containsKey("buoy_safe_water:category") || keys.containsKey("buoy_safe_water:shape") || keys.containsKey("buoy_safe_water:colour")) {
            mark = new MarkSaw(this);
            mark.setObject(Obj.BOYSAW);
        } else if (keys.containsKey("beacon_safe_water:category") || keys.containsKey("beacon_safe_water:shape") || keys.containsKey("beacon_safe_water:colour")) {
            mark = new MarkSaw(this);
            mark.setObject(Obj.BCNSAW);
        } else if (keys.containsKey("buoy_special_purpose:category") || keys.containsKey("buoy_special_purpose:shape") || keys.containsKey("buoy_special_purpose:colour")) {
            mark = new MarkSpec(this);
            mark.setObject(Obj.BOYSPP);
        } else if (keys.containsKey("beacon_special_purpose:category") || keys.containsKey("beacon_special_purpose:shape") || keys.containsKey("beacon_special_purpose:colour")) {
            mark = new MarkSpec(this);
            mark.setObject(Obj.BCNSPP);
        }

        if (mark == null) {
            manager.showVisualMessage(Messages.getString("NoMark"));
            panelMain.clearSelections();
        } else {
            mark.parseMark();
            mark.paintSign();
        }
    }
}
