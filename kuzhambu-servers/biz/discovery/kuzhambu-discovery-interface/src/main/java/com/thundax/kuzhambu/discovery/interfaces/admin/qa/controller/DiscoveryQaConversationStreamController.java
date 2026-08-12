package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.exception.ApiException;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeQaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.configure.DiscoveryQaStreamExecutorConfiguration;
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
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "发现模块-Admin 问答会话流", description = "Discovery Admin 问答会话 SSE 接口")
@SysLogger(module = {"发现", "知识助手"})
@RequestMapping("/api/discovery/qa")
@WrappedApiController
@RestController
public class DiscoveryQaConversationStreamController {

    private static final long STREAM_TIMEOUT_MILLIS = 600_000L;

    private final KnowledgeQaApplicationService knowledgeQaApplicationService;
    private final Executor streamExecutor;

    public DiscoveryQaConversationStreamController(
            KnowledgeQaApplicationService knowledgeQaApplicationService,
            @Qualifier(DiscoveryQaStreamExecutorConfiguration.QA_STREAM_EXECUTOR) Executor streamExecutor) {
        this.knowledgeQaApplicationService = knowledgeQaApplicationService;
        this.streamExecutor = streamExecutor;
    }

    @Operation(summary = "OpenAI 风格流式提问", description = "Admin 知识助手 OpenAI 风格 SSE 提问")
    @HasPermission("discovery:qa:view")
    @IgnoreSysLogger
    @PostMapping(value = "chat/submit", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public SseEmitter submitChatCompletion(@Valid @RequestBody DiscoveryQaRequests.ChatCompletionsRequest request) {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MILLIS);
        try {
            CompletableFuture<Void> future =
                    CompletableFuture.runAsync(() -> emitChatCompletion(request, emitter), streamExecutor);
            emitter.onTimeout(() -> {
                future.cancel(true);
                emitter.complete();
            });
            emitter.onError(error -> future.cancel(true));
            emitter.onCompletion(() -> future.cancel(true));
        } catch (RejectedExecutionException ex) {
            emitter.completeWithError(ex);
        }
        return emitter;
    }

    private void emitChatCompletion(DiscoveryQaRequests.ChatCompletionsRequest request, SseEmitter emitter) {
        try {
            Map<String, Object> started = new LinkedHashMap<>();
            started.put("sessionId", request.getSessionId());
            sendEvent(emitter, "started", started);
            DiscoveryQaResponses.ChatCompletionsResponse response =
                    DiscoveryQaPortalInterfaceAssembler.toChatCompletionsResponse(
                            knowledgeQaApplicationService.chatCompletionStream(
                                    DiscoveryQaPortalInterfaceAssembler.toChatCompletionCommand(request, true),
                                    content -> sendDeltaEvent(emitter, content)));
            sendEvent(emitter, "completed", response);
            emitter.complete();
        } catch (Exception ex) {
            sendBestEffortError(emitter, ex);
        }
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(name).data(data));
    }

    private void sendDeltaEvent(SseEmitter emitter, String content) {
        try {
            sendEvent(emitter, "delta", Map.of("content", content));
        } catch (IOException ex) {
            throw new ApiException("DISCOVERY-STREAM-00001", "discovery.qa.stream.send-failed", "问答流事件发送失败", ex);
        }
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
