
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
@XmlRootElement(name = "hosts")
public class Hosts {

    @XmlAttribute(name = "up")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String up;
    @XmlAttribute(name = "down")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String down;
    @XmlAttribute(name = "total", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String total;

    /**
     * Gets the value of the up property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUp() {
        if (up == null) {
            return "0";
        } else {
            return up;
        }
    }

    /**
     * Sets the value of the up property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUp(String value) {
        this.up = value;
    }

    /**
     * Gets the value of the down property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDown() {
        if (down == null) {
            return "0";
        } else {
            return down;
        }
    }

    /**
     * Sets the value of the down property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDown(String value) {
        this.down = value;
    }

    /**
     * Gets the value of the total property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTotal() {
        return total;
    }

    /**
     * Sets the value of the total property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTotal(String value) {
        this.total = value;
    }

}
