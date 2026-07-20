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
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @Operation(summary = "OpenAI 风格流式提问", description = "Admin 知识助手 OpenAI 风格 SSE 提问")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping(value = "chat/completions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public SseEmitter chatCompletionsStream(@Valid @RequestBody DiscoveryQaRequests.ChatCompletionsRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> emitChatCompletion(request, emitter));
        return emitter;
    }

    private void emitChatCompletion(DiscoveryQaRequests.ChatCompletionsRequest request, SseEmitter emitter) {
        try {
            Map<String, Object> started = new LinkedHashMap<>();
            started.put("sessionId", request.getSessionId());
            sendEvent(emitter, "started", started);
            DiscoveryQaResponses.ChatCompletionsResponse response =
                    DiscoveryQaPortalInterfaceAssembler.toChatCompletionsResponse(
                            knowledgeQaApplicationService.chatCompletion(
                                    DiscoveryQaPortalInterfaceAssembler.toChatCompletionCommand(request, false)));
            String answer = response == null ? null : response.getAnswer();
            if (StringUtils.isNotBlank(answer)) {
                sendEvent(emitter, "delta", Map.of("content", answer));
            }
            sendEvent(emitter, "completed", response);
            emitter.complete();
        } catch (Exception ex) {
            sendBestEffortError(emitter, ex);
        }
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(name).data(data));
    }

    private void sendBestEffortError(SseEmitter emitter, Exception ex) {
        try {
            sendEvent(emitter, "error", Map.of("message", toClientErrorMessage(ex)));
            emitter.complete();
        } catch (IOException sendEx) {
            emitter.completeWithError(sendEx);
        }
    }

    private String toClientErrorMessage(Exception ex) {
        String message = ex == null ? null : ex.getMessage();
        if (StringUtils.containsIgnoreCase(message, "appId is empty")) {
            return "问答应用未配置，请检查 FastGPT 应用配置。";
        }
        return "问答生成失败，请稍后重试。";
    }
}
