
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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@Generated("transform.xslt")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "scaninfo")
public class Scaninfo {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "scanflags")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String scanflags;
    @XmlAttribute(name = "protocol", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String protocol;
    @XmlAttribute(name = "numservices", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String numservices;
    @XmlAttribute(name = "services", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String services;

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
     * Gets the value of the scanflags property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScanflags() {
        return scanflags;
    }

    /**
     * Sets the value of the scanflags property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScanflags(String value) {
        this.scanflags = value;
    }

    /**
     * Gets the value of the protocol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the value of the protocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProtocol(String value) {
        this.protocol = value;
    }

    /**
     * Gets the value of the numservices property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumservices() {
        return numservices;
    }

    /**
     * Sets the value of the numservices property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumservices(String value) {
        this.numservices = value;
    }

    /**
     * Gets the value of the services property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServices() {
        return services;
    }

    /**
     * Sets the value of the services property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServices(String value) {
        this.services = value;
    }

}
