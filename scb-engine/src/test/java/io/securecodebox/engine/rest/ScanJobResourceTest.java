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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.TestHelper;
import io.securecodebox.engine.SecureCodeBoxEngine;
import io.securecodebox.engine.SwaggerMarkupGeneratorTest;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.Severity;
import io.securecodebox.model.rest.ScanResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.fail;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 21.05.18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SecureCodeBoxEngine.class })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ScanJobResourceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testInvalidScannerTypeWhitespace() throws Exception {

        ScanResult scanResult = generateScanResult("test scanner");

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(objectMapper.writeValueAsString(scanResult))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testInvalidScannerTypeChar() throws Exception {

        ScanResult scanResult = generateScanResult("test!scanner");

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(objectMapper.writeValueAsString(scanResult))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testInvalidScannerTypeChar2() throws Exception {

        ScanResult scanResult = generateScanResult("test{scanner");

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(objectMapper.writeValueAsString(scanResult))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testInvalidScannerTypeChar3() throws Exception {

        ScanResult scanResult = generateScanResult("test(scanner");

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(objectMapper.writeValueAsString(scanResult))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testValidScannerType() throws Exception {

        ScanResult scanResult = generateScanResult("test_scanner");

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(objectMapper.writeValueAsString(scanResult))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testInvalidUUID() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    private ScanResult generateScanResult(String s) {
        Finding finding1 = TestHelper.createBasicFinding();
        Finding finding2 = TestHelper.createBasicFinding(UUID.randomUUID());
        ScanResult scanResult = new ScanResult();
        scanResult.setFindings(Arrays.asList(finding1, finding2));
        scanResult.setScannerId(UUID.randomUUID());
        scanResult.setScannerType(s);
        scanResult.setRawFindings("someKindOfString");
        return scanResult;
    }

    @Test
    public void testInvalidFindingUUID() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"INVALID1-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testInvalidFindingName() throws Exception {

        // Testing: \
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD\\TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: {
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD{TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: (
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD(TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: $
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD$TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: %
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD%TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testInvalidFindingDescription() throws Exception {

        // Testing: \
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some \\coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: {
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some {coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: (
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some (coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: $
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some $coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: %
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some %coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testInvalidFindingCategory() throws Exception {

        // Testing: \
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_\\TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: {
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL{_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: (
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL(_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: $
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_$TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: %
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_%TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testInvalidFindingHint() throws Exception {

        // Testing: \
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You \\might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: {
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You {might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: (
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You (might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: $
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might$ wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Testing: %
        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might% wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testInvalidFindingOsi() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_FOUND_JUST_TEST_OSI_LAYER\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        try {
            Severity.valueOf("NOT_FOUND_JUST_TEST_OSI_LAYER");
            fail("Eh the invalid osi layer was found in the enum o.O");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testInvalidFindingSeverity() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"NOT_FOUND_JUST_TEST\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        try {
            Severity.valueOf("NOT_FOUND_JUST_TEST");
            fail("Eh the invalid severity was found in the enum o.O");
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * WORKAROUND for ApplicationContext loadin issues {@see camunda-spring-boot-starter 288}{@link SwaggerMarkupGeneratorTest}
     */
    @Test
    public void testExecuteSwagger() throws Exception {
        // WORKAROUD: This test executes the Swagger generator test, as it's getting an dirty context by camunda process engine!
        SwaggerMarkupGeneratorTest test = new SwaggerMarkupGeneratorTest();
        test.mockMvc = mockMvc;
        test.testCreateSwaggerFile();
    }

}
