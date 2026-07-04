package com.thundax.kuzhambu.common.knowledge.model;

import java.util.Map;

public record KnowledgeCollectionCreateRequest(
        String datasetId, String name, String text, Map<String, Object> metadata, Map<String, Object> options) {}
