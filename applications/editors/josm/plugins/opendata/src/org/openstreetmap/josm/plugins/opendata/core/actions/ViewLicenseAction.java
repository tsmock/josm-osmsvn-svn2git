// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.Action;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.gui.ViewLicenseDialog;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Logging;

public class ViewLicenseAction extends JosmAction {

    private final License license;
    
    public ViewLicenseAction(License license, String title, String description) {
        super(title, null, description, null, false);
        CheckParameterUtil.ensureParameterNotNull(license, "license");
        this.license = license;
        putValue(Action.SMALL_ICON, OdUtils.getImageIcon(OdConstants.ICON_AGREEMENT_24));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            new ViewLicenseDialog(license).showDialog();
        } catch (IOException ex) {
            Logging.error(ex);
        }
    }
}
