// License: GPL. For details, see LICENSE file.
package relcontext.relationfix;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.MainApplication;

import org.openstreetmap.josm.tools.Utils;
import relcontext.actions.PublicTransportHelper;

/**
 * Helper function for determinate role in public_transport relation
 * @author freeExec
 * @see <a href="https://wiki.openstreetmap.org/wiki/Key:public_transport">osmwiki:Key:public_transport</a>
 */
public class PublicTransportFixer extends RelationFixer {

    public PublicTransportFixer() {
        super("route", "public_transport");
    }

    /*protected PublicTransportFixer(String...types) {
        super(types);
    }*/

    @Override
    public boolean isRelationGood(Relation rel) {
        for (RelationMember m : rel.getMembers()) {
            if (m.getType().equals(OsmPrimitiveType.NODE)
                    && !(m.getRole().startsWith(PublicTransportHelper.STOP) || m.getRole().startsWith(PublicTransportHelper.PLATFORM))) {
                setWarningMessage(tr("Node without ''stop'' or ''platform'' role found"));
                return false;
            }
            if (m.getType().equals(OsmPrimitiveType.WAY)
                    && PublicTransportHelper.isWayPlatform(m)
                    && !m.getRole().startsWith(PublicTransportHelper.PLATFORM)) {
                setWarningMessage(tr("Way platform without ''platform'' role found") + " r" + m.getUniqueId());
                return false;
            }
        }
        clearWarningMessage();
        return true;
    }

    /*@Override
    public boolean isFixerApplicable(Relation rel) {
        return true;
    }*/

    @Override
    public Command fixRelation(Relation rel) {
        Relation r = rel;
        Relation rr = fixStopPlatformRole(r);
        boolean fixed = false;
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

    private Relation fixStopPlatformRole(Relation source) {
        Relation r = new Relation(source);
        boolean fixed = false;
        for (int i = 0; i < r.getMembersCount(); i++) {
            RelationMember m = r.getMember(i);
            String role = PublicTransportHelper.getRoleByMember(m);

            if (role != null && !m.getRole().startsWith(role)) {
                r.setMember(i, new RelationMember(role, m.getMember()));
                fixed = true;
            }
        }
        return fixed ? r : null;
    }
}
