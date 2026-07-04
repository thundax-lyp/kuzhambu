package com.thundax.kuzhambu.common.knowledge.model;

import java.util.List;
import java.util.Map;

public record KnowledgeCollectionPageResult(List<KnowledgeCollectionResult> collections, Map<String, Object> raw) {}
