//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.04.06 at 11:45:18 AM PDT 
//


package com.pacificbiosciences.pacbiocollectionmetadata;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
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
 *         &lt;element name="TemplatePrepKitPastExpiration" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="BindingKitPastExpiration" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="CellPacPastExpiration" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="CellPacPastExpiration" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="SequencingKitPastExpiration" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="SequencingTube0PastExpiration" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="SequencingTube1PastExpiration" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "content"
})
@XmlRootElement(name = "ExpirationData")
public class ExpirationData {

    @XmlElementRefs({
        @XmlElementRef(name = "SequencingTube1PastExpiration", namespace = "http://pacificbiosciences.com/PacBioCollectionMetadata.xsd", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "TemplatePrepKitPastExpiration", namespace = "http://pacificbiosciences.com/PacBioCollectionMetadata.xsd", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "BindingKitPastExpiration", namespace = "http://pacificbiosciences.com/PacBioCollectionMetadata.xsd", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "CellPacPastExpiration", namespace = "http://pacificbiosciences.com/PacBioCollectionMetadata.xsd", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "SequencingTube0PastExpiration", namespace = "http://pacificbiosciences.com/PacBioCollectionMetadata.xsd", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "SequencingKitPastExpiration", namespace = "http://pacificbiosciences.com/PacBioCollectionMetadata.xsd", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<Integer>> content;

    /**
     * Gets the rest of the content model. 
     * 
     * <p>
     * You are getting this "catch-all" property because of the following reason: 
     * The field name "CellPacPastExpiration" is used by two different parts of a schema. See: 
     * line 230 of file:/Users/mkocher/workspaces/mkocher_mb_services/services/pb-common-models/src/main/resources/pb-common-xsds/PacBioCollectionMetadata.xsd
     * line 225 of file:/Users/mkocher/workspaces/mkocher_mb_services/services/pb-common-models/src/main/resources/pb-common-xsds/PacBioCollectionMetadata.xsd
     * <p>
     * To get rid of this property, apply a property customization to one 
     * of both of the following declarations to change their names: 
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * 
     * 
     */
    public List<JAXBElement<Integer>> getContent() {
        if (content == null) {
            content = new ArrayList<JAXBElement<Integer>>();
        }
        return this.content;
    }

}
