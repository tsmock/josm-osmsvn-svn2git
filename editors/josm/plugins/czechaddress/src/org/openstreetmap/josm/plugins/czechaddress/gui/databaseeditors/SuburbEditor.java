package org.openstreetmap.josm.plugins.czechaddress.gui.databaseeditors;

import javax.swing.DefaultComboBoxModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Street;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Suburb;
import org.openstreetmap.josm.plugins.czechaddress.gui.utils.UniversalListRenderer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 *
 * @author radek
 */
public class SuburbEditor extends ExtendedDialog {

    private Suburb suburb = null;
    private AddressElement parent = null;

    public SuburbEditor(Suburb suburb) {
        super(Main.parent, "Upravit městskou čtvrť",
                new String[] {"OK", "Zrušit"}, true);
        initComponents();

        this.suburb = suburb;
        this.parent = suburb.getParent();
        nameField.setText(suburb.getName());

        if (parent != null)
            parentField.setText(parent.getName());
        else
            parentField.setEnabled(false);

        parentEditButton.setIcon(ImageProvider.get("actions", "edit.png"));
        parentEditButton.setText("");
        parentEditButton.setEnabled(EditorFactory.isEditable(parent));

        houseList.setModel(new DefaultComboBoxModel(suburb.getHouses().toArray()));
        houseList.setCellRenderer(new UniversalListRenderer());

        houseEditButton.setIcon(ImageProvider.get("actions", "edit.png"));
        houseEditButton.setText("");
        houseListChanged(null);

        streetList.setModel(new DefaultComboBoxModel(suburb.getStreets().toArray()));
        streetList.setCellRenderer(new UniversalListRenderer());

        streetEditButton.setIcon(ImageProvider.get("actions", "edit.png"));
        streetEditButton.setText("");
        streetListChanged(null);

        // And finalize initializing the form.
        setContent(mainPanel);
        this.setButtonIcons(new String[] {"ok.png", "cancel.png"});
        setDefaultButton(1);
        setCancelButton(2);
        setupDialog();
    }

    public String getSuburbName() {
        return nameField.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        parentField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        houseList = new javax.swing.JList();
        parentEditButton = new javax.swing.JButton();
        houseEditButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        streetList = new javax.swing.JList();
        streetEditButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();

        setLayout(new java.awt.GridLayout());

        jLabel1.setText("Rodič:");

        jTextField1.setText("jTextField1");

        parentField.setEditable(false);

        jLabel2.setText("Jméno:");

        jLabel3.setText("Domy:");

        houseList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                houseListChanged(evt);
            }
        });
        jScrollPane1.setViewportView(houseList);

        parentEditButton.setText("    ");
        parentEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parentEditButtonActionPerformed(evt);
            }
        });

        houseEditButton.setText("    ");
        houseEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                houseEditButtonActionPerformed(evt);
            }
        });

        streetList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                streetListChanged(evt);
            }
        });
        jScrollPane2.setViewportView(streetList);

        streetEditButton.setText("    ");
        streetEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                streetEditButtonActionPerformed(evt);
            }
        });

        jLabel4.setText("Ulice:");

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(streetEditButton))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(parentField, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(parentEditButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(houseEditButton))
                    .addComponent(nameField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(parentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(parentEditButton)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(houseEditButton)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(streetEditButton)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

    private House selectedHouse = null;
    private Street selectedStreet = null;

    private void houseListChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_houseListChanged
        selectedHouse = (House) houseList.getSelectedValue();
        houseEditButton.setEnabled(EditorFactory.isEditable(selectedHouse));
}//GEN-LAST:event_houseListChanged

    private void parentEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parentEditButtonActionPerformed
        assert parent != null;
        if (EditorFactory.edit(parent))
            parentField.setText(parent.getName());
}//GEN-LAST:event_parentEditButtonActionPerformed

    private void houseEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_houseEditButtonActionPerformed
        assert selectedHouse != null;
        if (EditorFactory.editHouse(selectedHouse))
            houseList.setModel(new DefaultComboBoxModel(suburb.getHouses().toArray()));
}//GEN-LAST:event_houseEditButtonActionPerformed

    private void streetListChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_streetListChanged
        selectedStreet = (Street) streetList.getSelectedValue();
        streetEditButton.setEnabled(EditorFactory.isEditable(selectedStreet));
    }//GEN-LAST:event_streetListChanged

    private void streetEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_streetEditButtonActionPerformed
        assert selectedStreet != null;
        if (EditorFactory.editStreet(selectedStreet))
            streetList.setModel(new DefaultComboBoxModel(suburb.getStreets().toArray()));
    }//GEN-LAST:event_streetEditButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton houseEditButton;
    private javax.swing.JList houseList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField nameField;
    private javax.swing.JButton parentEditButton;
    private javax.swing.JTextField parentField;
    private javax.swing.JButton streetEditButton;
    private javax.swing.JList streetList;
    // End of variables declaration//GEN-END:variables

}
