package com.thundax.kuzhambu.common.knowledge.model.item;

import java.util.Map;

public record KnowledgeItemResult(
        String knowledgeItemId, String knowledgeBaseId, String itemKey, String title, Map<String, Object> raw) {}
