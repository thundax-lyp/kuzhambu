package com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KnowledgePortalAtlasRequest {
    private String level;
    private String categoryCode;
    private Long entityId;
    private String knowledgeBase;
    private String keyword;
    private String tag;
    private String timeRange;
}
