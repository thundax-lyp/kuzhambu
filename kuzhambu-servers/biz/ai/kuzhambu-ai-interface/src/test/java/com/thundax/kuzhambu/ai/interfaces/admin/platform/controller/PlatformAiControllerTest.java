package com.thundax.kuzhambu.ai.interfaces.admin.platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.platform.command.PlatformAiInvokeCommand;
import com.thundax.kuzhambu.ai.application.platform.service.PlatformAiApplicationService;
import com.thundax.kuzhambu.ai.interfaces.admin.platform.controller.request.PlatformAiRequests.InvokeRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.platform.controller.response.PlatformAiResponses.InvokeResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class PlatformAiControllerTest {

    @Test
    void routesShouldExposePlatformAiUsecaseEntries() throws Exception {
        assertEquals(
                "/api/ai/platform",
                PlatformAiController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertPostMapping("buildPromptSuggestion", "prompt-suggestion", "ai:prompt:edit");
        assertPostMapping("summarizeVersion", "version-summary", "ai:prompt:view");
    }

    @Test
    void promptSuggestionShouldDelegateToApplicationService() {
        RecordingPlatformAiApplicationService service = new RecordingPlatformAiApplicationService();
        PlatformAiController controller = new PlatformAiController(service);

        InvokeResponse response = controller.buildPromptSuggestion(request());

        assertEquals("prompt", service.lastMethod);
        assertEquals("req-1", service.lastCommand.getRequestId());
        assertEquals("trace-1", service.lastCommand.getTraceId());
        assertEquals("prompt_suggestion", response.getCapability());
        assertEquals(102L, response.getCandidateId());
    }

    @Test
    void versionSummaryShouldDelegateToApplicationService() {
        RecordingPlatformAiApplicationService service = new RecordingPlatformAiApplicationService();
        PlatformAiController controller = new PlatformAiController(service);

        InvokeResponse response = controller.summarizeVersion(request());

        assertEquals("summary", service.lastMethod);
        assertEquals("version_summary", response.getCapability());
    }

    private static void assertPostMapping(String methodName, String path, String permission) throws Exception {
        Method method = PlatformAiController.class.getDeclaredMethod(methodName, InvokeRequest.class);
        assertEquals(path, method.getAnnotation(PostMapping.class).value()[0]);
        assertEquals(permission, method.getAnnotation(HasPermission.class).value()[0]);
    }

    private static InvokeRequest request() {
        InvokeRequest request = new InvokeRequest();
        request.setContentType("PROMPT_TEMPLATE");
        request.setContentId(10L);
        request.setObjectId(20L);
        request.setModelId(30L);
        request.setModelName("model-a");
        request.setPromptVersionId(40L);
        request.setRequestId("req-1");
        request.setTraceId("trace-1");
        request.setPromptMessagesJson("[{\"role\":\"user\",\"content\":\"hello\"}]");
        request.setInputPayloadJson("{\"template\":\"hello\"}");
        request.setForceJson(Boolean.TRUE);
        request.setCreateCandidate(Boolean.TRUE);
        return request;
    }

    private static class RecordingPlatformAiApplicationService implements PlatformAiApplicationService {

        private String lastMethod;
        private PlatformAiInvokeCommand lastCommand;

        @Override
        public AiInvokeResult buildPromptSuggestion(PlatformAiInvokeCommand command) {
            lastMethod = "prompt";
            lastCommand = command;
            return result("prompt_suggestion", 102L);
        }

        @Override
        public AiInvokeResult summarizeVersion(PlatformAiInvokeCommand command) {
            lastMethod = "summary";
            lastCommand = command;
            return result("version_summary", null);
        }

        private AiInvokeResult result(String capability, Long candidateId) {
            AiInvokeResult result = new AiInvokeResult();
            result.setCallId(101L);
            result.setCandidateId(candidateId);
            result.setRequestId("req-1");
            result.setTraceId("trace-1");
            result.setStatus("SUCCEEDED");
            result.setCapability(capability);
            result.setResultFormat("TEXT");
            result.setResultPayload("ok");
            return result;
        }
    }
}
