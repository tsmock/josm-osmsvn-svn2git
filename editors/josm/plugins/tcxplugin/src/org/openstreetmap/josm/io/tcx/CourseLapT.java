//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.08.10 at 10:24:05 AM CEST 
//


package org.openstreetmap.josm.io.tcx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CourseLap_t complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CourseLap_t">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TotalTimeSeconds" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="DistanceMeters" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="BeginPosition" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Position_t" minOccurs="0"/>
 *         &lt;element name="BeginAltitudeMeters" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="EndPosition" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Position_t" minOccurs="0"/>
 *         &lt;element name="EndAltitudeMeters" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="AverageHeartRateBpm" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}HeartRateInBeatsPerMinute_t" minOccurs="0"/>
 *         &lt;element name="MaximumHeartRateBpm" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}HeartRateInBeatsPerMinute_t" minOccurs="0"/>
 *         &lt;element name="Intensity" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Intensity_t"/>
 *         &lt;element name="Cadence" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}CadenceValue_t" minOccurs="0"/>
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
@XmlType(name = "CourseLap_t", propOrder = {
    "totalTimeSeconds",
    "distanceMeters",
    "beginPosition",
    "beginAltitudeMeters",
    "endPosition",
    "endAltitudeMeters",
    "averageHeartRateBpm",
    "maximumHeartRateBpm",
    "intensity",
    "cadence",
    "extensions"
})
public class CourseLapT {

    @XmlElement(name = "TotalTimeSeconds")
    protected double totalTimeSeconds;
    @XmlElement(name = "DistanceMeters")
    protected double distanceMeters;
    @XmlElement(name = "BeginPosition")
    protected PositionT beginPosition;
    @XmlElement(name = "BeginAltitudeMeters")
    protected Double beginAltitudeMeters;
    @XmlElement(name = "EndPosition")
    protected PositionT endPosition;
    @XmlElement(name = "EndAltitudeMeters")
    protected Double endAltitudeMeters;
    @XmlElement(name = "AverageHeartRateBpm")
    protected HeartRateInBeatsPerMinuteT averageHeartRateBpm;
    @XmlElement(name = "MaximumHeartRateBpm")
    protected HeartRateInBeatsPerMinuteT maximumHeartRateBpm;
    @XmlElement(name = "Intensity", required = true)
    protected IntensityT intensity;
    @XmlElement(name = "Cadence")
    protected Short cadence;
    @XmlElement(name = "Extensions")
    protected ExtensionsT extensions;

    /**
     * Gets the value of the totalTimeSeconds property.
     * 
     */
    public double getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    /**
     * Sets the value of the totalTimeSeconds property.
     * 
     */
    public void setTotalTimeSeconds(double value) {
        this.totalTimeSeconds = value;
    }

    /**
     * Gets the value of the distanceMeters property.
     * 
     */
    public double getDistanceMeters() {
        return distanceMeters;
    }

    /**
     * Sets the value of the distanceMeters property.
     * 
     */
    public void setDistanceMeters(double value) {
        this.distanceMeters = value;
    }

    /**
     * Gets the value of the beginPosition property.
     * 
     * @return
     *     possible object is
     *     {@link PositionT }
     *     
     */
    public PositionT getBeginPosition() {
        return beginPosition;
    }

    /**
     * Sets the value of the beginPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link PositionT }
     *     
     */
    public void setBeginPosition(PositionT value) {
        this.beginPosition = value;
    }

    /**
     * Gets the value of the beginAltitudeMeters property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getBeginAltitudeMeters() {
        return beginAltitudeMeters;
    }

    /**
     * Sets the value of the beginAltitudeMeters property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setBeginAltitudeMeters(Double value) {
        this.beginAltitudeMeters = value;
    }

    /**
     * Gets the value of the endPosition property.
     * 
     * @return
     *     possible object is
     *     {@link PositionT }
     *     
     */
    public PositionT getEndPosition() {
        return endPosition;
    }

    /**
     * Sets the value of the endPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link PositionT }
     *     
     */
    public void setEndPosition(PositionT value) {
        this.endPosition = value;
    }

    /**
     * Gets the value of the endAltitudeMeters property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getEndAltitudeMeters() {
        return endAltitudeMeters;
    }

    /**
     * Sets the value of the endAltitudeMeters property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setEndAltitudeMeters(Double value) {
        this.endAltitudeMeters = value;
    }

    /**
     * Gets the value of the averageHeartRateBpm property.
     * 
     * @return
     *     possible object is
     *     {@link HeartRateInBeatsPerMinuteT }
     *     
     */
    public HeartRateInBeatsPerMinuteT getAverageHeartRateBpm() {
        return averageHeartRateBpm;
    }

    /**
     * Sets the value of the averageHeartRateBpm property.
     * 
     * @param value
     *     allowed object is
     *     {@link HeartRateInBeatsPerMinuteT }
     *     
     */
    public void setAverageHeartRateBpm(HeartRateInBeatsPerMinuteT value) {
        this.averageHeartRateBpm = value;
    }

    /**
     * Gets the value of the maximumHeartRateBpm property.
     * 
     * @return
     *     possible object is
     *     {@link HeartRateInBeatsPerMinuteT }
     *     
     */
    public HeartRateInBeatsPerMinuteT getMaximumHeartRateBpm() {
        return maximumHeartRateBpm;
    }

    /**
     * Sets the value of the maximumHeartRateBpm property.
     * 
     * @param value
     *     allowed object is
     *     {@link HeartRateInBeatsPerMinuteT }
     *     
     */
    public void setMaximumHeartRateBpm(HeartRateInBeatsPerMinuteT value) {
        this.maximumHeartRateBpm = value;
    }

    /**
     * Gets the value of the intensity property.
     * 
     * @return
     *     possible object is
     *     {@link IntensityT }
     *     
     */
    public IntensityT getIntensity() {
        return intensity;
    }

    /**
     * Sets the value of the intensity property.
     * 
     * @param value
     *     allowed object is
     *     {@link IntensityT }
     *     
     */
    public void setIntensity(IntensityT value) {
        this.intensity = value;
    }

    /**
     * Gets the value of the cadence property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getCadence() {
        return cadence;
    }

    /**
     * Sets the value of the cadence property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setCadence(Short value) {
        this.cadence = value;
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
