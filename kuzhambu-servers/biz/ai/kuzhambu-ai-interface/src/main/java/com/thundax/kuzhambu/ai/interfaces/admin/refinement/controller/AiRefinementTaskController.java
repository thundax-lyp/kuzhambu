package com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "AI模块-精修任务", description = "Classics内容AI精修任务")
@SysLogger(module = {"AI", "精修任务"})
@RequestMapping(value = "/api/ai/refinement/task")
@WrappedApiController
public class AiRefinementTaskController {

    private final AiRefinementTaskApplicationService taskApplicationService;

    public AiRefinementTaskController(AiRefinementTaskApplicationService taskApplicationService) {
        this.taskApplicationService = taskApplicationService;
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
}
