package com.thundax.kuzhambu.discovery.application.search.result;

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
public class SearchSourceContent {
    private String contentDomain;
    private String contentType;
    private String contentId;
    private String knowledgeBase;
    private String categoryCode;
    private String categoryName;
    private String title;
    private String summary;
    private List<String> textSegments;
    private List<String> tagNames;
    private Integer currentVersionNo;
    private Instant publishedAt;
    private Instant updatedAt;

    public SearchSourceContent(
            String contentDomain,
            String contentType,
            String contentId,
            String knowledgeBase,
            String categoryCode,
            String categoryName,
            String title,
            String summary,
            List<String> textSegments,
            List<String> tagNames,
            Instant publishedAt,
            Instant updatedAt) {
        this(
                contentDomain,
                contentType,
                contentId,
                knowledgeBase,
                categoryCode,
                categoryName,
                title,
                summary,
                textSegments,
                tagNames,
                null,
                publishedAt,
                updatedAt);
    }
}
