package com.thundax.kuzhambu.common.knowledge.model.chat;

import java.util.List;
import java.util.Map;

public record KnowledgeChatResult(
        String id,
        String object,
        Long created,
        String model,
        List<KnowledgeChatChoice> choices,
        KnowledgeChatUsage usage,
        List<KnowledgeChatSource> sources,
        Map<String, Object> raw) {}
