/* Copyright (c) 2008, Henrik Niehaus
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.osb.gui.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.widgets.HistoryChangedListener;
import org.openstreetmap.josm.plugins.osb.ConfigKeys;
import org.openstreetmap.josm.plugins.osb.OsbPlugin;
import org.openstreetmap.josm.plugins.osb.api.NewAction;
import org.openstreetmap.josm.plugins.osb.gui.dialogs.TextInputDialog;

public class NewIssueAction extends OsbAction {

    private static final long serialVersionUID = 1L;

    private OsbPlugin plugin;

    private String result;

    private Point p;

    private NewAction newAction = new NewAction();

    public NewIssueAction(OsbPlugin plugin, Point p) {
        super(tr("New issue"), plugin.getDialog());
        this.plugin = plugin;
        this.p = p;
    }

    @Override
    protected void doActionPerformed(ActionEvent e) throws IOException, InterruptedException {
        List<String> history = new LinkedList<String>(Main.pref.getCollection(ConfigKeys.OSB_NEW_HISTORY, new LinkedList<String>()));
        HistoryChangedListener l = new HistoryChangedListener() {
            public void historyChanged(List<String> history) {
                Main.pref.putCollection(ConfigKeys.OSB_NEW_HISTORY, history);
            }
        };

        result = TextInputDialog.showDialog(
                Main.map,
                tr("Create issue"),
                tr("Describe the problem precisely"),
                OsbPlugin.loadIcon("icon_error_add22.png"),
                history, l);

        if(result == null) {
            canceled = true;
        }
    }

    @Override
    public void execute() throws IOException {
        if (result.length() > 0) {
            result = addMesgInfo(result);
            Node n = newAction.execute(p, result);
            plugin.getDataSet().addPrimitive(n);
            if (Main.pref.getBoolean(ConfigKeys.OSB_API_DISABLED)) {
                plugin.updateGui();
            } else {
                plugin.updateData();
            }
        }
    }

    @Override
    public String toString() {
        return tr("Create: " + result);
    }

    @Override
    public OsbAction clone() {
        NewIssueAction action = new NewIssueAction(plugin, p);
        action.canceled = canceled;
        action.p = p;
        action.result = result;
        return action;
    }
}
