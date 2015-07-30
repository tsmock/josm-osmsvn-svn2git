package org.openstreetmap.josm.plugins.mapillary.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.plugins.mapillary.MapillaryPlugin;
import org.openstreetmap.josm.plugins.mapillary.downloads.MapillaryDownloader;
import org.openstreetmap.josm.plugins.mapillary.oauth.OAuthPortListener;

/**
 * Creates the preferences panel for the plugin.
 *
 * @author nokutu
 *
 */
public class MapillaryPreferenceSetting implements SubPreferenceSetting {

  private JCheckBox reverseButtons = new JCheckBox(
      tr("Reverse buttons position when displaying images."));
  private JComboBox<String> downloadMode = new JComboBox<>(
      MapillaryDownloader.MODES);
  private JCheckBox displayHour = new JCheckBox(
      tr("Display hour when the picture was taken"));
  private JCheckBox format24 = new JCheckBox(tr("Use 24 hour format"));
  private JCheckBox moveTo = new JCheckBox(
      tr("Move to picture's location with next/previous buttons"));

  @Override
  public TabPreferenceSetting getTabPreferenceSetting(PreferenceTabbedPane gui) {
    return gui.getDisplayPreference();
  }

  @Override
  public void addGui(PreferenceTabbedPane gui) {
    JPanel panel = new JPanel();

    reverseButtons.setSelected(Main.pref
        .getBoolean("mapillary.reverse-buttons"));
    displayHour.setSelected(Main.pref
        .getBoolean("mapillary.display-hour", true));
    format24.setSelected(Main.pref.getBoolean("mapillary.format-24"));
    moveTo.setSelected(Main.pref.getBoolean("mapillary.move-to-picture", true));

    panel.setLayout(new FlowLayout(FlowLayout.LEFT));
    panel.add(reverseButtons);

    // Sets the value of the ComboBox.
    if (Main.pref.get("mapillary.download-mode").equals(
        MapillaryDownloader.MODES[0]))
      downloadMode.setSelectedItem(MapillaryDownloader.MODES[0]);
    if (Main.pref.get("mapillary.download-mode").equals(
        MapillaryDownloader.MODES[1]))
      downloadMode.setSelectedItem(MapillaryDownloader.MODES[1]);
    if (Main.pref.get("mapillary.download-mode").equals(
        MapillaryDownloader.MODES[2]))
      downloadMode.setSelectedItem(MapillaryDownloader.MODES[2]);
    JPanel downloadModePanel = new JPanel();
    downloadModePanel.add(new JLabel(tr("Download mode: ")));
    downloadModePanel.add(downloadMode);
    panel.add(downloadModePanel);

    panel.add(displayHour);
    panel.add(format24);
    panel.add(moveTo);
    JButton oauth = new JButton(new OAuthAction());
    if (Main.pref.get("mapillary.access-token") == null)
      oauth.setText("Login");
    else
      oauth.setText("Logged as: " + Main.pref.get("mapillary.username") + ". Click to relogin.");
    panel.add(oauth);
    gui.getDisplayPreference().addSubTab(this, "Mapillary", panel);
  }

  @Override
  public boolean ok() {
    boolean mod = false;
    Main.pref.put("mapillary.reverse-buttons", reverseButtons.isSelected());

    MapillaryPlugin.setMenuEnabled(MapillaryPlugin.DOWNLOAD_VIEW_MENU, false);
    if (downloadMode.getSelectedItem().equals(MapillaryDownloader.MODES[0]))
      Main.pref.put("mapillary.download-mode", MapillaryDownloader.MODES[0]);
    if (downloadMode.getSelectedItem().equals(MapillaryDownloader.MODES[1]))
      Main.pref.put("mapillary.download-mode", MapillaryDownloader.MODES[1]);
    if (downloadMode.getSelectedItem().equals(MapillaryDownloader.MODES[2])) {
      Main.pref.put("mapillary.download-mode", MapillaryDownloader.MODES[2]);
      MapillaryPlugin.setMenuEnabled(MapillaryPlugin.DOWNLOAD_VIEW_MENU, true);
    }

    Main.pref.put("mapillary.display-hour", displayHour.isSelected());
    Main.pref.put("mapillary.format-24", format24.isSelected());
    Main.pref.put("mapillary.move-to-picture", moveTo.isSelected());
    return mod;
  }

  @Override
  public boolean isExpert() {
    return false;
  }

  /**
   * Opens the MapillaryOAuthUI window and lets the user log in.
   *
   * @author nokutu
   *
   */
  public class OAuthAction extends AbstractAction {

    private static final long serialVersionUID = -3908477563072057344L;

    @Override
    public void actionPerformed(ActionEvent arg0) {
      OAuthPortListener portListener = new OAuthPortListener();
      portListener.start();

      String url = "http://www.mapillary.com/connect?redirect_uri=http:%2F%2Flocalhost:8763%2F&client_id=T1Fzd20xZjdtR0s1VDk5OFNIOXpYdzoxNDYyOGRkYzUyYTFiMzgz&response_type=token&scope=user:read%20public:upload%20public:write";
      Desktop desktop = Desktop.getDesktop();
      if (desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e1) {
          Main.error(e1);
        }
      } else {
        Runtime runtime = Runtime.getRuntime();
        try {
          runtime.exec("xdg-open " + url);
        } catch (IOException exc) {
          exc.printStackTrace();
        }
      }
    }
  }
}
