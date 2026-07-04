package com.thundax.kuzhambu.common.knowledge.model.base;

import java.util.List;
import java.util.Map;

public record KnowledgeBasePageResult(List<KnowledgeBaseResult> knowledgeBases, Map<String, Object> raw) {}
