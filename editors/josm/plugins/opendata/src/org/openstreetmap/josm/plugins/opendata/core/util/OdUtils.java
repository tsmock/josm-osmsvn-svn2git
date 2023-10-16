// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Utils;

public abstract class OdUtils {

    private static final String TEMP_DIR_PREFIX = "josm_opendata_temp_";

    public static boolean isMultipolygon(OsmPrimitive p) {
        return p instanceof Relation && p.isMultipolygon();
    }

    public static String[] stripQuotesAndExtraChars(String[] split, String sep) {
        List<String> result = new ArrayList<>();
        boolean append = false;
        for (int i = 0; i < split.length; i++) {
            if (append) {
                int index = result.size()-1;
                if (split[i].endsWith("\"") && StringUtils.countMatches(split[i], "\"") % 2 != 0) {
                    append = false;
                }
                result.set(index, result.get(index)+sep+split[i].replace("\"", ""));
            } else if (split[i].startsWith("\"")) {
                if (!(split[i].endsWith("\"") && StringUtils.countMatches(split[i], "\"") % 2 == 0)) {
                    append = true;
                }
                result.add(split[i].replace("\"", ""));
            } else {
                result.add(split[i]);
            }
        }
        // Remove exotic characters such as U+FEFF found in some CSV files
        for (ListIterator<String> it = result.listIterator(); it.hasNext();) {
            it.set(Utils.strip(it.next()));
        }
        return result.toArray(new String[0]);
    }

    public static ImageIcon getImageIcon(String iconName) {
        return getImageIcon(iconName, false);
    }

    public static ImageIcon getImageIcon(String iconName, boolean optional) {
        return getImageProvider(iconName, optional).get();
    }

    public static ImageProvider getImageProvider(String iconName) {
        return getImageProvider(iconName, false);
    }

    public static ImageProvider getImageProvider(String iconName, boolean optional) {
        return new ImageProvider(iconName).setOptional(optional);
    }

    public static String getJosmLanguage() {
        String lang = Config.getPref().get("language");
        if (lang == null || lang.isEmpty()) {
            lang = Locale.getDefault().toString();
        }
        return lang;
    }

    public static double convertMinuteSecond(double minute, double second) {
        return (minute/60.0) + (second/3600.0);
    }

    public static double convertDegreeMinuteSecond(double degree, double minute, double second) {
        return degree + convertMinuteSecond(minute, second);
    }

    public static File createTempDir() throws IOException {
        return Files.createTempDirectory(TEMP_DIR_PREFIX).toFile();
    }

    public static void deleteDir(File dir) {
        for (File file : dir.listFiles()) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
        if (!dir.delete()) {
            dir.deleteOnExit();
        }
    }

    public static void deletePreviousTempDirs() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        if (tmpDir.exists() && tmpDir.isDirectory()) {
            for (File dir : tmpDir.listFiles((dir, name) -> name.startsWith(TEMP_DIR_PREFIX))) {
                deleteDir(dir);
            }
        }
    }
}
