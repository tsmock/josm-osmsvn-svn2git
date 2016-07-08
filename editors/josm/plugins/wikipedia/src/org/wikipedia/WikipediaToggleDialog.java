// License: GPL. See LICENSE file for details.
package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.search.SearchAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListenerAdapter;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.LanguageInfo;
import org.openstreetmap.josm.tools.OpenBrowser;
import org.wikipedia.WikipediaApp.WikipediaEntry;

public class WikipediaToggleDialog extends ToggleDialog implements ActiveLayerChangeListener, DataSetListenerAdapter.Listener {

    public WikipediaToggleDialog() {
        super(tr("Wikipedia"), "wikipedia", tr("Fetch Wikipedia articles with coordinates"), null, 150);
        createLayout(list, true, Arrays.asList(
                new SideButton(new WikipediaLoadCoordinatesAction()),
                new SideButton(new WikipediaLoadCategoryAction()),
                new SideButton(new PasteWikipediaArticlesAction()),
                new SideButton(new AddWikipediaTagAction()),
                new SideButton(new WikipediaSettingsAction(), false)));
        updateTitle();
    }
    /** A string describing the context (use-case) for determining the dialog title */
    String titleContext = null;
    final StringProperty wikipediaLang = new StringProperty("wikipedia.lang", LanguageInfo.getJOSMLocaleCode().substring(0, 2));
    final Set<String> articles = new HashSet<>();
    final DefaultListModel<WikipediaEntry> model = new DefaultListModel<>();
    final JList<WikipediaEntry> list = new JList<WikipediaEntry>(model) {

        {
            setToolTipText(tr("Double click on item to search for object with article name (and center coordinate)"));
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && getSelectedValue() != null) {
                        final WikipediaEntry entry = (WikipediaEntry) getSelectedValue();
                        if (entry.coordinate != null) {
                            BoundingXYVisitor bbox = new BoundingXYVisitor();
                            bbox.visit(entry.coordinate);
                            Main.map.mapView.zoomTo(bbox);
                        }
                        SearchAction.search(entry.name.replaceAll("\\(.*\\)", ""), SearchAction.SearchMode.replace);
                    }
                }
            });

            setCellRenderer(new DefaultListCellRenderer() {

                @Override
                public JLabel getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    final WikipediaEntry entry = (WikipediaEntry) value;
                    if (entry.getWiwosmStatus() != null && entry.getWiwosmStatus()) {
                        label.setIcon(ImageProvider.getIfAvailable("misc", "grey_check"));
                        label.setToolTipText(/* I18n: WIWOSM server already links Wikipedia article to object/s */ tr("Available via WIWOSM server"));
                    } else if (articles.contains(entry.wikipediaArticle)) {
                        label.setIcon(ImageProvider.getIfAvailable("misc", "green_check"));
                        label.setToolTipText(/* I18n: object/s from dataset contain link to Wikipedia article */ tr("Available in local dataset"));
                    } else {
                        label.setToolTipText(tr("Not linked yet"));
                    }
                    return label;
                }
            });

            final JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(new OpenWikipediaArticleAction());
            popupMenu.add(new ZoomToWikipediaArticleAction());
            setComponentPopupMenu(popupMenu);
        }
    };

    private void updateTitle() {
        if (titleContext == null) {
            setTitle(/* I18n: [language].Wikipedia.org */ tr("{0}.Wikipedia.org", wikipediaLang.get()));
        } else {
            setTitle(/* I18n: [language].Wikipedia.org: [context] */ tr("{0}.Wikipedia.org: {1}", wikipediaLang.get(), titleContext));
        }
    }

    class WikipediaLoadCoordinatesAction extends AbstractAction {

        public WikipediaLoadCoordinatesAction() {
            super(tr("Coordinates"));
            new ImageProvider("dialogs", "wikipedia").getResource().attachImageIcon(this, true);
            putValue(SHORT_DESCRIPTION, tr("Fetches all coordinates from Wikipedia in the current view"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // determine bbox
                final LatLon min = Main.map.mapView.getLatLon(0, Main.map.mapView.getHeight());
                final LatLon max = Main.map.mapView.getLatLon(Main.map.mapView.getWidth(), 0);
                // add entries to list model
                titleContext = tr("coordinates");
                updateTitle();
                new UpdateWikipediaArticlesSwingWorker() {

                    @Override
                    List<WikipediaEntry> getEntries() {
                        return WikipediaApp.getEntriesFromCoordinates(
                                wikipediaLang.get(), min, max);
                    }
                }.execute();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    abstract class UpdateWikipediaArticlesSwingWorker extends SwingWorker<Void, WikipediaEntry> {

        private final IntegerProperty wikipediaStatusUpdateChunkSize = new IntegerProperty("wikipedia.statusupdate.chunk-size", 20);

        abstract List<WikipediaEntry> getEntries();

        @Override
        protected Void doInBackground() throws Exception {
            final List<WikipediaEntry> entries = getEntries();
            Collections.sort(entries);
            publish(entries.toArray(new WikipediaEntry[entries.size()]));
            for (List<WikipediaEntry> chunk : WikipediaApp.partitionList(entries, wikipediaStatusUpdateChunkSize.get())) {
                WikipediaApp.updateWIWOSMStatus(wikipediaLang.get(), chunk);
            }
            return null;
        }

        @Override
        protected void process(List<WikipediaEntry> chunks) {
            model.clear();
            for (WikipediaEntry i : chunks) {
                model.addElement(i);
            }
        }

    }

    class WikipediaLoadCategoryAction extends AbstractAction {

        public WikipediaLoadCategoryAction() {
            super(tr("Category"));
            new ImageProvider("data", "sequence").getResource().attachImageIcon(this, true);
            putValue(SHORT_DESCRIPTION, tr("Fetches a list of all Wikipedia articles of a category"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final String category = JOptionPane.showInputDialog(
                    Main.parent,
                    tr("Enter the Wikipedia category"));
            if (category == null) {
                return;
            }

            titleContext = category;
            updateTitle();

            new UpdateWikipediaArticlesSwingWorker() {
                @Override
                List<WikipediaEntry> getEntries() {
                    return WikipediaApp.getEntriesFromCategory(
                            wikipediaLang.get(), category, Main.pref.getInteger("wikipedia.depth", 3));
                }
            }.execute();
        }
    }

    class PasteWikipediaArticlesAction extends AbstractAction {

        public PasteWikipediaArticlesAction() {
            super(tr("Clipboard"));
            new ImageProvider("paste").getResource().attachImageIcon(this, true);
            putValue(SHORT_DESCRIPTION, tr("Pastes Wikipedia articles from the system clipboard"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            titleContext = tr("clipboard");
            updateTitle();
            new UpdateWikipediaArticlesSwingWorker() {

                @Override
                List<WikipediaEntry> getEntries() {
                    return WikipediaApp.getEntriesFromClipboard(wikipediaLang.get());
                }
            }.execute();
        }
    }

    class OpenWikipediaArticleAction extends AbstractAction {

        public OpenWikipediaArticleAction() {
            super(tr("Open Article"));
            new ImageProvider("browser").getResource().attachImageIcon(this);
            putValue(SHORT_DESCRIPTION, tr("Opens the Wikipedia article of the selected item in a browser"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (list.getSelectedValue() != null) {
                final String url = ((WikipediaEntry) list.getSelectedValue()).getBrowserUrl();
                Main.info("Wikipedia: opening " + url);
                OpenBrowser.displayUrl(url);
            }
        }
    }

    class WikipediaSettingsAction extends AbstractAction {

        public WikipediaSettingsAction() {
            super(tr("Language"));
            new ImageProvider("dialogs/settings").getResource().attachImageIcon(this, true);
            putValue(SHORT_DESCRIPTION, tr("Sets the default language for the Wikipedia articles"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String lang = JOptionPane.showInputDialog(
                    Main.parent,
                    tr("Enter the Wikipedia language"),
                    wikipediaLang.get());
            if (lang != null) {
                wikipediaLang.put(lang);
                updateTitle();
                updateWikipediaArticles();
            }
        }
    }

    class AddWikipediaTagAction extends AbstractAction {

        public AddWikipediaTagAction() {
            super(tr("Add Tag"));
            new ImageProvider("pastetags").getResource().attachImageIcon(this, true);
            putValue(SHORT_DESCRIPTION, tr("Adds a ''wikipedia'' tag corresponding to this article to the selected objects"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (list.getSelectedValue() != null) {
                Tag tag = ((WikipediaEntry) list.getSelectedValue()).createWikipediaTag();
                if (tag != null) {
                    final Collection<OsmPrimitive> selected = Main.getLayerManager().getEditDataSet().getSelected();
                    if (!GuiUtils.confirmOverwrite(tag.getKey(), tag.getValue(), selected)) {
                        return;
                    }
                    ChangePropertyCommand cmd = new ChangePropertyCommand(
                            selected,
                            tag.getKey(), tag.getValue());
                    Main.main.undoRedo.add(cmd);
                    Main.worker.submit(new FetchWikidataAction.Fetcher(selected));
                }
            }
        }
    }

    class ZoomToWikipediaArticleAction extends AbstractAction {

        ZoomToWikipediaArticleAction() {
            super(tr("Zoom to selection"));
            new ImageProvider("dialogs/autoscale", "selection").getResource().attachImageIcon(this);
            putValue(SHORT_DESCRIPTION, tr("Zoom to selection"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final WikipediaEntry entry = list.getSelectedValue();
            if (entry == null) {
                return;
            }
            final LatLon latLon = WikipediaApp.getCoordinateForArticle(entry.wikipediaLang, entry.wikipediaArticle);
            if (latLon == null) {
                return;
            }
            Main.map.mapView.zoomTo(latLon);
        }
    }

    protected void updateWikipediaArticles() {
        articles.clear();
        if (Main.main != null && Main.getLayerManager().getEditDataSet() != null) {
            for (final OsmPrimitive p : Main.getLayerManager().getEditDataSet().allPrimitives()) {
                articles.addAll(WikipediaApp.getWikipediaArticles(wikipediaLang.get(), p));
            }
        }
    }

    private final DataSetListenerAdapter dataChangedAdapter = new DataSetListenerAdapter(this);

    @Override
    public void showNotify() {
        DatasetEventManager.getInstance().addDatasetListener(dataChangedAdapter, FireMode.IN_EDT_CONSOLIDATED);
        Main.getLayerManager().addActiveLayerChangeListener(this);
        updateWikipediaArticles();
    }

    @Override
    public void hideNotify() {
        DatasetEventManager.getInstance().removeDatasetListener(dataChangedAdapter);
        Main.getLayerManager().removeActiveLayerChangeListener(this);
        articles.clear();
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        updateWikipediaArticles();
        list.repaint();
    }

    @Override
    public void processDatasetEvent(AbstractDatasetChangedEvent event) {
        updateWikipediaArticles();
        list.repaint();
    }
}
