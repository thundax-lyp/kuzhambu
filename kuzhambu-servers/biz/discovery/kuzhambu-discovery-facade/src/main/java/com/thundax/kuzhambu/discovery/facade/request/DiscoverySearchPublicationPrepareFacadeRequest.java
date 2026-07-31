package com.thundax.kuzhambu.discovery.facade.request;

import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscoverySearchPublicationPrepareFacadeRequest {

    private final String sourceId;
    private final String contentType;
    private final String contentId;
    private final String contentVersionId;
    private final Integer contentVersionNo;
    private final String title;
    private final String summary;
    private final String categoryId;
    private final String categoryName;
    private final String volumeId;
    private final String volumeTitle;
    private final List<String> textSegments;
    private final List<String> tagNames;
    private final Instant contentUpdatedAt;
}
