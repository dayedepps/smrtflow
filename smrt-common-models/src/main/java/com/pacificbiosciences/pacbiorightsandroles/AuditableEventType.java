//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.19 at 11:50:03 AM PST 
//


package com.pacificbiosciences.pacbiorightsandroles;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com.pacificbiosciences.pacbiobasedatamodel.StrictEntityType;


/**
 * <p>Java class for AuditableEventType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuditableEventType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}StrictEntityType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://pacificbiosciences.com/PacBioRightsAndRoles.xsd}EventToken"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="DateCreated" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}dateTime"&gt;
 *             &lt;minInclusive value="1000-01-01T00:00:00"/&gt;
 *             &lt;maxInclusive value="9999-12-31T23:59:59"/&gt;
 *             &lt;pattern value="\p{Nd}{4}-\p{Nd}{2}-\p{Nd}{2}T\p{Nd}{2}:\p{Nd}{2}:\p{Nd}{2}"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="AuditEventType" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;maxLength value="255"/&gt;
 *             &lt;minLength value="1"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="OldValue"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}base64Binary"&gt;
 *             &lt;maxLength value="2147483647"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="NewValue" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}base64Binary"&gt;
 *             &lt;maxLength value="2147483647"/&gt;
 *             &lt;minLength value="1"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="Reason"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;maxLength value="255"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="ESig"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}base64Binary"&gt;
 *             &lt;maxLength value="2147483647"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="UserIdentityReference" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditableEventType", propOrder = {
    "eventToken"
})
public class AuditableEventType
    extends StrictEntityType
{

    @XmlElement(name = "EventToken", required = true)
    protected EventToken eventToken;
    @XmlAttribute(name = "DateCreated", required = true)
    protected XMLGregorianCalendar dateCreated;
    @XmlAttribute(name = "AuditEventType", required = true)
    protected String auditEventType;
    @XmlAttribute(name = "OldValue")
    protected byte[] oldValue;
    @XmlAttribute(name = "NewValue", required = true)
    protected byte[] newValue;
    @XmlAttribute(name = "Reason")
    protected String reason;
    @XmlAttribute(name = "ESig")
    protected byte[] eSig;
    @XmlAttribute(name = "UserIdentityReference")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object userIdentityReference;

    /**
     * A handoff event token to be used between system components - maybe superceded by the user's tokenId
     * 
     * @return
     *     possible object is
     *     {@link EventToken }
     *     
     */
    public EventToken getEventToken() {
        return eventToken;
    }

    /**
     * Sets the value of the eventToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventToken }
     *     
     */
    public void setEventToken(EventToken value) {
        this.eventToken = value;
    }

    /**
     * Gets the value of the dateCreated property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateCreated() {
        return dateCreated;
    }

    /**
     * Sets the value of the dateCreated property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateCreated(XMLGregorianCalendar value) {
        this.dateCreated = value;
    }

    /**
     * Gets the value of the auditEventType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuditEventType() {
        return auditEventType;
    }

    /**
     * Sets the value of the auditEventType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuditEventType(String value) {
        this.auditEventType = value;
    }

    /**
     * Gets the value of the oldValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getOldValue() {
        return oldValue;
    }

    /**
     * Sets the value of the oldValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setOldValue(byte[] value) {
        this.oldValue = value;
    }

    /**
     * Gets the value of the newValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getNewValue() {
        return newValue;
    }

    /**
     * Sets the value of the newValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setNewValue(byte[] value) {
        this.newValue = value;
    }

    /**
     * Gets the value of the reason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReason(String value) {
        this.reason = value;
    }

    /**
     * Gets the value of the eSig property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getESig() {
        return eSig;
    }

    /**
     * Sets the value of the eSig property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setESig(byte[] value) {
        this.eSig = value;
    }

    /**
     * Gets the value of the userIdentityReference property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getUserIdentityReference() {
        return userIdentityReference;
    }

    /**
     * Sets the value of the userIdentityReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setUserIdentityReference(Object value) {
        this.userIdentityReference = value;
    }

}
