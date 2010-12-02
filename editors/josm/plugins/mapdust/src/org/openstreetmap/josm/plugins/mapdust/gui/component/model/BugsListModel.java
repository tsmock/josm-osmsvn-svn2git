/* Copyright (c) 2010, skobbler GmbH
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
package org.openstreetmap.josm.plugins.mapdust.gui.component.model;


import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;


/**
 * The list model for the <code>MapdustBug</code> objects.
 *
 * @author Bea
 *
 */
public class BugsListModel implements ListModel {

    /** The list of <code>MapdustBug</code> objects */
    private final List<MapdustBug> bugs;

    /**
     * Builds a <code>BugsListModel</code> object
     */
    public BugsListModel() {
        this.bugs = null;
    }

    /**
     * Builds a <code>BugsListModel</code> object
     *
     * @param bugs The list of <code>MapdustBug</code> objects
     */
    public BugsListModel(List<MapdustBug> bugs) {
        this.bugs = bugs;
    }

    @Override
    public void addListDataListener(ListDataListener l) {}

    @Override
    public Object getElementAt(int index) {
        return bugs.get(index);
    }

    @Override
    public int getSize() {
        return (bugs != null ? bugs.size() : 0);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {}

}
