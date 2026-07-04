package com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
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

    @Operation(summary = "OpenAI 风格提问", description = "Portal 问答 OpenAI 风格提问")
    @PostMapping("chat/completions")
    public DiscoveryQaResponses.ChatCompletionsResponse chatCompletions(
            @Valid @RequestBody DiscoveryQaRequests.ChatCompletionsRequest request) {
        return DiscoveryQaPortalInterfaceAssembler.toChatCompletionsResponse(
                knowledgeQaApplicationService.chatCompletion(
                        DiscoveryQaPortalInterfaceAssembler.toChatCompletionCommand(request)));
    }
}
