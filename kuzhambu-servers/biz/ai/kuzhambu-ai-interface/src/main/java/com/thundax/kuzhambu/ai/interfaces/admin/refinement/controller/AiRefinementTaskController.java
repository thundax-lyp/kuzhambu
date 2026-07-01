package com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller;

import com.thundax.kuzhambu.ai.application.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.batch.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementTaskApplicationService;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.assembler.AiRefinementInterfaceAssembler;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.request.AiRefinementRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.response.AiRefinementResponses;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "AI模块-精修任务", description = "Classics内容AI精修任务")
@SysLogger(module = {"AI", "精修任务"})
@RequestMapping(value = "/api/ai/refinement/task")
@WrappedApiController
public class AiRefinementTaskController {

    private final AiRefinementTaskApplicationService taskApplicationService;
    private final AiBatchJobApplicationService batchJobApplicationService;

    public AiRefinementTaskController(
            AiRefinementTaskApplicationService taskApplicationService,
            AiBatchJobApplicationService batchJobApplicationService) {
        this.taskApplicationService = taskApplicationService;
        this.batchJobApplicationService = batchJobApplicationService;
    }

    @Operation(summary = "创建AI精修任务", description = "ai:refinement:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "任务创建")
    @PostMapping(value = "add")
    public AiRefinementResponses.TaskAcceptedResponse addTask(
            @Valid @RequestBody AiRefinementRequests.RefinementRequest request) {
        return AiRefinementInterfaceAssembler.toTaskAcceptedResponse(
                taskApplicationService.addTask(AiRefinementInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "获取AI精修任务", description = "ai:refinement:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:view")
    @SysLogger(value = "任务详情")
    @PostMapping(value = "get")
    public AiRefinementResponses.TaskDetailResponse getTask(
            @Valid @RequestBody AiRefinementRequests.TaskIdRequest request) {
        return AiRefinementInterfaceAssembler.toTaskDetailResponse(taskApplicationService.getTask(request.getTaskId()));
    }

    @Operation(summary = "分页查询AI精修任务", description = "ai:refinement:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:view")
    @SysLogger(value = "任务分页")
    @PostMapping(value = "page")
    public AiRefinementResponses.TaskPageResponse pageTasks(
            @Valid @RequestBody AiRefinementRequests.TaskPageRequest request) {
        var page = taskApplicationService.pageTasks(
                request.getCapability(),
                request.getStatus(),
                request.getContentType(),
                request.getContentId(),
                request.getRequestedBy(),
                PageInterfaceAssembler.toPageQuery(request));
        return AiRefinementInterfaceAssembler.toTaskPageResponse(
                page.getPageNo(), page.getPageSize(), page.getTotalCount(), page.getRecords());
    }

    @Operation(summary = "取消AI精修任务", description = "ai:refinement:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "任务取消")
    @PostMapping(value = "cancel")
    public AiRefinementResponses.TaskCancelResponse cancelTask(
            @Valid @RequestBody AiRefinementRequests.TaskCancelRequest request) {
        return AiRefinementInterfaceAssembler.toTaskCancelResponse(
                taskApplicationService.cancelTask(request.getTaskId(), request.getRequestedBy()));
    }

    @Operation(summary = "创建AI精修批量任务", description = "ai:refinement:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "批量任务创建")
    @PostMapping(value = "batch/create")
    public AiRefinementResponses.BatchJobResponse createBatch(@RequestBody Map<String, Object> request) {
        AiBatchJobCreateCommand command = new AiBatchJobCreateCommand();
        command.setScope(readString(request, "scope"));
        command.setCapability(readString(request, "capability"));
        command.setContentType(readString(request, "contentType"));
        command.setTotalCount(readInteger(request, "totalCount"));
        command.setFailureSummaryJson(readString(request, "failureSummaryJson"));
        Long batchId = batchJobApplicationService.create(command);
        return toBatchResponse(batchJobApplicationService.get(batchId));
    }

    @Operation(summary = "获取AI精修批量任务", description = "ai:refinement:view")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:view")
    @SysLogger(value = "批量任务详情")
    @PostMapping(value = "batch/get")
    public AiRefinementResponses.BatchJobResponse getBatch(@RequestBody Map<String, Object> request) {
        return toBatchResponse(batchJobApplicationService.get(readLong(request, "batchId")));
    }

    @Operation(summary = "取消AI精修批量任务", description = "ai:refinement:edit")
    @ApiImplicitParams({})
    @HasPermission(value = "ai:refinement:edit")
    @SysLogger(value = "批量任务取消")
    @PostMapping(value = "batch/cancel")
    public AiRefinementResponses.BatchJobResponse cancelBatch(@RequestBody Map<String, Object> request) {
        return toBatchResponse(batchJobApplicationService.cancel(readLong(request, "batchId")));
    }

    private static AiRefinementResponses.BatchJobResponse toBatchResponse(
            com.thundax.kuzhambu.ai.application.batch.result.AiBatchJobResult result) {
        if (result == null) {
            return AiRefinementResponses.BatchJobResponse.builder().build();
        }
        return AiRefinementResponses.BatchJobResponse.builder()
                .batchId(result.getBatchId())
                .scope(result.getScope())
                .capability(result.getCapability())
                .contentType(result.getContentType())
                .status(result.getStatus())
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

    private static Long readLong(Map<String, Object> request, String key) {
        Object value = request == null ? null : request.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.valueOf(text.trim());
        }
        throw new IllegalArgumentException("缺少字段: " + key);
    }

    private static Integer readInteger(Map<String, Object> request, String key) {
        Object value = request == null ? null : request.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.valueOf(text.trim());
        }
        return 0;
    }

    private static String readString(Map<String, Object> request, String key) {
        Object value = request == null ? null : request.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }
}
