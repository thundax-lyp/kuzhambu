package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeQaApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.common.DiscoveryInterfaceIdCodec;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler.DiscoveryQaPortalInterfaceAssembler;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request.DiscoveryQaRequests;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.response.DiscoveryQaResponses;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "发现模块-Admin 问答会话", description = "Discovery Admin 问答会话接口")
@SysLogger(module = {"发现", "知识助手"})
@RequestMapping("/api/discovery/qa")
@WrappedApiController
public class DiscoveryQaConversationController {

    private final QaApplicationService qaApplicationService;
    private final KnowledgeQaApplicationService knowledgeQaApplicationService;

    public DiscoveryQaConversationController(
            QaApplicationService qaApplicationService, KnowledgeQaApplicationService knowledgeQaApplicationService) {
        this.qaApplicationService = qaApplicationService;
        this.knowledgeQaApplicationService = knowledgeQaApplicationService;
    }

    @Operation(summary = "创建问答会话", description = "Admin 知识助手创建会话")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping("session/open")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoveryQaResponses.OpenSessionResponse openSession(
            @Valid @RequestBody DiscoveryQaRequests.OpenSessionRequest request) {
        return DiscoveryQaPortalInterfaceAssembler.toOpenSessionResponse(
                qaApplicationService.openSession(DiscoveryQaPortalInterfaceAssembler.toOpenSessionCommand(request)));
    }

    @Operation(summary = "分页查询问答会话", description = "Admin 知识助手查询会话")
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
    public PageResponse<DiscoveryQaResponses.QaSessionResponse> pageSessions(
            @Valid @RequestBody DiscoveryQaRequests.QaSessionPageRequest request) {
        return DiscoveryQaPortalInterfaceAssembler.toSessionPageResponse(
                qaApplicationService.listPortalSessions(
                        DiscoveryQaPortalInterfaceAssembler.ownerType(),
                        DiscoveryQaPortalInterfaceAssembler.ownerId(request.getOwnerUserId()),
                        DiscoveryQaPortalInterfaceAssembler.limit(request)),
                request);
    }

    @Operation(summary = "获取问答会话详情", description = "Admin 知识助手获取会话详情")
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
    public DiscoveryQaResponses.QaSessionDetailResponse getSession(
            @Valid @RequestBody DiscoveryQaRequests.QaSessionGetRequest request) {
        return DiscoveryQaPortalInterfaceAssembler.toSessionDetailResponse(qaApplicationService.getPortalSessionDetail(
                DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()),
                DiscoveryQaPortalInterfaceAssembler.ownerType(),
                DiscoveryQaPortalInterfaceAssembler.ownerId(request.getOwnerUserId())));
    }

    @Operation(summary = "删除问答会话", description = "Admin 知识助手删除会话")
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
    public void deleteSession(@Valid @RequestBody DiscoveryQaRequests.QaSessionDeleteRequest request) {
        qaApplicationService.deleteSession(DiscoveryQaPortalInterfaceAssembler.toDeleteSessionCommand(request));
    }

    @Operation(summary = "导出问答会话", description = "Admin 知识助手导出会话 CSV")
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
    public DiscoveryQaResponses.QaSessionExportResponse exportSession(
            @Valid @RequestBody DiscoveryQaRequests.QaSessionExportRequest request) {
        return DiscoveryQaPortalInterfaceAssembler.toSessionExportResponse(qaApplicationService.exportSession(
                DiscoveryQaPortalInterfaceAssembler.toExportSessionCommand(request)));
    }

    @Operation(summary = "OpenAI 风格提问", description = "Admin 知识助手 OpenAI 风格提问")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping("chat/completions")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoveryQaResponses.ChatCompletionsResponse chatCompletions(
            @Valid @RequestBody DiscoveryQaRequests.ChatCompletionsRequest request) {
        return DiscoveryQaPortalInterfaceAssembler.toChatCompletionsResponse(
                knowledgeQaApplicationService.chatCompletion(
                        DiscoveryQaPortalInterfaceAssembler.toChatCompletionCommand(request)));
    }
}
