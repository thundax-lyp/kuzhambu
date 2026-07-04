package com.thundax.kuzhambu.common.knowledge.model;

import java.util.Map;

public record KnowledgeSyncResult(String syncId, String status, Map<String, Object> raw) {}
