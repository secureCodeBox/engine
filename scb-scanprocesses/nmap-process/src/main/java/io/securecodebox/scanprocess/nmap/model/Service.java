
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
@XmlRootElement(name = "service")
public class Service {

    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String name;
    @XmlAttribute(name = "conf", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String conf;
    @XmlAttribute(name = "method", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String method;
    @XmlAttribute(name = "version")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlAttribute(name = "product")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String product;
    @XmlAttribute(name = "extrainfo")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String extrainfo;
    @XmlAttribute(name = "tunnel")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String tunnel;
    @XmlAttribute(name = "proto")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String proto;
    @XmlAttribute(name = "rpcnum")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String rpcnum;
    @XmlAttribute(name = "lowver")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String lowver;
    @XmlAttribute(name = "highver")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String highver;
    @XmlAttribute(name = "hostname")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String hostname;
    @XmlAttribute(name = "ostype")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String ostype;
    @XmlAttribute(name = "devicetype")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String devicetype;
    @XmlAttribute(name = "servicefp")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String servicefp;
    protected List<Cpe> cpe;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the conf property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConf() {
        return conf;
    }

    /**
     * Sets the value of the conf property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConf(String value) {
        this.conf = value;
    }

    /**
     * Gets the value of the method property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMethod(String value) {
        this.method = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the product property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProduct() {
        return product;
    }

    /**
     * Sets the value of the product property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProduct(String value) {
        this.product = value;
    }

    /**
     * Gets the value of the extrainfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtrainfo() {
        return extrainfo;
    }

    /**
     * Sets the value of the extrainfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtrainfo(String value) {
        this.extrainfo = value;
    }

    /**
     * Gets the value of the tunnel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTunnel() {
        return tunnel;
    }

    /**
     * Sets the value of the tunnel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTunnel(String value) {
        this.tunnel = value;
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
     * Gets the value of the rpcnum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRpcnum() {
        return rpcnum;
    }

    /**
     * Sets the value of the rpcnum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRpcnum(String value) {
        this.rpcnum = value;
    }

    /**
     * Gets the value of the lowver property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLowver() {
        return lowver;
    }

    /**
     * Sets the value of the lowver property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLowver(String value) {
        this.lowver = value;
    }

    /**
     * Gets the value of the highver property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHighver() {
        return highver;
    }

    /**
     * Sets the value of the highver property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHighver(String value) {
        this.highver = value;
    }

    /**
     * Gets the value of the hostname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the value of the hostname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHostname(String value) {
        this.hostname = value;
    }

    /**
     * Gets the value of the ostype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOstype() {
        return ostype;
    }

    /**
     * Sets the value of the ostype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOstype(String value) {
        this.ostype = value;
    }

    /**
     * Gets the value of the devicetype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDevicetype() {
        return devicetype;
    }

    /**
     * Sets the value of the devicetype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDevicetype(String value) {
        this.devicetype = value;
    }

    /**
     * Gets the value of the servicefp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServicefp() {
        return servicefp;
    }

    /**
     * Sets the value of the servicefp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServicefp(String value) {
        this.servicefp = value;
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
