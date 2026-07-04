package com.thundax.kuzhambu.common.knowledge.model;

import java.util.Map;

public record KnowledgeDatasetCreateRequest(String name, String description, Map<String, Object> options) {}
