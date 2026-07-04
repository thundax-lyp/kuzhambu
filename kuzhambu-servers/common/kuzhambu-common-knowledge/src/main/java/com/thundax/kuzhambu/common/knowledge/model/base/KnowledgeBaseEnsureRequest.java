package com.thundax.kuzhambu.common.knowledge.model.base;

import java.util.Map;

public record KnowledgeBaseEnsureRequest(String name, String description, Map<String, Object> options) {}
