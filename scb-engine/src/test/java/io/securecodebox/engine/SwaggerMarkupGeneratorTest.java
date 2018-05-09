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

package io.securecodebox.engine;

import io.securecodebox.engine.rest.SwaggerConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This test class is a little trick to generate the documentation markup for our developer guide.
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.05.18
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { SecureCodeBoxEngine.class, SwaggerConfiguration.class })
@AutoConfigureMockMvc
public class SwaggerMarkupGeneratorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateSwaggerFile() throws Exception {

        MockHttpServletResponse response = mockMvc.perform(
                MockMvcRequestBuilders.get("/v2/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        try (BufferedWriter writer = Files.newBufferedWriter(getTargetFolderPath(), StandardCharsets.UTF_8)) {
            writer.write(response.getContentAsString());
        }
    }

    private Path getTargetFolderPath() {
        return Paths.get(new File(
                        SwaggerMarkupGeneratorTest.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(),
                "swagger.json");
    }
}
