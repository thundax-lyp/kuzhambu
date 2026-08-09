package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ExportQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.SyncKnowledgeContentCommand;
import com.thundax.kuzhambu.discovery.application.qa.query.KnowledgeSyncItemPageQuery;
import com.thundax.kuzhambu.discovery.application.qa.query.QaSessionQuery;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeSyncItemResult;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeSyncApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.assembler.DiscoveryQaAdminInterfaceAssembler;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.request.DiscoveryQaAdminRequests;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.response.DiscoveryQaAdminResponses;
import com.thundax.kuzhambu.discovery.interfaces.common.DiscoveryInterfaceIdCodec;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "发现模块-Admin 问答", description = "Discovery Admin 问答运维接口")
@SysLogger(module = {"发现", "问答运维"})
@RequestMapping("/api/discovery/qa-admin")
@WrappedApiController
public class DiscoveryQaAdminController {

    private final QaApplicationService qaApplicationService;
    private final KnowledgeSyncApplicationService knowledgeSyncApplicationService;

    public DiscoveryQaAdminController(
            QaApplicationService qaApplicationService,
            KnowledgeSyncApplicationService knowledgeSyncApplicationService) {
        this.qaApplicationService = qaApplicationService;
        this.knowledgeSyncApplicationService = knowledgeSyncApplicationService;
    }

    @Operation(summary = "知识库健康", description = "Discovery QA 知识库健康检查")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping("knowledge/health")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoveryQaAdminResponses.QaKnowledgeHealthResponse getKnowledgeHealth() {
        return DiscoveryQaAdminInterfaceAssembler.toHealthResponse(knowledgeSyncApplicationService.health());
    }

    @Operation(summary = "重建知识库", description = "Discovery QA 知识库全量重建")
    @HasPermission("discovery:qa:edit")
    @IgnoreSysLogger
    @PostMapping("knowledge/rebuild")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public Long rebuildKnowledge() {
        return knowledgeSyncApplicationService.rebuild();
    }

    @Operation(summary = "同步知识内容", description = "Discovery QA 知识内容同步")
    @HasPermission("discovery:qa:edit")
    @IgnoreSysLogger
    @PostMapping("knowledge/sync")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoveryQaAdminResponses.QaSyncItemResponse syncKnowledge(
            @Valid @RequestBody DiscoveryQaAdminRequests.KnowledgeSyncRequest request) {
        KnowledgeSyncItemResult result = knowledgeSyncApplicationService.syncContent(toSyncContentCommand(request));
        return DiscoveryQaAdminInterfaceAssembler.toSyncItemResponse(result);
    }

    @Operation(summary = "分页查询同步内容", description = "Discovery QA 知识内容同步分页")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping("knowledge/sync/page")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public PageResponse<DiscoveryQaAdminResponses.QaSyncItemResponse> pageKnowledgeSyncItems(
            @Valid @RequestBody DiscoveryQaAdminRequests.KnowledgeSyncPageRequest request) {
        return PageResponseHelper.fromPageResult(
                knowledgeSyncApplicationService.pageSyncItems(toSyncPageQuery(request)),
                DiscoveryQaAdminInterfaceAssembler::toSyncItemResponse);
    }

    @Operation(summary = "获取会话详情", description = "Discovery QA 会话详情")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping("session/get")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoveryQaAdminResponses.QaSessionDetailResponse getSession(
            @Valid @RequestBody DiscoveryQaAdminRequests.QaSessionGetRequest request) {
        return DiscoveryQaAdminInterfaceAssembler.toSessionDetailResponse(
                qaApplicationService.getSessionDetail(DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId())));
    }

    @Operation(summary = "分页查询会话", description = "Discovery QA 会话分页")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping("session/page")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public PageResponse<DiscoveryQaAdminResponses.QaSessionResponse> pageSessions(
            @Valid @RequestBody DiscoveryQaAdminRequests.QaSessionPageRequest request) {
        return PageResponseHelper.fromPageResult(
                qaApplicationService.pageSessions(toSessionQuery(request), PageInterfaceAssembler.toPageQuery(request)),
                DiscoveryQaAdminInterfaceAssembler::toSessionResponse);
    }

    @Operation(summary = "删除会话", description = "Discovery QA Admin 软删除会话")
    @HasPermission("discovery:qa:edit")
    @IgnoreSysLogger
    @PostMapping("session/delete")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public void deleteSession(@Valid @RequestBody DiscoveryQaAdminRequests.QaSessionDeleteRequest request) {
        qaApplicationService.deleteSession(toDeleteSessionCommand(request));
    }

    @Operation(summary = "导出会话", description = "Discovery QA Admin 导出会话 CSV")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping("session/export")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoveryQaAdminResponses.QaSessionExportResponse exportSession(
            @Valid @RequestBody DiscoveryQaAdminRequests.QaSessionExportRequest request) {
        return DiscoveryQaAdminInterfaceAssembler.toSessionExportResponse(
                qaApplicationService.exportSession(toExportSessionCommand(request)));
    }

    private static SyncKnowledgeContentCommand toSyncContentCommand(
            DiscoveryQaAdminRequests.KnowledgeSyncRequest request) {
        return new SyncKnowledgeContentCommand(
                request == null ? null : request.getContentType(),
                request == null ? null : request.getContentId(),
                request == null ? null : request.getCurrentVersionNo(),
                request == null ? null : request.getRequestId(),
                request == null ? null : request.getTraceId());
    }

    private static KnowledgeSyncItemPageQuery toSyncPageQuery(
            DiscoveryQaAdminRequests.KnowledgeSyncPageRequest request) {
        if (request == null) {
            return new KnowledgeSyncItemPageQuery(null, null, 0, 0);
        }
        return new KnowledgeSyncItemPageQuery(
                request.getContentType(),
                request.getSyncStatus(),
                Objects.requireNonNullElse(request.getPageNo(), 0),
                Objects.requireNonNullElse(request.getPageSize(), 0));
    }

    private static DeleteQaSessionCommand toDeleteSessionCommand(
            DiscoveryQaAdminRequests.QaSessionDeleteRequest request) {
        return new DeleteQaSessionCommand(
                request == null ? null : DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()),
                null,
                null,
                true);
    }

    private static QaSessionQuery toSessionQuery(DiscoveryQaAdminRequests.QaSessionPageRequest request) {
        if (request == null) {
            return new QaSessionQuery(null, null, null);
        }
        return new QaSessionQuery(request.getTitle(), request.getOpenedAtStart(), request.getOpenedAtEnd());
    }

    private static ExportQaSessionCommand toExportSessionCommand(
            DiscoveryQaAdminRequests.QaSessionExportRequest request) {
        return new ExportQaSessionCommand(
                request == null ? null : DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()),
                request == null ? null : request.getRequesterUserId(),
                null,
                null,
                true,
                request == null ? null : request.getFormat());
    }
}
