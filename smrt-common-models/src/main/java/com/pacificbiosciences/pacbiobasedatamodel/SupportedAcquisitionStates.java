//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.04.06 at 11:45:18 AM PDT 
//


package com.pacificbiosciences.pacbiobasedatamodel;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SupportedAcquisitionStates.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SupportedAcquisitionStates">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Ready"/>
 *     &lt;enumeration value="Initializing"/>
 *     &lt;enumeration value="Acquiring"/>
 *     &lt;enumeration value="Aligning"/>
 *     &lt;enumeration value="Aligned"/>
 *     &lt;enumeration value="Aborting"/>
 *     &lt;enumeration value="Aborted"/>
 *     &lt;enumeration value="Failed"/>
 *     &lt;enumeration value="Completing"/>
 *     &lt;enumeration value="Complete"/>
 *     &lt;enumeration value="Calibrating"/>
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="Pending"/>
 *     &lt;enumeration value="ReadyToCalibrate"/>
 *     &lt;enumeration value="CalibrationComplete"/>
 *     &lt;enumeration value="ReadyToAcquire"/>
 *     &lt;enumeration value="FinishingAnalysis"/>
 *     &lt;enumeration value="PostPrimaryPending"/>
 *     &lt;enumeration value="PostPrimaryAnalysis"/>
 *     &lt;enumeration value="TransferPending"/>
 *     &lt;enumeration value="TransferringResults"/>
 *     &lt;enumeration value="Error"/>
 *     &lt;enumeration value="Stopped"/>
 *     &lt;enumeration value="TransferFailed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SupportedAcquisitionStates")
@XmlEnum
public enum SupportedAcquisitionStates {

    @XmlEnumValue("Ready")
    READY("Ready"),
    @XmlEnumValue("Initializing")
    INITIALIZING("Initializing"),
    @XmlEnumValue("Acquiring")
    ACQUIRING("Acquiring"),
    @XmlEnumValue("Aligning")
    ALIGNING("Aligning"),
    @XmlEnumValue("Aligned")
    ALIGNED("Aligned"),
    @XmlEnumValue("Aborting")
    ABORTING("Aborting"),
    @XmlEnumValue("Aborted")
    ABORTED("Aborted"),
    @XmlEnumValue("Failed")
    FAILED("Failed"),
    @XmlEnumValue("Completing")
    COMPLETING("Completing"),
    @XmlEnumValue("Complete")
    COMPLETE("Complete"),
    @XmlEnumValue("Calibrating")
    CALIBRATING("Calibrating"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown"),
    @XmlEnumValue("Pending")
    PENDING("Pending"),
    @XmlEnumValue("ReadyToCalibrate")
    READY_TO_CALIBRATE("ReadyToCalibrate"),
    @XmlEnumValue("CalibrationComplete")
    CALIBRATION_COMPLETE("CalibrationComplete"),
    @XmlEnumValue("ReadyToAcquire")
    READY_TO_ACQUIRE("ReadyToAcquire"),
    @XmlEnumValue("FinishingAnalysis")
    FINISHING_ANALYSIS("FinishingAnalysis"),
    @XmlEnumValue("PostPrimaryPending")
    POST_PRIMARY_PENDING("PostPrimaryPending"),
    @XmlEnumValue("PostPrimaryAnalysis")
    POST_PRIMARY_ANALYSIS("PostPrimaryAnalysis"),
    @XmlEnumValue("TransferPending")
    TRANSFER_PENDING("TransferPending"),
    @XmlEnumValue("TransferringResults")
    TRANSFERRING_RESULTS("TransferringResults"),
    @XmlEnumValue("Error")
    ERROR("Error"),
    @XmlEnumValue("Stopped")
    STOPPED("Stopped"),
    @XmlEnumValue("TransferFailed")
    TRANSFER_FAILED("TransferFailed");
    private final String value;

    SupportedAcquisitionStates(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SupportedAcquisitionStates fromValue(String v) {
        for (SupportedAcquisitionStates c: SupportedAcquisitionStates.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
