package oseam.panels;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.Shp;

import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Iterator;

public class PanelPrefStbd extends JPanel {

    private OSeaMAction dlg;
    private ButtonGroup regionButtons = new ButtonGroup();
    public JRadioButton regionAButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RegionAButton.png")));
    public JRadioButton regionBButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RegionBButton.png")));
    private ActionListener alRegion = new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            regionAButton.setBorderPainted(regionAButton.isSelected());
            regionBButton.setBorderPainted(regionBButton.isSelected());
            dlg.mark.paintSign();
        }
    };
    private ButtonGroup shapeButtons = new ButtonGroup();
    public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
    public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
    public JRadioButton coneButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeButton.png")));
    public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
    public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
    public JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
    private EnumMap<Shp, JRadioButton> shapes = new EnumMap<Shp, JRadioButton>(Shp.class);
    private ActionListener alShape = new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            Iterator<Shp> it = shapes.keySet().iterator();
            while (it.hasNext()) {
                Shp shp = it.next();
                JRadioButton button = shapes.get(shp);
                if (button.isSelected()) {
                    dlg.mark.setShape(shp);
                    button.setBorderPainted(true);
                } else
                    button.setBorderPainted(false);
            }
            if (dlg.mark != null)
                dlg.mark.paintSign();
        }
    };

    public PanelPrefStbd(OSeaMAction dia) {
        dlg = dia;
        this.setLayout(null);
        this.add(getRegionButton(regionAButton, 0, 2, 34, 30, "RegionATip"), null);
        this.add(getRegionButton(regionBButton, 0, 32, 34, 30, "RegionBTip"), null);
        this.add(getShapeButton(pillarButton, 0, 64, 34, 32, "PillarTip", Shp.PILLAR), null);
        this.add(getShapeButton(sparButton, 0, 96, 34, 32, "SparTip", Shp.SPAR), null);
        this.add(getShapeButton(coneButton, 0, 128, 34, 32, "ConeTip", Shp.CONE), null);
        this.add(getShapeButton(floatButton, 35, 0, 34, 32, "FloatTip", Shp.FLOAT), null);
        this.add(getShapeButton(beaconButton, 35, 32, 34, 32, "BeaconTip", Shp.BEACON), null);
        this.add(getShapeButton(towerButton, 35, 64, 34, 32, "TowerTip", Shp.TOWER), null);

    }

    public void clearSelections() {
        shapeButtons.clearSelection();
        alShape.actionPerformed(null);
    }

    private JRadioButton getRegionButton(JRadioButton button, int x, int y, int w, int h, String tip) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
        button.setToolTipText(Messages.getString(tip));
        button.addActionListener(alRegion);
        regionButtons.add(button);
        return button;
    }

    private JRadioButton getShapeButton(JRadioButton button, int x, int y, int w, int h, String tip, Shp shp) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
        button.setToolTipText(Messages.getString(tip));
        button.addActionListener(alShape);
        shapeButtons.add(button);
        shapes.put(shp, button);
        return button;
    }

}
