package com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.ai.application.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.batch.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementTaskApplicationService;
import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.request.AiRefinementRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.response.AiRefinementResponses;
import com.thundax.kuzhambu.common.core.page.PageQuery;
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
        addRequest.setRequestedBy(88L);
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
        cancelRequest.setRequestedBy(88L);
        AiRefinementResponses.TaskCancelResponse cancelled = controller.cancelTask(cancelRequest);

        assertEquals(7001L, accepted.getTaskId());
        assertEquals("PENDING", accepted.getStatus());
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
        public AiBatchJobResult get(Long batchId) {
            return null;
        }

        @Override
        public Long create(AiBatchJobCreateCommand command) {
            return null;
        }

        @Override
        public boolean canDispatchNextUnit(Long batchId) {
            return false;
        }

        @Override
        public AiBatchJobResult recordSuccess(Long batchId) {
            return null;
        }

        @Override
        public AiBatchJobResult recordFailure(Long batchId, String failureSummaryJson) {
            return null;
        }

        @Override
        public AiBatchJobResult cancel(Long batchId) {
            return null;
        }
    }

    private static final class FakeTaskApplicationService implements AiRefinementTaskApplicationService {

        @Override
        public AiRefinementTask addTask(AiRefinementRequestCommand command) {
            AiRefinementTask task = baseTask();
            task.setStatus("PENDING");
            task.setCapability(command.getCapability());
            task.setRequestedBy(command.getRequestedBy());
            return task;
        }

        @Override
        public AiRefinementTask getTask(Long taskId) {
            AiRefinementTask task = baseTask();
            task.setTaskId(taskId);
            task.setStatus("SUCCEEDED");
            task.setCallId(9001L);
            task.setCandidateId(9002L);
            task.setResultFormat("TEXT");
            task.setResultPreview("摘要");
            task.setCompletedAt(Instant.parse("2026-01-01T00:02:00Z"));
            return task;
        }

        @Override
        public PageResult<AiRefinementTask> pageTasks(
                String capability,
                String status,
                String contentType,
                Long contentId,
                Long requestedBy,
                PageQuery pageQuery) {
            return PageResult.of(1, 10, 1, List.of(getTask(7001L)));
        }

        @Override
        public void streamTaskEvents(Long taskId, Consumer<AiStreamEventResult> eventConsumer) {
            AiStreamEventResult event = new AiStreamEventResult();
            event.setEventType("completed");
            event.setEventId("evt-1");
            event.setRequestId("req-1");
            event.setTraceId("trace-1");
            event.setStatus("SUCCEEDED");
            eventConsumer.accept(event);
        }

        @Override
        public AiRefinementTask cancelTask(Long taskId, Long requestedBy) {
            AiRefinementTask task = baseTask();
            task.setTaskId(taskId);
            task.setRequestedBy(requestedBy);
            task.setStatus("CANCELLED");
            task.setCancelledAt(Instant.parse("2026-01-01T00:03:00Z"));
            return task;
        }

        private AiRefinementTask baseTask() {
            AiRefinementTask task = new AiRefinementTask();
            task.setTaskId(7001L);
            task.setScope("classics");
            task.setCapability("summary");
            task.setContentType("SANCAI_ENTRY");
            task.setContentId(101L);
            task.setRequestedAt(Instant.parse("2026-01-01T00:00:00Z"));
            task.setStartedAt(Instant.parse("2026-01-01T00:01:00Z"));
            task.setRequestId("req-1");
            task.setTraceId("trace-1");
            task.setModelId(201L);
            task.setModelName("gpt-test");
            task.setPromptVersionId(301L);
            task.setStreamEnabled(true);
            return task;
        }
    }
}
