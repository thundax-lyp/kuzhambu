package com.thundax.kuzhambu.classics.facade.dto;

import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsQaKnowledgeFacadeDto {

    private final String sourceId;
    private final String contentType;
    private final String contentId;
    private final String knowledgeBase;
    private final Integer currentVersionNo;
    private final String knowledgeRevision;
    private final String visibility;
    private final String status;
    private final String sourcePath;
    private final Instant updatedAt;
    private final String title;
    private final String categoryPath;
    private final String summary;
    private final String body;
    private final String originalText;
    private final String translationText;
    private final String originalExcerpts;
    private final List<String> tags;
    private final List<QaPair> qaPairs;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QaPair {

        private final String question;
        private final String answer;
    }
}
