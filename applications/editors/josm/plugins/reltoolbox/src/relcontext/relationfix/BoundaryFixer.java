// License: GPL. For details, see LICENSE file.
package relcontext.relationfix;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Utils;

/**
 * Fix multipolygon boundaries
 * @see <a href="https://wiki.openstreetmap.org/wiki/Relation:boundary">osmwiki:Relation:boundary</a>
 */
public class BoundaryFixer extends MultipolygonFixer {

    public BoundaryFixer() {
        super("boundary", "multipolygon");
    }

    /**
     * For boundary relations both "boundary" and "multipolygon" types are applicable, but
     * it should also have key boundary=administrative to be fully boundary.
     * @see <a href="https://wiki.openstreetmap.org/wiki/Relation:boundary">osmwiki:Relation:boundary</a>
     */
    @Override
    public boolean isFixerApplicable(Relation rel) {
        return super.isFixerApplicable(rel) && "administrative".equals(rel.get("boundary"));
    }

    @Override
    public boolean isRelationGood(Relation rel) {
        for (RelationMember m : rel.getMembers()) {
            if (m.getType().equals(OsmPrimitiveType.RELATION) && !"subarea".equals(m.getRole())) {
                setWarningMessage(tr("Relation without ''subarea'' role found"));
                return false;
            }
            if (m.getType().equals(OsmPrimitiveType.NODE) && !("label".equals(m.getRole()) || "admin_centre".equals(m.getRole()))) {
                setWarningMessage(tr("Node without ''label'' or ''admin_centre'' role found"));
                return false;
            }
            if (m.getType().equals(OsmPrimitiveType.WAY) && !("outer".equals(m.getRole()) || "inner".equals(m.getRole()))) {
                setWarningMessage(tr("Way without ''inner'' or ''outer'' role found"));
                return false;
            }
        }
        clearWarningMessage();
        return true;
    }

    @Override
    public Command fixRelation(Relation rel) {
        Relation r = rel;
        Relation rr = fixMultipolygonRoles(r);
        boolean fixed = false;
        if (rr != null) {
            fixed = true;
            r = rr;
        }
        rr = fixBoundaryRoles(r);
        if (rr != null) {
            fixed = true;
            r = rr;
        }
        if (fixed) {
            final DataSet ds = Utils.firstNonNull(rel.getDataSet(), MainApplication.getLayerManager().getEditDataSet());
            return new ChangeCommand(ds, rel, r);
        }
        return null;
    }

    private Relation fixBoundaryRoles(Relation source) {
        Relation r = new Relation(source);
        boolean fixed = false;
        for (int i = 0; i < r.getMembersCount(); i++) {
            RelationMember m = r.getMember(i);
            String role = null;
            if (m.isRelation()) {
                role = "subarea";
            } else if (m.isNode()) {
                Node n = (Node) m.getMember();
                if (!n.isIncomplete()) {
                    if (n.hasKey("place")) {
                        String place = n.get("place");
                        if (place.equals("state") || place.equals("country") ||
                                place.equals("county") || place.equals("region")) {
                            role = "label";
                        } else {
                            role = "admin_centre";
                        }
                    } else {
                        role = "label";
                    }
                }
            }
            if (role != null && !role.equals(m.getRole())) {
                r.setMember(i, new RelationMember(role, m.getMember()));
                fixed = true;
            }
        }
        return fixed ? r : null;
    }
}
