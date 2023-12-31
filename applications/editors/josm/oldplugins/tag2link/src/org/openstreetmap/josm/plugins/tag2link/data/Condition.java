//    JOSM tag2link plugin.
//    Copyright (C) 2011-2012 Don-vip & FrViPofm
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.tag2link.data;

import java.util.regex.Pattern;

/**
 * A condition allowing to match a rule against an OSM primitive or tag.
 * @author Don-vip
 *
 */
public class Condition {
    
    /**
     * The pattern used against tag key. May be null.
     */
    public Pattern keyPattern;

    /**
     * The pattern used against tag value. May be null.
     */
    public Pattern valPattern;

    /**
     * The condition id. May be null.
     */
    public String id;
    
    @Override
    public String toString() {
        return "Condition ["
                + (keyPattern != null ? "k=" + keyPattern + ", " : "")
                + (valPattern != null ? "v=" + valPattern + ", " : "")
                + (id != null ? "id=" + id : "") + "]";
    }
}
