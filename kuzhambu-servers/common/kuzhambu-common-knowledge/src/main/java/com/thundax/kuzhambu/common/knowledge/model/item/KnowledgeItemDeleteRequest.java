package com.thundax.kuzhambu.common.knowledge.model.item;

import java.util.Map;

public record KnowledgeItemDeleteRequest(
        String knowledgeBaseName, String knowledgeItemId, String itemKey, Map<String, Object> options) {}
