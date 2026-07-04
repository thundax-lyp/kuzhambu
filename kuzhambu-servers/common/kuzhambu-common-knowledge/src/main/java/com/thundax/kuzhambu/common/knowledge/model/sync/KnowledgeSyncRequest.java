package com.thundax.kuzhambu.common.knowledge.model.sync;

import java.util.Map;

public record KnowledgeSyncRequest(String knowledgeBaseName, String knowledgeItemId, Map<String, Object> options) {}
