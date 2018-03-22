
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


/**
 * 
 */
@Generated("transform.xslt")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "hop")
public class Hop {

    @XmlAttribute(name = "ttl", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String ttl;
    @XmlAttribute(name = "rtt")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String rtt;
    @XmlAttribute(name = "ipaddr")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String ipaddr;
    @XmlAttribute(name = "host")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String host;

    /**
     * Gets the value of the ttl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTtl() {
        return ttl;
    }

    /**
     * Sets the value of the ttl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTtl(String value) {
        this.ttl = value;
    }

    /**
     * Gets the value of the rtt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRtt() {
        return rtt;
    }

    /**
     * Sets the value of the rtt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRtt(String value) {
        this.rtt = value;
    }

    /**
     * Gets the value of the ipaddr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIpaddr() {
        return ipaddr;
    }

    /**
     * Sets the value of the ipaddr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIpaddr(String value) {
        this.ipaddr = value;
    }

    /**
     * Gets the value of the host property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHost(String value) {
        this.host = value;
    }

}
