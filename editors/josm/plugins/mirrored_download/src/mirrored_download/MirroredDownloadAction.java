// License: GPL. For details, see LICENSE file.
package mirrored_download;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.downloadtasks.PostDownloadHandler;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.DataSource;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.widgets.AbstractTextComponentValidator;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;
import org.openstreetmap.josm.io.BoundingBoxDownloader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.GBC;

public class MirroredDownloadAction extends JosmAction {

    static final String XAPI_QUERY_HISTORY_KEY = "plugin.mirrored_download.query-history";
    static final String XAPI_QUERY_TOOLTIP = tr("XAPI query, e.g., '''' (to download all data), ''[highway=*]'', or ''[[network=VRR][ref=603|613]''");

    public MirroredDownloadAction() {
        super(tr("Download from OSM mirror..."), "download_mirror", tr("Download map data from the OSM server."),
                null,
                true, "mirroreddownload/download", true);
        putValue("help", ht("/Action/MirroredDownload"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MirroredDownloadDialog dialog = MirroredDownloadDialog.getInstance();
        dialog.restoreSettings();
        dialog.setVisible(true);
        if (!dialog.isCanceled()) {
            dialog.rememberSettings();
            Bounds area = dialog.getSelectedDownloadArea();
            DownloadOsmTask task = new DownloadOsmTask() {

                protected final String encodePartialUrl(String url, String safePart) {
                    if (url != null && safePart != null) {
                        int pos = url.indexOf(safePart);
                        if (pos > -1) {
                            pos += safePart.length();
                            try {
                                return url.substring(0, pos) + URLEncoder.encode(url.substring(pos), "UTF-8").replaceAll("\\+", "%20");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return url;
                }

                @Override
                protected String modifyUrlBeforeLoad(String url) {
                    if (url.matches(PATTERN_OVERPASS_API_URL)) {
                        return encodePartialUrl(url, "/interpreter?data="); // encode only the part after the = sign
                    } else if (url.matches(PATTERN_OVERPASS_API_XAPI_URL)) {
                        return encodePartialUrl(url, "/xapi?"); // encode only the part after the ? sign
                    } else {
                        return url;
                    }
                }

            };
            Future<?> future = task.download(
                    new MirroredDownloadReader(area, dialog.getOverpassType(), dialog.getOverpassQuery()),
                    dialog.isNewLayerRequired(), area, null);
            Main.worker.submit(new PostDownloadHandler(task, future));
        }
    }

    static class XAPIQueryValidator extends AbstractTextComponentValidator {

        static final Pattern pattern = Pattern.compile("^(\\[([^=]+=[^=]*)\\])*$");

        public XAPIQueryValidator(JTextComponent tc) throws IllegalArgumentException {
            super(tc);
        }

        @Override
        public void validate() {
            if (pattern.matcher(getComponent().getText().trim()).matches()) {
                feedbackValid(XAPI_QUERY_TOOLTIP);
            } else {
                feedbackInvalid(tr("This XAPI query seems to be invalid, please doublecheck"));
            }
        }

        @Override
        public boolean isValid() {
            return pattern.matcher(getComponent().getText().trim()).matches();
        }
    }

    static class MirroredDownloadDialog extends DownloadDialog {

        protected JComboBox<String> overpassType;
        protected HistoryComboBox overpassQuery;
        private static MirroredDownloadDialog instance;

        private MirroredDownloadDialog(Component parent) {
            super(parent);
        }

        static public MirroredDownloadDialog getInstance() {
            if (instance == null) {
                instance = new MirroredDownloadDialog(Main.parent);
            }
            return instance;
        }

        @Override
        protected void buildMainPanelAboveDownloadSelections(JPanel pnl) {
            overpassType = new JComboBox<>(new String[]{"*", "node", "way", "relation"});
            pnl.add(new JLabel(tr("Object type: ")), GBC.std().insets(5, 5, 5, 5));
            pnl.add(overpassType, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
            overpassType.setToolTipText(tr("OSM object type to download (''*'' stands for any)"));
            overpassQuery = new HistoryComboBox();
            pnl.add(new JLabel(tr("XAPI query: ")), GBC.std().insets(5, 5, 5, 5));
            new XAPIQueryValidator((JTextComponent) overpassQuery.getEditor().getEditorComponent());
            pnl.add(overpassQuery, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
            overpassQuery.setToolTipText(XAPI_QUERY_TOOLTIP);
        }

        public String getOverpassQuery() {
            return overpassQuery.getText();
        }

        public String getOverpassType() {
            return (String)overpassType.getItemAt(overpassType.getSelectedIndex());
        }

        @Override
        public void restoreSettings() {
            super.restoreSettings();
            cbDownloadOsmData.setSelected(true);
            cbDownloadGpxData.setSelected(false);
            cbDownloadNotes.setSelected(false);
            cbDownloadOsmData.setEnabled(false);
            cbDownloadGpxData.setEnabled(false);
            cbDownloadNotes.setEnabled(false);
            cbStartup.setEnabled(false);
            overpassQuery.setPossibleItems(
                    Main.pref.getCollection(XAPI_QUERY_HISTORY_KEY, new LinkedList<String>()));
        }

        @Override
        public void rememberSettings() {
            super.rememberSettings();
            overpassQuery.addCurrentItemToHistory();
            Main.pref.putCollection(XAPI_QUERY_HISTORY_KEY, overpassQuery.getHistory());
        }
    }

    static class MirroredDownloadReader extends BoundingBoxDownloader {

        final String overpassType;
        final String overpassQuery;

        public MirroredDownloadReader(Bounds downloadArea, String overpassType, String overpassQuery) {
            super(downloadArea);
            this.overpassType = overpassType;
            this.overpassQuery = overpassQuery.trim();
        }

        @Override
        protected String getBaseUrl() {
            return MirroredDownloadPlugin.getDownloadUrl();
        }

        @Override
        protected String getRequestForBbox(double lon1, double lat1, double lon2, double lat2) {
            if (overpassQuery.isEmpty() && "*".equals(overpassType))
                return super.getRequestForBbox(lon1, lat1, lon2, lat2);
            else
            {
                if (MirroredDownloadPlugin.getAddMeta())
                {
                    // Overpass compatibility layer
                    String url = overpassType
                        + (overpassQuery.contains("[bbox=") ? "" : "[bbox=" + lon1 + "," + lat1 + "," + lon2 + "," + lat2 + "]")
                        + (overpassQuery.contains("[@meta]") ? "" : "[@meta]")
                        + overpassQuery;
                    try
                    {
                        url = URLEncoder.encode(url, "UTF-8");
                    }
                    catch (UnsupportedEncodingException e)
                    {
                    }
                    return url;
                }
                else
                    // Old style XAPI
                    return overpassType + "[bbox=" + lon1 + "," + lat1 + "," + lon2 + "," + lat2 + "]"
                        + overpassQuery;
            }
        }

        @Override
        public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {

            DataSet ds = super.parseOsm(progressMonitor);

            // add bounds if necessary (note that Overpass API does not return bounds in the response XML)
            if (ds != null && ds.dataSources.isEmpty()) {
                if (crosses180th) {
                    Bounds bounds = new Bounds(lat1, lon1, lat2, 180.0);
                    DataSource src = new DataSource(bounds, MirroredDownloadPlugin.getDownloadUrl());
                    ds.dataSources.add(src);

                    bounds = new Bounds(lat1, -180.0, lat2, lon2);
                    src = new DataSource(bounds, MirroredDownloadPlugin.getDownloadUrl());
                    ds.dataSources.add(src);
                } else {
                    Bounds bounds = new Bounds(lat1, lon1, lat2, lon2);
                    DataSource src = new DataSource(bounds, MirroredDownloadPlugin.getDownloadUrl());
                    ds.dataSources.add(src);
                }
            }

            return ds;
        }
    }
}
