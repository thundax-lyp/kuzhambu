package com.thundax.kuzhambu.common.knowledge.model;

import java.util.List;
import java.util.Map;

public record KnowledgeChatRequest(
        String appId,
        String chatId,
        List<KnowledgeChatMessage> messages,
        boolean stream,
        Map<String, Object> options) {}
