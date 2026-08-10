package com.thundax.kuzhambu.knowledge.application.portal.query;

public record KnowledgePortalAtlasQuery(
        String level,
        String categoryCode,
        Long entityId,
        String knowledgeBase,
        String keyword,
        String tag,
        String timeRange) {}
