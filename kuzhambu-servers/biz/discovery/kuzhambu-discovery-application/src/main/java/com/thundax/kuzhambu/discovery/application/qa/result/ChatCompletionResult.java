package com.thundax.kuzhambu.discovery.application.qa.result;

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
public class ChatCompletionResult {
    private Long sessionId;
    private Long questionMessageId;
    private Long answerMessageId;
    private String question;
    private String answerStatus;
    private String failureReason;
    private List<ChatCompletionChoice> choices;
    private List<ChatCompletionSource> sources;
    private ChatUsageResult usage;
    private Map<String, Object> raw;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatCompletionChoice {
        private Integer index;
        private ChatCompletionMessage message;
        private String finishReason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatCompletionMessage {
        private String role;
        private String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatCompletionSource {
        private String sourceId;
        private String knowledgeBase;
        private String contentType;
        private String contentId;
        private String title;
        private String snippet;
        private Double score;
        private Map<String, Object> raw;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatUsageResult {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
