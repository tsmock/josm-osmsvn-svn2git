//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.07.19 at 03:50:48 odp. CEST 
//


package org.openstreetmap.josm.plugins.routes.xml;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.openstreetmap.josm.plugins.routes.xml package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.openstreetmap.josm.plugins.routes.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RoutesXMLRoute }
     * 
     */
    public RoutesXMLRoute createRoutesXMLRoute() {
        return new RoutesXMLRoute();
    }

    /**
     * Create an instance of {@link RoutesXMLLayer }
     * 
     */
    public RoutesXMLLayer createRoutesXMLLayer() {
        return new RoutesXMLLayer();
    }

    /**
     * Create an instance of {@link Routes }
     * 
     */
    public Routes createRoutes() {
        return new Routes();
    }

}
