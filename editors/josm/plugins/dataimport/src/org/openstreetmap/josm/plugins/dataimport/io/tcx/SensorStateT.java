//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2008.08.10 at 10:24:05 AM CEST
//


package org.openstreetmap.josm.plugins.dataimport.io.tcx;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SensorState_t.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SensorState_t">
 *   &lt;restriction base="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Token_t">
 *     &lt;enumeration value="Present"/>
 *     &lt;enumeration value="Absent"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 *
 */
@XmlType(name = "SensorState_t")
@XmlEnum
public enum SensorStateT {

    @XmlEnumValue("Present")
    PRESENT("Present"),
    @XmlEnumValue("Absent")
    ABSENT("Absent");
    private final String value;

    SensorStateT(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SensorStateT fromValue(String v) {
        for (SensorStateT c: SensorStateT.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
