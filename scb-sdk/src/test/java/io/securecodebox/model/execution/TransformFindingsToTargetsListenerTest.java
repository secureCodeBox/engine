package io.securecodebox.model.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.constants.DefaultFields;
import io.securecodebox.scanprocess.listener.TransformFindingsToTargetsListener;
import junit.framework.AssertionFailedError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class TransformFindingsToTargetsListenerTest {

    private static final String input = "[\n" +
            "  {\n" +
            "    \"location\": \"http://bodgeit:8080/bodgeit\",\n" +
            "    \"attributes\": {\n" +
            "      \"statusCode\": \"302\",\n" +
            "      \"statusReason\": \"Found\",\n" +
            "      \"method\": \"GET\",\n" +
            "      \"messageId\": \"2\",\n" +
            "      \"requestDateTime\": \"2018-04-17T13:22:01.386+00:00\",\n" +
            "      \"responseTime\": 50,\n" +
            "      \"postData\": \"\",\n" +
            "      \"headers\": \"User-Agent=Mozilla%2F5.0+%28Windows+NT+6.3%3B+WOW64%3B+rv%3A39.0%29+Gecko%2F20100101+Firefox%2F39.0&Pragma=no-cache&Cache-Control=no-cache&Content-Length=0&Host=bodgeit%3A8080\",\n" +
            "      \"queryString\": \"\",\n" +
            "      \"cookies\": \"\"\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"location\": \"http://bodgeit:8080/bodgeit/\",\n" +
            "    \"attributes\": {\n" +
            "      \"statusCode\": \"200\",\n" +
            "      \"statusReason\": \"OK\",\n" +
            "      \"method\": \"GET\",\n" +
            "      \"messageId\": \"5\",\n" +
            "      \"requestDateTime\": \"2018-04-17T13:22:01.469+00:00\",\n" +
            "      \"responseTime\": 258,\n" +
            "      \"postData\": \"\",\n" +
            "      \"headers\": \"User-Agent=Mozilla%2F5.0+%28Windows+NT+6.3%3B+WOW64%3B+rv%3A39.0%29+Gecko%2F20100101+Firefox%2F39.0&Pragma=no-cache&Cache-Control=no-cache&Content-Length=0&Referer=http%3A%2F%2Fbodgeit%3A8080%2Fbodgeit&Host=bodgeit%3A8080\",\n" +
            "      \"queryString\": \"\",\n" +
            "      \"cookies\": \"\"\n" +
            "    }\n" +
            "  }\n" +
            "]";

    @Mock
    private DelegateExecution delegateExecution;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = AssertionFailedError.class)
    public void testWrongInputShouldNotSucceed(){
        String input = "[\n" +
                "  {\n" +
                "    \"location\": \"http://bodgeit:8080/bodgeit/\",\n" +
                "    \"attributes\": {}\n" +
                "  }\n" +
                "]";

        testTransformationOfTargetToFindings(input, generateExpectedResult());
    }

    @Test
    public void testWithCorrectInputShouldSucceed(){
        testTransformationOfTargetToFindings(input, generateExpectedResult());
    }

    public void testTransformationOfTargetToFindings(String input, List<Target> expectedResult){

        try {

            doAnswer(invocationOnMock -> {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Target> targets = objectMapper.readValue(
                        (String)invocationOnMock.getArgumentAt(1, ObjectValue.class).getValue(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));
                checkTargets(targets, expectedResult);
                return null;
            }).when(delegateExecution).setVariable(eq(DefaultFields.PROCESS_TARGETS.name()), Mockito.any());

            when(delegateExecution.getVariable(eq(DefaultFields.PROCESS_FINDINGS.name()))).thenReturn(input);

            TransformFindingsToTargetsListener delegate = new TransformFindingsToTargetsListener();
            delegate.notify(delegateExecution);

        }
        catch (Exception e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testApplyingOfAttributeMapping(){

        String attributeMapping = "[{\"from\":\"statusCode\",\"to\":\"CODE\"}]";

        when(delegateExecution.getVariable(eq(DefaultFields.PROCESS_ATTRIBUTE_MAPPING.name()))).thenReturn(attributeMapping);

        final List<Target> expectedResult = generateExpectedResult();
        for(Target t : expectedResult) {
            Object value = t.getAttributes().get("statusCode");
            t.getAttributes().remove("statusCode");
            t.getAttributes().put("CODE", value);
        }

        testTransformationOfTargetToFindings(input, expectedResult);
    }

    private static List<Target> generateExpectedResult(){
        final List<Target> expectedResult = new LinkedList<>();
        Target target1 = new Target();
        target1.setLocation("http://bodgeit:8080/bodgeit");
        target1.appendOrUpdateAttribute("statusCode", "302");
        target1.appendOrUpdateAttribute("statusReason", "Found");
        target1.appendOrUpdateAttribute("method", "GET");
        target1.appendOrUpdateAttribute("messageId", "2");
        target1.appendOrUpdateAttribute("requestDateTime", "2018-04-17T13:22:01.386+00:00");
        target1.appendOrUpdateAttribute("responseTime", 50);
        target1.appendOrUpdateAttribute("postData", "");
        target1.appendOrUpdateAttribute("headers", "User-Agent=Mozilla%2F5.0+%28Windows+NT+6.3%3B+WOW64%3B+rv%3A39.0%29+Gecko%2F20100101+Firefox%2F39.0&Pragma=no-cache&Cache-Control=no-cache&Content-Length=0&Host=bodgeit%3A8080");
        target1.appendOrUpdateAttribute("queryString", "");
        target1.appendOrUpdateAttribute("cookies", "");

        Target target2 = new Target();
        target2.setLocation("http://bodgeit:8080/bodgeit/");
        target2.appendOrUpdateAttribute("statusCode", "200");
        target2.appendOrUpdateAttribute("statusReason", "OK");
        target2.appendOrUpdateAttribute("method", "GET");
        target2.appendOrUpdateAttribute("messageId", "5");
        target2.appendOrUpdateAttribute("requestDateTime", "2018-04-17T13:22:01.469+00:00");
        target2.appendOrUpdateAttribute("responseTime", 258);
        target2.appendOrUpdateAttribute("postData", "");
        target2.appendOrUpdateAttribute("headers", "User-Agent=Mozilla%2F5.0+%28Windows+NT+6.3%3B+WOW64%3B+rv%3A39.0%29+Gecko%2F20100101+Firefox%2F39.0&Pragma=no-cache&Cache-Control=no-cache&Content-Length=0&Referer=http%3A%2F%2Fbodgeit%3A8080%2Fbodgeit&Host=bodgeit%3A8080");
        target2.appendOrUpdateAttribute("queryString", "");
        target2.appendOrUpdateAttribute("cookies", "");

        expectedResult.add(target1);
        expectedResult.add(target2);
        return expectedResult;
    }

    private void checkTargets(List<Target> targets, List<Target> expectedResult) {

        for(Target target : targets){
            assertTrue(contains(expectedResult, target));
        }
    }

    private static boolean contains(List<Target> targets, Target t){

        for(Target target : targets){

            boolean attributesEqual = true;

            for(String s : target.getAttributes().keySet()){
                if(!target.getAttributes().get(s).equals(t.getAttributes().get(s))){
                    attributesEqual = false;
                    break;
                }
            }
            if(target.getLocation().equals(t.getLocation()) && attributesEqual){
                return true;
            }
        }
        return false;
    }
}
