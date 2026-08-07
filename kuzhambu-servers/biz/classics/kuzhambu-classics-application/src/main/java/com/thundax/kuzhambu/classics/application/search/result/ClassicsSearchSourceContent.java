package com.thundax.kuzhambu.classics.application.search.result;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsSearchSourceContent {
    private String contentType;
    private String contentId;
    private String knowledgeBase;
    private String categoryCode;
    private String categoryName;
    private String volumeCode;
    private String volumeName;
    private String title;
    private String summary;
    private List<String> textSegments;
    private List<String> tagNames;
    private String status;
    private String visibility;
    private Integer currentVersionNo;
    private Instant publishedAt;
    private Instant updatedAt;

    public ClassicsSearchSourceContent(
            String contentType,
            String contentId,
            String knowledgeBase,
            String categoryCode,
            String categoryName,
            String title,
            String summary,
            List<String> textSegments,
            List<String> tagNames,
            String status,
            String visibility,
            Integer currentVersionNo,
            Instant publishedAt,
            Instant updatedAt) {
        this(
                contentType,
                contentId,
                knowledgeBase,
                categoryCode,
                categoryName,
                null,
                null,
                title,
                summary,
                textSegments,
                tagNames,
                status,
                visibility,
                currentVersionNo,
                publishedAt,
                updatedAt);
    }

    public ClassicsSearchSourceContent(
            String contentType,
            String contentId,
            String knowledgeBase,
            String categoryCode,
            String categoryName,
            String volumeCode,
            String volumeName,
            String title,
            String summary,
            List<String> textSegments,
            List<String> tagNames,
            String status,
            String visibility,
            Instant publishedAt,
            Instant updatedAt) {
        this(
                contentType,
                contentId,
                knowledgeBase,
                categoryCode,
                categoryName,
                volumeCode,
                volumeName,
                title,
                summary,
                textSegments,
                tagNames,
                status,
                visibility,
                null,
                publishedAt,
                updatedAt);
    }
}
