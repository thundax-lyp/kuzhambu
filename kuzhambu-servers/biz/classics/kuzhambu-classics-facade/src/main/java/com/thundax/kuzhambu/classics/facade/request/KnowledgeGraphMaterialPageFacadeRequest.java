package com.thundax.kuzhambu.classics.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeGraphMaterialPageFacadeRequest {
    private final String subjectId;
    private final String keyword;
    private final String contentType;
    private final String categoryCode;
    private final String volumeCode;
    private final Integer pageNo;
    private final Integer pageSize;
}
