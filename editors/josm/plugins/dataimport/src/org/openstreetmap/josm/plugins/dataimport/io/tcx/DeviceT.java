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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Identifies the originating GPS device that tracked a run or
 *                                used to identify the type of device capable of handling
 *                                the data for loading.
 *
 * <p>Java class for Device_t complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Device_t">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}AbstractSource_t">
 *       &lt;sequence>
 *         &lt;element name="UnitId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/>
 *         &lt;element name="ProductID" type="{http://www.w3.org/2001/XMLSchema}unsignedShort"/>
 *         &lt;element name="Version" type="{http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2}Version_t"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Device_t", propOrder = {
    "unitId",
    "productID",
    "version"
})
public class DeviceT
    extends AbstractSourceT
{

    @XmlElement(name = "UnitId")
    @XmlSchemaType(name = "unsignedInt")
    protected long unitId;
    @XmlElement(name = "ProductID")
    @XmlSchemaType(name = "unsignedShort")
    protected int productID;
    @XmlElement(name = "Version", required = true)
    protected VersionT version;

    /**
     * Gets the value of the unitId property.
     *
     */
    public long getUnitId() {
        return unitId;
    }

    /**
     * Sets the value of the unitId property.
     *
     */
    public void setUnitId(long value) {
        this.unitId = value;
    }

    /**
     * Gets the value of the productID property.
     *
     */
    public int getProductID() {
        return productID;
    }

    /**
     * Sets the value of the productID property.
     *
     */
    public void setProductID(int value) {
        this.productID = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return
     *     possible object is
     *     {@link VersionT }
     *
     */
    public VersionT getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value
     *     allowed object is
     *     {@link VersionT }
     *
     */
    public void setVersion(VersionT value) {
        this.version = value;
    }

}
