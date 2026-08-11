package com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeQaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.configure.DiscoveryQaStreamExecutorConfiguration;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler.DiscoveryQaPortalInterfaceAssembler;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request.DiscoveryQaRequests;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.response.DiscoveryQaResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "发现模块-Portal 问答流", description = "Discovery Portal 问答 SSE 接口")
@PublicApi
@RequestMapping("/api/portal/discovery/qa")
@RestController
public class DiscoveryQaPortalStreamController {

    private static final long STREAM_TIMEOUT_MILLIS = 600_000L;

    private final KnowledgeQaApplicationService knowledgeQaApplicationService;
    private final Executor streamExecutor;

    public DiscoveryQaPortalStreamController(
            KnowledgeQaApplicationService knowledgeQaApplicationService,
            @Qualifier(DiscoveryQaStreamExecutorConfiguration.QA_STREAM_EXECUTOR) Executor streamExecutor) {
        this.knowledgeQaApplicationService = knowledgeQaApplicationService;
        this.streamExecutor = streamExecutor;
    }

    @Operation(summary = "OpenAI 风格流式提问", description = "Portal 问答 OpenAI 风格 SSE 提问")
    @PostMapping(value = "chat/submit", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
            throw new UncheckedIOException(ex);
        }
    }

    private void sendBestEffortError(SseEmitter emitter, Exception ex) {
        try {
            sendEvent(emitter, "error", Map.of("message", toClientErrorMessage()));
            emitter.complete();
        } catch (IOException sendEx) {
            emitter.completeWithError(sendEx);
        }
    }

    private String toClientErrorMessage() {
        return "问答生成失败，请稍后重试。";
    }
}
