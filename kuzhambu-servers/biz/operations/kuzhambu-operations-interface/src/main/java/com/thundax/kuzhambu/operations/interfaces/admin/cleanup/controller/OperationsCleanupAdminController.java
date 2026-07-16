package com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.operations.application.cleanup.service.CleanupApplicationService;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.assembler.OperationsCleanupInterfaceAssembler;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request.OperationsCleanupDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request.OperationsCleanupExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request.OperationsCleanupPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.response.OperationsCleanupDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.response.OperationsCleanupExecuteResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.response.OperationsCleanupPageResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "运营模块-清理", description = "Operations 清理接口")
@SysLogger(module = {"运营", "清理"})
@RequestMapping("/api/operations/cleanup")
@WrappedApiController
public class OperationsCleanupAdminController {

    private final CleanupApplicationService cleanupApplicationService;

    public OperationsCleanupAdminController(CleanupApplicationService cleanupApplicationService) {
        this.cleanupApplicationService = cleanupApplicationService;
    }

    @Operation(summary = "执行清理任务", description = "operations:cleanup:execute")
    @HasPermission("operations:cleanup:execute")
    @IgnoreSysLogger
    @PostMapping("execute")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public OperationsCleanupExecuteResponse execute(@Valid @RequestBody OperationsCleanupExecuteRequest request) {
        return OperationsCleanupInterfaceAssembler.toResponse(
                cleanupApplicationService.execute(OperationsCleanupInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "分页查询清理任务", description = "operations:cleanup:view")
    @HasPermission("operations:cleanup:view")
    @IgnoreSysLogger
    @PostMapping("page")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public PageResponse<OperationsCleanupPageResponse> page(@Valid @RequestBody OperationsCleanupPageRequest request) {
        return PageResponseHelper.fromPageResult(
                cleanupApplicationService.page(
                        OperationsCleanupInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                OperationsCleanupInterfaceAssembler::toResponse);
    }

    @Operation(summary = "获取清理任务详情", description = "operations:cleanup:view")
    @HasPermission("operations:cleanup:view")
    @IgnoreSysLogger
    @PostMapping("detail")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public OperationsCleanupDetailResponse detail(@Valid @RequestBody OperationsCleanupDetailRequest request) {
        return OperationsCleanupInterfaceAssembler.toDetailResponse(
                cleanupApplicationService.detail(OperationsCleanupInterfaceAssembler.toQuery(request)));
    }
}
