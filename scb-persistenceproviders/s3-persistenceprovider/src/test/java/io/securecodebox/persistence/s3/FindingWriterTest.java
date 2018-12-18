package io.securecodebox.persistence.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.Severity;
import io.securecodebox.model.securitytest.SecurityTest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

        Map<String, String> metaData = new HashMap<>();
        metaData.put("started-by", "PersistenceProviderTest");

        Target target = new Target();
        target.setLocation("localhost");
        target.setName("Target name");

        SecurityTest securityTest = new SecurityTest(UUID.fromString("281ccc0f-a933-4106-a3d3-209954e6305e"), "testContext","nmap", target, null, metaData);

        // when
        File file = findingWriter.writeFindingToFile(finding, securityTest);

        // then
        String content = FileUtils.readFileToString(file, "UTF-8");
        assertEquals(
                "{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\"," +
                        "\"name\":\"BAD_TEST_FINDIG\"," +
                        "\"description\":\"BAD_TEST_FINDIG_DESC\"," +
                        "\"severity\":\"HIGH\"," +
                        "\"attributes\":{\"my-attribute\":\"abc\"}," +
                        "\"false_positive\":false," +
                        "\"context\":\"testContext\"," +
                        "\"security_test_name\":\"nmap\"," +
                        "\"security_test_id\":\"281ccc0f-a933-4106-a3d3-209954e6305e\"," +
                        "\"target_name\":\"Target name\"," +
                        "\"target_location\":\"localhost\"," +
                        "\"security_test_metaData\":{\"started-by\":\"PersistenceProviderTest\"}}",
                content
        );
    }
}
