package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;

public class WMSPreferenceEditor implements PreferenceSetting {
	private DefaultTableModel model;
	private JComboBox browser;
	private HashMap<Integer, WMSInfo> oldValues = new HashMap<Integer, WMSInfo>();

	JCheckBox overlapCheckBox;
	JSpinner spinEast;
	JSpinner spinNorth;
	JSpinner spinSimConn;
	JCheckBox remoteCheckBox;
	boolean allowRemoteControl = true;

	public void addGui(final PreferenceTabbedPane gui) {
		JPanel p = gui.createPreferenceTab("wms", tr("WMS Plugin Preferences"), tr("Modify list of WMS servers displayed in the WMS plugin menu"));

		model = new DefaultTableModel(new String[]{tr("Menu Name"), tr("WMS URL")}, 0);
		final JTable list = new JTable(model);
		JScrollPane scroll = new JScrollPane(list);
		p.add(scroll, GBC.eol().fill(GridBagConstraints.BOTH));
		scroll.setPreferredSize(new Dimension(200,200));

		for (WMSInfo i : WMSPlugin.wmsList) {
			oldValues.put(i.prefid, i);
			model.addRow(new String[]{i.name, i.url});
		}

		final DefaultTableModel modeldef = new DefaultTableModel(
				new String[]{tr("Menu Name (Default)"), tr("WMS URL (Default)")}, 0);
		final JTable listdef = new JTable(modeldef){
			@Override
			public boolean isCellEditable(int row,int column){return false;}
		};
		JScrollPane scrolldef = new JScrollPane(listdef);
		// scrolldef is added after the buttons so it's clearer the buttons
		// control the top list and not the default one
		scrolldef.setPreferredSize(new Dimension(200,200));

		for (Map.Entry<String,String> i : WMSPlugin.wmsListDefault.entrySet()) {
			modeldef.addRow(new String[]{i.getKey(), i.getValue()});
		}

		JPanel buttonPanel = new JPanel(new FlowLayout());

		JButton add = new JButton(tr("Add"));
		buttonPanel.add(add, GBC.std().insets(0,5,0,0));
		add.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JPanel p = new JPanel(new GridBagLayout());
				p.add(new JLabel(tr("Menu Name")), GBC.std().insets(0,0,5,0));
				JTextField key = new JTextField(40);
				JTextField value = new JTextField(40);
				p.add(key, GBC.eop().insets(5,0,0,0).fill(GridBagConstraints.HORIZONTAL));
				p.add(new JLabel(tr("WMS URL")), GBC.std().insets(0,0,5,0));
				p.add(value, GBC.eol().insets(5,0,0,0).fill(GridBagConstraints.HORIZONTAL));
				int answer = JOptionPane.showConfirmDialog(
						gui, p,
						tr("Enter a menu name and WMS URL"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (answer == JOptionPane.OK_OPTION) {
					model.addRow(new String[]{key.getText(), value.getText()});
				}
			}
		});

		JButton delete = new JButton(tr("Delete"));
		buttonPanel.add(delete, GBC.std().insets(0,5,0,0));
		delete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedRow() == -1)
					JOptionPane.showMessageDialog(gui, tr("Please select the row to delete."));
				else
				{
					Integer i;
					while ((i = list.getSelectedRow()) != -1)
						model.removeRow(i);
				}
			}
		});

		JButton copy = new JButton(tr("Copy Selected Default(s)"));
		buttonPanel.add(copy, GBC.std().insets(0,5,0,0));
		copy.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int[] lines = listdef.getSelectedRows();
				if (lines.length == 0) {
					JOptionPane.showMessageDialog(
							gui,
							tr("Please select at least one row to copy."),
							tr("Information"),
							JOptionPane.INFORMATION_MESSAGE
					);
					return;
				}

				outer: for(int i = 0; i < lines.length; i++) {
					String c1 = modeldef.getValueAt(lines[i], 0).toString();
					String c2 = modeldef.getValueAt(lines[i], 1).toString();

					// Check if an entry with exactly the same values already
					// exists
					for(int j = 0; j < model.getRowCount(); j++) {
						if(c1.equals(model.getValueAt(j, 0).toString())
								&& c2.equals(model.getValueAt(j, 1).toString())) {
							// Select the already existing row so the user has
							// some feedback in case an entry exists
							list.getSelectionModel().setSelectionInterval(j, j);
							list.scrollRectToVisible(list.getCellRect(j, 0, true));
							continue outer;
						}
					}

					model.addRow(new String[] {c1, c2});
					int lastLine = model.getRowCount() - 1;
					list.getSelectionModel().setSelectionInterval(lastLine, lastLine);
					list.scrollRectToVisible(list.getCellRect(lastLine, 0, true));
				}
			}
		});

		p.add(buttonPanel);
		p.add(Box.createHorizontalGlue(), GBC.eol().fill(GridBagConstraints.HORIZONTAL));
		// Add default item list
		p.add(scrolldef, GBC.eol().insets(0,5,0,0).fill(GridBagConstraints.BOTH));

		browser = new JComboBox(new String[]{
				"webkit-image {0}",
				"gnome-web-photo --mode=photo --format=png {0} /dev/stdout",
				"gnome-web-photo-fixed {0}",
		"webkit-image-gtk {0}"});
		browser.setEditable(true);
		browser.setSelectedItem(Main.pref.get("wmsplugin.browser", "webkit-image {0}"));
		p.add(new JLabel(tr("Downloader:")), GBC.eol().fill(GridBagConstraints.HORIZONTAL));
		p.add(browser);

		//Overlap
		p.add(Box.createHorizontalGlue(), GBC.eol().fill(GridBagConstraints.HORIZONTAL));

		overlapCheckBox = new JCheckBox(tr("Overlap tiles"), WMSPlugin.doOverlap );
		JLabel labelEast = new JLabel(tr("% of east:"));
		JLabel labelNorth = new JLabel(tr("% of north:"));
		spinEast = new JSpinner(new SpinnerNumberModel(WMSPlugin.overlapEast, 1, 50, 1));
		spinNorth = new JSpinner(new SpinnerNumberModel(WMSPlugin.overlapNorth, 1, 50, 1));

		JPanel overlapPanel = new JPanel(new FlowLayout());
		overlapPanel.add(overlapCheckBox);
		overlapPanel.add(labelEast);
		overlapPanel.add(spinEast);
		overlapPanel.add(labelNorth);
		overlapPanel.add(spinNorth);

		p.add(overlapPanel);

		// Simultaneous connections
		p.add(Box.createHorizontalGlue(), GBC.eol().fill(GridBagConstraints.HORIZONTAL));
		JLabel labelSimConn = new JLabel(tr("Simultaneous connections"));
		spinSimConn = new JSpinner(new SpinnerNumberModel(WMSPlugin.simultaneousConnections, 1, 30, 1));
		JPanel overlapPanelSimConn = new JPanel(new FlowLayout());
		overlapPanelSimConn.add(labelSimConn);
		overlapPanelSimConn.add(spinSimConn);
		p.add(overlapPanelSimConn, GBC.eol().fill(GridBagConstraints.HORIZONTAL));


		allowRemoteControl = Main.pref.getBoolean("wmsplugin.remotecontrol", true);
		remoteCheckBox = new JCheckBox(tr("Allow remote control (reqires remotecontrol plugin)"), allowRemoteControl );
		JPanel remotePanel = new JPanel(new FlowLayout());
		remotePanel.add(remoteCheckBox);

		p.add(remotePanel);

	}

	public boolean ok() {
		boolean change = false;
		for (int i = 0; i < model.getRowCount(); ++i) {
			String name = model.getValueAt(i,0).toString();
			String url = model.getValueAt(i,1).toString();

			WMSInfo origValue = oldValues.get(i);
			if (origValue == null)
			{
				new WMSInfo(name, url, i).save();
				change = true;
			}
			else
			{
				if (!origValue.name.equals(name) || !origValue.url.equals(url))
				{
					origValue.name = name;
					origValue.url = url;
					origValue.save();
					change = true;
				}
				oldValues.remove(i);
			}
		}

		// using null values instead of empty string really deletes
		// the preferences entry
		for (WMSInfo i : oldValues.values())
		{
			i.url = null;
			i.name = null;
			i.save();
			change = true;
		}

		if (change) WMSPlugin.refreshMenu();

		WMSPlugin.doOverlap = overlapCheckBox.getModel().isSelected();
		WMSPlugin.overlapEast = (Integer) spinEast.getModel().getValue();
		WMSPlugin.overlapNorth = (Integer) spinNorth.getModel().getValue();
		WMSPlugin.simultaneousConnections = (Integer) spinSimConn.getModel().getValue();
		allowRemoteControl = remoteCheckBox.getModel().isSelected();

		Main.pref.put("wmsplugin.url.overlap",    String.valueOf(WMSPlugin.doOverlap));
		Main.pref.put("wmsplugin.url.overlapEast", String.valueOf(WMSPlugin.overlapEast));
		Main.pref.put("wmsplugin.url.overlapNorth", String.valueOf(WMSPlugin.overlapNorth));

		Main.pref.put("wmsplugin.browser", browser.getEditor().getItem().toString());
		Main.pref.put("wmsplugin.simultaneousConnections", String.valueOf(WMSPlugin.simultaneousConnections));

		Main.pref.put("wmsplugin.remotecontrol",    String.valueOf(allowRemoteControl));
		return false;
	}

	/**
	 * Updates a server URL in the preferences dialog. Used by other plugins.
	 *
	 * @param server The server name
	 * @param url The server URL
	 */
	public void setServerUrl(String server, String url)
	{
		for (int i = 0; i < model.getRowCount(); i++)
		{
			if( server.equals(model.getValueAt(i,0).toString()) )
			{
				model.setValueAt(url, i, 1);
				return;
			}
		}
		model.addRow(new String[]{server, url});
	}

	/**
	 * Gets a server URL in the preferences dialog. Used by other plugins.
	 *
	 * @param server The server name
	 * @return The server URL
	 */
	public String getServerUrl(String server)
	{
		for (int i = 0; i < model.getRowCount(); i++)
		{
			if( server.equals(model.getValueAt(i,0).toString()) )
			{
				return model.getValueAt(i,1).toString();
			}
		}
		return null;
	}
}

