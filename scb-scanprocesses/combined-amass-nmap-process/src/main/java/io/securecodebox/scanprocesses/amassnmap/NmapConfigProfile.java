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

package io.securecodebox.scanprocesses.amassnmap;

public enum NmapConfigProfile {
    HTTP_PORTS("-Pn -p 80,8080,443,8443"),
    HTTP_PORTS_WITH_HTTP_HEADERS("-Pn -p 80,8080,443,8443 --script=http-headers"),
    TOP_100_PORTS("-Pn --top-ports 100"),
    TOP_100_PORTS_WITH_HTTP_HEADERS("-Pn --top-ports 100 --script=http-headers"),
    TOP_250_PORTS("-Pn --top-ports 250"),
    TOP_250_PORTS_WITH_HTTP_HEADERS("-Pn --top-ports 250 --script=http-headers"),
    TOP_500_PORTS("-Pn --top-ports 500"),
    TOP_500_PORTS_WITH_HTTP_HEADERS("-Pn --top-ports 500 --script=http-headers"),
    TOP_1000_PORTS("-Pn --top-ports 1000"),
    TOP_1000_PORTS_WITH_HTTP_HEADERS("-Pn --top-ports 1000 --script=http-headers");
    private final String parameter;

    NmapConfigProfile(final String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() { return parameter; }
}
