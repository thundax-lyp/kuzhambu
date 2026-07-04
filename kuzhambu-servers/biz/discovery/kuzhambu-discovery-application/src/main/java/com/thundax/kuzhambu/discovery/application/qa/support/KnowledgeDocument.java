package com.thundax.kuzhambu.discovery.application.qa.support;

import java.util.Date;
import java.util.List;

public record KnowledgeDocument(Metadata metadata, Knowledge knowledge) {

    public record Metadata(
            String sourceId,
            String contentType,
            String contentId,
            String knowledgeBase,
            Integer currentVersionNo,
            String knowledgeRevision,
            String visibility,
            String status,
            String sourcePath,
            Date updatedAt) {}

    public record Knowledge(
            String title,
            String categoryPath,
            String summary,
            String body,
            String originalText,
            String translationText,
            String originalExcerpts,
            List<String> tags,
            List<QaPair> qaPairs) {}

    public record QaPair(String question, String answer) {}
}
