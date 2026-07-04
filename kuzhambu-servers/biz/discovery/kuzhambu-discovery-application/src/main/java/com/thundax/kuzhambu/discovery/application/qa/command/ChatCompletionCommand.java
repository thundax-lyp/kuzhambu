package com.thundax.kuzhambu.discovery.application.qa.command;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionCommand {
    private Long sessionId;
    private String model;
    private List<ChatMessage> messages;
    private boolean stream;
    private Map<String, Object> metadata;
    private Map<String, Object> options;
    private String requestId;
    private String traceId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
