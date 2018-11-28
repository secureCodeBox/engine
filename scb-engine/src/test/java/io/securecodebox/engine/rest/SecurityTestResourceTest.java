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

import io.securecodebox.engine.service.AuthService;
import io.securecodebox.engine.service.SecurityTestService;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.rest.Report;
import io.securecodebox.model.securitytest.SecurityTest;
import io.securecodebox.model.securitytest.SecurityTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SecurityTestResourceTest {
    @InjectMocks
    SecurityTestResource classUnderTest;

    @Mock
    SecurityTestService securityTestServiceDummy;

    @Mock
    AuthService authService;

    // Tests for: Start securityTest

    @Test
    public void shouldReturnAnErrorWhenNoSuchSecurityTestIsAvailable() throws Exception {
        willThrow(new SecurityTestService.NonExistentSecurityTestDefinitionException()).given(securityTestServiceDummy).checkSecurityTestDefinitionExistence(any());

        SecurityTestConfiguration secTest = new SecurityTestConfiguration();
        secTest.setName("this-process-will-never-exist");

        ResponseEntity<List<UUID>> response = classUnderTest.startSecurityTests(Arrays.asList(secTest));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void shouldReturnAMultipleChoicesErrorIfThereAreMultipleSecurityTestDefinitionsForTheSecurityTestName() throws Exception {
        willThrow(new SecurityTestService.DuplicateSecurityTestDefinitionForKeyException()).given(securityTestServiceDummy).checkSecurityTestDefinitionExistence(any());

        SecurityTestConfiguration secTest = new SecurityTestConfiguration();
        secTest.setName("this-process-key-has-multiple-implementations");

        ResponseEntity<List<UUID>> response = classUnderTest.startSecurityTests(Arrays.asList(secTest));

        assertEquals(HttpStatus.MULTIPLE_CHOICES, response.getStatusCode());
    }

    @Test
    public void shouldStartASecurityTestAndReturnItsUUID() throws Exception {
        given(securityTestServiceDummy.startSecurityTest(any())).willReturn(UUID.fromString("47bd8786-84f2-49ed-9ca9-20ed22be532b"));

        SecurityTestConfiguration secTest = new SecurityTestConfiguration();
        secTest.setName("this-process-is-ok");

        ResponseEntity<List<UUID>> response = classUnderTest.startSecurityTests(Arrays.asList(secTest));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Arrays.asList(UUID.fromString("47bd8786-84f2-49ed-9ca9-20ed22be532b")), response.getBody());

        verify(securityTestServiceDummy, times(1)).startSecurityTest(secTest);
    }

    @Test
    public void shouldReturnA401IfTheUserIsntAuthorizedToStartASecurityTest() throws Exception {
        given(securityTestServiceDummy.startSecurityTest(any())).willReturn(UUID.fromString("47bd8786-84f2-49ed-9ca9-20ed22be532b"));
        willThrow(new InsufficientAuthenticationException("Foobar")).given(authService).checkAuthorizedFor(any(), any(), any());
        SecurityTestConfiguration secTest = new SecurityTestConfiguration();
        secTest.setName("this-process-is-ok");

        ResponseEntity<List<UUID>> response = classUnderTest.startSecurityTests(Arrays.asList(secTest));

        assertEquals(401, response.getStatusCodeValue());
        verify(securityTestServiceDummy, times(0)).startSecurityTest(any());
    }

    @Test
    public void shouldReturnA401IfTheUserIsntAuthorizedToOneOfTheSecurityTestsOfThePayload() throws Exception {
        given(securityTestServiceDummy.startSecurityTest(any())).willReturn(UUID.fromString("47bd8786-84f2-49ed-9ca9-20ed22be532b"));
        willThrow(new InsufficientAuthenticationException("Foobar")).given(authService).checkAuthorizedFor(eq("this-isnt-process"), any(), any());

        SecurityTestConfiguration secTest = new SecurityTestConfiguration();
        secTest.setName("this-process-is-ok");

        SecurityTestConfiguration secTest2 = new SecurityTestConfiguration();
        secTest2.setName("this-isnt");

        ResponseEntity<List<UUID>> response = classUnderTest.startSecurityTests(Arrays.asList(secTest, secTest2));

        assertEquals(401, response.getStatusCodeValue());
        verify(securityTestServiceDummy, times(0)).startSecurityTest(any());
    }

    // Tests for: Get securityTest result

    @Test
    public void shouldReturnAFullSecurityTest() throws Exception {
        UUID id = UUID.randomUUID();
        given(securityTestServiceDummy.getCompletedSecurityTest(any())).willReturn(
                new SecurityTest(
                        id,
                    "Feature Team 1",
                    "nmap",
                    new Target(),
                    new Report()
                )
        );

        ResponseEntity<SecurityTest> response = classUnderTest.getSecurityTest(UUID.randomUUID());

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(id, response.getBody().getId());
        assertEquals("Feature Team 1", response.getBody().getContext());
        assertEquals("nmap", response.getBody().getName());
    }

    @Test
    public void shouldReturnA404WhenNoSecurityTestWasFound() throws Exception {
        given(securityTestServiceDummy.getCompletedSecurityTest(any())).willThrow(new SecurityTestService.SecurityTestNotFoundException());

        ResponseEntity<SecurityTest> response = classUnderTest.getSecurityTest(UUID.randomUUID());

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void shouldReturnA206IfTheSecurityTestIsntFinished() throws Exception {
        UUID id = UUID.randomUUID();
        given(securityTestServiceDummy.getCompletedSecurityTest(any())).willReturn(
                new SecurityTest(
                        id,
                        "Feature Team 1",
                        "nmap",
                        new Target(),
                        null
                )
        );

        ResponseEntity<SecurityTest> response = classUnderTest.getSecurityTest(UUID.randomUUID());

        assertEquals(206, response.getStatusCodeValue());
        assertEquals(id, response.getBody().getId());
        assertEquals("Feature Team 1", response.getBody().getContext());
        assertEquals("nmap", response.getBody().getName());
        assertNull(response.getBody().getReport());
        assertFalse(response.getBody().isFinished());
    }

    @Test
    public void shouldReturnA500IfTheSecurityTestErrored() throws Exception {
        given(securityTestServiceDummy.getCompletedSecurityTest(any())).willThrow(new SecurityTestService.SecurityTestErroredException());

        ResponseEntity<SecurityTest> response = classUnderTest.getSecurityTest(UUID.randomUUID());

        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    public void shouldReturnA401WhenTheUserIsntPermittedToAccessTheSecurityTest() throws Exception {
        UUID id = UUID.randomUUID();
        willThrow(new InsufficientAuthenticationException("Foobar")).given(authService).checkAuthorizedFor(eq(id.toString()), any(), any());

        ResponseEntity<SecurityTest> response = classUnderTest.getSecurityTest(id);

        assertEquals(401, response.getStatusCodeValue());
    }
}