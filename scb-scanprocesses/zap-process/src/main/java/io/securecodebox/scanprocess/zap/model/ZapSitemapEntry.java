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
package io.securecodebox.scanprocess.zap.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZapSitemapEntry {
    public ZapSitemapEntry(String location, String method, String payload){
        this.location = location;
        this.method = method;
        this.payload = payload;
    }

    @JsonProperty("LOCATION")
    public String location;

    @JsonProperty("METHOD")
    public String method;

    @JsonProperty("PAYLOAD")
    public String payload;
}