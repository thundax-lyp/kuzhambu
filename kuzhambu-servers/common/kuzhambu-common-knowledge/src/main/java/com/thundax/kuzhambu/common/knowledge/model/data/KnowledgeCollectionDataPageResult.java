package com.thundax.kuzhambu.common.knowledge.model.data;

import java.util.List;
import java.util.Map;

public record KnowledgeCollectionDataPageResult(
        int total, List<KnowledgeCollectionDataResult> items, Map<String, Object> raw) {}
