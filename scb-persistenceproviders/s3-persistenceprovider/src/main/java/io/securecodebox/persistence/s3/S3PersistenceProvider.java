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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.securitytest.SecurityTest;
import io.securecodebox.persistence.PersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@ConditionalOnProperty(name = "securecodebox.persistence.s3.enabled", havingValue = "true")
@Component
public class S3PersistenceProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(S3PersistenceProvider.class);

    @Value("${securecodebox.persistence.s3.bucket}")
    private String bucketName;

    @Value("${securecodebox.persistence.s3.region}")
    private String awsRegion;

    @Autowired
    FindingWriter findingWriter;

    @Override
    public void persist(SecurityTest securityTest) {
        if (securityTest == null) {
            LOG.warn("Report is null, nothing to persist.");
            return;
        }

        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        String fileName = securityTest.getContext().replace('/', '-') + '/' + securityTest.getId();

        for (Finding finding: securityTest.getReport().getFindings()) {
            writeFindingFileToS3Bucket(s3Client, fileName, finding, securityTest);
        }
    }

    private void writeFindingFileToS3Bucket(AmazonS3 s3Client, String fileName, Finding finding, SecurityTest securityTest) {
        try {
            File findingFile = findingWriter.writeFindingToFile(finding, securityTest);
            writeFileToS3Bucket(s3Client, findingFile, fileName + "-finding-" + finding.getId());
            if( findingFile != null) {
                findingFile.delete();
            }
        } catch (IOException exception) {
            LOG.error("Could not write tempfile for finding: ", exception);
        }
    }

    private void writeFileToS3Bucket(AmazonS3 s3Client, File file, String fileName) {
        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/json");
        request.setMetadata(metadata);
        s3Client.putObject(request);
    }

}
