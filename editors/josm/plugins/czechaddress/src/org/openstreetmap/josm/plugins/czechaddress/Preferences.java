// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.czechaddress;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
/**
 *
 * @author radek
 */
public final class Preferences extends DefaultTabPreferenceSetting {

    public JPanel thisPanel;

    public int optimize;

    private String KEY_OPTIMIZE = "czechaddress.opzimize";

    public boolean addBuildingTag;

    private String KEY_BUILDINGTAG = "czechaddress.buildingtag";

    public boolean addNewTag;
    public String addNewTagKey;
    public String addNewTagValue;

    private String KEY_ADDNEWTAG = "czechaddress.newtag";
    private String KEY_ADDNEWTAGKEY = "czechaddress.newtagkey";
    private String KEY_ADDNEWTAGVALUE = "czechaddress.newtagvalue";

    private static Preferences singleton = null;
    public static Preferences getInstance() {
        if (singleton == null)
            singleton = new Preferences();
        return singleton;
    }

    /** Creates new form Preferences */
    private Preferences() {
        super("czech_flag",
                "Nastavení CzechAddressPlugin",
                "Nastavení pluginu pro úpravu a zadávání adres na území ČR.");
        thisPanel = new JPanel();
        initComponents();
        addBuildingTag = Main.pref.getBoolean(KEY_BUILDINGTAG, buildingCheckBox.isSelected());
        addNewTag = Main.pref.getBoolean(KEY_ADDNEWTAG, addNewTagCheckBox.isSelected());
        addNewTagKey = Main.pref.get(KEY_ADDNEWTAGKEY, addNewTagKeyField.getText());
        addNewTagValue = Main.pref.get(KEY_ADDNEWTAGVALUE, addNewTagValueField.getText());
        optimize = Main.pref.getInteger(KEY_OPTIMIZE, optimizeComboBox.getSelectedIndex());
    }

    public void reloadSettings() {
        buildingCheckBox.setSelected(addBuildingTag);

        addNewTagCheckBox.setSelected(addNewTag);
        addNewTagKeyField.setText(addNewTagKey);
        addNewTagValueField.setText(addNewTagValue);

        optimizeComboBox.setSelectedIndex(optimizeComboBox.getSelectedIndex());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new JPanel();
        addNewTagCheckBox = new JCheckBox();
        addNewTagKeyField = new JTextField();
        jLabel1 = new JLabel();
        addNewTagValueField = new JTextField();
        jLabel2 = new JLabel();
        optimizeComboBox = new JComboBox<>();
        buildingCheckBox = new JCheckBox();

        thisPanel.setLayout(new java.awt.GridLayout(1, 0));

        addNewTagCheckBox.setText("Novým primitivám přidávat tag:");
        addNewTagCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                addNewTagChanged(evt);
            }
        });
        addNewTagCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addNewTagCheckBoxActionPerformed(evt);
            }
        });

        addNewTagKeyField.setText("source:position");
        addNewTagKeyField.setEnabled(false);

        jLabel1.setText("=");

        addNewTagValueField.setText("cuzk:km");
        addNewTagValueField.setEnabled(false);

        jLabel2.setText("Optimalizovat algoritmus přiřazování:");

        optimizeComboBox.setModel(new DefaultComboBoxModel<>(new String[] {"pro rychlejší odezvu", "menší spotřebu paměti"}));
        optimizeComboBox.setSelectedIndex(1);
        optimizeComboBox.setEnabled(false);

        buildingCheckBox.setText("Nově polygonům přidávat tag \"building=yes\"");
        buildingCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                buildingCheckBoxaddNewTagChanged(evt);
            }
        });
        buildingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildingCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(optimizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2)
                    .addComponent(addNewTagCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(addNewTagKeyField, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addNewTagValueField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(buildingCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optimizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(addNewTagCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addNewTagKeyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(addNewTagValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buildingCheckBox)
                .addContainerGap(155, Short.MAX_VALUE))
        );

        thisPanel.add(mainPanel);
    } // </editor-fold>//GEN-END:initComponents

    private void addNewTagChanged(javax.swing.event.ChangeEvent evt) { //GEN-FIRST:event_addNewTagChanged
        addNewTagKeyField.setEnabled(addNewTagCheckBox.isSelected());
        addNewTagValueField.setEnabled(addNewTagCheckBox.isSelected());
    } //GEN-LAST:event_addNewTagChanged

    private void addNewTagCheckBoxActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_addNewTagCheckBoxActionPerformed

    } //GEN-LAST:event_addNewTagCheckBoxActionPerformed

    private void buildingCheckBoxaddNewTagChanged(javax.swing.event.ChangeEvent evt) { //GEN-FIRST:event_buildingCheckBoxaddNewTagChanged

    } //GEN-LAST:event_buildingCheckBoxaddNewTagChanged

    private void buildingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buildingCheckBoxActionPerformed

    } //GEN-LAST:event_buildingCheckBoxActionPerformed

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel p = gui.createPreferenceTab(this);
        p.add(mainPanel);
        reloadSettings();
    }

    @Override
    public boolean ok() {
        addBuildingTag = buildingCheckBox.isSelected();
        Main.pref.put(KEY_BUILDINGTAG, addBuildingTag);

        addNewTag = addNewTagCheckBox.isSelected();
        Main.pref.put(KEY_ADDNEWTAG, addNewTag);

        addNewTagKey = addNewTagKeyField.getText();
        Main.pref.put(KEY_ADDNEWTAGKEY, addNewTagKey);

        addNewTagValue = addNewTagValueField.getText();
        Main.pref.put(KEY_ADDNEWTAGVALUE, addNewTagValue);

        optimize = optimizeComboBox.getSelectedIndex();
        Main.pref.putInteger(KEY_OPTIMIZE, optimize);

        return false;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addNewTagCheckBox;
    private javax.swing.JTextField addNewTagKeyField;
    private javax.swing.JTextField addNewTagValueField;
    private javax.swing.JCheckBox buildingCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JComboBox<String> optimizeComboBox;
    // End of variables declaration//GEN-END:variables

}
