package com.thundax.kuzhambu.common.knowledge.model.base;

import java.util.Map;

public record KnowledgeBaseResult(String knowledgeBaseId, String name, Map<String, Object> raw) {}
