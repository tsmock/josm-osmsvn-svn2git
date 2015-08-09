package org.openstreetmap.josm.plugins.rasterfilters.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.server.UID;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.plugins.rasterfilters.model.FilterStateModel;
import org.openstreetmap.josm.plugins.rasterfilters.model.StateChangeListener;
import org.openstreetmap.josm.plugins.rasterfilters.values.BooleanValue;
import org.openstreetmap.josm.plugins.rasterfilters.values.ColorValue;
import org.openstreetmap.josm.plugins.rasterfilters.values.SelectValue;
import org.openstreetmap.josm.plugins.rasterfilters.values.SliderValue;

import com.bric.swing.ColorPicker;

public class FilterGuiListener implements ChangeListener, ItemListener,
ActionListener, PropertyChangeListener, FilterStateOwner {

	private StateChangeListener handler;
	private FilterStateModel filterState;
	private Set<ComboBoxModel<String>> models = new HashSet<>();
	private UID filterId;

	public FilterGuiListener(StateChangeListener handler) {
		this.handler = handler;
	}

	public void setFilterState(FilterStateModel state) {
		this.filterState = state;
	}

	@Override
	public void stateChanged(ChangeEvent e) {

		JSlider slider = (JSlider) e.getSource();

		if (!slider.getValueIsAdjusting()) {
			slider.setToolTipText(String.valueOf((double) slider.getValue() / 100));
		}

		String parameterName = slider.getName();

		if (filterState.getParams().containsKey(parameterName)) {

			SliderValue<Number> value = (SliderValue<Number>) filterState.getParams().get(parameterName);

			if (value.isDouble()) {
				value.setValue((double) slider.getValue() / 100);
			} else {
				value.setValue(slider.getValue());
			}

			filterState.getParams().put(parameterName, value);
		}

		// notify about state is changed now so send msg to FiltersManager
		handler.filterStateChanged(filterId, filterState);
	}

	@Override
	public FilterStateModel getState() {
		return filterState;
	}

	public ComboBoxModel<String> addModel(ComboBoxModel<String> model) {
		models.add(model);
		return model;
	}

	public void setFilterId(UID filterId) {
		this.filterId = filterId;
	}

	public UID getFilterId() {
		return filterId;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		JCheckBox box = (JCheckBox) e.getSource();

		String parameterName = box.getName();

		BooleanValue value = (BooleanValue) filterState.getParams().get(
				parameterName);
		value.setValue(box.isSelected());

		handler.filterStateChanged(filterId, filterState);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		JComboBox<String> box = (JComboBox<String>) e.getSource();

		String parameterName = box.getName();
		SelectValue<String> value = (SelectValue<String>) filterState.getParams().get(parameterName);

		ComboBoxModel<String> model = box.getModel();
		String selectedItem = (String) model.getSelectedItem();

		value.setValue(selectedItem);

		// notify about state is changed now so send msg to FiltersManager
		handler.filterStateChanged(filterId, filterState);

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		ColorPicker picker = (ColorPicker) evt.getSource();

		int r = picker.getColor().getRed();
		int g = picker.getColor().getGreen();
		int b = picker.getColor().getBlue();

		String parameterName = picker.getName();

		ColorValue<Color> value = (ColorValue<Color>) filterState.getParams().get(parameterName);
		value.setValue(new Color(r, g, b));

		handler.filterStateChanged(filterId, filterState);
	}

}