package com.thundax.kuzhambu.common.knowledge.model;

import java.util.List;
import java.util.Map;

public record KnowledgeDatasetPageResult(List<KnowledgeDatasetResult> datasets, Map<String, Object> raw) {}
