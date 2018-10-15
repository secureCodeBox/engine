package io.securecodebox.persistence.s3;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class S3PersistenceProviderTest {

    @InjectMocks
    S3PersistenceProvider s3PersistenceProvider;

//    @Mock
//    private ObjectMapper objMapper;

      public static final String DEFAULT_RESULT_STRING = "{}";
//    private static final String DEFAULT_EXECUTION = "{\"id\":\"5a4e9d37-09b0-4109-badd-d79dfa8fce2a\",\"context\":\"TEST_CONTEXT\",\"automated\":false,\"scanners\":[{\"id\":\"62fa8ffb-e3bc-433e-b322-9c02108c5171\",\"type\":\"Test_SCANNER\",\"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\",\"false_positive\":false}],\"rawFindings\":\"[{\\\"pudding\\\":\\\"Bier\\\"}]\"}]}";
//    public static final String DEFAULT_RESULT_STRING = "{\"execution\": " + DEFAULT_EXECUTION + ", \"findings\":[{}]}";
//    public static final String DEFAULT_RESULT_STRING = "{\"execution\": null, \"findings\":[{\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\",\"name\":\"BAD_TEST_FINDIG\",\"description\":\"Some coder has tested this!\",\"category\":\"COOL_TEST_STUFF\",\"osi_layer\":\"NOT_APPLICABLE\",\"severity\":\"HIGH\",\"reference\":{\"id\":\"UNI_CODE_STUFF\",\"source\":\"RISCOOL\"},\"hint\":\"You might wan't to blame Rüdiger!\",\"attributes\":{\"TEST\":\"Kekse\",\"HORRIBLE\":\"Coke\"},\"location\":\"mett.brot.securecodebox.io\",\"false_positive\":false}]}";

    private ObjectMapper objectMapper = new ObjectMapper();


    // Todo: find out how to initialize a example report correctly
//    @Test
//    public void testWriteReportToFile() throws IOException {
//        Mockito.when(objMapper.writeValueAsString(any())).thenReturn(DEFAULT_RESULT_STRING);
//        File file = s3PersistenceProvider.writeReportToFile(objectMapper.readValue(DEFAULT_RESULT_STRING, Report.class));
//        String content = FileUtils.readFileToString(file, "UTF-8");
//        assertTrue(content.contains("TEST_CONTEXT"));
//    }

    @Test
    public void testNullReport() throws IOException {
        File file = s3PersistenceProvider.writeReportToFile(null);
        assertTrue("null".equals(readFile(file.getPath(), Charset.forName("UTF-8"))));
    }


    private static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}