
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
@XmlRootElement(name = "nmaprun")
public class NmapRawResult {

    @XmlAttribute(name = "scanner", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String scanner;
    @XmlAttribute(name = "args")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String args;
    @XmlAttribute(name = "start")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String start;
    @XmlAttribute(name = "startstr")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String startstr;
    @XmlAttribute(name = "version", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlAttribute(name = "profile_name")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String profileName;
    @XmlAttribute(name = "xmloutputversion", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String xmloutputversion;
    protected List<Scaninfo> scaninfo;
    @XmlElement(required = true)
    protected Verbose verbose;
    @XmlElement(required = true)
    protected Debugging debugging;
    @XmlElements({ @XmlElement(name = "target", type = Target.class),
            @XmlElement(name = "taskbegin", type = Taskbegin.class),
            @XmlElement(name = "taskprogress", type = Taskprogress.class),
            @XmlElement(name = "taskend", type = Taskend.class),
            @XmlElement(name = "prescript", type = Prescript.class),
            @XmlElement(name = "postscript", type = Postscript.class),
            @XmlElement(name = "output", type = Output.class) })
    protected List<Object> targetOrTaskbeginOrTaskprogressOrTaskendOrPrescriptOrPostscriptOrHostOrOutput;

    @XmlElements({ @XmlElement(name = "host", type = Host.class) })
    protected List<Host> hosts;
    @XmlElement(required = true)
    protected Runstats runstats;

    /**
     * Gets the value of the scanner property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getScanner() {
        return scanner;
    }

    /**
     * Sets the value of the scanner property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setScanner(String value) {
        this.scanner = value;
    }

    /**
     * Gets the value of the args property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getArgs() {
        return args;
    }

    /**
     * Sets the value of the args property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setArgs(String value) {
        this.args = value;
    }

    /**
     * Gets the value of the start property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStart(String value) {
        this.start = value;
    }

    /**
     * Gets the value of the startstr property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStartstr() {
        return startstr;
    }

    /**
     * Sets the value of the startstr property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStartstr(String value) {
        this.startstr = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the profileName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Sets the value of the profileName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProfileName(String value) {
        this.profileName = value;
    }

    /**
     * Gets the value of the xmloutputversion property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getXmloutputversion() {
        return xmloutputversion;
    }

    /**
     * Sets the value of the xmloutputversion property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setXmloutputversion(String value) {
        this.xmloutputversion = value;
    }

    /**
     * Gets the value of the scaninfo property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scaninfo property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScaninfo().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Scaninfo }
     */
    public List<Scaninfo> getScaninfo() {
        if (scaninfo == null) {
            scaninfo = new ArrayList<Scaninfo>();
        }
        return this.scaninfo;
    }

    /**
     * Gets the value of the verbose property.
     *
     * @return possible object is
     * {@link Verbose }
     */
    public Verbose getVerbose() {
        return verbose;
    }

    /**
     * Sets the value of the verbose property.
     *
     * @param value allowed object is
     *              {@link Verbose }
     */
    public void setVerbose(Verbose value) {
        this.verbose = value;
    }

    /**
     * Gets the value of the debugging property.
     *
     * @return possible object is
     * {@link Debugging }
     */
    public Debugging getDebugging() {
        return debugging;
    }

    /**
     * Sets the value of the debugging property.
     *
     * @param value allowed object is
     *              {@link Debugging }
     */
    public void setDebugging(Debugging value) {
        this.debugging = value;
    }

    /**
     * Gets the value of the targetOrTaskbeginOrTaskprogressOrTaskendOrPrescriptOrPostscriptOrHostOrOutput property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the targetOrTaskbeginOrTaskprogressOrTaskendOrPrescriptOrPostscriptOrHostOrOutput property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTargetOrTaskbeginOrTaskprogressOrTaskendOrPrescriptOrPostscriptOrHostOrOutput().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Target }
     * {@link Taskbegin }
     * {@link Taskprogress }
     * {@link Taskend }
     * {@link Prescript }
     * {@link Postscript }
     * {@link Output }
     */
    public List<Object> getTargetOrTaskbeginOrTaskprogressOrTaskendOrPrescriptOrPostscriptOrHostOrOutput() {
        if (targetOrTaskbeginOrTaskprogressOrTaskendOrPrescriptOrPostscriptOrHostOrOutput == null) {
            targetOrTaskbeginOrTaskprogressOrTaskendOrPrescriptOrPostscriptOrHostOrOutput = new ArrayList<Object>();
        }
        return this.targetOrTaskbeginOrTaskprogressOrTaskendOrPrescriptOrPostscriptOrHostOrOutput;
    }

    /**
     * Gets the value of the runstats property.
     *
     * @return possible object is
     * {@link Runstats }
     */
    public Runstats getRunstats() {
        return runstats;
    }

    /**
     * Sets the value of the runstats property.
     *
     * @param value allowed object is
     *              {@link Runstats }
     */
    public void setRunstats(Runstats value) {
        this.runstats = value;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }
}
