/**
 * 
 */
package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.MainApplication;

/**
 * @author tilusnet <tilusnet@gmail.com>
 * 
 */
public class AlignWaysSelBothState extends AlignWaysState {

    @Override
    public void leftClick(AlignWaysMode alignWaysMode) {
        // No state change, nothing to do
    }

    @Override
    public void ctrlLClick(AlignWaysMode alignWaysMode) {
        // No state change, nothing to do
    }

    @Override
    public void setHelpText() {
        MainApplication.getMap().statusLine
        .setHelpText(AlignWaysPlugin.getAwAction().getShortcut().getKeyText() +
                tr(": Align segments; Alt-click: Clear selection"));
    }

}
