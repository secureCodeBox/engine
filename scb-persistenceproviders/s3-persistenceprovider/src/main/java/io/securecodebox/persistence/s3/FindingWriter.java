package io.securecodebox.persistence.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.findings.Finding;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class FindingWriter {

    private static final Logger LOG = LoggerFactory.getLogger(FindingWriter.class);

    @Autowired
    private ObjectMapper mapper;

    File writeFindingToFile(Finding finding) throws IOException {
        LOG.debug("Write finding " + finding.getId() + " to tempFile");
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".json");
        mapper.writeValue(tempFile, finding);
        return tempFile;
    }
}
