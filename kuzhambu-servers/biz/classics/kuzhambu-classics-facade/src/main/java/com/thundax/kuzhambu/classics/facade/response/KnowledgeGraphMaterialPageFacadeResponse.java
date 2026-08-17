package com.thundax.kuzhambu.classics.facade.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeGraphMaterialPageFacadeResponse {
    private final int pageNo;
    private final int pageSize;
    private final long totalCount;
    private final List<Source> records;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Source {
        private final String contentType;
        private final String contentId;
        private final String title;
        private final String categoryCode;
        private final String categoryName;
        private final String volumeCode;
        private final String volumeName;
        private final boolean graphable;
    }
}
