package com.thundax.kuzhambu.classics.facade.request;

import java.util.List;
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
    private final List<SourceRef> contentRefs;
    private final List<SourceRef> excludedContentRefs;
    private final Integer pageNo;
    private final Integer pageSize;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SourceRef {
        private final String contentType;
        private final String contentId;
    }
}
