// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public final class CacheFileUTM20NFilter extends FileFilter {

    /**
     * Derived from ExtensionFileFilter writen by imi
     */
    private final String extension;
    private final String description;

    /**
     * Filters for each one of the 4 French UTM zones.
     */
    public static CacheFileUTM20NFilter[] filters = {
        new CacheFileUTM20NFilter("utm1", tr("Guadeloupe Fort-Marigot cache file (.UTM1)")),
        new CacheFileUTM20NFilter("utm2", tr("Guadeloupe Ste-Anne cache file (.UTM2)")),
        new CacheFileUTM20NFilter("utm3", tr("Martinique Fort Desaix cache file (.UTM3)")),
        new CacheFileUTM20NFilter("utm4", tr("Reunion RGR92 cache file (.UTM4)"))
        };

    /**
     * Construct an extension file filter by giving the extension to check after.
     * @param extension file extension
     * @param description file description
     */
    private CacheFileUTM20NFilter(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    boolean acceptName(String filename) {
        String name = filename.toLowerCase();
        for (String ext : extension.split(",")) {
            if (name.endsWith("." + ext))
                return true;
        }
        return false;
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory())
            return true;
        return acceptName(pathname.getName());
    }

    @Override
    public String getDescription() {
        return description;
    }
}
