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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.fail;

/**
 * This tests is a integration test for http body validation
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 21.05.18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = { SecureCodeBoxEngine.class },
        properties = { "securecodebox.rest.auth=none" }
)
@AutoConfigureMockMvc
public class ScanJobResourceValidationTest {

    public static final String SWAGGER_UI_SUCCESS_BODY =
            "{\n" + "  \"findings\": [\n" + "    {\n" + "      \"attributes\": {\n" + "        \"NMAP_PORT\": 34,\n"
                    + "        \"NMAP_IP\": \"162.222.1.3\"\n" + "      },\n"
                    + "      \"category\": \"Infrastructure\",\n"
                    + "      \"description\": \"The DNS Port is open.\",\n"
                    + "      \"hint\": \"SQL-Injection: Please think about using prepared statements.\",\n"
                    + "      \"id\": \"3dd4840c-81ae-4fed-90b5-b3eea3d4c701\",\n"
                    + "      \"location\": \"tcp://162.222.1.3:53\",\n" + "      \"name\": \"Open Port\",\n"
                    + "      \"osi_layer\": \"NETWORK\",\n" + "      \"reference\": {\n"
                    + "        \"id\": \"CVE-2017-15707\",\n"
                    + "        \"source\": \"https://www.cvedetails.com/cve/CVE-2017-15707/\"\n" + "      },\n"
                    + "      \"severity\": \"HIGH\"\n" + "    }\n" + "  ],\n" + "  \"rawFindings\": \"string\",\n"
                    + "  \"scannerId\": \"5dd0840c-81ae-4fed-90b5-b3eea3d4c701\",\n" + "  \"scannerType\": \"nmap\"\n"
                    + "}";
    public static final String SWAGGER_UI_FAILURE_BODY =
            "{\n" + "  \"errorDetails\": \"It was not possible to resolve a DNS entry!\",\n"
                    + "  \"errorMessage\": \"The host down.securecodebox.io is nor reachable!\",\n"
                    + "  \"scannerId\": \"29bf7fd3-8512-4d73-a28f-608e493cd726\"\n" + "}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSuccessResourceInvalidScannerTypeWhitespace() throws Exception {

        ScanResult scanResult = generateScanResult("test scanner");

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(objectMapper.writeValueAsString(scanResult))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testSuccessResourceInvalidScannerTypeChar() throws Exception {

        ScanResult scanResult = generateScanResult("test!scanner");

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(objectMapper.writeValueAsString(scanResult))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testSuccessResourceInvalidScannerTypeChar2() throws Exception {

        ScanResult scanResult = generateScanResult("test{scanner");

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(objectMapper.writeValueAsString(scanResult))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testSuccessResourceInvalidScannerTypeChar3() throws Exception {

        ScanResult scanResult = generateScanResult("test(scanner");

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(objectMapper.writeValueAsString(scanResult))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testSuccessResourceValidScannerType() throws Exception {

        ScanResult scanResult = generateScanResult("test_scanner");

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(objectMapper.writeValueAsString(scanResult))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testSuccessResourceValidSwaggerUi() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(SWAGGER_UI_SUCCESS_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testSuccessResourceInvalidUUID() throws Exception {

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
    public void testSuccessResourceInvalidFindingUUID() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/29bf7fd3-8512-4d73-a28f-608e493cd722/result")
                .content(
                        "{\"findings\":[{\"id\":\"INVALID1-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"},{\"id\":\"f49689b4-c16b-4e71-a08a-7d021a9a593f\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\"}],\"rawFindings\":\"someKindOfString\",\"scannerId\":\"INVALIDb-ba11-48e2-b77c-7d8a14cde2e8\",\"scannerType\":\"test_scanner\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testSuccessResourceInvalidFindingName() throws Exception {

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
    public void testSuccessResourceInvalidFindingDescription() throws Exception {

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
    public void testSuccessResourceInvalidFindingCategory() throws Exception {

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
    public void testSuccessResourceInvalidFindingHint() throws Exception {

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
    public void testSuccessResourceInvalidFindingOsi() throws Exception {

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
    public void testSuccessResourceInvalidFindingSeverity() throws Exception {

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

    @Test
    public void testFailureResourceValidSwaggerUi() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/box/jobs/16f7f8bc-5cd1-11e8-ae96-ce3a6d282fee/failure")
                .content(SWAGGER_UI_FAILURE_BODY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    /**
     * WORKAROUND for ApplicationContext loading issues {@see camunda-spring-boot-starter 288}{@link SwaggerMarkupGeneratorTest}
     */
    @Test
    public void testExecuteSwagger() throws Exception {
        // WORKAROUD: This test executes the Swagger generator test, as it's getting an dirty context by camunda process engine!
        SwaggerMarkupGeneratorTest test = new SwaggerMarkupGeneratorTest();
        test.mockMvc = mockMvc;
        test.testCreateSwaggerFile();
    }

}
