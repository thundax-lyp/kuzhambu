package com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller;

import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.CancelAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.GetAiBatchJobQuery;
import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.configuration.AiRefinementExecutorConfiguration;
import com.thundax.kuzhambu.ai.application.scenario.service.AiRefinementTaskApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.assembler.AiRefinementInterfaceAssembler;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.request.AiRefinementRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.response.AiRefinementResponses;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.PostJsonApiExempt;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "AI模块-精修任务", description = "Classics内容AI精修任务")
@SysLogger(module = {"AI", "精修任务"})
@RequestMapping(value = "/api/ai/refinement/task")
@WrappedApiController
public class AiRefinementTaskController {

    private final AiRefinementTaskApplicationService taskApplicationService;
    private final AiBatchJobApplicationService batchJobApplicationService;
    private final Executor streamExecutor;

    public AiRefinementTaskController(
            AiRefinementTaskApplicationService taskApplicationService,
            AiBatchJobApplicationService batchJobApplicationService,
            @Qualifier(AiRefinementExecutorConfiguration.STREAM_EXECUTOR) Executor streamExecutor) {
        this.taskApplicationService = taskApplicationService;
        this.batchJobApplicationService = batchJobApplicationService;
        this.streamExecutor = streamExecutor;
    }

    @Operation(summary = "创建AI精修任务", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "任务创建")
    @PostMapping(value = "add")
    public AiRefinementResponses.TaskAcceptedResponse addTask(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return AiRefinementInterfaceAssembler.toTaskAcceptedResponse(
                taskApplicationService.submit(AiRefinementInterfaceAssembler.toSubmitTaskCommand(request)));
    }

    @Operation(summary = "获取AI精修任务", description = "ai:refinement:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:view")
    @SysLogger(value = "任务详情")
    @PostMapping(value = "get")
    public AiRefinementResponses.TaskDetailResponse getTask(
            @Valid @RequestBody AiRefinementRequests.TaskIdRequest request) {
        return AiRefinementInterfaceAssembler.toTaskDetailResponse(
                taskApplicationService.get(AiRefinementInterfaceAssembler.toGetTaskQuery(request.getTaskId())));
    }

    @Operation(summary = "订阅AI精修任务流式过程", description = "ai:refinement:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:view")
    @SysLogger(value = "任务流式过程")
    @PostJsonApiExempt(reason = "SSE 事件流必须使用浏览器可直接建立的 GET 流式连接")
    @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTask(@RequestParam(value = "taskId") Long taskId) {
        SseEmitter emitter = new SseEmitter(600_000L);
        Runnable subscription = () -> {
            try {
                taskApplicationService.subscribeEvents(
                        AiRefinementInterfaceAssembler.toSubscribeTaskEventsQuery(taskId),
                        event -> sendEvent(emitter, event));
                emitter.complete();
            } catch (RuntimeException exception) {
                emitter.completeWithError(exception);
            }
        };
        CompletableFuture.runAsync(subscription, streamExecutor);
        return emitter;
    }

    @Operation(summary = "分页查询AI精修任务", description = "ai:refinement:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:view")
    @SysLogger(value = "任务分页")
    @PostMapping(value = "page")
    public AiRefinementResponses.TaskPageResponse pageTasks(
            @Valid @RequestBody AiRefinementRequests.TaskPageRequest request) {
        var page = taskApplicationService.page(AiRefinementInterfaceAssembler.toPageTasksQuery(request));
        return AiRefinementInterfaceAssembler.toTaskPageResponse(
                page.getPageNo(), page.getPageSize(), page.getTotalCount(), page.getRecords());
    }

    @Operation(summary = "取消AI精修任务", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "任务取消")
    @PostMapping(value = "cancel")
    public AiRefinementResponses.TaskCancelResponse cancelTask(
            @Valid @RequestBody AiRefinementRequests.TaskCancelRequest request) {
        return AiRefinementInterfaceAssembler.toTaskCancelResponse(
                taskApplicationService.cancel(AiRefinementInterfaceAssembler.toCancelTaskCommand(request.getTaskId())));
    }

    @Operation(summary = "创建AI精修批量任务", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "批量任务创建")
    @PostMapping(value = "batch/create")
    public AiRefinementResponses.BatchJobResponse createBatch(
            @Valid @RequestBody AiRefinementRequests.BatchCreateRequest request) {
        AiBatchJobCreateCommand command = new AiBatchJobCreateCommand();
        command.setScope(request.getScope());
        command.setCapability(AiBusinessCapability.from(request.getCapability()));
        command.setContentRef(AiContentRef.ofNullable(request.getContentType(), null));
        command.setTotalCount(request.getTotalCount());
        command.setFailureSummaryJson(request.getFailureSummaryJson());
        var batchId = batchJobApplicationService.create(command);
        return toBatchResponse(batchJobApplicationService.get(new GetAiBatchJobQuery(batchId)));
    }

    @Operation(summary = "获取AI精修批量任务", description = "ai:refinement:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:view")
    @SysLogger(value = "批量任务详情")
    @PostMapping(value = "batch/get")
    public AiRefinementResponses.BatchJobResponse getBatch(
            @Valid @RequestBody AiRefinementRequests.BatchIdRequest request) {
        return toBatchResponse(batchJobApplicationService.get(
                new GetAiBatchJobQuery(AiBatchJobIdCodec.toDomain(request.getBatchId()))));
    }

    @Operation(summary = "取消AI精修批量任务", description = "ai:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "批量任务取消")
    @PostMapping(value = "batch/cancel")
    public AiRefinementResponses.BatchJobResponse cancelBatch(
            @Valid @RequestBody AiRefinementRequests.BatchIdRequest request) {
        return toBatchResponse(batchJobApplicationService.cancel(
                new CancelAiBatchJobCommand(AiBatchJobIdCodec.toDomain(request.getBatchId()))));
    }

    private static AiRefinementResponses.BatchJobResponse toBatchResponse(
            com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult result) {
        if (result == null) {
            return AiRefinementResponses.BatchJobResponse.builder().build();
        }
        return AiRefinementResponses.BatchJobResponse.builder()
                .batchId(AiBatchJobIdCodec.toValue(result.getBatchId()))
                .scope(result.getScope())
                .capability(
                        result.getCapability() == null
                                ? null
                                : result.getCapability().value())
                .contentType(AiContentRefCodec.toContentType(result.getContentRef()))
                .contentId(AiContentRefCodec.toContentId(result.getContentRef()))
                .status(result.getStatus() == null ? null : result.getStatus().name())
                .totalCount(result.getTotalCount())
                .successCount(result.getSuccessCount())
                .failedCount(result.getFailedCount())
                .cancelledCount(result.getCancelledCount())
                .failureSummaryJson(result.getFailureSummaryJson())
                .requestedAt(result.getRequestedAt())
                .cancelledAt(result.getCancelledAt())
                .completedAt(result.getCompletedAt())
                .build();
    }

    private static void sendEvent(
            SseEmitter emitter, com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult event) {
        try {
            emitter.send(SseEmitter.event()
                    .id(event.getEventId())
                    .name(event.getEventType())
                    .data(event));
        } catch (IOException exception) {
            throw new IllegalStateException("AI refinement task stream send failed", exception);
        }
    }
}
