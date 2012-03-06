/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.shapefile.indexed;

import org.geotools.data.shapefile.ShpFileType;

/**
 * Enumerates the different types of Shapefile geometry indices there are.
 * 
 * @author jesse
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/plugin/shapefile/src/main/java/org/geotools/data/shapefile/indexed/IndexType.java $
 */
public enum IndexType {
    /**
     * Don't use indexing
     */
    NONE(null),
    /**
     * The same index as mapserver. Its the most reliable and is the default
     */
    QIX(ShpFileType.QIX);

    private IndexType(ShpFileType shpFileType) {
    }
}
