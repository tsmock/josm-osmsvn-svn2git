package org.openstreetmap.josm.plugins.czechaddress.gui.databaseeditors;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Dialog for editing a {@link House}.
 *
 * @author Radomír Ćernoch
 */
public class HouseEditor extends ExtendedDialog {

    AddressElement parent = null;

    public HouseEditor(House house) {
        super(Main.parent, "Upravit dům", new String[] {"OK", "Zrušit"}, true);
        initComponents();

        parent = house.getParent();
        if (parent != null)
            parentField.setText(parent.getName());
        else
            parentField.setEnabled(false);

        parentEditButton.setIcon(ImageProvider.get("actions", "edit.png"));
        parentEditButton.setText("");
        parentEditButton.setEnabled(EditorFactory.isEditable(parent));

        coField.setText(house.getCO());
        cpField.setText(house.getCP());

        // And finalize initializing the form.
        setContent(mainPanel);
        this.setButtonIcons(new String[] {"ok.png", "cancel.png"});
        setDefaultButton(1);
        setCancelButton(2);
        setupDialog();
    }

    public String getCO() {
        return coField.getText();
    }
    public String getCP() {
        return cpField.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        parentField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cpField = new javax.swing.JTextField();
        coField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        parentEditButton = new javax.swing.JButton();

        setLayout(new java.awt.GridLayout());

        jLabel1.setText("Rodič:");

        jTextField1.setText("jTextField1");

        parentField.setEditable(false);

        jLabel2.setText("č.p.:");

        jLabel3.setText("č.o.:");

        parentEditButton.setText("    ");
        parentEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parentEditButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(parentField, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(parentEditButton))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(cpField, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(coField, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE))))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(parentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(parentEditButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cpField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(coField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void parentEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parentEditButtonActionPerformed
        assert parent != null;
        if (EditorFactory.edit(parent))
            parentField.setText(parent.getName());
    }//GEN-LAST:event_parentEditButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField coField;
    private javax.swing.JTextField cpField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton parentEditButton;
    private javax.swing.JTextField parentField;
    // End of variables declaration//GEN-END:variables

}
