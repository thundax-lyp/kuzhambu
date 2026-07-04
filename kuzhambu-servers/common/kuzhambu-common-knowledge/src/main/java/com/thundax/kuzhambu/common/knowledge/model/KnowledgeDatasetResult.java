package com.thundax.kuzhambu.common.knowledge.model;

import java.util.Map;

public record KnowledgeDatasetResult(String datasetId, String name, Map<String, Object> raw) {}
