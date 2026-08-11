package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.discovery.application.qa.query.QaSessionDetailQuery;
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
    @PostMapping("knowledge/get")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoveryQaAdminResponses.QaKnowledgeHealthResponse getKnowledge() {
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
    @PostMapping("knowledge/update")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoveryQaAdminResponses.QaSyncItemResponse updateKnowledge(
            @Valid @RequestBody DiscoveryQaAdminRequests.KnowledgeSyncRequest request) {
        KnowledgeSyncItemResult result = knowledgeSyncApplicationService.syncContent(
                DiscoveryQaAdminInterfaceAssembler.toSyncKnowledgeContentCommand(request));
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
                knowledgeSyncApplicationService.pageSyncItems(
                        DiscoveryQaAdminInterfaceAssembler.toKnowledgeSyncItemQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
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
        return DiscoveryQaAdminInterfaceAssembler.toSessionDetailResponse(qaApplicationService.getSessionDetail(
                new QaSessionDetailQuery(DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()))));
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
                qaApplicationService.pageSessions(
                        DiscoveryQaAdminInterfaceAssembler.toQaSessionQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
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
        qaApplicationService.deleteSession(DiscoveryQaAdminInterfaceAssembler.toDeleteQaSessionCommand(request));
    }

    @Operation(summary = "导出会话", description = "Discovery QA Admin 导出会话 CSV")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping("session/download")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoveryQaAdminResponses.QaSessionExportResponse downloadSession(
            @Valid @RequestBody DiscoveryQaAdminRequests.QaSessionExportRequest request) {
        return DiscoveryQaAdminInterfaceAssembler.toSessionExportResponse(qaApplicationService.exportSession(
                DiscoveryQaAdminInterfaceAssembler.toExportQaSessionCommand(request)));
    }
}
