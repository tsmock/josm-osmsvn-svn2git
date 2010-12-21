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
import oseam.seamarks.MarkCard;
import oseam.seamarks.MarkIsol;
import oseam.seamarks.MarkLat;
import oseam.seamarks.MarkLight;
import oseam.seamarks.MarkSpec;
import oseam.seamarks.MarkUkn;
import oseam.seamarks.MarkSaw;
import smed.plug.ifc.SmedPluginManager;

public class OSeaMAction {

	private SmedPluginManager manager;
	public PanelMain panelMain = null;

	public SeaMark mark = null;
	public Node node = null;
	private Collection<? extends OsmPrimitive> Selection = null;
	private OsmPrimitive lastNode = null;

	public SelectionChangedListener SmpListener = new SelectionChangedListener() {
		public void selectionChanged(
				Collection<? extends OsmPrimitive> newSelection) {
			Node nextNode = null;
			Selection = newSelection;

			// System.out.println(newSelection);
			for (OsmPrimitive osm : Selection) {
				if (osm instanceof Node) {
					nextNode = (Node) osm;
					if (Selection.size() == 1) {
						if (nextNode.compareTo(lastNode) != 0) {
							lastNode = nextNode;
							parseNode(nextNode);
						}
					} else
						manager.showVisualMessage(Messages.getString("OneNode"));
				}
			}
			if (nextNode == null) {
				panelMain.clearSelections();
				lastNode = null;
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
			Main.pref.put("mappaint.style.sources", str
					+ "http://dev.openseamap.org/josm/seamark_styles.xml");
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

	private void parseNode(Node newNode) {

		Map<String, String> keys;

		manager.showVisualMessage("");
		node = newNode;
		mark = null;
		String type = "";
		String str = "";

		keys = node.getKeys();

		if (keys.containsKey("seamark:type"))
			type = keys.get("seamark:type");
		if (type.equals("buoy_lateral") || type.equals("beacon_lateral")) {
			mark = new MarkLat(this);
		} else if (type.equals("buoy_cardinal")
				|| type.equals("beacon_cardinal")) {
			mark = new MarkCard(this);
		} else if (type.equals("buoy_safe_water")
				|| type.equals("beacon_safe_water")) {
			mark = new MarkSaw(this);
		} else if (type.equals("buoy_special_purpose")
				|| type.equals("beacon_special_purpose")) {
			mark = new MarkSpec(this);
		} else if (type.equals("buoy_isolated_danger")
				|| type.equals("beacon_isolated_danger")) {
			mark = new MarkIsol(this);
		} else if (type.equals("landmark") || type.equals("light_vessel")
				|| type.equals("light_major") || type.equals("light_minor")) {
			mark = new MarkLight(this);
		} else if (type.equals("light_float")) {
			if (keys.containsKey("seamark:light_float:colour")) {
				str = keys.get("seamark:light_float:colour");
				if (str.equals("red") || str.equals("green")
						|| str.equals("red;green;red")
						|| str.equals("green;red;green")) {
					mark = new MarkLat(this);
				} else if (str.equals("black;yellow")
						|| str.equals("black;yellow;black")
						|| str.equals("yellow;black")
						|| str.equals("yellow;black;yellow")) {
					mark = new MarkCard(this);
				} else if (str.equals("black;red;black")) {
					mark = new MarkIsol(this);
				} else if (str.equals("red;white")) {
					mark = new MarkSaw(this);
				} else if (str.equals("yellow")) {
					mark = new MarkSpec(this);
				}
			} else if (keys.containsKey("seamark:light_float:topmark:shape")) {
				str = keys.get("seamark:light_float:topmark:shape");
				if (str.equals("cylinder") || str.equals("cone, point up")) {
					mark = new MarkLat(this);
				}
			} else if (keys.containsKey("seamark:light_float:topmark:colour")) {
				str = keys.get("seamark:light_float:topmark:colour");
				if (str.equals("red") || str.equals("green")) {
					mark = new MarkLat(this);
				}
			}
		} else if (keys.containsKey("buoy_lateral:category")
				|| keys.containsKey("beacon_lateral:category")) {
			mark = new MarkLat(this);
		} else if (keys.containsKey("buoy_cardinal:category")
				|| keys.containsKey("beacon_cardinal:category")) {
			mark = new MarkCard(this);
		} else if (keys.containsKey("buoy_isolated_danger:category")
				|| keys.containsKey("beacon_isolated_danger:category")) {
			mark = new MarkIsol(this);
		} else if (keys.containsKey("buoy_safe_water:category")
				|| keys.containsKey("beacon_safe_water:category")) {
			mark = new MarkSaw(this);
		} else if (keys.containsKey("buoy_special_purpose:category")
				|| keys.containsKey("beacon_special_purpose:category")) {
			mark = new MarkSpec(this);
		} else if (keys.containsKey("buoy_lateral:shape")
				|| keys.containsKey("beacon_lateral:shape")) {
			mark = new MarkLat(this);
		} else if (keys.containsKey("buoy_cardinal:shape")
				|| keys.containsKey("beacon_cardinal:shape")) {
			mark = new MarkCard(this);
		} else if (keys.containsKey("buoy_isolated_danger:shape")
				|| keys.containsKey("beacon_isolated_danger:shape")) {
			mark = new MarkIsol(this);
		} else if (keys.containsKey("buoy_safe_water:shape")
				|| keys.containsKey("beacon_safe_water:shape")) {
			mark = new MarkSaw(this);
		} else if (keys.containsKey("buoy_special_purpose:shape")
				|| keys.containsKey("beacon_special_purpose:shape")) {
			mark = new MarkSpec(this);
		} else if (keys.containsKey("buoy_lateral:colour")
				|| keys.containsKey("beacon_lateral:colour")) {
			mark = new MarkLat(this);
		} else if (keys.containsKey("buoy_cardinal:colour")
				|| keys.containsKey("beacon_cardinal:colour")) {
			mark = new MarkCard(this);
		} else if (keys.containsKey("buoy_isolated_danger:colour")
				|| keys.containsKey("beacon_isolated_danger:colour")) {
			mark = new MarkIsol(this);
		} else if (keys.containsKey("buoy_safe_water:colour")
				|| keys.containsKey("beacon_safe_water:colour")) {
			mark = new MarkSaw(this);
		} else if (keys.containsKey("buoy_special_purpose:colour")
				|| keys.containsKey("beacon_special_purpose:colour")) {
			mark = new MarkSpec(this);
		}

		if (mark == null) {
			manager.showVisualMessage(Messages.getString("NoMark"));
			mark = new MarkUkn(this);
		} else {
			mark.parseMark();
			mark.paintSign();
		}
	}
}
