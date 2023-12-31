// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.waypointSearch;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.tools.Shortcut;

class SelectWaypointDialog extends ToggleDialog implements KeyListener, MouseListener {

    private JTextField searchPattern = new JTextField(20);
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> searchResult = new JList<>(listModel);
    private transient List<Marker> searchResultObjectCache = new ArrayList<>();
    private boolean firstTimeSearch = true;

    SelectWaypointDialog(String name, String iconName, String tooltip,
            Shortcut shortcut, int preferredHeight) {
        super(name, iconName, tooltip, shortcut, preferredHeight);
        build();
    }

    protected void build() {
        //add panel - all the gui of the plugin goes in here
        JPanel panel = new JPanel(new BorderLayout());

        //search field
        searchPattern.setText(tr("Enter search expression here.."));
        searchPattern.addKeyListener(this);
        searchPattern.addMouseListener(this);
        panel.add(searchPattern, BorderLayout.NORTH);

        //add result table
        searchResult.setLayoutOrientation(JList.VERTICAL);
        searchResult.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResult.addMouseListener(this);
        JScrollPane scrollPane = new JScrollPane(searchResult);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);

        //add label
        JLabel label = new JLabel(tr("Select waypoint to move map"));
        panel.add(label, BorderLayout.SOUTH);

        //add panel to JOSM gui
        createLayout(panel, false, null);
    }

    void updateSearchResults() {
        String searchfor = "";
        listModel.clear();
        searchResultObjectCache.clear();
        if (!firstTimeSearch) {
            searchfor = searchPattern.getText();
        }
        for (Iterator<Marker> i = Engine.searchGpxWaypoints(searchfor).iterator(); i.hasNext();) {
            Marker marker = i.next();
            listModel.addElement(marker.getText());
            searchResultObjectCache.add(marker);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Do nothing
    }

    @Override
    public void keyReleased(KeyEvent e) {
        updateSearchResults();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        firstTimeSearch = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == searchResult) {
            //click on the search result box
            int idx = searchResult.getSelectedIndex();
            if (idx >= 0) {
                Marker marker = searchResultObjectCache.get(idx);
                MainApplication.getMap().mapView.zoomTo(marker.getCoor());
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (searchPattern.getSelectedText() == null) {
            searchPattern.selectAll();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Do nothing
    }
}
