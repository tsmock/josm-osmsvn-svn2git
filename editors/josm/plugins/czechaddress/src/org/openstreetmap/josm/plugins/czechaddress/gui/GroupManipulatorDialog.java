package org.openstreetmap.josm.plugins.czechaddress.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.tree.TreePath;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.gui.utils.UniversalTreeRenderer;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.ReasonerListener;
import org.openstreetmap.josm.plugins.czechaddress.proposal.Proposal;
import org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalContainer;
import org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalDatabase;

/**
 * A dialog window proposing changes to addresses. It allows to browse the
 * proposals, delete or confirm them.
 *
 * <p>Apart from proposals, it also shows a list of conflicts, which arouse
 * during the address completion.</p>
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 *
 * @see ProposalDatabase
 * @see ConflictDatabase
 */
public class GroupManipulatorDialog extends ExtendedDialog implements ReasonerListener {

    private static GroupManipulatorDialog singleton = null;
    public static GroupManipulatorDialog getInstance() {
        if (singleton == null)
            singleton = new GroupManipulatorDialog();
        return singleton;
    }

    private ProposalDatabase database = null;

    /**
     * Creates a new dialog window and sets the list of primitives
     * to be changed. The proposals are created automatically.
     * @param data set of primitives, which can be changed
     */
    public GroupManipulatorDialog() {
        super(Main.parent, "Přiřadit adresy",
                            new String[] { "OK", "Zrušit" }, true);
        initComponents();

        // Do some nice-look stuff.
        proposalTree.setCellRenderer(new UniversalTreeRenderer());

        // And finalize initializing the form.
        setContent(mainPanel);
        setButtonIcons(new String[] {"ok.png", "cancel.png"});
        setDefaultButton(1);
        setCancelButton(2);
        setupDialog();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // TODO: Why does it always crash if the modality is set in constructor?
        setModal(false);
    }

    @Override
    protected void buttonAction(int i, ActionEvent evt) {
        super.buttonAction(i, evt);
        if (getValue() == 1)
            database.applyAll();
    }

    @Override
    public void setVisible(boolean visible) {

        if (!isVisible() && visible) {
            Reasoner.getInstance().addListener(this);
            recreateProposals();
        } else
            Reasoner.getInstance().removeListener(this);

        super.setVisible(visible);
    }

    public void recreateProposals() {
        locationTextField.setText(CzechAddressPlugin.getLocation().toString());

        database = Reasoner.getInstance().getProposals();
        proposalTree.setModel(database);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     *
     * <p><b>WARNING:</b> Do NOT modify this code. The content of this method is
     * always regenerated by the Netbeans Form Editor.</p>
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        locationLabel = new javax.swing.JLabel();
        locationChangeButton = new javax.swing.JButton();
        locationTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        proposalTree = new javax.swing.JTree();

        setLayout(new java.awt.GridLayout());

        locationLabel.setText("Místo:");

        locationChangeButton.setText("Změnit");
        locationChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationChangeButtonActionPerformed(evt);
            }
        });

        locationTextField.setEditable(false);

        jLabel3.setText("• Pro smazání návrhu použijte klávesu Del.");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        proposalTree.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                proposalTreeKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(proposalTree);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(locationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(locationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(locationChangeButton))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationChangeButton)
                    .addComponent(locationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3))
        );

        add(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event handler for clicking on the button, which changes
     * the current location.
     */
    private void locationChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationChangeButtonActionPerformed
        CzechAddressPlugin.changeLocation();
    }//GEN-LAST:event_locationChangeButtonActionPerformed

    private void proposalTreeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_proposalTreeKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {

            for (TreePath path : proposalTree.getSelectionPaths()) {

                if (path.getLastPathComponent() instanceof Proposal) {
                    ProposalContainer pc = (ProposalContainer)
                            path.getParentPath().getLastPathComponent();
                    pc.removeProposal((Proposal) path.getLastPathComponent());


                } else if (path.getLastPathComponent() instanceof ProposalContainer) {
                    ((ProposalDatabase) proposalTree.getModel())
                            .removeContainer( (ProposalContainer)
                                                    path.getLastPathComponent() );
                }
            }
        }
    }//GEN-LAST:event_proposalTreeKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton locationChangeButton;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField locationTextField;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTree proposalTree;
    // End of variables declaration//GEN-END:variables

    public void elementChanged(AddressElement elem) {
        for (ProposalContainer container : database.getContainers()) {
            if (Reasoner.getInstance().translate(elem) == container.getTarget()) {
                recreateProposals();
                break;
            }
        }
    }

    public void primitiveChanged(OsmPrimitive prim) {
        for (ProposalContainer container : database.getContainers()) {
            if (container.getTarget() == prim) {
                recreateProposals();
                break;
            }
        }
    }

    public void resonerReseted() {
        recreateProposals();
    }

}
