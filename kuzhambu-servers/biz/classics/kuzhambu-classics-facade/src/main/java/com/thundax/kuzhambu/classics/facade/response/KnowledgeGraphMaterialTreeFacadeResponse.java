package com.thundax.kuzhambu.classics.facade.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeGraphMaterialTreeFacadeResponse {
    private final List<Node> nodes;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Node {
        private final String id;
        private final String parentId;
        private final String title;
        private final String nodeType;
        private final String contentType;
        private final String categoryCode;
        private final String volumeCode;
        private final boolean leaf;
    }
}
