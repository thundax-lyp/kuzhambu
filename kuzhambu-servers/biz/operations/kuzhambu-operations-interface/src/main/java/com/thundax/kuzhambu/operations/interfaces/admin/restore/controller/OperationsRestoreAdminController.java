package com.thundax.kuzhambu.operations.interfaces.admin.restore.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.operations.application.restore.service.RestoreApplicationService;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.assembler.OperationsRestoreInterfaceAssembler;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestoreDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestoreExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestorePageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestoreDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestoreExecuteResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestorePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "运营模块-恢复", description = "Operations 恢复接口")
@SysLogger(module = {"运营", "恢复"})
@RequestMapping("/api/operations/restore")
@WrappedApiController
public class OperationsRestoreAdminController {

    private final RestoreApplicationService restoreApplicationService;

    public OperationsRestoreAdminController(RestoreApplicationService restoreApplicationService) {
        this.restoreApplicationService = restoreApplicationService;
    }

    @Operation(summary = "从备份执行恢复", description = "operations:restore:execute")
    @HasPermission("operations:restore:execute")
    @IgnoreSysLogger
    @PostMapping("execute")
    public OperationsRestoreExecuteResponse execute(@Valid @RequestBody OperationsRestoreExecuteRequest request) {
        return OperationsRestoreInterfaceAssembler.toResponse(
                restoreApplicationService.execute(OperationsRestoreInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "分页查询恢复任务", description = "operations:restore:view")
    @HasPermission("operations:restore:view")
    @IgnoreSysLogger
    @PostMapping("page")
    public PageResponse<OperationsRestorePageResponse> page(@Valid @RequestBody OperationsRestorePageRequest request) {
        return PageResponseHelper.fromPageResult(
                restoreApplicationService.page(
                        OperationsRestoreInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                OperationsRestoreInterfaceAssembler::toResponse);
    }

    @Operation(summary = "获取恢复任务详情", description = "operations:restore:view")
    @HasPermission("operations:restore:view")
    @IgnoreSysLogger
    @PostMapping("detail")
    public OperationsRestoreDetailResponse detail(@Valid @RequestBody OperationsRestoreDetailRequest request) {
        return OperationsRestoreInterfaceAssembler.toDetailResponse(
                restoreApplicationService.detail(OperationsRestoreInterfaceAssembler.toQuery(request)));
    }
}
