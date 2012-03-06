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
package org.geotools.data.shapefile.shp;

import java.nio.ByteBuffer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Wrapper for a Shapefile point.
 * 
 * @author aaime
 * @author Ian Schneider
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/plugin/shapefile/src/main/java/org/geotools/data/shapefile/shp/PointHandler.java $
 * 
 */
public class PointHandler implements ShapeHandler {

    final ShapeType shapeType;
    GeometryFactory geometryFactory;

    public PointHandler(ShapeType type, GeometryFactory gf) throws ShapefileException {
        if ((type != ShapeType.POINT) && (type != ShapeType.POINTM)
                && (type != ShapeType.POINTZ)) { // 2d, 2d+m, 3d+m
            throw new ShapefileException(
                    "PointHandler constructor: expected a type of 1, 11 or 21");
        }

        shapeType = type;
        this.geometryFactory = gf;
    }

    public PointHandler() {
        shapeType = ShapeType.POINT; // 2d
    }
    
    public Object read(ByteBuffer buffer, ShapeType type, boolean flatGeometry) {
        if (type == ShapeType.NULL) {
            return createNull();
        }
        
        int dimension = shapeType == ShapeType.POINTZ && !flatGeometry ? 3 : 2;
        CoordinateSequence cs = geometryFactory.getCoordinateSequenceFactory().create(1, dimension);
        cs.setOrdinate(0, 0, buffer.getDouble());
        cs.setOrdinate(0, 1, buffer.getDouble());

        if (shapeType == ShapeType.POINTM) {
            buffer.getDouble();
        }
        
        if (dimension > 2) {
            cs.setOrdinate(0, 2, buffer.getDouble());
        }

        return geometryFactory.createPoint(cs);
    }

    private Object createNull() {
        return geometryFactory.createPoint(new Coordinate(Double.NaN,
                Double.NaN, Double.NaN));
    }
}
