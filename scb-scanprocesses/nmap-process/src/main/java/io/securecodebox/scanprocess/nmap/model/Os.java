
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 */
@Generated("transform.xslt")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "portused",
    "osmatch",
    "osfingerprint"
})
@XmlRootElement(name = "os")
public class Os {

    protected List<Portused> portused;
    protected List<Osmatch> osmatch;
    protected List<Osfingerprint> osfingerprint;

    /**
     * Gets the value of the portused property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the portused property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPortused().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Portused }
     * 
     * 
     */
    public List<Portused> getPortused() {
        if (portused == null) {
            portused = new ArrayList<Portused>();
        }
        return this.portused;
    }

    /**
     * Gets the value of the osmatch property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the osmatch property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOsmatch().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Osmatch }
     * 
     * 
     */
    public List<Osmatch> getOsmatch() {
        if (osmatch == null) {
            osmatch = new ArrayList<Osmatch>();
        }
        return this.osmatch;
    }

    /**
     * Gets the value of the osfingerprint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the osfingerprint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOsfingerprint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Osfingerprint }
     * 
     * 
     */
    public List<Osfingerprint> getOsfingerprint() {
        if (osfingerprint == null) {
            osfingerprint = new ArrayList<Osfingerprint>();
        }
        return this.osfingerprint;
    }

}
