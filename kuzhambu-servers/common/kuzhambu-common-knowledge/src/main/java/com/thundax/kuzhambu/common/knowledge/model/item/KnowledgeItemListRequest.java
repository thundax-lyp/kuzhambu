package com.thundax.kuzhambu.common.knowledge.model.item;

public record KnowledgeItemListRequest(
        String knowledgeBaseName, Integer pageNum, Integer pageSize, String searchText) {}
