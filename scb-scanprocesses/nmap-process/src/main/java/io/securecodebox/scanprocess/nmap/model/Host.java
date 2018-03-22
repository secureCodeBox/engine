
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 */
@Generated("transform.xslt")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "host")
public class Host {

    @XmlAttribute(name = "starttime")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String starttime;
    @XmlAttribute(name = "endtime")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String endtime;
    @XmlAttribute(name = "comment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String comment;

    @XmlElements({
            @XmlElement(name = "ports", type = Ports.class)
    })
    protected List<Ports> ports;
    @XmlElements({
        @XmlElement(name = "status", type = Status.class),
        @XmlElement(name = "address", type = Address.class),
        @XmlElement(name = "hostnames", type = Hostnames.class),
        @XmlElement(name = "smurf", type = Smurf.class),
        @XmlElement(name = "os", type = Os.class),
        @XmlElement(name = "distance", type = Distance.class),
        @XmlElement(name = "uptime", type = Uptime.class),
        @XmlElement(name = "tcpsequence", type = Tcpsequence.class),
        @XmlElement(name = "ipidsequence", type = Ipidsequence.class),
        @XmlElement(name = "tcptssequence", type = Tcptssequence.class),
        @XmlElement(name = "hostscript", type = Hostscript.class),
        @XmlElement(name = "trace", type = Trace.class),
        @XmlElement(name = "times", type = Times.class)
    })
    protected List<Object> statusOrAddressOrHostnamesOrSmurfOrPortsOrOsOrDistanceOrUptimeOrTcpsequenceOrIpidsequenceOrTcptssequenceOrHostscriptOrTraceOrTimes;

    public Address getAdress(){
        return (Address) this.getStatusOrAddressOrHostnamesOrSmurfOrPortsOrOsOrDistanceOrUptimeOrTcpsequenceOrIpidsequenceOrTcptssequenceOrHostscriptOrTraceOrTimes()
            .stream()
            .filter(item -> (item instanceof Address && ((Address) item ).getAddrtype().equals("ipv4")))
            .collect(Collectors.toList())
            .get(0);
    }

    /**
     * Gets the value of the starttime property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStarttime() {
        return starttime;
    }

    /**
     * Sets the value of the starttime property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStarttime(String value) {
        this.starttime = value;
    }

    /**
     * Gets the value of the endtime property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEndtime() {
        return endtime;
    }

    /**
     * Sets the value of the endtime property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEndtime(String value) {
        this.endtime = value;
    }

    /**
     * Gets the value of the comment property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the statusOrAddressOrHostnamesOrSmurfOrPortsOrOsOrDistanceOrUptimeOrTcpsequenceOrIpidsequenceOrTcptssequenceOrHostscriptOrTraceOrTimes property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the statusOrAddressOrHostnamesOrSmurfOrPortsOrOsOrDistanceOrUptimeOrTcpsequenceOrIpidsequenceOrTcptssequenceOrHostscriptOrTraceOrTimes property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStatusOrAddressOrHostnamesOrSmurfOrPortsOrOsOrDistanceOrUptimeOrTcpsequenceOrIpidsequenceOrTcptssequenceOrHostscriptOrTraceOrTimes().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Status }
     * {@link Address }
     * {@link Hostnames }
     * {@link Smurf }
     * {@link Ports }
     * {@link Os }
     * {@link Distance }
     * {@link Uptime }
     * {@link Tcpsequence }
     * {@link Ipidsequence }
     * {@link Tcptssequence }
     * {@link Hostscript }
     * {@link Trace }
     * {@link Times }
     *
     *
     */
    public List<Object> getStatusOrAddressOrHostnamesOrSmurfOrPortsOrOsOrDistanceOrUptimeOrTcpsequenceOrIpidsequenceOrTcptssequenceOrHostscriptOrTraceOrTimes() {
        if (statusOrAddressOrHostnamesOrSmurfOrPortsOrOsOrDistanceOrUptimeOrTcpsequenceOrIpidsequenceOrTcptssequenceOrHostscriptOrTraceOrTimes == null) {
            statusOrAddressOrHostnamesOrSmurfOrPortsOrOsOrDistanceOrUptimeOrTcpsequenceOrIpidsequenceOrTcptssequenceOrHostscriptOrTraceOrTimes = new ArrayList<Object>();
        }
        return this.statusOrAddressOrHostnamesOrSmurfOrPortsOrOsOrDistanceOrUptimeOrTcpsequenceOrIpidsequenceOrTcptssequenceOrHostscriptOrTraceOrTimes;
    }


    public List<Ports> getPorts() {
        return ports;
    }

    public void setPorts(List<Ports> ports) {
        this.ports = ports;
    }

}
