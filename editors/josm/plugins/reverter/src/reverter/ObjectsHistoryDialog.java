// License: GPL. For details, see LICENSE file.
package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;

@SuppressWarnings("serial")
public class ObjectsHistoryDialog extends ExtendedDialog {
    public ObjectsHistoryDialog() {
        super(MainApplication.getMainFrame(), tr("Objects history"), new String[] {"Revert", "Cancel"}, false);
        contentInsets = new Insets(10, 10, 10, 5);
        setButtonIcons(new String[] {"ok.png", "cancel.png" });
        setContent(new JPanel(new GridBagLayout()));
        setupDialog();
    }
}
