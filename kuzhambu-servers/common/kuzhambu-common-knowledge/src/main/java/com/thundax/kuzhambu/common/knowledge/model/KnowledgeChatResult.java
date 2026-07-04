package com.thundax.kuzhambu.common.knowledge.model;

import java.util.Map;

public record KnowledgeChatResult(String chatId, String content, Map<String, Object> raw) {}
