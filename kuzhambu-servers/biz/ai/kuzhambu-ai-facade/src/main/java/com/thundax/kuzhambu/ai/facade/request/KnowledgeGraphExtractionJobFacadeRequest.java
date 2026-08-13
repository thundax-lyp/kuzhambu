package com.thundax.kuzhambu.ai.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeGraphExtractionJobFacadeRequest {

    private final String scope;
    private final String contentType;
    private final Long contentId;
    private final String contentTitle;
    private final String contentSnapshotJson;
    private final Long requestedBy;
}
