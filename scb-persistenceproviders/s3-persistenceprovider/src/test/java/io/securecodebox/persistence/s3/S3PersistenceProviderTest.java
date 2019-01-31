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

package io.securecodebox.persistence.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.rest.Report;
import io.securecodebox.model.securitytest.SecurityTest;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest(AmazonS3ClientBuilder.class)
public class S3PersistenceProviderTest {

    @InjectMocks
    private S3PersistenceProvider persistenceProvider;

    @Mock
    private FindingWriter findingWriter;

    @Mock
    private AmazonS3Client s3ClientDummy;

    @Test
    public void persistShouldWriteOneFilePerFinding() throws IOException {
        // given
        PowerMockito.mockStatic(AmazonS3ClientBuilder.class);
        given(AmazonS3ClientBuilder.defaultClient()).willReturn(s3ClientDummy);

        SecurityTest securityTest = new SecurityTest();
        securityTest.setContext("test-context");
        Report report = new Report();
        securityTest.setReport(report);
        Finding f1 = new Finding();
        Finding f2 = new Finding();
        report.setFindings(Arrays.asList(f1, f2));

        // when
        persistenceProvider.persist(securityTest);

        // then
        verify(findingWriter, times(2)).writeFindingToFile(any(), any());
        verify(s3ClientDummy, times(2)).putObject(any());
    }
}
