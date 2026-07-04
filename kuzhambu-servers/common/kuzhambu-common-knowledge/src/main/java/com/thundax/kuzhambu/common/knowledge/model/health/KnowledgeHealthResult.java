package com.thundax.kuzhambu.common.knowledge.model.health;

import java.util.Map;

public record KnowledgeHealthResult(boolean available, String provider, String message, Map<String, Object> raw) {}
