package com.thundax.kuzhambu.system.interfaces.admin.audit.controller;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageRules;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.system.application.audit.runtime.AuditSnapshotAssemblerRegistry;
import com.thundax.kuzhambu.system.application.audit.service.AuditApplicationService;
import com.thundax.kuzhambu.system.interfaces.admin.audit.assembler.AuditInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request.AuditLogDetailRequest;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request.AuditLogPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request.AuditMetaRequest;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request.AuditObjectFieldRequest;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request.AuditObjectPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditLogDetailResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditLogResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditMetaResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditObjectFieldResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditObjectOverviewResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditOptionsResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "系统模块", description = "系统管理")
@SysLogger(module = {"系统", "审计"})
@RequestMapping(value = "/api/audit/log")
@WrappedApiController
public class AuditController {

    private final AuditApplicationService auditService;
    private final AuditSnapshotAssemblerRegistry auditSnapshotAssemblerRegistry;

    public AuditController(
            AuditApplicationService auditService, AuditSnapshotAssemblerRegistry auditSnapshotAssemblerRegistry) {
        this.auditService = auditService;
        this.auditSnapshotAssemblerRegistry = auditSnapshotAssemblerRegistry;
    }

    @Operation(summary = "获取审计元数据", description = "audit:view")
    @HasPermission(value = "audit:view")
    @IgnoreSysLogger
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "meta")
    public AuditMetaResponse meta(@Valid @RequestBody AuditMetaRequest request) {
        return AuditInterfaceAssembler.toMetaResponse(
                auditService.getMeta(AuditInterfaceAssembler.toMetaQuery(request)));
    }

    @Operation(summary = "获取对象审计历史", description = "audit:view")
    @HasPermission(value = "audit:view")
    @IgnoreSysLogger
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "history")
    public PageResponse<AuditLogResponse> history(@Valid @RequestBody AuditObjectPageRequest request) {
        return PageResponseHelper.fromPageResult(
                auditService.page(
                        AuditInterfaceAssembler.toLogQuery(request), PageInterfaceAssembler.toPageQuery(request)),
                log -> AuditInterfaceAssembler.toLogResponse(log, auditSnapshotAssemblerRegistry));
    }

    @Operation(summary = "获取审计日志详情", description = "audit:view")
    @HasPermission(value = "audit:view")
    @IgnoreSysLogger
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "detail")
    public AuditLogDetailResponse detail(@Valid @RequestBody AuditLogDetailRequest request) {
        return AuditInterfaceAssembler.toLogDetailResponse(
                auditService.getLog(AuditInterfaceAssembler.toLogId(request)), auditSnapshotAssemblerRegistry);
    }

    @Operation(summary = "获取对象审计概览", description = "audit:view")
    @HasPermission(value = "audit:view")
    @IgnoreSysLogger
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "object/overview")
    public AuditObjectOverviewResponse objectOverview(@Valid @RequestBody AuditMetaRequest request) {
        return AuditInterfaceAssembler.toOverviewResponse(
                auditService.getMeta(AuditInterfaceAssembler.toMetaQuery(request)),
                auditService.page(
                        AuditInterfaceAssembler.toObjectLogQuery(request),
                        new PageQuery(PageRules.firstPageIndex(), 5)),
                auditSnapshotAssemblerRegistry);
    }

    @Operation(summary = "获取对象审计分页", description = "audit:view")
    @HasPermission(value = "audit:view")
    @IgnoreSysLogger
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "object/page")
    public PageResponse<AuditLogResponse> objectPage(@Valid @RequestBody AuditObjectPageRequest request) {
        return PageResponseHelper.fromPageResult(
                auditService.page(
                        AuditInterfaceAssembler.toLogQuery(request), PageInterfaceAssembler.toPageQuery(request)),
                log -> AuditInterfaceAssembler.toLogResponse(log, auditSnapshotAssemblerRegistry));
    }

    @Operation(summary = "审计日志分页", description = "audit:view")
    @HasPermission(value = "audit:view")
    @IgnoreSysLogger
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "page")
    public PageResponse<AuditLogResponse> page(@Valid @RequestBody AuditLogPageRequest request) {
        PageQuery pageQuery = PageInterfaceAssembler.toPageQuery(request);
        return PageResponseHelper.fromPageResult(
                auditService.page(AuditInterfaceAssembler.toLogQuery(request), pageQuery),
                log -> AuditInterfaceAssembler.toLogResponse(log, auditSnapshotAssemblerRegistry));
    }

    @Operation(summary = "获取审计选项", description = "audit:view")
    @HasPermission(value = "audit:view")
    @IgnoreSysLogger
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "options")
    public AuditOptionsResponse options() {
        return AuditInterfaceAssembler.toOptionsResponse(auditSnapshotAssemblerRegistry);
    }

    @Operation(summary = "获取审计对象字段", description = "audit:view")
    @HasPermission(value = "audit:view")
    @IgnoreSysLogger
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "fields")
    public List<AuditObjectFieldResponse> fields(@Valid @RequestBody AuditObjectFieldRequest request) {
        return AuditInterfaceAssembler.toFieldResponses(auditSnapshotAssemblerRegistry, request.getObjectType());
    }
}
