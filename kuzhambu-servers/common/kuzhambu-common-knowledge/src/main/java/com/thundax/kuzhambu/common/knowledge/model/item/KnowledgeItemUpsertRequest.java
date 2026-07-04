package com.thundax.kuzhambu.common.knowledge.model.item;

import java.util.Map;

public record KnowledgeItemUpsertRequest(
        String knowledgeBaseName,
        String itemKey,
        String title,
        String text,
        Map<String, Object> metadata,
        Map<String, Object> options) {}
