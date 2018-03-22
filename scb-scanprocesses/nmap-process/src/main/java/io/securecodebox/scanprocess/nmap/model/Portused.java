
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
@XmlRootElement(name = "portused")
public class Portused {

    @XmlAttribute(name = "state", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String state;
    @XmlAttribute(name = "proto", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String proto;
    @XmlAttribute(name = "portid", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String portid;

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setState(String value) {
        this.state = value;
    }

    /**
     * Gets the value of the proto property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProto() {
        return proto;
    }

    /**
     * Sets the value of the proto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProto(String value) {
        this.proto = value;
    }

    /**
     * Gets the value of the portid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPortid() {
        return portid;
    }

    /**
     * Sets the value of the portid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPortid(String value) {
        this.portid = value;
    }

}
