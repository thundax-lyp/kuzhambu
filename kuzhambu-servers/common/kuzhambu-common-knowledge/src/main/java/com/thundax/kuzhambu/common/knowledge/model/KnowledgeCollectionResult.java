package com.thundax.kuzhambu.common.knowledge.model;

import java.util.Map;

public record KnowledgeCollectionResult(String collectionId, String datasetId, String name, Map<String, Object> raw) {}
