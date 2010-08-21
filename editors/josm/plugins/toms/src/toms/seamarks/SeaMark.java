//License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig & Malcolm Herring

package toms.seamarks;

import java.util.Iterator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Node;

abstract public class SeaMark {

    /**
     * CONSTANTS
     */

    /**
     * Colours
     */

    public final static int UNKNOWN_COLOUR = 0;
    public final static int RED = 1;
    public final static int GREEN = 2;
    public final static int RED_GREEN_RED = 3;
    public final static int GREEN_RED_GREEN = 4;
    public final static int RED_WHITE = 5;
    public final static int BLACK_YELLOW = 6;
    public final static int BLACK_YELLOW_BLACK = 7;
    public final static int YELLOW_BLACK = 8;
    public final static int YELLOW_BLACK_YELLOW = 9;
    public final static int BLACK_RED_BLACK = 10;
    public final static int YELLOW = 11;
    public final static int RED_LIGHT = 1;
    public final static int GREEN_LIGHT = 2;
    public final static int WHITE_LIGHT = 3;

    /**
     * Types - correspond to TypeIndex
     */
    public final static int UNKNOWN_CAT = 0;
    public final static int PORT_HAND = 1;
    public final static int STARBOARD_HAND = 2;
    public final static int PREF_PORT_HAND = 3;
    public final static int PREF_STARBOARD_HAND = 4;
    public final static int SAFE_WATER = 5;
    public final static int CARD_NORTH = 6;
    public final static int CARD_EAST = 7;
    public final static int CARD_SOUTH = 8;
    public final static int CARD_WEST = 9;
    public final static int ISOLATED_DANGER = 10;
    public final static int SPECIAL_PURPOSE = 11;
    public final static int LIGHT = 12;

    /**
     * Regions
     */
    public final static boolean IALA_A = false;
    public final static boolean IALA_B = true;

    /**
     * Shapes - correspond to StyleIndex
     */
    public final static int UNKNOWN_SHAPE = 0;
    public final static int LAT_CAN = 1;
    public final static int LAT_CONE = 1;
    public final static int LAT_PILLAR = 2;
    public final static int LAT_SPAR = 3;
    public final static int LAT_BEACON = 4;
    public final static int LAT_TOWER = 5;
    public final static int LAT_FLOAT = 6;
    public final static int LAT_PERCH = 7;
    public final static int CARD_PILLAR = 1;
    public final static int CARD_SPAR = 2;
    public final static int CARD_BEACON = 3;
    public final static int CARD_TOWER = 4;
    public final static int CARD_FLOAT = 5;
    public final static int SAFE_PILLAR = 1;
    public final static int SAFE_SPAR = 2;
    public final static int SAFE_SPHERE = 3;
    public final static int SAFE_BEACON = 4;
    public final static int SAFE_FLOAT = 5;
    public final static int ISOL_PILLAR = 1;
    public final static int ISOL_SPAR = 2;
    public final static int ISOL_BEACON = 3;
    public final static int ISOL_TOWER = 4;
    public final static int ISOL_FLOAT = 5;
    public final static int SPEC_PILLAR = 1;
    public final static int SPEC_SPAR = 2;
    public final static int SPEC_SPHERE = 3;
    public final static int SPEC_BARREL = 4;
    public final static int SPEC_FLOAT = 5;
    public final static int SPEC_BEACON = 6;
    public final static int LIGHT_HOUSE = 1;
    public final static int LIGHT_MAJOR = 2;
    public final static int LIGHT_MINOR = 3;
    public final static int LIGHT_FLOAT = 4;

    /**
     * Variables
     */

    /**
     * private Variablen
     */

    public abstract void paintSign();

    public abstract void saveSign();

    private int Colour = UNKNOWN_COLOUR;

    public int getColour() {
        return Colour;
    }

    public void setColour(int colour) {
        if (colour < UNKNOWN_COLOUR || colour > RED_WHITE) {
            return;
        }
        Colour = colour;

    }

    private String ErrMsg = null;

    public String getErrMsg() {
        return ErrMsg;
    }

    public void setErrMsg(String errMsg) {
        ErrMsg = errMsg;
    }

    private String Name;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    private boolean valid = true;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;

    }

    protected void delSeaMarkKeys(Node node) {
        Iterator<String> it = node.getKeys().keySet().iterator();
        String str;

        while (it.hasNext()) {
            str = it.next();

            if (str.contains("seamark") == true)
                if (str.compareTo("seamark") != 0) {
                    Main.main.undoRedo.add(new ChangePropertyCommand(node, str, null));
                }
        }
    }

}
