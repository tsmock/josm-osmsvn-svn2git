package org.openstreetmap.josm.plugins.walkingpapers;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * Main class for the walking papers plugin.
 *
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class WalkingPapersPlugin extends Plugin
{
    static JMenu walkingPapersMenu;

    public WalkingPapersPlugin()
    {
        MainMenu menu = Main.main.menu;
        walkingPapersMenu = menu.addMenu(marktr("Walking Papers"), KeyEvent.VK_K, menu.defaultMenuPos, ht("/Plugin/WalkingPapers"));
        walkingPapersMenu.add(new JMenuItem(new WalkingPapersAddLayerAction()));
    }
}
