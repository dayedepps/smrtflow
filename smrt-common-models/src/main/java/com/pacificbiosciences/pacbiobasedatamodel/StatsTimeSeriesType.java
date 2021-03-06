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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Time series (for time-dependent metrics)
 * 
 * <p>Java class for StatsTimeSeriesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StatsTimeSeriesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}BaseEntityType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="TimeUnits" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ValueUnits" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="StartTime" type="{http://www.w3.org/2001/XMLSchema}float"/&gt;
 *         &lt;element name="MeasInterval" type="{http://www.w3.org/2001/XMLSchema}float"/&gt;
 *         &lt;element name="Values" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Val" type="{http://www.w3.org/2001/XMLSchema}float" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
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
@XmlType(name = "StatsTimeSeriesType", propOrder = {
    "timeUnits",
    "valueUnits",
    "startTime",
    "measInterval",
    "values"
})
public class StatsTimeSeriesType
    extends BaseEntityType
{

    @XmlElement(name = "TimeUnits", required = true)
    protected String timeUnits;
    @XmlElement(name = "ValueUnits", required = true)
    protected String valueUnits;
    @XmlElement(name = "StartTime")
    protected float startTime;
    @XmlElement(name = "MeasInterval")
    protected float measInterval;
    @XmlElement(name = "Values")
    protected StatsTimeSeriesType.Values values;

    /**
     * Gets the value of the timeUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeUnits() {
        return timeUnits;
    }

    /**
     * Sets the value of the timeUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeUnits(String value) {
        this.timeUnits = value;
    }

    /**
     * Gets the value of the valueUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueUnits() {
        return valueUnits;
    }

    /**
     * Sets the value of the valueUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueUnits(String value) {
        this.valueUnits = value;
    }

    /**
     * Gets the value of the startTime property.
     * 
     */
    public float getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     */
    public void setStartTime(float value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the measInterval property.
     * 
     */
    public float getMeasInterval() {
        return measInterval;
    }

    /**
     * Sets the value of the measInterval property.
     * 
     */
    public void setMeasInterval(float value) {
        this.measInterval = value;
    }

    /**
     * Gets the value of the values property.
     * 
     * @return
     *     possible object is
     *     {@link StatsTimeSeriesType.Values }
     *     
     */
    public StatsTimeSeriesType.Values getValues() {
        return values;
    }

    /**
     * Sets the value of the values property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatsTimeSeriesType.Values }
     *     
     */
    public void setValues(StatsTimeSeriesType.Values value) {
        this.values = value;
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
     *         &lt;element name="Val" type="{http://www.w3.org/2001/XMLSchema}float" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "val"
    })
    public static class Values {

        @XmlElement(name = "Val", type = Float.class)
        protected List<Float> val;

        /**
         * Gets the value of the val property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the val property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getVal().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Float }
         * 
         * 
         */
        public List<Float> getVal() {
            if (val == null) {
                val = new ArrayList<Float>();
            }
            return this.val;
        }

    }

}
