//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.19 at 11:50:03 AM PST 
//


package com.pacificbiosciences.pacbiobasedatamodel;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com.pacificbiosciences.pacbiocollectionmetadata.WellSample;
import com.pacificbiosciences.pacbiodatamodel.ExperimentContainerType;
import com.pacificbiosciences.pacbiodatasets.Contigs;
import com.pacificbiosciences.pacbioreagentkit.ReagentKitType;
import com.pacificbiosciences.pacbioreagentkit.ReagentPlateRowType;
import com.pacificbiosciences.pacbioreagentkit.ReagentTubeType;
import com.pacificbiosciences.pacbioreagentkit.ReagentType;


/**
 * This is the base element type for all types in this data model
 * 
 * <p>Java class for BaseEntityType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BaseEntityType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Extensions" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}ExtensionElement" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="Description" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="Tags" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="Format" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="ResourceId" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="Version" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="CreatedAt"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}dateTime"&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="ModifiedAt"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}dateTime"&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseEntityType", propOrder = {
    "extensions"
})
@XmlSeeAlso({
    AutomationConstraintType.class,
    DNABarcode.class,
    AutomationType.class,
    IncompatiblePairType.class,
    com.pacificbiosciences.pacbiobasedatamodel.SequencingChemistry.DyeSet.class,
    StatsContinuousDistType.class,
    StatsDiscreteDistType.class,
    StatsTimeSeriesType.class,
    ReagentKitType.class,
    ReagentType.class,
    ReagentTubeType.class,
    ReagentPlateRowType.class,
    ExperimentContainerType.class,
    Contigs.Contig.class,
    Contigs.class,
    WellSample.class,
    DataEntityType.class,
    StrictEntityType.class,
    AnalogType.class
})
public class BaseEntityType {

    @XmlElement(name = "Extensions")
    protected BaseEntityType.Extensions extensions;
    @XmlAttribute(name = "Name")
    protected String name;
    @XmlAttribute(name = "Description")
    protected String description;
    @XmlAttribute(name = "Tags")
    protected String tags;
    @XmlAttribute(name = "Format")
    protected String format;
    @XmlAttribute(name = "ResourceId")
    @XmlSchemaType(name = "anyURI")
    protected String resourceId;
    @XmlAttribute(name = "Version")
    protected String version;
    @XmlAttribute(name = "CreatedAt")
    protected XMLGregorianCalendar createdAt;
    @XmlAttribute(name = "ModifiedAt")
    protected XMLGregorianCalendar modifiedAt;

    /**
     * Gets the value of the extensions property.
     * 
     * @return
     *     possible object is
     *     {@link BaseEntityType.Extensions }
     *     
     */
    public BaseEntityType.Extensions getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseEntityType.Extensions }
     *     
     */
    public void setExtensions(BaseEntityType.Extensions value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the tags property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTags() {
        return tags;
    }

    /**
     * Sets the value of the tags property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTags(String value) {
        this.tags = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormat(String value) {
        this.format = value;
    }

    /**
     * Gets the value of the resourceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Sets the value of the resourceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResourceId(String value) {
        this.resourceId = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the createdAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the value of the createdAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreatedAt(XMLGregorianCalendar value) {
        this.createdAt = value;
    }

    /**
     * Gets the value of the modifiedAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getModifiedAt() {
        return modifiedAt;
    }

    /**
     * Sets the value of the modifiedAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setModifiedAt(XMLGregorianCalendar value) {
        this.modifiedAt = value;
    }


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
     *         &lt;element ref="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}ExtensionElement" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "extensionElement"
    })
    public static class Extensions {

        @XmlElement(name = "ExtensionElement")
        protected List<Object> extensionElement;

        /**
         * Gets the value of the extensionElement property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the extensionElement property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getExtensionElement().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Object }
         * 
         * 
         */
        public List<Object> getExtensionElement() {
            if (extensionElement == null) {
                extensionElement = new ArrayList<Object>();
            }
            return this.extensionElement;
        }

    }

}
