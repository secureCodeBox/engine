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
package io.securecodebox.engine.rest;

import io.securecodebox.engine.SecureCodeBoxEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SecureCodeBoxEngine.class})
@AutoConfigureMockMvc
public class SecurityTestResourceTest {
    @Autowired
    private MockMvc mockMvc;

    private static String nonExistingSecurityTestPayload = "[\n" +
            "  {\n" +
            "    \"context\": \"foobar\",\n" +
            "    \"name\": \"this-test-will-never-ever-exist\",\n" +
            "    \"target\": {\n" +
            "      \"location\": \"bodgeit\",\n" +
            "      \"name\": \"foobar\",\n" +
            "      \"attributes\": {}\n" +
            "    }\n" +
            "  }\n" +
            "]";

    @Test
    public void shouldReturnA404OnUnknownSecurityTestName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/box/security-tests")
                .content(nonExistingSecurityTestPayload)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}