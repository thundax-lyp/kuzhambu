package com.thundax.kuzhambu.classics.facade.dto;

import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ClassicsPublicContentFacadeDto {

    private final String contentType;
    private final String contentId;
    private final String knowledgeBase;
    private final String categoryCode;
    private final String categoryName;
    private final String title;
    private final String summary;
    private final List<String> textSegments;
    private final List<String> tagNames;
    private final String status;
    private final String visibility;
    private final Integer currentVersionNo;
    private final Date publishedAt;
    private final Date updatedAt;

    @Builder
    private ClassicsPublicContentFacadeDto(
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
            Date publishedAt,
            Date updatedAt) {
        this.contentType = contentType;
        this.contentId = contentId;
        this.knowledgeBase = knowledgeBase;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.title = title;
        this.summary = summary;
        this.textSegments = textSegments;
        this.tagNames = tagNames;
        this.status = status;
        this.visibility = visibility;
        this.currentVersionNo = currentVersionNo;
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
    }
}
