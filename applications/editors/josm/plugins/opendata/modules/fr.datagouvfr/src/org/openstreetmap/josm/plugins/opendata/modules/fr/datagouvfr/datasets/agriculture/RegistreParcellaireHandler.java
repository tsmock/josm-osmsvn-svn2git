// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.agriculture;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.preferences.sources.ExtendedSourceEntry;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchAdministrativeUnit;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;
import org.openstreetmap.josm.tools.Pair;

public class RegistreParcellaireHandler extends DataGouvDataSetHandler {
    
    protected static final int PAS_D_INFORMATION = 0;
    protected static final int BLE_TENDRE = 1;
    protected static final int MAIS_GRAIN_ET_ENSILAGE = 2;
    protected static final int ORGE = 3;
    protected static final int AUTRES_CEREALES = 4;
    protected static final int COLZA = 5;
    protected static final int TOURNESOL = 6;
    protected static final int AUTRES_OLEAGINEUX = 7;
    protected static final int PROTEAGINEUX = 8;
    protected static final int PLANTES_A_FIBRES = 9;
    protected static final int SEMENCES = 10;
    protected static final int GEL_SURFACES_GELEES_SANS_PRODUCTION = 11;
    protected static final int GEL_INDUSTRIEL = 12;
    protected static final int AUTRES_GELS = 13;
    protected static final int RIZ = 14;
    protected static final int LEGUMINEUSES_A_GRAINS = 15;
    protected static final int FOURRAGE = 16;
    protected static final int ESTIVES_LANDES = 17;
    protected static final int PRAIRIES_PERMANENTES = 18;
    protected static final int PRAIRIES_TEMPORAIRES = 19;
    protected static final int VERGERS = 20;
    protected static final int VIGNES = 21;
    protected static final int FRUITS_A_COQUE = 22;
    protected static final int OLIVIERS = 23;
    protected static final int AUTRES_CULTURES_INDUSTRIELLES = 24;
    protected static final int LEGUMES_FLEURS = 25;
    protected static final int CANNE_A_SUCRE = 26;
    protected static final int ARBORICULTURE = 27;
    protected static final int DIVERS = 28;
    
    public RegistreParcellaireHandler() {
        super();
        setName("Registre Parcellaire Graphique");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsShpFilename(filename, "RPG_20.._...");
    }

    @Override
    public ExtendedSourceEntry getMapPaintStyle() {
        return getMapPaintStyle("Registre Parcellaire Graphique (France)");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (OsmPrimitive p : ds.allPrimitives()) {
            String code = p.get("CULT_MAJ");
            
            if (code != null && !code.isEmpty()) {
                replace(p, "NUM_ILOT", "ref:FR:RPG");
                replace(p, "CULT_MAJ", "code:FR:RPG");
                
                switch (Integer.parseInt(code)) {
                case ARBORICULTURE:
                    p.put("landuse", "forest");
                    break;
                case FOURRAGE:
                case PRAIRIES_PERMANENTES:
                case PRAIRIES_TEMPORAIRES:
                case ESTIVES_LANDES:
                case GEL_SURFACES_GELEES_SANS_PRODUCTION:
                case GEL_INDUSTRIEL:
                case AUTRES_GELS:
                    p.put("landuse", "meadow");
                    break;
                case OLIVIERS:
                    p.put("trees", "olive_tree");
                case VERGERS:
                    p.put("landuse", "orchard");
                    break;
                case VIGNES:
                    p.put("landuse", "vineyard");
                    break;
                case PAS_D_INFORMATION:
                case BLE_TENDRE:
                case MAIS_GRAIN_ET_ENSILAGE:
                case ORGE:
                case AUTRES_CEREALES:
                case COLZA:
                case TOURNESOL:
                case AUTRES_OLEAGINEUX:
                case PROTEAGINEUX:
                case PLANTES_A_FIBRES:
                case SEMENCES:
                case RIZ:
                case LEGUMINEUSES_A_GRAINS:
                case FRUITS_A_COQUE:
                case AUTRES_CULTURES_INDUSTRIELLES:
                case LEGUMES_FLEURS:
                case CANNE_A_SUCRE:
                case DIVERS:
                default:
                    p.put("landuse", "farm");
                }
            }
        }
    }

    private Pair<String, URL> getRpgURL(String number, String name) throws MalformedURLException {
        return new Pair<>(number+" - "+name, new URL("http://www.data.gouv.fr/var/download/ign/RPG_2010_"+number+".ZIP"));
    }
    
    @Override
    public List<Pair<String, URL>> getDataURLs() {
        List<Pair<String, URL>> result = new ArrayList<>();
        try {
            for (FrenchAdministrativeUnit dpt : FrenchAdministrativeUnit.allDepartments) {
                result.add(getRpgURL(dpt.getCode(), dpt.getName()));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
