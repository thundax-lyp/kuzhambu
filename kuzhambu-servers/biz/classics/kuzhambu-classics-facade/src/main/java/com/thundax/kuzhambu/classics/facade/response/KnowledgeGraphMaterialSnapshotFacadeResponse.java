package com.thundax.kuzhambu.classics.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeGraphMaterialSnapshotFacadeResponse {
    private final KnowledgeGraphMaterialPageFacadeResponse.Source source;
    private final String contentSnapshot;
}
