package com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeQaApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.common.DiscoveryInterfaceIdCodec;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler.DiscoveryQaPortalInterfaceAssembler;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request.DiscoveryQaRequests;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.response.DiscoveryQaResponses;
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
                DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()),
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

    @Operation(summary = "OpenAI 风格流式提问", description = "Portal 问答 OpenAI 风格 SSE 提问")
    @PostMapping(value = "chat/completions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
            sendEvent(emitter, "error", Map.of("message", ex.getMessage() == null ? "问答生成失败" : ex.getMessage()));
            emitter.complete();
        } catch (IOException sendEx) {
            emitter.completeWithError(sendEx);
        }
    }
}
