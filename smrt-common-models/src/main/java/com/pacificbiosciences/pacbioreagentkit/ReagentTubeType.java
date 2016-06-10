//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.06.09 at 04:52:36 PM PDT 
//


package com.pacificbiosciences.pacbioreagentkit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com.pacificbiosciences.pacbiobasedatamodel.BaseEntityType;


/**
 * <p>Java class for ReagentTubeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReagentTubeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}BaseEntityType">
 *       &lt;attribute name="ProductCode" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="ReagentKey" use="required" type="{http://pacificbiosciences.com/PacBioReagentKit.xsd}ReagentKey" />
 *       &lt;attribute name="Volume" use="required" type="{http://www.w3.org/2001/XMLSchema}short" />
 *       &lt;attribute name="DeadVolume" use="required" type="{http://www.w3.org/2001/XMLSchema}short" />
 *       &lt;attribute name="ActiveInHour" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="TubeWellType" use="required" type="{http://pacificbiosciences.com/PacBioReagentKit.xsd}TubeSize" />
 *       &lt;attribute name="ReagentTubeType" use="required" type="{http://pacificbiosciences.com/PacBioReagentKit.xsd}TubeLocation" />
 *       &lt;attribute name="InitialUse" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReagentTubeType")
public class ReagentTubeType
    extends BaseEntityType
{

    @XmlAttribute(name = "ProductCode", required = true)
    protected String productCode;
    @XmlAttribute(name = "ReagentKey", required = true)
    protected ReagentKey reagentKey;
    @XmlAttribute(name = "Volume", required = true)
    protected short volume;
    @XmlAttribute(name = "DeadVolume", required = true)
    protected short deadVolume;
    @XmlAttribute(name = "ActiveInHour", required = true)
    protected int activeInHour;
    @XmlAttribute(name = "TubeWellType", required = true)
    protected TubeSize tubeWellType;
    @XmlAttribute(name = "ReagentTubeType", required = true)
    protected TubeLocation reagentTubeType;
    @XmlAttribute(name = "InitialUse")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar initialUse;

    /**
     * Gets the value of the productCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductCode() {
        return productCode;
    }

    /**
     * Sets the value of the productCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductCode(String value) {
        this.productCode = value;
    }

    /**
     * Gets the value of the reagentKey property.
     * 
     * @return
     *     possible object is
     *     {@link ReagentKey }
     *     
     */
    public ReagentKey getReagentKey() {
        return reagentKey;
    }

    /**
     * Sets the value of the reagentKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReagentKey }
     *     
     */
    public void setReagentKey(ReagentKey value) {
        this.reagentKey = value;
    }

    /**
     * Gets the value of the volume property.
     * 
     */
    public short getVolume() {
        return volume;
    }

    /**
     * Sets the value of the volume property.
     * 
     */
    public void setVolume(short value) {
        this.volume = value;
    }

    /**
     * Gets the value of the deadVolume property.
     * 
     */
    public short getDeadVolume() {
        return deadVolume;
    }

    /**
     * Sets the value of the deadVolume property.
     * 
     */
    public void setDeadVolume(short value) {
        this.deadVolume = value;
    }

    /**
     * Gets the value of the activeInHour property.
     * 
     */
    public int getActiveInHour() {
        return activeInHour;
    }

    /**
     * Sets the value of the activeInHour property.
     * 
     */
    public void setActiveInHour(int value) {
        this.activeInHour = value;
    }

    /**
     * Gets the value of the tubeWellType property.
     * 
     * @return
     *     possible object is
     *     {@link TubeSize }
     *     
     */
    public TubeSize getTubeWellType() {
        return tubeWellType;
    }

    /**
     * Sets the value of the tubeWellType property.
     * 
     * @param value
     *     allowed object is
     *     {@link TubeSize }
     *     
     */
    public void setTubeWellType(TubeSize value) {
        this.tubeWellType = value;
    }

    /**
     * Gets the value of the reagentTubeType property.
     * 
     * @return
     *     possible object is
     *     {@link TubeLocation }
     *     
     */
    public TubeLocation getReagentTubeType() {
        return reagentTubeType;
    }

    /**
     * Sets the value of the reagentTubeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link TubeLocation }
     *     
     */
    public void setReagentTubeType(TubeLocation value) {
        this.reagentTubeType = value;
    }

    /**
     * Gets the value of the initialUse property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getInitialUse() {
        return initialUse;
    }

    /**
     * Sets the value of the initialUse property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setInitialUse(XMLGregorianCalendar value) {
        this.initialUse = value;
    }

}
