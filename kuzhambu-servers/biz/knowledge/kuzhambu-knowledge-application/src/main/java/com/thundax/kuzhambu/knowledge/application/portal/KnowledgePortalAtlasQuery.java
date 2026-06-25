package com.thundax.kuzhambu.knowledge.application.portal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePortalAtlasQuery {
    private String level;
    private String categoryCode;
    private Long entityId;
    private String knowledgeBase;
    private String keyword;
    private String tag;
    private String timeRange;
}
