package com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.operations.application.dashboard.service.OperationsDashboardApplicationService;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.assembler.OperationsDashboardInterfaceAssembler;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.request.OperationsDashboardOverviewRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.response.OperationsDashboardOverviewResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "运营模块-看板", description = "Operations 运营看板接口")
@SysLogger(module = {"运营", "看板"})
@RequestMapping("/api/operations/dashboard")
@WrappedApiController
public class OperationsDashboardAdminController {

    private final OperationsDashboardApplicationService operationsDashboardApplicationService;

    public OperationsDashboardAdminController(
            OperationsDashboardApplicationService operationsDashboardApplicationService) {
        this.operationsDashboardApplicationService = operationsDashboardApplicationService;
    }

    @Operation(summary = "查询运营看板概览", description = "operations:dashboard:view")
    @HasPermission("operations:dashboard:view")
    @IgnoreSysLogger
    @PostMapping("overview")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public OperationsDashboardOverviewResponse overview(
            @Valid @RequestBody OperationsDashboardOverviewRequest request) {
        var result =
                operationsDashboardApplicationService.overview(OperationsDashboardInterfaceAssembler.toQuery(request));
        return result == null
                ? OperationsDashboardOverviewResponse.builder().build()
                : OperationsDashboardInterfaceAssembler.toResponse(result);
    }
}
