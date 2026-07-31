package com.thundax.kuzhambu.common.knowledge.model.data;

import java.util.Map;

public record KnowledgeCollectionDataPushResult(int insertLen, Map<String, Object> raw) {}
