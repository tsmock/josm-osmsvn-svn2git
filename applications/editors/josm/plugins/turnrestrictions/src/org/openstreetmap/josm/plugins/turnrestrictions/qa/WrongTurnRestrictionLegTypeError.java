// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.NavigationControler;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.NavigationControler.BasicEditorFokusTargets;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.RelationMemberEditorModel;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionLegRole;

/**
 * Issue if the type of a turn restriction leg is either an OSM node or an OSM relation.
 *
 */
public class WrongTurnRestrictionLegTypeError extends Issue {
    private TurnRestrictionLegRole role;
    private OsmPrimitive leg;

    /**
     * Create the issue
     *
     * @param parent the parent model
     * @param role the role of the turn restriction leg
     * @param leg the leg
     */
    public WrongTurnRestrictionLegTypeError(IssuesModel parent, TurnRestrictionLegRole role, OsmPrimitive leg) {
        super(parent, Severity.ERROR);
        this.role = role;
        this.leg = leg;
        actions.add(new DeleteAction());
        actions.add(new FixInEditorAction());
    }

    @Override
    public String getText() {
        String msg = null;
        switch(leg.getType()) {
        case NODE:
            msg = tr(
                "This turn restriction uses the node <span class=\"object-name\">{0}</span> as member with role <tt>{1}</tt>.",
                leg.getDisplayName(DefaultNameFormatter.getInstance()),
                role.toString()
            );
            break;
        case RELATION:
            msg = tr("This turn restriction uses the relation <span class=\"object-name\">{0}</span> as member with role <tt>{1}</tt>.",
                    leg.getDisplayName(DefaultNameFormatter.getInstance()),
                    role.toString()
                );
            break;
        default:
            throw new AssertionError("Unexpected type for leg: "+leg.getType());
        }
        return msg + " " + tr("A way is required instead.");
    }

    class DeleteAction extends AbstractAction {
        DeleteAction() {
            putValue(NAME, tr("Delete"));
            putValue(SHORT_DESCRIPTION, tr("Delete the member from the turn restriction"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RelationMemberEditorModel model = getIssuesModel().getEditorModel().getRelationMemberEditorModel();
            switch(role) {
            case FROM:
                model.setFromPrimitive(null);
                break;
            case TO:
                model.setToPrimitive(null);
                break;
            }
        }
    }

    class FixInEditorAction extends AbstractAction {
        FixInEditorAction() {
            putValue(NAME, tr("Fix in editor"));
            putValue(SHORT_DESCRIPTION, tr("Change to the Basic Editor and select a way"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NavigationControler controler = getIssuesModel().getNavigationControler();
            switch(role) {
            case FROM:
                controler.gotoBasicEditor(BasicEditorFokusTargets.FROM);
                break;
            case TO:
                controler.gotoBasicEditor(BasicEditorFokusTargets.TO);
                break;
            }
        }
    }
}
