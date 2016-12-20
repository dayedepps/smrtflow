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
import javax.xml.bind.annotation.XmlType;


/**
 * A container for a set of analogs
 * 
 * <p>Java class for SequencingChemistryConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SequencingChemistryConfig"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}DataEntityType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Analogs"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Analog" type="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}AnalogType" maxOccurs="4"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="DefaultLaserSetPoint" type="{http://www.w3.org/2001/XMLSchema}float"/&gt;
 *         &lt;element name="SNRCut" type="{http://www.w3.org/2001/XMLSchema}float"/&gt;
 *         &lt;element name="TargetSNR"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="SNR_A" use="required" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                 &lt;attribute name="SNR_C" use="required" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                 &lt;attribute name="SNR_G" use="required" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                 &lt;attribute name="SNR_T" use="required" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SequencingChemistryConfig", propOrder = {
    "analogs",
    "defaultLaserSetPoint",
    "snrCut",
    "targetSNR"
})
public class SequencingChemistryConfig
    extends DataEntityType
{

    @XmlElement(name = "Analogs", required = true)
    protected SequencingChemistryConfig.Analogs analogs;
    @XmlElement(name = "DefaultLaserSetPoint")
    protected float defaultLaserSetPoint;
    @XmlElement(name = "SNRCut")
    protected float snrCut;
    @XmlElement(name = "TargetSNR", required = true)
    protected SequencingChemistryConfig.TargetSNR targetSNR;

    /**
     * Gets the value of the analogs property.
     * 
     * @return
     *     possible object is
     *     {@link SequencingChemistryConfig.Analogs }
     *     
     */
    public SequencingChemistryConfig.Analogs getAnalogs() {
        return analogs;
    }

    /**
     * Sets the value of the analogs property.
     * 
     * @param value
     *     allowed object is
     *     {@link SequencingChemistryConfig.Analogs }
     *     
     */
    public void setAnalogs(SequencingChemistryConfig.Analogs value) {
        this.analogs = value;
    }

    /**
     * Gets the value of the defaultLaserSetPoint property.
     * 
     */
    public float getDefaultLaserSetPoint() {
        return defaultLaserSetPoint;
    }

    /**
     * Sets the value of the defaultLaserSetPoint property.
     * 
     */
    public void setDefaultLaserSetPoint(float value) {
        this.defaultLaserSetPoint = value;
    }

    /**
     * Gets the value of the snrCut property.
     * 
     */
    public float getSNRCut() {
        return snrCut;
    }

    /**
     * Sets the value of the snrCut property.
     * 
     */
    public void setSNRCut(float value) {
        this.snrCut = value;
    }

    /**
     * Gets the value of the targetSNR property.
     * 
     * @return
     *     possible object is
     *     {@link SequencingChemistryConfig.TargetSNR }
     *     
     */
    public SequencingChemistryConfig.TargetSNR getTargetSNR() {
        return targetSNR;
    }

    /**
     * Sets the value of the targetSNR property.
     * 
     * @param value
     *     allowed object is
     *     {@link SequencingChemistryConfig.TargetSNR }
     *     
     */
    public void setTargetSNR(SequencingChemistryConfig.TargetSNR value) {
        this.targetSNR = value;
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
     *         &lt;element name="Analog" type="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}AnalogType" maxOccurs="4"/&gt;
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
        "analog"
    })
    public static class Analogs {

        @XmlElement(name = "Analog", required = true)
        protected List<AnalogType> analog;

        /**
         * Gets the value of the analog property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the analog property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAnalog().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link AnalogType }
         * 
         * 
         */
        public List<AnalogType> getAnalog() {
            if (analog == null) {
                analog = new ArrayList<AnalogType>();
            }
            return this.analog;
        }

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
     *       &lt;attribute name="SNR_A" use="required" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *       &lt;attribute name="SNR_C" use="required" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *       &lt;attribute name="SNR_G" use="required" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *       &lt;attribute name="SNR_T" use="required" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class TargetSNR {

        @XmlAttribute(name = "SNR_A", required = true)
        protected float snra;
        @XmlAttribute(name = "SNR_C", required = true)
        protected float snrc;
        @XmlAttribute(name = "SNR_G", required = true)
        protected float snrg;
        @XmlAttribute(name = "SNR_T", required = true)
        protected float snrt;

        /**
         * Gets the value of the snra property.
         * 
         */
        public float getSNRA() {
            return snra;
        }

        /**
         * Sets the value of the snra property.
         * 
         */
        public void setSNRA(float value) {
            this.snra = value;
        }

        /**
         * Gets the value of the snrc property.
         * 
         */
        public float getSNRC() {
            return snrc;
        }

        /**
         * Sets the value of the snrc property.
         * 
         */
        public void setSNRC(float value) {
            this.snrc = value;
        }

        /**
         * Gets the value of the snrg property.
         * 
         */
        public float getSNRG() {
            return snrg;
        }

        /**
         * Sets the value of the snrg property.
         * 
         */
        public void setSNRG(float value) {
            this.snrg = value;
        }

        /**
         * Gets the value of the snrt property.
         * 
         */
        public float getSNRT() {
            return snrt;
        }

        /**
         * Sets the value of the snrt property.
         * 
         */
        public void setSNRT(float value) {
            this.snrt = value;
        }

    }

}
