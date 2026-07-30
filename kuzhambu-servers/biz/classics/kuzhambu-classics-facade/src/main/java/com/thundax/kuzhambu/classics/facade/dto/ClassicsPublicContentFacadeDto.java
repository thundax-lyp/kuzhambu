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
    private final Instant publishedAt;
    private final Instant updatedAt;
}
