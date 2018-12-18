package io.securecodebox.persistence.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.Severity;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


import static junit.framework.TestCase.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class FindingWriterTest {

    @InjectMocks
    private FindingWriter findingWriter;

    @Spy
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testWriteFindingToFile() throws IOException {
        // given
        Finding finding = new Finding();
        finding.setName("BAD_TEST_FINDIG");
        finding.setFalsePositive(false);
        finding.setId(UUID.fromString("49bf7fd3-8512-4d73-a28f-608e493cd726"));
        finding.addAttribute("my-attribute","abc");
        finding.setDescription("BAD_TEST_FINDIG_DESC");
        finding.setSeverity(Severity.HIGH);

        // when
        File file = findingWriter.writeFindingToFile(finding, "my-context");

        // then
        String content = FileUtils.readFileToString(file, "UTF-8");
        assertEquals(
                "{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\"," +
                        "\"name\":\"BAD_TEST_FINDIG\"," +
                        "\"description\":\"BAD_TEST_FINDIG_DESC\"," +
                        "\"severity\":\"HIGH\"," +
                        "\"attributes\":{\"my-attribute\":\"abc\"}," +
                        "\"false_positive\":false," +
                        "\"context\":\"my-context\"}",
                content
        );
    }
}
