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

import io.securecodebox.engine.service.SecurityTestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class SecurityTestDefinitionsResourceTest {
    @InjectMocks
    SecurityTestDefinitionResource classUnderTest;

    @Mock
    SecurityTestService securityTestServiceDummy;

    @Test
    public void shouldReturnAllAvailableProcessKeys() throws Exception {
        given(securityTestServiceDummy.getAvailableSecurityTestDefinitionNames()).willReturn(Arrays.asList("foo", "bar"));
        ResponseEntity<List<String>> response = classUnderTest.getSecurityTestDefinitions();

        assertEquals(Arrays.asList("foo", "bar"), response.getBody());
    }

    @Test
    public void shouldReturnAnEmptyListIfNoProcessesAreAvailable() throws Exception {
        given(securityTestServiceDummy.getAvailableSecurityTestDefinitionNames()).willReturn(new LinkedList<>());
        ResponseEntity<List<String>> response = classUnderTest.getSecurityTestDefinitions();

        assertEquals(new LinkedList<>(), response.getBody());
    }
}