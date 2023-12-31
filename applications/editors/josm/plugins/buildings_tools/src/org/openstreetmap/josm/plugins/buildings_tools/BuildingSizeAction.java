// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

public class BuildingSizeAction extends JosmAction {

    public BuildingSizeAction() {
        super(tr("Set buildings size"), "mapmode/building", tr("Set buildings size"),
                Shortcut.registerShortcut("edit:buildingsdialog", tr("Data: {0}", tr("Set buildings size")),
                KeyEvent.VK_B, Shortcut.ALT_CTRL),
                true, "edit:buildingsdialog", false);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        BuildingSizeDialog dlg = new BuildingSizeDialog();
        if (dlg.getValue() == 1) {
            dlg.saveSettings();
        }
    }
}
