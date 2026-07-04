package com.thundax.kuzhambu.common.knowledge.model.chat;

import java.util.List;
import java.util.Map;

public record KnowledgeChatRequest(
        String model,
        List<KnowledgeChatMessage> messages,
        boolean stream,
        Map<String, Object> metadata,
        Map<String, Object> options) {}
