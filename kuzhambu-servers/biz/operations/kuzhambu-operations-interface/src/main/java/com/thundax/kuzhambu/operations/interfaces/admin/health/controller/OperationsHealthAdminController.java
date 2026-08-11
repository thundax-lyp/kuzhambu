package com.thundax.kuzhambu.operations.interfaces.admin.health.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.operations.application.health.service.HealthCheckApplicationService;
import com.thundax.kuzhambu.operations.interfaces.admin.health.assembler.OperationsHealthInterfaceAssembler;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthSummaryRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthTrendRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthPageResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthSummaryResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthTrendResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "运营模块-健康", description = "Operations 健康接口")
@SysLogger(module = {"运营", "健康"})
@RequestMapping("/api/operations/health")
@WrappedApiController
public class OperationsHealthAdminController {

    private final HealthCheckApplicationService healthCheckApplicationService;

    public OperationsHealthAdminController(HealthCheckApplicationService healthCheckApplicationService) {
        this.healthCheckApplicationService = healthCheckApplicationService;
    }

    @Operation(summary = "查询健康摘要", description = "operations:health:view")
    @HasPermission("operations:health:view")
    @IgnoreSysLogger
    @PostMapping("list")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public List<OperationsHealthSummaryResponse> listSummary(
            @Valid @RequestBody OperationsHealthSummaryRequest request) {
        return healthCheckApplicationService.summary().stream()
                .map(OperationsHealthInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "分页查询健康记录", description = "operations:health:view")
    @HasPermission("operations:health:view")
    @IgnoreSysLogger
    @PostMapping("page")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public PageResponse<OperationsHealthPageResponse> page(@Valid @RequestBody OperationsHealthPageRequest request) {
        return PageResponseHelper.fromPageResult(
                healthCheckApplicationService.page(
                        OperationsHealthInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                OperationsHealthInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查询健康趋势", description = "operations:health:view")
    @HasPermission("operations:health:view")
    @IgnoreSysLogger
    @PostMapping("list")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public List<OperationsHealthTrendResponse> listTrend(@Valid @RequestBody OperationsHealthTrendRequest request) {
        return healthCheckApplicationService.trend(OperationsHealthInterfaceAssembler.toQuery(request)).stream()
                .map(OperationsHealthInterfaceAssembler::toResponse)
                .collect(Collectors.toList());
    }
}
