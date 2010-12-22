package oseam.panels;

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

import java.awt.Cursor;
import java.awt.event.ActionListener;

public class PanelLit extends JPanel {

	private OSeaMAction dlg;
	private PanelCol panelCol = null;

	public PanelLit(OSeaMAction dia) {
		dlg = dia;
		panelCol = new PanelCol(dlg);
		panelCol.setBounds(new Rectangle(0, 0, 34, 160));
		this.setLayout(null);
		this.add(panelCol, null);
		panelCol.blackButton.setVisible(false);
	}

	public void clearSelections() {

	}

}
