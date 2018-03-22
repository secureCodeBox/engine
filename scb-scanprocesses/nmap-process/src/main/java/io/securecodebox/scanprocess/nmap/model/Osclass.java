
/*
 *
 *  SecureCodeBox (SCB)
 *  Copyright 2015-2018 iteratec GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package io.securecodebox.scanprocess.nmap.model;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 */
@Generated("transform.xslt")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cpe"
})
@XmlRootElement(name = "osclass")
public class Osclass {

    @XmlAttribute(name = "vendor", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String vendor;
    @XmlAttribute(name = "osgen")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String osgen;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "accuracy", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String accuracy;
    @XmlAttribute(name = "osfamily", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String osfamily;
    protected List<Cpe> cpe;

    /**
     * Gets the value of the vendor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Sets the value of the vendor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVendor(String value) {
        this.vendor = value;
    }

    /**
     * Gets the value of the osgen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOsgen() {
        return osgen;
    }

    /**
     * Sets the value of the osgen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOsgen(String value) {
        this.osgen = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the accuracy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccuracy() {
        return accuracy;
    }

    /**
     * Sets the value of the accuracy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccuracy(String value) {
        this.accuracy = value;
    }

    /**
     * Gets the value of the osfamily property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOsfamily() {
        return osfamily;
    }

    /**
     * Sets the value of the osfamily property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOsfamily(String value) {
        this.osfamily = value;
    }

    /**
     * Gets the value of the cpe property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cpe property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCpe().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Cpe }
     * 
     * 
     */
    public List<Cpe> getCpe() {
        if (cpe == null) {
            cpe = new ArrayList<Cpe>();
        }
        return this.cpe;
    }

}
