// License: GPL. See LICENSE file for details./*
package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.io.remotecontrol.AddTagsDialog;
import org.openstreetmap.josm.tools.LanguageInfo;

public class WikipediaAddNamesAction extends JosmAction {

    public WikipediaAddNamesAction() {
        super(tr("Add names from Wikipedia"), "dialogs/wikipedia",
                tr("Fetches interwiki links from Wikipedia in order to add several name tags"),
                null, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final WikipediaApp.WikipediaLangArticle wp = WikipediaApp.WikipediaLangArticle.parseTag("wikipedia", getWikipediaValue());
        List<String[]> tags = new ArrayList<>();
        for (WikipediaApp.WikipediaLangArticle i : WikipediaApp.getInterwikiArticles(wp.lang, wp.article)) {
            if (useWikipediaLangArticle(i)) {
                tags.add(new String[]{"name:" + i.lang, i.article});
            }
        }
        if (Main.isDebugEnabled()) {
            Main.debug(tags.toString());
        }
        AddTagsDialog.addTags(tags.toArray(new String[tags.size()][]), "Wikipedia", getLayerManager().getEditDataSet().getSelected());
    }

    protected boolean useWikipediaLangArticle(WikipediaApp.WikipediaLangArticle i) {
        return (!Main.pref.getBoolean("wikipedia.filter-iso-languages", true)
                || Arrays.asList(Locale.getISOLanguages()).contains(i.lang))
                && (!Main.pref.getBoolean("wikipedia.filter-same-names", true)
                || !i.article.equals(getLayerManager().getEditDataSet().getSelected().iterator().next().get("name")));
    }

    protected String getWikipediaValue() {
        DataSet ds = getLayerManager().getEditDataSet();
        if (ds == null || ds.getSelected() == null || ds.getSelected().size() != 1) {
            return null;
        } else {
            return ds.getSelected().iterator().next().get("wikipedia");
        }
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getWikipediaValue() != null);
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        updateEnabledState();
    }
}
