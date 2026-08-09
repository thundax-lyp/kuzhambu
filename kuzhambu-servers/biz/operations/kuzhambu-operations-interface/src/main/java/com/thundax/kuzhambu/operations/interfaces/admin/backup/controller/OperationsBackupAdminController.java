package com.thundax.kuzhambu.operations.interfaces.admin.backup.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.operations.application.backup.service.BackupApplicationService;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.assembler.OperationsBackupInterfaceAssembler;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupExecuteResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupPageResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "运营模块-备份", description = "Operations 备份接口")
@SysLogger(module = {"运营", "备份"})
@RequestMapping("/api/operations/backup")
@WrappedApiController
public class OperationsBackupAdminController {

    private final BackupApplicationService backupApplicationService;

    public OperationsBackupAdminController(BackupApplicationService backupApplicationService) {
        this.backupApplicationService = backupApplicationService;
    }

    @Operation(summary = "执行手动备份", description = "operations:backup:execute")
    @HasPermission("operations:backup:execute")
    @IgnoreSysLogger
    @PostMapping("execute")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public OperationsBackupExecuteResponse execute(@Valid @RequestBody OperationsBackupExecuteRequest request) {
        var result = backupApplicationService.execute(OperationsBackupInterfaceAssembler.toCommand(request));
        return result == null
                ? OperationsBackupExecuteResponse.builder().build()
                : OperationsBackupInterfaceAssembler.toResponse(result);
    }

    @Operation(summary = "分页查询备份任务", description = "operations:backup:view")
    @HasPermission("operations:backup:view")
    @IgnoreSysLogger
    @PostMapping("page")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public PageResponse<OperationsBackupPageResponse> page(@Valid @RequestBody OperationsBackupPageRequest request) {
        return PageResponseHelper.fromPageResult(
                backupApplicationService.page(
                        OperationsBackupInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                OperationsBackupInterfaceAssembler::toResponse);
    }

    @Operation(summary = "获取备份任务详情", description = "operations:backup:view")
    @HasPermission("operations:backup:view")
    @IgnoreSysLogger
    @PostMapping("detail")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public OperationsBackupDetailResponse detail(@Valid @RequestBody OperationsBackupDetailRequest request) {
        var result = backupApplicationService.detail(OperationsBackupInterfaceAssembler.toQuery(request));
        return result == null
                ? OperationsBackupDetailResponse.builder().build()
                : OperationsBackupInterfaceAssembler.toDetailResponse(result);
    }
}
