package com.thundax.kuzhambu.common.knowledge.model;

public record KnowledgeCollectionListRequest(String datasetId, Integer pageNum, Integer pageSize, String searchText) {}
