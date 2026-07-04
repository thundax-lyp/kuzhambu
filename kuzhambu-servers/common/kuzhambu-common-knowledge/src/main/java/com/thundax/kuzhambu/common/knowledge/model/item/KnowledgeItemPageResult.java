package com.thundax.kuzhambu.common.knowledge.model.item;

import java.util.List;
import java.util.Map;

public record KnowledgeItemPageResult(List<KnowledgeItemResult> knowledgeItems, Map<String, Object> raw) {}
