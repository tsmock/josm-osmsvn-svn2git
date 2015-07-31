/**
 *
 */
package org.openstreetmap.josm.plugins.mapillary.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.mapillary.MapillaryLayer;
import org.openstreetmap.josm.plugins.mapillary.MapillaryPlugin;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Action that triggers the plugin. If in automatic mode, it will automatically
 * download the images in the areas where there is OSM data.
 *
 * @author nokutu
 *
 */
public class MapillaryDownloadAction extends JosmAction {

  private static final long serialVersionUID = 325060354730454948L;

  /**
   * Main constructor.
   */
  public MapillaryDownloadAction() {
    super(tr("Mapillary"), MapillaryPlugin.getProvider("icon24.png"),
        tr("Create Mapillary layer"), Shortcut.registerShortcut("Mapillary",
            tr("Start Mapillary layer"), KeyEvent.VK_COMMA, Shortcut.SHIFT),
        false, "mapillaryDownload", false);
    this.setEnabled(false);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (MapillaryLayer.INSTANCE == null) {
      MapillaryLayer.getInstance();
    } else {
      if (Main.map.mapView.getActiveLayer() != MapillaryLayer.getInstance())
        Main.map.mapView.setActiveLayer(MapillaryLayer.getInstance());
      else
        Main.map.mapView.setActiveLayer(Main.map.mapView.getEditLayer());
    }
  }
}
