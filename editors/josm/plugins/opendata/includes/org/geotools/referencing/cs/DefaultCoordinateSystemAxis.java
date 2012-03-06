/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.cs;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.util.NameFactory;
import org.geotools.util.SimpleInternationalString;
import org.geotools.util.Utilities;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.RangeMeaning;
import org.opengis.util.InternationalString;


/**
 * Definition of a coordinate system axis. This is used to label axes, and indicate the orientation.
 * See {@linkplain org.opengis.referencing.cs#AxisNames axis name constraints}.
 * <p>
 * In some case, the axis name is constrained by ISO 19111 depending on the
 * {@linkplain org.opengis.referencing.crs.CoordinateReferenceSystem coordinate reference system}
 * type. These constraints are identified in the javadoc by "<cite>ISO 19111 name is...</cite>"
 * sentences. This constraint works in two directions; for example the names
 * "<cite>geodetic latitude</cite>" and "<cite>geodetic longitude</cite>" shall be used to
 * designate the coordinate axis names associated with a
 * {@linkplain org.opengis.referencing.crs.GeographicCRS geographic coordinate reference system}.
 * Conversely, these names shall not be used in any other context.
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/cs/DefaultCoordinateSystemAxis.java $
 * @version $Id: DefaultCoordinateSystemAxis.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 *
 * @see AbstractCS
 * @see Unit
 */
public class DefaultCoordinateSystemAxis extends AbstractIdentifiedObject
        implements CoordinateSystemAxis
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7883614853277827689L;

    /**
     * Number of directions from "North", "North-North-East", "North-East", etc.
     * This is verified by {@code DefaultCoordinateSystemAxisTest.testCompass}.
     */
    static final int COMPASS_DIRECTION_COUNT = 16;

    /**
     * Number of items in {@link #PREDEFINED}. Should be considered
     * as a constant after the class initialisation is completed.
     */
    private static int PREDEFINED_COUNT = 0;

    /**
     * The list of predefined constants declared in this class,
     * in declaration order. This order matter.
     */
    private static final DefaultCoordinateSystemAxis[] PREDEFINED = new DefaultCoordinateSystemAxis[26];

    /**
     * Default axis info for geodetic longitudes in a
     * {@linkplain org.opengis.referencing.crs.GeographicCRS geographic CRS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain NonSI#DEGREE_ANGLE decimal degrees}.
     *
     * The ISO 19111 name is "<cite>geodetic longitude</cite>" and the abbreviation is "&lambda;"
     * (lambda).
     *
     * This axis is usually part of a {@link #GEODETIC_LONGITUDE}, {@link #GEODETIC_LATITUDE},
     * {@link #ELLIPSOIDAL_HEIGHT} set.
     *
     * @see #LONGITUDE
     * @see #SPHERICAL_LONGITUDE
     * @see #GEODETIC_LATITUDE
     */
    public static final DefaultCoordinateSystemAxis GEODETIC_LONGITUDE = new DefaultCoordinateSystemAxis(
            VocabularyKeys.GEODETIC_LONGITUDE, "\u03BB", AxisDirection.EAST, NonSI.DEGREE_ANGLE);

    /**
     * Default axis info for geodetic latitudes in a
     * {@linkplain org.opengis.referencing.crs.GeographicCRS geographic CRS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain NonSI#DEGREE_ANGLE decimal degrees}.
     *
     * The ISO 19111 name is "<cite>geodetic latitude</cite>" and the abbreviation is "&phi;" (phi).
     *
     * This axis is usually part of a {@link #GEODETIC_LONGITUDE}, {@link #GEODETIC_LATITUDE},
     * {@link #ELLIPSOIDAL_HEIGHT} set.
     *
     * @see #LATITUDE
     * @see #SPHERICAL_LATITUDE
     * @see #GEODETIC_LONGITUDE
     */
    public static final DefaultCoordinateSystemAxis GEODETIC_LATITUDE = new DefaultCoordinateSystemAxis(
            VocabularyKeys.GEODETIC_LATITUDE, "\u03C6", AxisDirection.NORTH, NonSI.DEGREE_ANGLE);

    /**
     * The default axis for height values above the ellipsoid in a
     * {@linkplain org.opengis.referencing.crs.GeographicCRS geographic CRS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     *
     * The ISO 19111 name is "<cite>ellipsoidal heigt</cite>" and the abbreviation is lower case
     * "<var>h</var>".
     *
     * This axis is usually part of a {@link #GEODETIC_LONGITUDE}, {@link #GEODETIC_LATITUDE},
     * {@link #ELLIPSOIDAL_HEIGHT} set.
     *
     * @see #ALTITUDE
     * @see #GEOCENTRIC_RADIUS
     * @see #GRAVITY_RELATED_HEIGHT
     * @see #DEPTH
     */
    public static final DefaultCoordinateSystemAxis ELLIPSOIDAL_HEIGHT = new DefaultCoordinateSystemAxis(
            VocabularyKeys.ELLIPSOIDAL_HEIGHT, "h", AxisDirection.UP, SI.METER);

    /**
     * The default axis for altitude values.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     *
     * The abbreviation is lower case "<var>h</var>".
     *
     * This axis is usually part of a {@link #LONGITUDE}, {@link #LATITUDE}, {@link #ALTITUDE} set.
     *
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GEOCENTRIC_RADIUS
     * @see #GRAVITY_RELATED_HEIGHT
     * @see #DEPTH
     */
    public static final DefaultCoordinateSystemAxis ALTITUDE = new DefaultCoordinateSystemAxis(
            VocabularyKeys.ALTITUDE, "h", AxisDirection.UP, SI.METER);

    /**
     * The default axis for depth.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#DOWN down}
     * and units are {@linkplain SI#METER metres}.
     *
     * The ISO 19111 name is "<cite>depth</cite>".
     *
     * @see #ALTITUDE
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GEOCENTRIC_RADIUS
     * @see #GRAVITY_RELATED_HEIGHT
     */
    public static final DefaultCoordinateSystemAxis DEPTH = new DefaultCoordinateSystemAxis(
            VocabularyKeys.DEPTH, "d", AxisDirection.DOWN, SI.METER);
    static {
        ALTITUDE.opposite = DEPTH;
        DEPTH.opposite = ALTITUDE;
    }

    /**
     * Default axis info for radius in a
     * {@linkplain org.opengis.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain org.opengis.referencing.cs.SphericalCS spherical CS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     *
     * The ISO 19111 name is "<cite>geocentric radius</cite>" and the abbreviation is lower case
     * "<var>r</var>".
     *
     * This axis is usually part of a {@link #SPHERICAL_LONGITUDE}, {@link #SPHERICAL_LATITUDE},
     * {@link #GEOCENTRIC_RADIUS} set.
     *
     * @see #ALTITUDE
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GRAVITY_RELATED_HEIGHT
     * @see #DEPTH
     */
    public static final DefaultCoordinateSystemAxis GEOCENTRIC_RADIUS = new DefaultCoordinateSystemAxis(
            VocabularyKeys.GEOCENTRIC_RADIUS, "r", AxisDirection.UP, SI.METER);

    /**
     * Default axis info for longitudes in a
     * {@linkplain org.opengis.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain org.opengis.referencing.crs.SphericalCS spherical CS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain NonSI#DEGREE_ANGLE decimal degrees}.
     *
     * The ISO 19111 name is "<cite>spherical longitude</cite>" and the abbreviation is "&Omega;"
     * (omega).
     *
     * This axis is usually part of a {@link #SPHERICAL_LONGITUDE}, {@link #SPHERICAL_LATITUDE},
     * {@link #GEOCENTRIC_RADIUS} set.
     *
     * @see #LONGITUDE
     * @see #GEODETIC_LONGITUDE
     * @see #SPHERICAL_LATITUDE
     */
    public static final DefaultCoordinateSystemAxis SPHERICAL_LONGITUDE = new DefaultCoordinateSystemAxis(
            VocabularyKeys.SPHERICAL_LONGITUDE, "\u03A9", AxisDirection.EAST, NonSI.DEGREE_ANGLE);

    /**
     * Default axis info for latitudes in a
     * {@linkplain org.opengis.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain org.opengis.referencing.cs.SphericalCS spherical CS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain NonSI#DEGREE_ANGLE decimal degrees}.
     *
     * The ISO 19111 name is "<cite>spherical latitude</cite>" and the abbreviation is "&Theta;"
     * (theta).
     *
     * This axis is usually part of a {@link #SPHERICAL_LONGITUDE}, {@link #SPHERICAL_LATITUDE},
     * {@link #GEOCENTRIC_RADIUS} set.
     *
     * @see #LATITUDE
     * @see #GEODETIC_LATITUDE
     * @see #SPHERICAL_LONGITUDE
     */
    public static final DefaultCoordinateSystemAxis SPHERICAL_LATITUDE = new DefaultCoordinateSystemAxis(
            VocabularyKeys.SPHERICAL_LATITUDE, "\u03B8", AxisDirection.NORTH, NonSI.DEGREE_ANGLE);

    /**
     * Default axis info for <var>x</var> values in a
     * {@linkplain org.opengis.referencing.cs.CartesianCS cartesian CS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain SI#METER metres}.
     *
     * The abbreviation is lower case "<var>x</var>".
     *
     * This axis is usually part of a {@link #X}, {@link #Y}, {@link #Z} set.
     *
     * @see #EASTING
     * @see #WESTING
     * @see #GEOCENTRIC_X
     * @see #DISPLAY_X
     * @see #COLUMN
     */
    public static final DefaultCoordinateSystemAxis X = new DefaultCoordinateSystemAxis(
            -1, "x", AxisDirection.EAST, SI.METER);

    /**
     * Default axis info for <var>y</var> values in a
     * {@linkplain org.opengis.referencing.cs.CartesianCS cartesian CS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain SI#METER metres}.
     *
     * The abbreviation is lower case "<var>y</var>".
     *
     * This axis is usually part of a {@link #X}, {@link #Y}, {@link #Z} set.
     *
     * @see #NORTHING
     * @see #SOUTHING
     * @see #GEOCENTRIC_Y
     * @see #DISPLAY_Y
     * @see #ROW
     */
    public static final DefaultCoordinateSystemAxis Y = new DefaultCoordinateSystemAxis(
            -1, "y", AxisDirection.NORTH, SI.METER);

    /**
     * Default axis info for <var>z</var> values in a
     * {@linkplain org.opengis.referencing.cs.CartesianCS cartesian CS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     *
     * The abbreviation is lower case "<var>z</var>".
     *
     * This axis is usually part of a {@link #X}, {@link #Y}, {@link #Z} set.
     */
    public static final DefaultCoordinateSystemAxis Z = new DefaultCoordinateSystemAxis(
            -1, "z", AxisDirection.UP, SI.METER);

    /**
     * Default axis info for <var>x</var> values in a
     * {@linkplain org.opengis.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain org.opengis.referencing.cs.CartesianCS cartesian CS}.
     *
     * Increasing ordinates values go toward prime meridian
     * and units are {@linkplain SI#METER metres}.
     *
     * The ISO 19111 name is "<cite>geocentric X</cite>" and the abbreviation is upper case
     * "<var>X</var>".
     *
     * This axis is usually part of a {@link #GEOCENTRIC_X}, {@link #GEOCENTRIC_Y},
     * {@link #GEOCENTRIC_Z} set.
     */
    public static final DefaultCoordinateSystemAxis GEOCENTRIC_X = new DefaultCoordinateSystemAxis(
            VocabularyKeys.GEOCENTRIC_X, "X", AxisDirection.OTHER, SI.METER);

    /**
     * Default axis info for <var>y</var> values in a
     * {@linkplain org.opengis.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain org.opengis.referencing.cs.CartesianCS cartesian CS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain SI#METER metres}.
     *
     * The ISO 19111 name is "<cite>geocentric Y</cite>" and the abbreviation is upper case
     * "<var>Y</var>".
     *
     * This axis is usually part of a {@link #GEOCENTRIC_X}, {@link #GEOCENTRIC_Y},
     * {@link #GEOCENTRIC_Z} set.
     */
    public static final DefaultCoordinateSystemAxis GEOCENTRIC_Y = new DefaultCoordinateSystemAxis(
            VocabularyKeys.GEOCENTRIC_Y, "Y", AxisDirection.EAST, SI.METER);

    /**
     * Default axis info for <var>z</var> values in a
     * {@linkplain org.opengis.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain org.opengis.referencing.cs.CartesianCS cartesian CS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain SI#METER metres}.
     *
     * The ISO 19111 name is "<cite>geocentric Z</cite>" and the abbreviation is upper case
     * "<var>Z</var>".
     *
     * This axis is usually part of a {@link #GEOCENTRIC_X}, {@link #GEOCENTRIC_Y},
     * {@link #GEOCENTRIC_Z} set.
     */
    public static final DefaultCoordinateSystemAxis GEOCENTRIC_Z = new DefaultCoordinateSystemAxis(
            VocabularyKeys.GEOCENTRIC_Z, "Z", AxisDirection.NORTH, SI.METER);

    /**
     * Default axis info for Easting values in a
     * {@linkplain org.opengis.referencing.crs.ProjectedCRS projected CRS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain SI#METER metres}.
     *
     * The ISO 19111 name is "<cite>easting</cite>" and the abbreviation is upper case
     * "<var>E</var>".
     *
     * This axis is usually part of a {@link #EASTING}, {@link #NORTHING} set.
     *
     * @see #X
     * @see #EASTING
     * @see #WESTING
     */
    public static final DefaultCoordinateSystemAxis EASTING = new DefaultCoordinateSystemAxis(
            VocabularyKeys.EASTING, "E", AxisDirection.EAST, SI.METER);

    /**
     * Default axis info for Westing values in a
     * {@linkplain org.opengis.referencing.crs.ProjectedCRS projected CRS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#WEST West}
     * and units are {@linkplain SI#METER metres}.
     *
     * The ISO 19111 name is "<cite>westing</cite>" and the abbreviation is upper case
     * "<var>W</var>".
     *
     * @see #X
     * @see #EASTING
     * @see #WESTING
     */
    public static final DefaultCoordinateSystemAxis WESTING = new DefaultCoordinateSystemAxis(
            VocabularyKeys.WESTING, "W", AxisDirection.WEST, SI.METER);
    static {
        EASTING.opposite = WESTING;
        WESTING.opposite = EASTING;
    }

    /**
     * Default axis info for Northing values in a
     * {@linkplain org.opengis.referencing.crs.ProjectedCRS projected CRS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain SI#METER metres}.
     *
     * The ISO 19111 name is "<cite>northing</cite>" and the abbreviation is upper case
     * "<var>N</var>".
     *
     * This axis is usually part of a {@link #EASTING}, {@link #NORTHING} set.
     *
     * @see #Y
     * @see #NORTHING
     * @see #SOUTHING
     */
    public static final DefaultCoordinateSystemAxis NORTHING = new DefaultCoordinateSystemAxis(
            VocabularyKeys.NORTHING, "N", AxisDirection.NORTH, SI.METER);

    /**
     * Default axis info for Southing values in a
     * {@linkplain org.opengis.referencing.crs.ProjectedCRS projected CRS}.
     *
     * Increasing ordinates values go {@linkplain AxisDirection#SOUTH South}
     * and units are {@linkplain SI#METER metres}.
     *
     * The ISO 19111 name is "<cite>southing</cite>" and the abbreviation is upper case
     * "<var>S</var>".
     *
     * @see #Y
     * @see #NORTHING
     * @see #SOUTHING
     */
    public static final DefaultCoordinateSystemAxis SOUTHING = new DefaultCoordinateSystemAxis(
            VocabularyKeys.SOUTHING, "S", AxisDirection.SOUTH, SI.METER);
    static {
        NORTHING.opposite = SOUTHING;
        SOUTHING.opposite = NORTHING;
    }

    /**
     * A default axis for time values in a {@linkplain org.opengis.referencing.cs.TimeCS time CS}.
     *
     * Increasing time go toward {@linkplain AxisDirection#FUTURE future}
     * and units are {@linkplain NonSI#DAY days}.
     *
     * The abbreviation is lower case "<var>t</var>".
     */
    public static final DefaultCoordinateSystemAxis TIME = new DefaultCoordinateSystemAxis(
            VocabularyKeys.TIME, "t", AxisDirection.FUTURE, NonSI.DAY);

    /**
     * A default axis for column indices in a {@linkplain org.opengis.coverage.grid.GridCoverage
     * grid coverage}. Increasing values go toward {@linkplain AxisDirection#COLUMN_POSITIVE
     * positive column number}.
     *
     * The abbreviation is lower case "<var>i</var>".
     */
    public static final DefaultCoordinateSystemAxis COLUMN = new DefaultCoordinateSystemAxis(
            VocabularyKeys.COLUMN, "i", AxisDirection.COLUMN_POSITIVE, Unit.ONE);

    /**
     * A default axis for row indices in a {@linkplain org.opengis.coverage.grid.GridCoverage grid
     * coverage}. Increasing values go toward {@linkplain AxisDirection#ROW_POSITIVE positive row
     * number}.
     *
     * The abbreviation is lower case "<var>j</var>".
     */
    public static final DefaultCoordinateSystemAxis ROW = new DefaultCoordinateSystemAxis(
            VocabularyKeys.ROW, "j", AxisDirection.ROW_POSITIVE, Unit.ONE);

    /**
     * Some names to be treated as equivalent. This is needed because axis names are the primary
     * way to distinguish between {@link CoordinateSystemAxis} instances. Those names are strictly
     * defined by ISO 19111 as "Geodetic latitude" and "Geodetic longitude" among others, but the
     * legacy WKT specifications from OGC 01-009 defined the names as "Lon" and "Lat" for the same
     * axis.
     * <p>
     * Keys in this map are names <strong>in lower cases</strong>. Values are the axis that the
     * name is for. The actual axis instance doesn't matter (the algorithm using this map should
     * work for any axis instance); it is just a way to differentiate latitude and longitude.
     */
    private static final Map<String,CoordinateSystemAxis> ALIASES =
            new HashMap<String,CoordinateSystemAxis>(12);
    static {
        ALIASES.put("lat",                GEODETIC_LATITUDE);
        ALIASES.put("latitude",           GEODETIC_LATITUDE);
        ALIASES.put("geodetic latitude",  GEODETIC_LATITUDE);
        ALIASES.put("lon",                GEODETIC_LONGITUDE);
        ALIASES.put("long",               GEODETIC_LONGITUDE);
        ALIASES.put("longitude",          GEODETIC_LONGITUDE);
        ALIASES.put("geodetic longitude", GEODETIC_LONGITUDE);
        /*
         * "x" and "y" are sometime used in WKT for meaning "Easting" and "Northing".
         * We could be tempted to add them as alias in this map, but experience shows
         * that such alias have a lot of indesirable side effet. "x" and "y" are used
         * for too many things ("Easting", "Westing", "Geocentric X", "Display right",
         * "Display left", etc.) and declaring them as alias introduces confusion in
         * AbstractCS constructor (during the check of axis directions), in
         * PredefinedCS.standard(CoordinateSystem), etc.
         */
    }

    /**
     * Special cases for "x" and "y" names. "x" is considered equivalent to "Easting"
     * or "Westing", but the converse is not true. Note: by avoiding to put "x" in the
     * {@link #ALIASES} map, we avoid undesirable side effects like considering "Easting"
     * as equivalent to "Westing".
     *
     * @param  xy   The name which may be "x" or "y".
     * @param  name The second name to compare with.
     * @return {@code true} if the second name is equivalent to "x" or "y" (depending on
     *         the {@code xy} value), or {@code false} otherwise.
     */
    private static boolean nameMatchesXY(String xy, final String name) {
        xy = xy.trim();
        if (xy.length() == 1) {
            final DefaultCoordinateSystemAxis axis;
            switch (Character.toLowerCase(xy.charAt(0))) {
                case 'x': axis=EASTING;  break;
                case 'y': axis=NORTHING; break;
                default : return false;
            }
            return axis.nameMatches(name) || axis.getOpposite().nameMatches(name);
        }
        return false;
    }

    /**
     * The abbreviation used for this coordinate system axes. This abbreviation is also
     * used to identify the ordinates in coordinate tuple. Examples are "<var>X</var>"
     * and "<var>Y</var>".
     */
    private final String abbreviation;

    /**
     * Direction of this coordinate system axis. In the case of Cartesian projected
     * coordinates, this is the direction of this coordinate system axis locally.
     */
    private final AxisDirection direction;

    /**
     * The unit of measure used for this coordinate system axis.
     */
    private final Unit<?> unit;

    /**
     * Minimal and maximal value for this axis.
     */
    private final double minimum, maximum;

    /**
     * The range meaning for this axis.
     */
    private final RangeMeaning rangeMeaning;

    /**
     * The axis with opposite direction, or {@code null} if unknow.
     * Not serialized because only used for the predefined constants.
     */
    private transient DefaultCoordinateSystemAxis opposite;

    /**
     * Constructs a new coordinate system axis with the same values than the specified one.
     * This copy constructor provides a way to wrap an arbitrary implementation into a
     * Geotools one or a user-defined one (as a subclass), usually in order to leverage
     * some implementation-specific API. This constructor performs a shallow copy,
     * i.e. the properties are not cloned.
     *
     * @param axis The coordinate system axis to copy.
     *
     * @since 2.2
     */
    public DefaultCoordinateSystemAxis(final CoordinateSystemAxis axis) {
        super(axis);
        abbreviation = axis.getAbbreviation();
        direction    = axis.getDirection();
        unit         = axis.getUnit();
        minimum      = axis.getMinimumValue();
        maximum      = axis.getMaximumValue();
        rangeMeaning = axis.getRangeMeaning();
    }

    /**
     * Constructs an axis from a set of properties. The properties map is given unchanged to the
     * {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map) super-class constructor}.
     *
     * @param properties   Set of properties. Should contains at least {@code "name"}.
     * @param abbreviation The {@linkplain #getAbbreviation abbreviation} used for this
     *                     coordinate system axes.
     * @param direction    The {@linkplain #getDirection direction} of this coordinate system axis.
     * @param unit         The {@linkplain #getUnit unit of measure} used for this coordinate
     *                     system axis.
     * @param minimum      The minimum value normally allowed for this axis.
     * @param maximum      The maximum value normally allowed for this axis.
     * @param rangeMeaning The meaning of axis value range specified by the minimum and
     *                     maximum values.
     *
     * @since 2.3
     */
    public DefaultCoordinateSystemAxis(final Map<String,?> properties,
                                       final String        abbreviation,
                                       final AxisDirection direction,
                                       final Unit<?>       unit,
                                       final double        minimum,
                                       final double        maximum,
                                       final RangeMeaning  rangeMeaning)
    {
        super(properties);
        this.abbreviation = abbreviation;
        this.direction    = direction;
        this.unit         = unit;
        this.minimum      = minimum;
        this.maximum      = maximum;
        this.rangeMeaning = rangeMeaning;
        ensureNonNull("abbreviation", abbreviation);
        ensureNonNull("direction",    direction);
        ensureNonNull("unit",         unit);
        ensureNonNull("rangeMeaning", rangeMeaning);
        if (!(minimum < maximum)) { // Use '!' for catching NaN
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RANGE_$2, minimum, maximum));
        }
    }

    /**
     * Constructs an unbounded axis from a set of properties. The properties map is given
     * unchanged to the {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map)
     * super-class constructor}. The {@linkplain #getMinimumValue minimum} and
     * {@linkplain #getMaximumValue maximum} values are inferred from the axis unit and
     * direction.
     *
     * @param properties   Set of properties. Should contains at least {@code "name"}.
     * @param abbreviation The {@linkplain #getAbbreviation abbreviation} used for this
     *                     coordinate system axes.
     * @param direction    The {@linkplain #getDirection direction} of this coordinate system axis.
     * @param unit         The {@linkplain #getUnit unit of measure} used for this coordinate
     *                     system axis.
     */
    public DefaultCoordinateSystemAxis(final Map<String,?> properties,
                                       final String        abbreviation,
                                       final AxisDirection direction,
                                       final Unit<?>       unit)
    {
        // NOTE: we would invoke this(properties, abbreviation, ...) instead if Sun fixed
        // RFE #4093999 ("Relax constraint on placement of this()/super() call in constructors").
        super(properties);
        this.abbreviation = abbreviation;
        this.direction    = direction;
        this.unit         = unit;
        ensureNonNull("abbreviation", abbreviation);
        ensureNonNull("direction",    direction);
        ensureNonNull("unit",         unit);
        if (unit.isCompatible(NonSI.DEGREE_ANGLE)) {
            final UnitConverter fromDegrees = NonSI.DEGREE_ANGLE.getConverterTo(unit);
            final AxisDirection dir = direction.absolute();
            if (dir.equals(AxisDirection.NORTH)) {
                final double range = Math.abs(fromDegrees.convert(90));
                minimum = -range;
                maximum = +range;
                rangeMeaning = RangeMeaning.EXACT; // 90°N do not wraps to 90°S
                return;
            }
            if (dir.equals(AxisDirection.EAST)) {
                final double range = Math.abs(fromDegrees.convert(180));
                minimum = -range;
                maximum = +range;
                rangeMeaning = RangeMeaning.WRAPAROUND; // 180°E wraps to 180°W
                return;
            }
        }
        minimum = Double.NEGATIVE_INFINITY;
        maximum = Double.POSITIVE_INFINITY;
        rangeMeaning = RangeMeaning.EXACT;
    }

    /**
     * Constructs an axis with a name as an {@linkplain InternationalString international string}
     * and an abbreviation. The {@linkplain #getName name of this identified object} is set to the
     * unlocalized version of the {@code name} argument, as given by
     * <code>name.{@linkplain InternationalString#toString(Locale) toString}(null)</code>. The
     * same {@code name} argument is also stored as an {@linkplain #getAlias alias}, which
     * allows fetching localized versions of the name.
     *
     * @param name         The name of this axis. Also stored as an alias for localization purpose.
     * @param abbreviation The {@linkplain #getAbbreviation abbreviation} used for this
     *                     coordinate system axis.
     * @param direction    The {@linkplain #getDirection direction} of this coordinate system axis.
     * @param unit         The {@linkplain #getUnit unit of measure} used for this coordinate
     *                     system axis.
     */
    public DefaultCoordinateSystemAxis(final InternationalString name,
                                       final String        abbreviation,
                                       final AxisDirection direction,
                                       final Unit<?>       unit)
    {
        this(toMap(name), abbreviation, direction, unit);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static Map<String,Object> toMap(final InternationalString name) {
        final Map<String,Object> properties = new HashMap<String,Object>(4);
        if (name != null) {
            // The "null" locale argument is required for getting the unlocalized version.
            properties.put(NAME_KEY,  name.toString(null));
            properties.put(ALIAS_KEY, NameFactory.create(new InternationalString[] {name}));
        }
        return properties;
    }

    /**
     * Constructs an axis with a name and an abbreviation as a resource bundle key.
     * To be used for construction of pre-defined constants only.
     *
     * @param name         The resource bundle key for the name.
     * @param abbreviation The {@linkplain #getAbbreviation abbreviation} used for this
     *                     coordinate system axes.
     * @param direction    The {@linkplain #getDirection direction} of this coordinate system axis.
     * @param unit         The {@linkplain #getUnit unit of measure} used for this coordinate
     *                     system axis.
     */
    private DefaultCoordinateSystemAxis(final int           name,
                                        final String        abbreviation,
                                        final AxisDirection direction,
                                        final Unit<?>       unit)
    {
        this(name>=0 ? Vocabulary.formatInternational(name) :
                new SimpleInternationalString(abbreviation), abbreviation, direction, unit);
        PREDEFINED[PREDEFINED_COUNT++] = this;
    }

    /**
     * Returns one of the predefined axis for the given name and direction, or {@code null} if
     * none. This method searchs only in predefined constants like {@link #GEODETIC_LATITUDE},
     * not in any custom axis instantiated by a public constructor. The name of those constants
     * match ISO 19111 names or some names commonly found in <cite>Well Known Text</cite> (WKT)
     * formats.
     * <p>
     * This method first checks if the specified name matches the {@linkplain #getAbbreviation
     * abbreviation} of a predefined axis. The comparaison is case-sensitive (for example the
     * {@link #GEOCENTRIC_X} abbreviation is uppercase {@code "X"}, while the abbreviation for
     * the generic {@link #X} axis is lowercase {@code "x"}).
     * <p>
     * If the specified name doesn't match any abbreviation, then this method compares the name
     * against predefined axis {@linkplain #getName name} in a case-insensitive manner. Examples
     * of valid names are "<cite>Geodetic latitude</cite>" and "<cite>Northing</cite>".
     * <p>
     * The direction argument is optional and can be used in order to resolve ambiguity like
     * {@link #X} and {@link #DISPLAY_X} axis. If this argument is {@code null}, then the first
     * axis with a matching name or abbreviation will be returned.
     *
     * @param  name The axis name or abbreviation.
     * @param  direction An optional direction, or {@code null}.
     * @return One of the constants declared in this class, or {@code null}.
     *
     * @since 2.4
     */
    public static DefaultCoordinateSystemAxis getPredefined(String name, AxisDirection direction) {
        ensureNonNull("name", name);
        name = name.trim();
        DefaultCoordinateSystemAxis found = null;
        for (int i=0; i<PREDEFINED_COUNT; i++) {
            final DefaultCoordinateSystemAxis candidate = PREDEFINED[i];
            if (direction != null && !direction.equals(candidate.getDirection())) {
                continue;
            }
            // Reminder: case matter for abbreviation, so 'equalsIgnoreCase' is not allowed.
            if (candidate.abbreviation.equals(name)) {
                return candidate;
            }
            if (found == null && candidate.nameMatches(name)) {
                /*
                 * We need to perform a special check for Geodetic longitude and latitude.
                 * Because of the ALIAS map, the "Geodetic latitude" and "Latitude" names
                 * are considered equivalent, while they are two distinct predefined axis
                 * constants in Geotools. Because Geodetic longitude & latitude constants
                 * are declared first, they have precedence.  So we prevent the selection
                 * of GEODETIC_LATITUDE if the user is likely to ask for LATITUDE.
                 */
                if (candidate == GEODETIC_LONGITUDE || candidate == GEODETIC_LATITUDE) {
                    if (!name.toLowerCase().startsWith("geodetic")) {
                        continue;
                    }
                }
                found = candidate;
            }
        }
        return found;
    }

    /**
     * Returns a predefined axis similar to the specified one except for units.
     * Returns {@code null} if no predefined axis match.
     */
    static DefaultCoordinateSystemAxis getPredefined(final CoordinateSystemAxis axis) {
        return getPredefined(axis.getName().getCode(), axis.getDirection());
    }

    /**
     * Direction of this coordinate system axis. In the case of Cartesian projected
     * coordinates, this is the direction of this coordinate system axis locally.
     * Examples:
     * {@linkplain AxisDirection#NORTH north} or {@linkplain AxisDirection#SOUTH south},
     * {@linkplain AxisDirection#EAST  east}  or {@linkplain AxisDirection#WEST  west},
     * {@linkplain AxisDirection#UP    up}    or {@linkplain AxisDirection#DOWN  down}.
     *
     * <P>Within any set of coordinate system axes, only one of each pair of terms
     * can be used. For earth-fixed coordinate reference systems, this direction is often
     * approximate and intended to provide a human interpretable meaning to the axis. When a
     * geodetic datum is used, the precise directions of the axes may therefore vary slightly
     * from this approximate direction.</P>
     *
     * <P>Note that an {@link org.geotools.referencing.crs.DefaultEngineeringCRS} often requires
     * specific descriptions of the directions of its coordinate system axes.</P>
     */
    public AxisDirection getDirection() {
        return direction;
    }

    /**
     * The abbreviation used for this coordinate system axes. This abbreviation is also
     * used to identify the ordinates in coordinate tuple. Examples are "<var>X</var>"
     * and "<var>Y</var>".
     *
     * @return The coordinate system axis abbreviation.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * The unit of measure used for this coordinate system axis. The value of this
     * coordinate in a coordinate tuple shall be recorded using this unit of measure,
     * whenever those coordinates use a coordinate reference system that uses a
     * coordinate system that uses this axis.
     */
    public Unit<?> getUnit() {
        return unit;
    }

    /**
     * Returns the minimum value normally allowed for this axis, in the
     * {@linkplain #getUnit unit of measure for the axis}. If there is no minimum value, then
     * this method returns {@linkplain Double#NEGATIVE_INFINITY negative infinity}.
     *
     * @since 2.3
     */
    public double getMinimumValue() {
        return minimum;
    }

    /**
     * Returns the maximum value normally allowed for this axis, in the
     * {@linkplain #getUnit unit of measure for the axis}. If there is no maximum value, then
     * this method returns {@linkplain Double#POSITIVE_INFINITY negative infinity}.
     *
     * @since 2.3
     */
    public double getMaximumValue() {
        return maximum;
    }

    /**
     * Returns the meaning of axis value range specified by the {@linkplain #getMinimumValue
     * minimum} and {@linkplain #getMaximumValue maximum} values. This element shall be omitted
     * when both minimum and maximum values are omitted. It may be included when minimum and/or
     * maximum values are included. If this element is omitted when minimum or maximum values are
     * included, the meaning is unspecified.
     *
     * @since 2.3
     */
    public RangeMeaning getRangeMeaning() {
        return rangeMeaning;
    }

    /**
     * Returns an axis with the opposite direction of this one, or {@code null} if unknown.
     * This method is not public because only a few predefined constants have this information.
     */
    final DefaultCoordinateSystemAxis getOpposite() {
        return opposite;
    }

    /*
     * Returns {@code true} if the specified direction is a direction along a meridian.
     * Those directions are used in coordinate systems for polar area. Examples:
     * "<cite>North along 90 deg East</cite>", "<cite>North along 0 deg</cite>".
     *
     * We do not provide such method yet. If we want this functionality, maybe we should
     * consider making DirectionAlongMeridian a public class extending AxisDirection code
     * list instead.
     */
//  public static boolean isDirectionAlongMeridian(final AxisDirection direction);

    /**
     * Returns the arithmetic (counterclockwise) angle from the first direction to the second
     * direction, in decimal <strong>degrees</strong>. This method returns a value between
     * -180° and +180°, or {@link Double#NaN NaN} if no angle can be computed.
     * <p>
     * A positive angle denotes a right-handed system, while a negative angle denotes
     * a left-handed system. Example:
     * <p>
     * <ul>
     *   <li>The angle from {@linkplain AxisDirection#EAST EAST} to
     *       {@linkplain AxisDirection#NORTH NORTH} is 90°</li>
     *   <li>The angle from {@linkplain AxisDirection#SOUTH SOUTH} to
     *       {@linkplain AxisDirection#WEST WEST} is -90°</li>
     *   <li>The angle from "<cite>North along 90 deg East</cite>" to
     *       "<cite>North along 0 deg</cite>" is 90°.</li>
     * </ul>
     *
     * @param  source The source axis direction.
     * @param  target The target axis direction.
     * @return The arithmetic angle (in degrees) of the rotation to apply on a line pointing toward
     *         the source direction in order to make it point toward the target direction, or
     *         {@link Double#NaN} if this value can't be computed.
     *
     * @since 2.4
     */
    public static double getAngle(final AxisDirection source, final AxisDirection target) {
        ensureNonNull("source", source);
        ensureNonNull("target", target);
        // Tests for NORTH, SOUTH, EAST, EAST-NORTH-EAST, etc. directions.
        final int compass = getCompassAngle(source, target);
        if (compass != Integer.MIN_VALUE) {
            return compass * (360.0 / COMPASS_DIRECTION_COUNT);
        }
        // Tests for "South along 90 deg East", etc. directions.
        final DirectionAlongMeridian src = DirectionAlongMeridian.parse(source);
        if (src != null) {
            final DirectionAlongMeridian tgt = DirectionAlongMeridian.parse(target);
            if (tgt != null) {
                return src.getAngle(tgt);
            }
        }
        return Double.NaN;
    }

    /**
     * Tests for angle on compass only (do not tests angle between direction along meridians).
     * Returns {@link Integer#MIN_VALUE} if the angle can't be computed.
     */
    static int getCompassAngle(final AxisDirection source, final AxisDirection target) {
        final int base = AxisDirection.NORTH.ordinal();
        final int src  = source.ordinal() - base;
        if (src >= 0 && src < COMPASS_DIRECTION_COUNT) {
            int tgt = target.ordinal() - base;
            if (tgt >= 0 && tgt < COMPASS_DIRECTION_COUNT) {
                tgt = src - tgt;
                if (tgt < -COMPASS_DIRECTION_COUNT/2) {
                    tgt += COMPASS_DIRECTION_COUNT;
                } else if (tgt > COMPASS_DIRECTION_COUNT/2) {
                    tgt -= COMPASS_DIRECTION_COUNT;
                }
                return tgt;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Returns a new axis with the same properties than current axis except for the units.
     *
     * @param newUnit The unit for the new axis.
     * @return An axis using the specified unit.
     * @throws IllegalArgumentException If the specified unit is incompatible with the expected one.
     */
    final DefaultCoordinateSystemAxis usingUnit(final Unit<?> newUnit)
            throws IllegalArgumentException
    {
        if (unit.equals(newUnit)) {
            return this;
        }
        if (unit.isCompatible(newUnit)) {
            return new DefaultCoordinateSystemAxis(getProperties(this, null),
                       abbreviation, direction, newUnit, minimum, maximum, rangeMeaning);
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.INCOMPATIBLE_UNIT_$1, newUnit));
    }

    /**
     * Returns {@code true} if either the {@linkplain #getName() primary name} or at least
     * one {@linkplain #getAlias alias} matches the specified string. This method performs
     * all the searh done by the {@linkplain AbstractIdentifiedObject#nameMatches(String)
     * super-class}, with the addition of special processing for latitudes and longitudes:
     * <p>
     * <ul>
     *   <li>{@code "Lat"}, {@code "Latitude"} and {@code "Geodetic latitude"} are considered
     *       equivalent.</li>
     *   <li>{@code "Lon"}, {@code "Longitude"} and {@code "Geodetic longitude"} are considered
     *       equivalent.</li>
     * </ul>
     * <p>
     * The above special cases are needed in order to workaround a conflict in specifications:
     * ISO 19111 explicitly state that the latitude and longitude axis names shall be
     * "Geodetic latitude" and "Geodetic longitude", will legacy OGC 01-009 (where WKT is defined)
     * said that the default values shall be "Lat" and "Lon".
     *
     * @param  name The name to compare.
     * @return {@code true} if the primary name of at least one alias
     *         matches the specified {@code name}.
     */
    @Override
    public boolean nameMatches(final String name) {
        if (super.nameMatches(name)) {
            return true;
        }
        /*
         * The standard comparaisons didn't worked. Check for the aliases. Note: we don't
         * test for 'nameMatchesXY(...)' here because the "x" and "y" axis names are too
         * generic. We test them only in the 'equals' method, which has the extra-safety
         * of units comparaison (so less risk to treat incompatible axis as equivalent).
         *
         * TODO: replace Object by CoordinateSystemAxis when we will be allowed
         * to compile for J2SE 1.5.
         */
        final Object type = ALIASES.get(name.trim().toLowerCase());
        return (type != null) && (type == ALIASES.get(getName().getCode().trim().toLowerCase()));
    }

    /**
     * Compares the specified object with this axis for equality.
     *
     * @param  object The object to compare to {@code this}.
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
     */
    @Override
    public boolean equals(final AbstractIdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (super.equals(object, compareMetadata)) {
            return equals((DefaultCoordinateSystemAxis) object, compareMetadata, true);
        }
        return false;
    }

    /**
     * Compares the specified object with this axis for equality, with optional comparaison
     * of units. Units should always be compared (they are not just metadata), except in the
     * particular case of {@link AbstractCS#axisColinearWith}, which is used as a first step
     * toward units conversions through {@link AbstractCS#swapAndScaleAxis}.
     */
    final boolean equals(final DefaultCoordinateSystemAxis that,
                         final boolean compareMetadata, final boolean compareUnit)
    {
        if (compareMetadata) {
            if (!Utilities.equals(this.abbreviation, that.abbreviation) ||
                !Utilities.equals(this.rangeMeaning, that.rangeMeaning) ||
                Double.doubleToLongBits(minimum) != Double.doubleToLongBits(that.minimum) ||
                Double.doubleToLongBits(maximum) != Double.doubleToLongBits(that.maximum))
            {
                return false;
            }
        } else {
            /*
             * Checking the abbreviation is not suffisient. For example the polar angle and the
             * spherical latitude have the same abbreviation (theta).  Geotools extensions like
             * "Longitude" (in addition of ISO 19111 "Geodetic longitude") bring more potential
             * confusion. Furthermore, not all implementors will use the greek letters (even if
             * they are part of ISO 19111).    For example most CRS in WKT format use the "Lat"
             * abbreviation instead of the greek letter phi. For comparaisons without metadata,
             * we ignore the unreliable abbreviation and check the axis name instead. These
             * names are constrained by ISO 19111 specification (see class javadoc), so they
             * should be reliable enough.
             *
             * Note: there is no need to execute this block if 'compareMetadata' is true,
             *       because in this case a stricter check has already been performed by
             *       the 'equals' method in the superclass.
             */
            final String thatName = that.getName().getCode();
            if (!nameMatches(thatName)) {
                // The above test checked for special cases ("Lat" / "Lon" aliases, etc.).
                // The next line may not, but is tested anyway in case the user overrided
                // the 'that.nameMatches(...)' method.
                final String thisName = getName().getCode();
                if (!nameMatches(that, thisName)) {
                    // For the needs of AbstractCS.axisColinearWith(...), we must stop here.
                    // In addition it may be safer to not test 'nameMatchesXY' when we don't
                    // have the extra-safety of units comparaison, because "x" and "y" names
                    // are too generic.
                    if (!compareUnit) {
                        return false;
                    }
                    // Last chance: check for the special case of "x" and "y" axis names.
                    if (!nameMatchesXY(thatName, thisName) && !nameMatchesXY(thisName, thatName)) {
                        return false;
                    }
                }
            }
        }
        return Utilities.equals(this.direction, that.direction) &&
               (!compareUnit || Utilities.equals(this.unit, that.unit));
    }

    /**
     * Returns a hash value for this axis. This value doesn't need to be the same
     * in past or future versions of this class.
     */
    @Override
    public int hashCode() {
        int code = (int)serialVersionUID;
        code = code*37 + abbreviation.hashCode();
        code = code*37 + direction   .hashCode();
        code = code*37 + unit        .hashCode();
        return code;
    }
}
