package com.thundax.kuzhambu.common.knowledge.model.chat;

import java.util.Map;

public record KnowledgeChatSource(
        String sourceId,
        String knowledgeBase,
        String contentType,
        String contentId,
        String title,
        String snippet,
        Double score,
        Map<String, Object> raw) {}
