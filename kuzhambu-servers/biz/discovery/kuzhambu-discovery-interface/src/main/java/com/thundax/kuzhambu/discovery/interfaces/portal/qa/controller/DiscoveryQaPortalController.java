package com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeQaApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler.DiscoveryQaPortalInterfaceAssembler;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request.DiscoveryQaRequests;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.response.DiscoveryQaResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "发现模块-Portal 问答", description = "Discovery Portal 问答接口")
@PublicApi
@RequestMapping("/api/portal/discovery/qa")
@WrappedApiController
public class DiscoveryQaPortalController {

    private final QaApplicationService qaApplicationService;
    private final KnowledgeQaApplicationService knowledgeQaApplicationService;

    public DiscoveryQaPortalController(
            QaApplicationService qaApplicationService, KnowledgeQaApplicationService knowledgeQaApplicationService) {
        this.qaApplicationService = qaApplicationService;
        this.knowledgeQaApplicationService = knowledgeQaApplicationService;
    }

    @Operation(summary = "创建问答会话", description = "Portal 问答创建会话")
    @PostMapping("session/open")
    public DiscoveryQaResponses.OpenSessionResponse openSession(
            @Valid @RequestBody DiscoveryQaRequests.OpenSessionRequest request) {
        return DiscoveryQaPortalInterfaceAssembler.toOpenSessionResponse(
                qaApplicationService.openSession(DiscoveryQaPortalInterfaceAssembler.toOpenSessionCommand(request)));
    }

    @Operation(summary = "分页查询问答会话", description = "Portal 查询自己的未删除问答会话")
    @PostMapping("session/page")
    public PageResponse<DiscoveryQaResponses.QaSessionResponse> pageSessions(
            @Valid @RequestBody DiscoveryQaRequests.QaSessionPageRequest request) {
        return DiscoveryQaPortalInterfaceAssembler.toSessionPageResponse(
                qaApplicationService.listPortalSessions(
                        DiscoveryQaPortalInterfaceAssembler.ownerType(),
                        DiscoveryQaPortalInterfaceAssembler.ownerId(request.getOwnerUserId()),
                        DiscoveryQaPortalInterfaceAssembler.limit(request)),
                request);
    }

    @Operation(summary = "获取问答会话详情", description = "Portal 获取自己的未删除问答会话详情")
    @PostMapping("session/get")
    public DiscoveryQaResponses.QaSessionDetailResponse getSession(
            @Valid @RequestBody DiscoveryQaRequests.QaSessionGetRequest request) {
        return DiscoveryQaPortalInterfaceAssembler.toSessionDetailResponse(qaApplicationService.getPortalSessionDetail(
                request.getSessionId(),
                DiscoveryQaPortalInterfaceAssembler.ownerType(),
                DiscoveryQaPortalInterfaceAssembler.ownerId(request.getOwnerUserId())));
    }

    @Operation(summary = "删除问答会话", description = "Portal 软删除自己的问答会话")
    @PostMapping("session/delete")
    public void deleteSession(@Valid @RequestBody DiscoveryQaRequests.QaSessionDeleteRequest request) {
        qaApplicationService.deleteSession(DiscoveryQaPortalInterfaceAssembler.toDeleteSessionCommand(request));
    }

    @Operation(summary = "导出问答会话", description = "Portal 导出自己的未删除问答会话 CSV")
    @PostMapping("session/export")
    public DiscoveryQaResponses.QaSessionExportResponse exportSession(
            @Valid @RequestBody DiscoveryQaRequests.QaSessionExportRequest request) {
        return DiscoveryQaPortalInterfaceAssembler.toSessionExportResponse(qaApplicationService.exportSession(
                DiscoveryQaPortalInterfaceAssembler.toExportSessionCommand(request)));
    }

    @Operation(summary = "OpenAI 风格提问", description = "Portal 问答 OpenAI 风格提问")
    @PostMapping("chat/completions")
    public DiscoveryQaResponses.ChatCompletionsResponse chatCompletions(
            @Valid @RequestBody DiscoveryQaRequests.ChatCompletionsRequest request) {
        return DiscoveryQaPortalInterfaceAssembler.toChatCompletionsResponse(
                knowledgeQaApplicationService.chatCompletion(
                        DiscoveryQaPortalInterfaceAssembler.toChatCompletionCommand(request)));
    }
}
