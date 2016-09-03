// License: GPL. For details, see LICENSE file.
package reverter;

import javax.swing.JMenu;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.io.remotecontrol.RemoteControl;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class ReverterPlugin extends Plugin {
    static boolean reverterUsed = false;
    public ReverterPlugin(PluginInformation info) {
        super(info);
        JMenu historyMenu = Main.main.menu.dataMenu;
        //MainMenu.add(historyMenu, new ObjectsHistoryAction());
        MainMenu.add(historyMenu, new RevertChangesetAction());
        UploadAction.registerUploadHook(new ReverterUploadHook(this));
        new RemoteControl().addRequestHandler(RevertChangesetHandler.command, RevertChangesetHandler.class);
    }
}
