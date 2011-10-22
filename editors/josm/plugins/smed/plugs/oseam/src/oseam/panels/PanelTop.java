package oseam.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelTop extends JPanel {

    private OSeaMAction dlg;
    public PanelPat panelPat = null;
    private ButtonGroup topButtons = new ButtonGroup();
    public JRadioButton noTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/NoneButton.png")));
    public JRadioButton canTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CanTopButton.png")));
    public JRadioButton coneTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeTopButton.png")));
    public JRadioButton sphereTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereTopButton.png")));
    public JRadioButton XTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/XTopButton.png")));
    public JRadioButton northTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/NorthTopButton.png")));
    public JRadioButton southTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SouthTopButton.png")));
    public JRadioButton eastTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/EastTopButton.png")));
    public JRadioButton westTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/WestTopButton.png")));
    public JRadioButton spheres2TopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/Spheres2TopButton.png")));
    public JRadioButton boardDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BoardDayButton.png")));
    public JRadioButton diamondDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/DiamondDayButton.png")));
    public JRadioButton triangleDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TriangleDayButton.png")));
    public JRadioButton triangleInvDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TriangleInvDayButton.png")));
    public JRadioButton squareDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SquareDayButton.png")));
    public JRadioButton circleDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CircleDayButton.png")));
    public JRadioButton mooringTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/MooringTopButton.png")));
    private EnumMap<Top, JRadioButton> tops = new EnumMap<Top, JRadioButton>(Top.class);
    private EnumMap<Day, JRadioButton> days = new EnumMap<Day, JRadioButton>(Day.class);
    private ActionListener alTop = new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            for (Top top : tops.keySet()) {
                JRadioButton button = tops.get(top);
                if (button.isSelected()) {
                    dlg.mark.setTopmark(top);
                    dlg.mark.setDaymark(Day.NONE);
                    button.setBorderPainted(true);
                } else
                    button.setBorderPainted(false);
            }
            for (Day day : days.keySet()) {
                JRadioButton button = days.get(day);
                if (button.isSelected()) {
                    dlg.mark.setDaymark(day);
                    dlg.mark.setTopmark(Top.NONE);
                    button.setBorderPainted(true);
                } else
                    button.setBorderPainted(false);
            }
            mooringTopButton.setBorderPainted(mooringTopButton.isSelected());
        }
    };

    public PanelTop(OSeaMAction dia) {
        dlg = dia;
        this.setLayout(null);
        panelPat = new PanelPat(dlg, Ent.TOPMARK);
        panelPat.setBounds(new Rectangle(0, 0, 110, 160));
        this.add(panelPat, null);
        this.add(getTopButton(noTopButton, 110, 5, 27, 27, "NoTop", Top.NONE), null);
        this.add(getTopButton(canTopButton, 140, 5, 27, 27, "CanTop", Top.CAN), null);
        this.add(getTopButton(coneTopButton, 170, 5, 27, 27, "ConeTop", Top.CONE), null);
        this.add(getTopButton(sphereTopButton, 200, 5, 27, 27, "SphereTop", Top.SPHERE), null);
        this.add(getTopButton(XTopButton, 230, 5, 27, 27, "XTop", Top.X_SHAPE), null);
        this.add(getTopButton(northTopButton, 110, 35, 27, 27, "NorthTop", Top.NORTH), null);
        this.add(getTopButton(southTopButton, 140, 35, 27, 27, "SouthTop", Top.SOUTH), null);
        this.add(getTopButton(eastTopButton, 170, 35, 27, 27, "EastTop", Top.EAST), null);
        this.add(getTopButton(westTopButton, 200, 35, 27, 27, "WestTop", Top.WEST), null);
        this.add(getTopButton(spheres2TopButton, 230, 35, 27, 27, "Spheres2Top", Top.SPHERES2), null);
        this.add(getDayButton(boardDayButton, 110, 65, 27, 27, "BoardDay", Day.BOARD), null);
        this.add(getDayButton(diamondDayButton, 140, 65, 27, 27, "DiamondDay", Day.DIAMOND), null);
        this.add(getDayButton(triangleDayButton, 170, 65, 27, 27, "TriangleDay", Day.TRIANGLE), null);
        this.add(getDayButton(triangleInvDayButton, 200, 65, 27, 27, "TriangleInvDay", Day.TRIANGLE_INV), null);
        this.add(getDayButton(squareDayButton, 230, 65, 27, 27, "SquareDay", Day.SQUARE), null);
        this.add(getDayButton(circleDayButton, 230, 95, 27, 27, "CircleDay", Day.CIRCLE), null);
        this.add(getMoorButton(mooringTopButton, 110, 95, 27, 27, "MooringTop"), null);
    }

    public void clearSelections() {
        topButtons.clearSelection();
        alTop.actionPerformed(null);
        panelPat.clearSelections();
    }

    public void enableAll(boolean state) {
        for (JRadioButton button : tops.values()) {
            button.setEnabled(state);
        }
        for (JRadioButton button : days.values()) {
            button.setEnabled(state);
        }
        mooringTopButton.setEnabled(state);
    }

    private JRadioButton getTopButton(JRadioButton button, int x, int y, int w, int h, String tip, Top top) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLoweredBevelBorder());
        button.setToolTipText(Messages.getString(tip));
        button.addActionListener(alTop);
        topButtons.add(button);
        tops.put(top, button);
        return button;
    }

    private JRadioButton getDayButton(JRadioButton button, int x, int y, int w, int h, String tip, Day day) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLoweredBevelBorder());
        button.setToolTipText(Messages.getString(tip));
        button.addActionListener(alTop);
        topButtons.add(button);
        days.put(day, button);
        return button;
    }

    private JRadioButton getMoorButton(JRadioButton button, int x, int y, int w, int h, String tip) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLoweredBevelBorder());
        button.setToolTipText(Messages.getString(tip));
        button.addActionListener(alTop);
        topButtons.add(button);
        return button;
    }

}
