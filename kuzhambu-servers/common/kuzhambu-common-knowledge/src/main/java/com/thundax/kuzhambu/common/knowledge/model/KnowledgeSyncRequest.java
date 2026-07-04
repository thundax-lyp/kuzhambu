package com.thundax.kuzhambu.common.knowledge.model;

import java.util.Map;

public record KnowledgeSyncRequest(String datasetId, String collectionId, Map<String, Object> options) {}
