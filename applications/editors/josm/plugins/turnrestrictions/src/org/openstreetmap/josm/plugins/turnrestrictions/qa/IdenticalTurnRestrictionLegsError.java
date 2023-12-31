// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Issue when the 'from' and 'to' leg are identical.
 *
 */
public class IdenticalTurnRestrictionLegsError extends Issue {
    private OsmPrimitive leg;

    public IdenticalTurnRestrictionLegsError(IssuesModel parent, OsmPrimitive leg) {
        super(parent, Severity.ERROR);
        actions.add(new DeleteFromAction());
        actions.add(new DeleteToAction());
        actions.add(new FixInEditorAction());
        this.leg = leg;
    }

    @Override
    public String getText() {
        // CHECKSTYLE.OFF: LineLength
        return tr("This turn restriction uses the way <span class=\"object-name\">{0}</span> with role <tt>from</tt> <strong>and</strong> with role <tt>to</tt>. "
                + "In a turn restriction, the way with role <tt>from</tt> should be different from the way with role <tt>to</tt>, though.",
                leg.getDisplayName(DefaultNameFormatter.getInstance())
                );
        // CHECKSTYLE.ON: LineLength
    }

    class DeleteFromAction extends AbstractAction {
        DeleteFromAction() {
            putValue(NAME, tr("Delete ''from''"));
            putValue(SHORT_DESCRIPTION, tr("Removes the member with role ''from''"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getIssuesModel().getEditorModel().getRelationMemberEditorModel().setFromPrimitive(null);
        }
    }

    class DeleteToAction extends AbstractAction {
        DeleteToAction() {
            putValue(NAME, tr("Delete ''to''"));
            putValue(SHORT_DESCRIPTION, tr("Removes the member with role ''to''"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getIssuesModel().getEditorModel().getRelationMemberEditorModel().setToPrimitive(null);
        }
    }

    class FixInEditorAction extends AbstractAction {
        FixInEditorAction() {
            putValue(NAME, tr("Fix in editor"));
            putValue(SHORT_DESCRIPTION, tr("Go to Basic Editor and manually choose members with roles ''from'' and ''to''"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getIssuesModel().getNavigationControler().gotoBasicEditor();
        }
    }
}
