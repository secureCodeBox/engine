
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
@XmlRootElement(name = "times")
public class Times {

    @XmlAttribute(name = "srtt", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String srtt;
    @XmlAttribute(name = "rttvar", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String rttvar;
    @XmlAttribute(name = "to", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String to;

    /**
     * Gets the value of the srtt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrtt() {
        return srtt;
    }

    /**
     * Sets the value of the srtt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrtt(String value) {
        this.srtt = value;
    }

    /**
     * Gets the value of the rttvar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRttvar() {
        return rttvar;
    }

    /**
     * Sets the value of the rttvar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRttvar(String value) {
        this.rttvar = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTo(String value) {
        this.to = value;
    }

}
