/*
 * Copyright (c) 2010, skobbler GmbH
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
 *
 * Created on Feb 10, 2011 by Bea
 * Modified on $DateTime$ by $Author$
 */
package org.openstreetmap.josm.plugins.mapdust.gui.observer;


import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBugFilter;

/**
 * The observable interface for the MapDust bug update action.
 *
 * @author Bea
 * @version $Revision$
 */
public interface MapdustUpdateObservable {

    /**
     * Adds a new observer to the list of observers.
     *
     * @param observer The <code>MapdustUpdateObserver</code> object
     */
    void addObserver(MapdustUpdateObserver observer);

    /**
     * Removes the given observer from the list of observers.
     *
     * @param observer The <code>MapdustUpdateObserver</code> object
     */
    void removeObserver(MapdustUpdateObserver observer);

    /**
     * Notifies all the observers observing the MapDust bug update action.
     *
     * @param filter The <code>MapdustBugFilter</code> object
     * @param initialUpdate Indicates if the update action is for the first time
     * or not.
     */
    void notifyObservers(MapdustBugFilter filter, boolean initialUpdate);
}
