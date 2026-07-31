package com.thundax.kuzhambu.common.knowledge.model.collection;

import java.util.Map;

public record KnowledgeCollectionResult(String collectionId, Boolean forbid, Map<String, Object> raw) {}
