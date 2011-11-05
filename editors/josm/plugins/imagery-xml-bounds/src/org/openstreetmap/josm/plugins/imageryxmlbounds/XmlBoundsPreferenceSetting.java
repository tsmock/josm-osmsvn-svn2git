//    JOSM Imagery XML Bounds plugin.
//    Copyright (C) 2011 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.imageryxmlbounds;

import javax.swing.JButton;

import org.openstreetmap.josm.gui.preferences.ImageryPreference;
import org.openstreetmap.josm.gui.preferences.ImageryPreference.ImageryProvidersPanel;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.EditEntriesAction;

/**
 * 
 * @author Don-vip
 *
 */
public class XmlBoundsPreferenceSetting implements PreferenceSetting {

	@Override
	public void addGui(PreferenceTabbedPane gui) {
		for (PreferenceSetting ps : gui.getSettings()) {
			if (ps instanceof ImageryPreference) {
				ImageryProvidersPanel ipp = ((ImageryPreference) ps).getProvidersPanel();
				EditEntriesAction action = new EditEntriesAction(ipp.defaultTable, ipp.defaultModel);
				ipp.middleToolbar.add(new JButton(action));
				// TODO
				break;
			}
		}
	}

	@Override
	public boolean ok() {
		return false;
	}
}
