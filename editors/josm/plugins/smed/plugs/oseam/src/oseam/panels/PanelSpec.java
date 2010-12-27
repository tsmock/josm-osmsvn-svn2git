package oseam.panels;

import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;

import java.util.EnumMap;
import java.util.Iterator;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.Shp;
import oseam.seamarks.SeaMark.Obj;
import oseam.seamarks.SeaMark.Ent;

public class PanelSpec extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup shapeButtons = new ButtonGroup();
	private JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
	private JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
	private JRadioButton canButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CanButton.png")));
	private JRadioButton coneButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeButton.png")));
	private JRadioButton sphereButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereButton.png")));
	private JRadioButton barrelButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BarrelButton.png")));
	private JRadioButton superButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SuperButton.png")));
	private JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
	private JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
	private JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
	private EnumMap<Shp, JRadioButton> shapes = new EnumMap<Shp, JRadioButton>(Shp.class);
	private EnumMap<Shp, Obj> objects = new EnumMap<Shp, Obj>(Shp.class);
	private PanelCol panelCol = null;
	private ActionListener alShape = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			Iterator<Shp> it = shapes.keySet().iterator();
			while (it.hasNext()) {
				Shp shp = it.next();
				JRadioButton button = shapes.get(shp);
				if (button.isSelected()) {
					dlg.mark.setShape(shp);
					dlg.mark.setObject(objects.get(shp));
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			if (dlg.mark != null)
				dlg.mark.paintSign();
		}
	};

	public PanelSpec(OSeaMAction dia) {
		dlg = dia;
		panelCol = new PanelCol(dlg, Ent.BODY);
		panelCol.setBounds(new Rectangle(9, 0, 34, 160));

		this.setLayout(null);
		this.add(panelCol, null);
		this.add(getShapeButton(pillarButton, 55, 0, 34, 32, "PillarTip", Shp.PILLAR, Obj.BOYSPP), null);
		this.add(getShapeButton(sparButton, 55, 32, 34, 32, "SparTip", Shp.SPAR, Obj.BOYSPP), null);
		this.add(getShapeButton(canButton, 55, 64, 34, 32, "CanTip", Shp.CAN, Obj.BOYSPP), null);
		this.add(getShapeButton(coneButton, 55, 96, 34, 32, "ConeTip", Shp.CONE, Obj.BOYSPP), null);
		this.add(getShapeButton(sphereButton, 55, 128, 34, 32, "SphereTip", Shp.SPHERE, Obj.BOYSPP), null);
		this.add(getShapeButton(barrelButton, 90, 0, 34, 32, "BarrelTip", Shp.BARREL, Obj.BOYSPP), null);
		this.add(getShapeButton(superButton, 90, 32, 34, 32, "SuperTip", Shp.SUPER, Obj.BOYSPP), null);
		this.add(getShapeButton(floatButton, 90, 64, 34, 32, "FloatTip", Shp.FLOAT, Obj.LITFLT), null);
		this.add(getShapeButton(beaconButton, 90, 96, 34, 32, "BeaconTip", Shp.BEACON, Obj.BCNSPP), null);
		this.add(getShapeButton(towerButton, 90, 128, 34, 32, "TowerTip", Shp.TOWER, Obj.BCNSPP), null);
	}

	public void clearSelections() {

	}

	private JRadioButton getShapeButton(JRadioButton button, int x, int y, int w, int h, String tip, Shp shp, Obj obj) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alShape);
		shapeButtons.add(button);
		shapes.put(shp, button);
		objects.put(shp, obj);
		return button;
	}

}
