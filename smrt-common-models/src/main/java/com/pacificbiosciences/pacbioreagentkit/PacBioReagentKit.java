//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.19 at 11:50:03 AM PST 
//


package com.pacificbiosciences.pacbioreagentkit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://pacificbiosciences.com/PacBioReagentKit.xsd}ReagentKit"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "reagentKit"
})
@XmlRootElement(name = "PacBioReagentKit")
public class PacBioReagentKit {

    @XmlElement(name = "ReagentKit", required = true)
    protected ReagentKitType reagentKit;

    /**
     * Gets the value of the reagentKit property.
     * 
     * @return
     *     possible object is
     *     {@link ReagentKitType }
     *     
     */
    public ReagentKitType getReagentKit() {
        return reagentKit;
    }

    /**
     * Sets the value of the reagentKit property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReagentKitType }
     *     
     */
    public void setReagentKit(ReagentKitType value) {
        this.reagentKit = value;
    }

}
