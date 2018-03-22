
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
@XmlRootElement(name = "finished")
public class Finished {

    @XmlAttribute(name = "time", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String time;
    @XmlAttribute(name = "timestr")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String timestr;
    @XmlAttribute(name = "elapsed", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String elapsed;
    @XmlAttribute(name = "summary")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String summary;
    @XmlAttribute(name = "exit")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String exit;
    @XmlAttribute(name = "errormsg")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String errormsg;

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTime(String value) {
        this.time = value;
    }

    /**
     * Gets the value of the timestr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimestr() {
        return timestr;
    }

    /**
     * Sets the value of the timestr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimestr(String value) {
        this.timestr = value;
    }

    /**
     * Gets the value of the elapsed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElapsed() {
        return elapsed;
    }

    /**
     * Sets the value of the elapsed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElapsed(String value) {
        this.elapsed = value;
    }

    /**
     * Gets the value of the summary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the value of the summary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSummary(String value) {
        this.summary = value;
    }

    /**
     * Gets the value of the exit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExit() {
        return exit;
    }

    /**
     * Sets the value of the exit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExit(String value) {
        this.exit = value;
    }

    /**
     * Gets the value of the errormsg property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrormsg() {
        return errormsg;
    }

    /**
     * Sets the value of the errormsg property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrormsg(String value) {
        this.errormsg = value;
    }

}
