package com.thundax.kuzhambu.operations.interfaces.admin.task.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.operations.application.task.service.TaskApplicationService;
import com.thundax.kuzhambu.operations.interfaces.admin.task.assembler.OperationsTaskInterfaceAssembler;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.request.OperationsTaskDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.request.OperationsTaskPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.response.OperationsTaskDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.response.OperationsTaskPageResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "运营模块-长任务", description = "Operations 长任务接口")
@SysLogger(module = {"运营", "长任务"})
@RequestMapping("/api/operations/task")
@WrappedApiController
public class OperationsTaskAdminController {

    private final TaskApplicationService taskApplicationService;

    public OperationsTaskAdminController(TaskApplicationService taskApplicationService) {
        this.taskApplicationService = taskApplicationService;
    }

    @Operation(summary = "分页查询长任务", description = "operations:task:view")
    @HasPermission("operations:task:view")
    @IgnoreSysLogger
    @PostMapping("page")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public PageResponse<OperationsTaskPageResponse> page(@Valid @RequestBody OperationsTaskPageRequest request) {
        return PageResponseHelper.fromPageResult(
                taskApplicationService.page(
                        OperationsTaskInterfaceAssembler.toQuery(request), PageInterfaceAssembler.toPageQuery(request)),
                OperationsTaskInterfaceAssembler::toResponse);
    }

    @Operation(summary = "获取长任务详情", description = "operations:task:view")
    @HasPermission("operations:task:view")
    @IgnoreSysLogger
    @PostMapping("detail")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public OperationsTaskDetailResponse detail(@Valid @RequestBody OperationsTaskDetailRequest request) {
        return OperationsTaskInterfaceAssembler.toDetailResponse(
                taskApplicationService.detail(OperationsTaskInterfaceAssembler.toQuery(request)));
    }
}
