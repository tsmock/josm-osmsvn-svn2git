// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tageditor.tagspec.ui;

import org.openstreetmap.josm.plugins.tageditor.tagspec.KeyValuePair;

public interface ITagSelectorListener {
    void itemSelected(KeyValuePair pair);
}
