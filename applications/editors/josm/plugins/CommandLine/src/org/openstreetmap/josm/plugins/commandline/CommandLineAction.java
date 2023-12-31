// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.commandline;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

public class CommandLineAction extends JosmAction {
    private final CommandLine parentPlugin;

    public CommandLineAction(CommandLine parentPlugin) {
        super(tr("Command line"), "commandline", tr("Set input focus to the command line."),
                Shortcut.registerShortcut("tool:commandline", tr("Commands: {0}", tr("Command line")), KeyEvent.VK_ENTER, Shortcut.DIRECT), true, "commandline", true);
        this.parentPlugin = parentPlugin;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        parentPlugin.activate();
    }
}
