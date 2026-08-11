package com.thundax.kuzhambu.discovery.application.qa.command;

import java.util.List;
import java.util.Map;

public record ChatCompletionCommand(
        Long sessionId,
        String model,
        List<ChatMessage> messages,
        boolean stream,
        Map<String, Object> metadata,
        Map<String, Object> options,
        String requestId,
        String traceId) {

    public record ChatMessage(String role, String content) {}
}
