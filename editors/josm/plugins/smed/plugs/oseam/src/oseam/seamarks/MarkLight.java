package oseam.seamarks;

import java.util.Map;

import org.openstreetmap.josm.data.osm.Node;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class MarkLight extends SeaMark {
    public MarkLight(OSeaMAction dia, Node node) {
        super(dia, node);
    }
    
    public void parseMark() {

        Map<String, String> keys;
        keys = getNode().getKeys();

        if (!dlg.panelMain.lightsButton.isSelected())
            dlg.panelMain.lightsButton.doClick();

        if (keys.containsKey("name"))
            setName(keys.get("name"));

        if (keys.containsKey("seamark:name"))
            setName(keys.get("seamark:name"));

        if (keys.containsKey("seamark:landmark:name"))
            setName(keys.get("seamark:landmark:name"));
        else if (keys.containsKey("seamark:light_major:name"))
            setName(keys.get("seamark:light_major:name"));
        else if (keys.containsKey("seamark:light_minor:name"))
            setName(keys.get("seamark:light_minor:name"));
        else if (keys.containsKey("seamark:light_vessel:name"))
            setName(keys.get("seamark:light_vessel:name"));
        else if (keys.containsKey("seamark:light_float:name"))
            setName(keys.get("seamark:light_float:name"));

        /*
        if (keys.containsKey("seamark:type")) {
            String type = keys.get("seamark:type");
            if (type.equals("landmark"))
                setBuoyIndex(LIGHT_HOUSE);
            else if (type.equals("light_major"))
                setBuoyIndex(LIGHT_MAJOR);
            else if (type.equals("light_minor"))
                setBuoyIndex(LIGHT_MINOR);
            else if (type.equals("light_vessel"))
                setBuoyIndex(LIGHT_VESSEL);
        }

        parseLights(keys);
        parseFogRadar(keys);
        setTopMark(false);
        setFired(true);

        dlg.cbM01CatOfMark.setSelectedIndex(getBuoyIndex());
        dlg.tfM01Name.setText(getName());
        dlg.cM01Fired.setEnabled(false);
        dlg.cM01Fired.setSelected(true);
*/	}
/*
    public boolean isValid() {
        return (getBuoyIndex() > 0);
    }
*/
    public void paintSign() {
/*		if (dlg.paintlock)
            return;
        super.paintSign();

        dlg.sM01StatusBar.setText(getErrMsg());

        if (isValid()) {
            dlg.cM01Radar.setVisible(true);
            dlg.cM01Racon.setVisible(true);
            dlg.cM01Fog.setVisible(true);

            dlg.rbM01Fired1.setVisible(true);
            dlg.rbM01FiredN.setVisible(true);
            dlg.lM01Height.setVisible(true);
            dlg.tfM01Height.setVisible(true);
            dlg.lM01Range.setVisible(true);
            dlg.tfM01Range.setVisible(true);

            switch (getBuoyIndex()) {
            case SeaMark.LIGHT_HOUSE:
                dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(
                        "/images/Light_House.png")));
                break;

            case SeaMark.LIGHT_MAJOR:
                dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(
                        "/images/Light_Major.png")));
                break;

            case SeaMark.LIGHT_MINOR:
                dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(
                        "/images/Light_Minor.png")));
                break;

            case SeaMark.LIGHT_VESSEL:
                dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(
                        "/images/Major_Float.png")));
                break;

            default:
            }
        }
*/	}

    public void saveSign() {
        Node node = getNode();

        if (node == null) {
            return;
        }

        switch (getCategory()) {
        case LIGHT_HOUSE:
            super.saveSign("landmark");
            break;
        case LIGHT_MAJOR:
            super.saveSign("light_major");
            break;
        case LIGHT_MINOR:
            super.saveSign("light_minor");
            break;
        case LIGHT_VESSEL:
            super.saveSign("light_vessel");
            break;
        default:
        }
        saveLightData();
        saveRadarFogData();
    }

    public void setLightColour() {
    }

}
