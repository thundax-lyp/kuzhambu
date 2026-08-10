package com.thundax.kuzhambu.system.interfaces.admin.audit.controller;

import com.thundax.kuzhambu.common.audit.runtime.AuditSnapshotAssemblerRegistry;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.page.PageRules;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.system.application.audit.service.AuditTrailApplicationService;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditMeta;
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

@Tag(name = "系统模块-审计日志", description = "审计日志")
@SysLogger(module = {"系统", "审计"})
@RequestMapping(value = "/api/audit/log")
@WrappedApiController
public class AuditController {

    private final AuditTrailApplicationService auditService;
    private final AuditSnapshotAssemblerRegistry auditSnapshotAssemblerRegistry;

    public AuditController(
            AuditTrailApplicationService auditService, AuditSnapshotAssemblerRegistry auditSnapshotAssemblerRegistry) {
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
    @PostMapping(value = "meta/get")
    public AuditMetaResponse getMeta(@Valid @RequestBody AuditMetaRequest request) {
        AuditMeta meta = auditService.getMeta(AuditInterfaceAssembler.toMetaQuery(request));
        return meta == null
                ? AuditInterfaceAssembler.emptyMetaResponse()
                : AuditInterfaceAssembler.toMetaResponse(meta);
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
    @PostMapping(value = "history/list")
    public PageResponse<AuditLogResponse> listHistory(@Valid @RequestBody AuditObjectPageRequest request) {
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
    @PostMapping(value = "detail/get")
    public AuditLogDetailResponse getDetail(@Valid @RequestBody AuditLogDetailRequest request) {
        AuditLog log = auditService.getLog(AuditInterfaceAssembler.toGetLogQuery(request));
        return log == null
                ? AuditInterfaceAssembler.emptyLogDetailResponse()
                : AuditInterfaceAssembler.toLogDetailResponse(log, auditSnapshotAssemblerRegistry);
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
    @PostMapping(value = "object/get")
    public AuditObjectOverviewResponse getObjectOverview(@Valid @RequestBody AuditMetaRequest request) {
        AuditMeta meta = auditService.getMeta(AuditInterfaceAssembler.toMetaQuery(request));
        PageResult<AuditLog> latestLogs = auditService.page(
                AuditInterfaceAssembler.toObjectLogQuery(request), new PageQuery(PageRules.firstPageIndex(), 5));
        if (meta == null) {
            return AuditInterfaceAssembler.emptyOverviewResponse(latestLogs);
        }
        return AuditInterfaceAssembler.toOverviewResponse(meta, latestLogs, auditSnapshotAssemblerRegistry);
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
    public PageResponse<AuditLogResponse> pageObject(@Valid @RequestBody AuditObjectPageRequest request) {
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
    @PostMapping(value = "options/list")
    public AuditOptionsResponse listOptions() {
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
    @PostMapping(value = "fields/list")
    public List<AuditObjectFieldResponse> listFields(@Valid @RequestBody AuditObjectFieldRequest request) {
        return AuditInterfaceAssembler.toFieldResponses(auditSnapshotAssemblerRegistry, request.getObjectType());
    }
}
