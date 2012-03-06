/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.index;

import java.util.ArrayList;

/**
 * Holds values (with associated DataDefinition)
 * 
 * @author Tommaso Nolli
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/plugin/shapefile/src/main/java/org/geotools/index/Data.java $
 */
public class Data {
    private DataDefinition def;
    private ArrayList values;

    /**
     * DOCUMENT ME!
     * 
     * @param def
     */
    public Data(DataDefinition def) {
        this.def = def;
        this.values = new ArrayList(def.getFieldsCount());
    }

    /**
     * DOCUMENT ME!
     * 
     * @param val
     * 
     * @return - this Data object
     * 
     * @throws TreeException
     */
    public Data addValue(Object val) throws TreeException {
        if (this.values.size() == def.getFieldsCount()) {
            throw new TreeException("Max number of values reached!");
        }

        int pos = this.values.size();

        if (!val.getClass().equals(def.getField(pos).getFieldClass())) {
            throw new TreeException("Wrong class type, was expecting "
                    + def.getField(pos).getFieldClass());
        }

        this.values.add(val);

        return this;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param i
     * 
     */
    public Object getValue(int i) {
        return this.values.get(i);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer ret = new StringBuffer();

        for (int i = 0; i < this.values.size(); i++) {
            if (i > 0) {
                ret.append(" - ");
            }

            ret.append(this.def.getField(i).getFieldClass());
            ret.append(": ");
            ret.append(this.values.get(i));
        }

        return ret.toString();
    }
    
    public void clear() {
        values.clear();
    }
}
