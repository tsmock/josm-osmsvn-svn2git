//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2008.08.10 at 10:24:05 AM CEST
//


package org.openstreetmap.josm.plugins.dataimport.io.tcx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Workouts_t complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Workouts_t">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Running" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}WorkoutFolder_t"/>
 *         &lt;element name="Biking" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}WorkoutFolder_t"/>
 *         &lt;element name="Other" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}WorkoutFolder_t"/>
 *         &lt;element name="Extensions" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Extensions_t" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Workouts_t", propOrder = {
    "running",
    "biking",
    "other",
    "extensions"
})
public class WorkoutsT {

    @XmlElement(name = "Running", required = true)
    protected WorkoutFolderT running;
    @XmlElement(name = "Biking", required = true)
    protected WorkoutFolderT biking;
    @XmlElement(name = "Other", required = true)
    protected WorkoutFolderT other;
    @XmlElement(name = "Extensions")
    protected ExtensionsT extensions;

    /**
     * Gets the value of the running property.
     *
     * @return
     *     possible object is
     *     {@link WorkoutFolderT }
     *
     */
    public WorkoutFolderT getRunning() {
        return running;
    }

    /**
     * Sets the value of the running property.
     *
     * @param value
     *     allowed object is
     *     {@link WorkoutFolderT }
     *
     */
    public void setRunning(WorkoutFolderT value) {
        this.running = value;
    }

    /**
     * Gets the value of the biking property.
     *
     * @return
     *     possible object is
     *     {@link WorkoutFolderT }
     *
     */
    public WorkoutFolderT getBiking() {
        return biking;
    }

    /**
     * Sets the value of the biking property.
     *
     * @param value
     *     allowed object is
     *     {@link WorkoutFolderT }
     *
     */
    public void setBiking(WorkoutFolderT value) {
        this.biking = value;
    }

    /**
     * Gets the value of the other property.
     *
     * @return
     *     possible object is
     *     {@link WorkoutFolderT }
     *
     */
    public WorkoutFolderT getOther() {
        return other;
    }

    /**
     * Sets the value of the other property.
     *
     * @param value
     *     allowed object is
     *     {@link WorkoutFolderT }
     *
     */
    public void setOther(WorkoutFolderT value) {
        this.other = value;
    }

    /**
     * Gets the value of the extensions property.
     *
     * @return
     *     possible object is
     *     {@link ExtensionsT }
     *
     */
    public ExtensionsT getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     *
     * @param value
     *     allowed object is
     *     {@link ExtensionsT }
     *
     */
    public void setExtensions(ExtensionsT value) {
        this.extensions = value;
    }

}
