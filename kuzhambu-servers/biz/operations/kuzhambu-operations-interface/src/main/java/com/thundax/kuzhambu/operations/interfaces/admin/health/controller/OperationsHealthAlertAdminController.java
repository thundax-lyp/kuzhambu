package com.thundax.kuzhambu.operations.interfaces.admin.health.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.operations.application.health.service.HealthAlertApplicationService;
import com.thundax.kuzhambu.operations.interfaces.admin.health.assembler.OperationsHealthAlertInterfaceAssembler;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertAckRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertRecoverRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthAlertPageResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "运营模块-健康告警", description = "Operations 健康告警接口")
@SysLogger(module = {"运营", "健康告警"})
@RequestMapping("/api/operations/health/alerts")
@WrappedApiController
public class OperationsHealthAlertAdminController {

    private final HealthAlertApplicationService healthAlertApplicationService;

    public OperationsHealthAlertAdminController(HealthAlertApplicationService healthAlertApplicationService) {
        this.healthAlertApplicationService = healthAlertApplicationService;
    }

    @Operation(summary = "分页查询健康告警", description = "operations:health:view")
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
    public PageResponse<OperationsHealthAlertPageResponse> page(
            @Valid @RequestBody OperationsHealthAlertPageRequest request) {
        return PageResponseHelper.fromPageResult(
                healthAlertApplicationService.page(
                        OperationsHealthAlertInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                OperationsHealthAlertInterfaceAssembler::toResponse);
    }

    @Operation(summary = "确认健康告警", description = "operations:health:manage")
    @HasPermission("operations:health:manage")
    @SysLogger(value = "确认健康告警")
    @PostMapping("confirm")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public void confirm(@Valid @RequestBody OperationsHealthAlertAckRequest request) {
        healthAlertApplicationService.ack(OperationsHealthAlertInterfaceAssembler.toCommand(request));
    }

    @Operation(summary = "人工恢复健康告警", description = "operations:health:manage")
    @HasPermission("operations:health:manage")
    @SysLogger(value = "人工恢复健康告警")
    @PostMapping("recover")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public void recover(@Valid @RequestBody OperationsHealthAlertRecoverRequest request) {
        healthAlertApplicationService.recover(OperationsHealthAlertInterfaceAssembler.toCommand(request));
    }
}
