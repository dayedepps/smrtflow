//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.06.09 at 04:52:36 PM PDT 
//


package com.pacificbiosciences.pacbiodatamodel;

import java.util.ArrayList;
import java.util.List;
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
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://pacificbiosciences.com/PacBioDataModel.xsd}CompatibleSequencingKits" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://pacificbiosciences.com/PacBioDataModel.xsd}CompatibleChipLayouts"/>
 *         &lt;element ref="{http://pacificbiosciences.com/PacBioDataModel.xsd}Validation" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "compatibleSequencingKits",
    "compatibleChipLayouts",
    "validation"
})
@XmlRootElement(name = "RunResources")
public class RunResources {

    @XmlElement(name = "CompatibleSequencingKits", required = true)
    protected List<CompatibleSequencingKits> compatibleSequencingKits;
    @XmlElement(name = "CompatibleChipLayouts", required = true)
    protected CompatibleChipLayouts compatibleChipLayouts;
    @XmlElement(name = "Validation")
    protected Validation validation;

    /**
     * Gets the value of the compatibleSequencingKits property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the compatibleSequencingKits property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCompatibleSequencingKits().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CompatibleSequencingKits }
     * 
     * 
     */
    public List<CompatibleSequencingKits> getCompatibleSequencingKits() {
        if (compatibleSequencingKits == null) {
            compatibleSequencingKits = new ArrayList<CompatibleSequencingKits>();
        }
        return this.compatibleSequencingKits;
    }

    /**
     * Gets the value of the compatibleChipLayouts property.
     * 
     * @return
     *     possible object is
     *     {@link CompatibleChipLayouts }
     *     
     */
    public CompatibleChipLayouts getCompatibleChipLayouts() {
        return compatibleChipLayouts;
    }

    /**
     * Sets the value of the compatibleChipLayouts property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompatibleChipLayouts }
     *     
     */
    public void setCompatibleChipLayouts(CompatibleChipLayouts value) {
        this.compatibleChipLayouts = value;
    }

    /**
     * Gets the value of the validation property.
     * 
     * @return
     *     possible object is
     *     {@link Validation }
     *     
     */
    public Validation getValidation() {
        return validation;
    }

    /**
     * Sets the value of the validation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Validation }
     *     
     */
    public void setValidation(Validation value) {
        this.validation = value;
    }

}
