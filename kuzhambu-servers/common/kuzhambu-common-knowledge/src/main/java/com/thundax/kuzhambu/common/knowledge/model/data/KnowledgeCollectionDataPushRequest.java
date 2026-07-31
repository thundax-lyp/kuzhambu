package com.thundax.kuzhambu.common.knowledge.model.data;

import java.util.List;

public record KnowledgeCollectionDataPushRequest(String collectionId, List<KnowledgeCollectionDataPushItem> data) {}
