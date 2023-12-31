// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tageditor.preset.ui;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class PresetsTable extends JTable {

    /**
     * initialize the table
     */
    protected void init() {
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowHeight(18); // icon height (=16) + minimal border
    }

    public PresetsTable(TableModel model, TableColumnModel columnModel) {
        super(model, columnModel);
        init();
    }

    /**
     * adjusts the width of the columns for the tag name and the tag value
     * to the width of the scroll panes viewport.
     *
     * Note: {@see #getPreferredScrollableViewportSize()} did not work as expected
     *
     * @param scrollPaneWidth the width of the scroll panes viewport
     */
    public void adjustColumnWidth(int scrollPaneWidth) {
        TableColumnModel tcm = getColumnModel();
        int width = scrollPaneWidth;
        width = width / 2;
        if (width > 0) {
            tcm.getColumn(0).setMinWidth(width);
            tcm.getColumn(0).setMaxWidth(width);
            tcm.getColumn(1).setMinWidth(width);
            tcm.getColumn(1).setMaxWidth(width);
        }
    }
}
