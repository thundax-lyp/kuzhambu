package com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.command.CancelAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.command.SubmitAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.query.GetAiRefinementTaskQuery;
import com.thundax.kuzhambu.ai.application.scenario.query.PageAiRefinementTasksQuery;
import com.thundax.kuzhambu.ai.application.scenario.query.SubscribeAiRefinementTaskEventsQuery;
import com.thundax.kuzhambu.ai.application.scenario.result.AiRefinementTaskResult;
import com.thundax.kuzhambu.ai.application.scenario.service.AiRefinementTaskApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.request.AiRefinementRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.response.AiRefinementResponses;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class AiRefinementTaskControllerTest {

    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    @Test
    void routesShouldKeepTaskApiPathsAndPermissions() throws Exception {
        assertRequestMapping(AiRefinementTaskController.class, "/api/ai/refinement/task");
        assertPostMapping(
                AiRefinementTaskController.class,
                "addTask",
                "add",
                "ai:refinement:edit",
                AiRefinementRequests.RefinementRequest.class);
        assertPostMapping(
                AiRefinementTaskController.class,
                "getTask",
                "get",
                "ai:refinement:view",
                AiRefinementRequests.TaskIdRequest.class);
        assertGetMapping(AiRefinementTaskController.class, "streamTask", "stream", "ai:refinement:view", Long.class);
        assertPostMapping(
                AiRefinementTaskController.class,
                "pageTasks",
                "page",
                "ai:refinement:view",
                AiRefinementRequests.TaskPageRequest.class);
        assertPostMapping(
                AiRefinementTaskController.class,
                "cancelTask",
                "cancel",
                "ai:refinement:edit",
                AiRefinementRequests.TaskCancelRequest.class);
        assertPostMapping(
                AiRefinementTaskController.class,
                "createBatch",
                "batch/create",
                "ai:refinement:edit",
                AiRefinementRequests.BatchCreateRequest.class);
        assertPostMapping(
                AiRefinementTaskController.class,
                "getBatch",
                "batch/get",
                "ai:refinement:view",
                AiRefinementRequests.BatchIdRequest.class);
        assertPostMapping(
                AiRefinementTaskController.class,
                "cancelBatch",
                "batch/cancel",
                "ai:refinement:edit",
                AiRefinementRequests.BatchIdRequest.class);
    }

    @Test
    void controllerShouldMapTaskLifecycleResponses() {
        AiRefinementTaskController controller = new AiRefinementTaskController(
                new FakeTaskApplicationService(), new NoOpBatchJobService(), DIRECT_EXECUTOR);

        AiRefinementRequests.RefinementRequest addRequest = new AiRefinementRequests.RefinementRequest();
        addRequest.setCapability("summary");
        addRequest.setScope("classics");
        addRequest.setContentType("SANCAI_ENTRY");
        addRequest.setContentId(101L);
        addRequest.setModelId(201L);
        addRequest.setModelName("gpt-test");
        addRequest.setPromptVersionId(301L);
        addRequest.setRequestId("req-1");
        addRequest.setTraceId("trace-1");
        addRequest.setPromptMessagesJson("[{\"role\":\"user\",\"content\":\"hi\"}]");
        addRequest.setInputPayloadJson("{\"text\":\"hi\"}");

        AiRefinementResponses.TaskAcceptedResponse accepted = controller.addTask(addRequest);

        AiRefinementRequests.TaskIdRequest getRequest = new AiRefinementRequests.TaskIdRequest();
        getRequest.setTaskId(7001L);
        AiRefinementResponses.TaskDetailResponse detail = controller.getTask(getRequest);

        AiRefinementRequests.TaskPageRequest pageRequest = new AiRefinementRequests.TaskPageRequest();
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(10);
        pageRequest.setContentType("SANCAI_ENTRY");
        AiRefinementResponses.TaskPageResponse page = controller.pageTasks(pageRequest);

        AiRefinementRequests.TaskCancelRequest cancelRequest = new AiRefinementRequests.TaskCancelRequest();
        cancelRequest.setTaskId(7001L);
        AiRefinementResponses.TaskCancelResponse cancelled = controller.cancelTask(cancelRequest);

        assertEquals(7001L, accepted.getTaskId());
        assertEquals("RUNNING", accepted.getStatus());
        assertNotNull(controller.streamTask(7001L));
        assertEquals(9001L, detail.getCallId());
        assertEquals(9002L, detail.getCandidateId());
        assertEquals(true, detail.getStreamEnabled());
        assertEquals(1, page.getItems().size());
        assertEquals(true, page.getItems().get(0).getStreamEnabled());
        assertEquals(1L, page.getTotal());
        assertEquals("CANCELLED", cancelled.getStatus());
    }

    private static void assertRequestMapping(Class<?> controllerType, String expectedPath) {
        RequestMapping mapping = controllerType.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertPostMapping(
            Class<?> controllerType,
            String methodName,
            String expectedPath,
            String permissionValue,
            Class<?> parameterType)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterType);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(permissionValue, permission.value()[0]);
    }

    private static void assertGetMapping(
            Class<?> controllerType,
            String methodName,
            String expectedPath,
            String permissionValue,
            Class<?> parameterType)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterType);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        assertEquals(MediaType.TEXT_EVENT_STREAM_VALUE, mapping.produces()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(permissionValue, permission.value()[0]);
    }

    private static final class NoOpBatchJobService implements AiBatchJobApplicationService {

        @Override
        public AiBatchJobResult get(AiBatchJobId batchId) {
            return null;
        }

        @Override
        public PageResult<AiBatchJobResult> page(
                String scope,
                AiBusinessCapability capability,
                AiBatchJobStatus status,
                AiContentRef contentRef,
                PageQuery pageQuery) {
            return PageResult.of(1, 10, 0, List.of());
        }

        @Override
        public PageResult<AiBatchJobResult> pageByCapabilities(
                String scope,
                List<AiBusinessCapability> capabilities,
                AiBatchJobStatus status,
                AiContentRef contentRef,
                PageQuery pageQuery) {
            return PageResult.of(1, 10, 0, List.of());
        }

        @Override
        public AiBatchJobId create(AiBatchJobCreateCommand command) {
            return null;
        }

        @Override
        public boolean canDispatchNextUnit(AiBatchJobId batchId) {
            return false;
        }

        @Override
        public AiBatchJobResult recordSuccess(AiBatchJobId batchId) {
            return null;
        }

        @Override
        public AiBatchJobResult recordSuccessIfRunning(AiBatchJobId batchId) {
            return null;
        }

        @Override
        public AiBatchJobResult recordFailure(AiBatchJobId batchId, String failureSummaryJson) {
            return null;
        }

        @Override
        public AiBatchJobResult recordFailureIfRunning(AiBatchJobId batchId, String failureSummaryJson) {
            return null;
        }

        @Override
        public AiBatchJobResult recordPartialIfRunning(AiBatchJobId batchId, String failureSummaryJson) {
            return null;
        }

        @Override
        public int expireRunning(
                String scope,
                List<AiBusinessCapability> capabilities,
                Instant requestedBefore,
                String failureSummaryJson,
                int limit) {
            return 0;
        }

        @Override
        public AiBatchJobResult cancel(AiBatchJobId batchId) {
            return null;
        }
    }

    private static final class FakeTaskApplicationService implements AiRefinementTaskApplicationService {

        @Override
        public AiRefinementTaskResult submitRefinementTask(SubmitAiRefinementTaskCommand command) {
            return task(
                    "RUNNING",
                    command.getCapability() == null
                            ? null
                            : command.getCapability().value(),
                    null,
                    null);
        }

        @Override
        public AiRefinementTaskResult getRefinementTask(GetAiRefinementTaskQuery query) {
            return task("SUCCEEDED", "summary", 9001L, 9002L);
        }

        @Override
        public PageResult<AiRefinementTaskResult> pageRefinementTasks(PageAiRefinementTasksQuery query) {
            return PageResult.of(
                    1, 10, 1, List.of(getRefinementTask(new GetAiRefinementTaskQuery(new AiBatchJobId(7001L)))));
        }

        @Override
        public void subscribeRefinementTaskEvents(
                SubscribeAiRefinementTaskEventsQuery query, Consumer<AiStreamEventResult> eventConsumer) {
            AiStreamEventResult event = new AiStreamEventResult();
            event.setEventType("completed");
            event.setEventId("evt-1");
            event.setRequestId(new com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId("req-1"));
            event.setTraceId(new com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId("trace-1"));
            event.setStatus(com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus.SUCCEEDED);
            eventConsumer.accept(event);
        }

        @Override
        public AiRefinementTaskResult cancelRefinementTask(CancelAiRefinementTaskCommand command) {
            return task("CANCELLED", "summary", null, null);
        }

        private AiRefinementTaskResult task(String status, String capability, Long callId, Long candidateId) {
            return new AiRefinementTaskResult(
                    new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId(7001L),
                    "classics",
                    com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.fromAlias(capability),
                    com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef.ofNullable(
                            "SANCAI_ENTRY", 101L),
                    null,
                    new com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId("req-1"),
                    new com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId("trace-1"),
                    com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus.valueOf(status),
                    null,
                    new com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId(201L),
                    com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName.of("gpt-test"),
                    new com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId(301L),
                    callId == null
                            ? null
                            : new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId(callId),
                    candidateId == null
                            ? null
                            : new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId(
                                    candidateId),
                    "TEXT",
                    "摘要",
                    null,
                    null,
                    null,
                    true,
                    Instant.parse("2026-01-01T00:00:00Z"),
                    Instant.parse("2026-01-01T00:01:00Z"),
                    Instant.parse("2026-01-01T00:02:00Z"),
                    "CANCELLED".equals(status) ? Instant.parse("2026-01-01T00:03:00Z") : null);
        }
    }
}
