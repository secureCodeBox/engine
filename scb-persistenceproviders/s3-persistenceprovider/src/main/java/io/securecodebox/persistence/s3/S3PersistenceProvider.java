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

import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.Report;
import io.securecodebox.persistence.PersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@ConditionalOnProperty(name = "securecodebox.persistence.provider", havingValue = "s3")
@Component
public class S3PersistenceProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(S3PersistenceProvider.class);

    @Autowired
    private ObjectMapper mapper;

    @Value("${securecodebox.persistence.s3.bucket}")
    private String bucketName;

    @Value("${securecodebox.persistence.s3.region}")
    private String awsRegion;

    @Override
    public void persist(Report report) {

        if (report == null) {
            LOG.warn("Report is null, nothing to persist.");
        } else {
            // Upload a file as a new object with ContentType and title specified.
            LOG.warn(System.getenv("AWS_CONTAINER_CREDENTIALS_RELATIVE_URI"));
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(awsRegion)
                    .withCredentials(new EC2ContainerCredentialsProviderWrapper())
                    .build();
            File file = writeReportToFile(report);

            String fileName = report.getExecution().getContext().replace('/', '-') + '/';
            fileName += UUID.randomUUID();
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/json");
            request.setMetadata(metadata);
            s3Client.putObject(request);
        }
    }

    File writeReportToFile(Report report) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(UUID.randomUUID().toString(), ".json");
            mapper.writeValue(tempFile, report);
        } catch (IOException exception) {
            LOG.error("Could not write tempfile: ", exception);
        }
        return tempFile;
    }
}
