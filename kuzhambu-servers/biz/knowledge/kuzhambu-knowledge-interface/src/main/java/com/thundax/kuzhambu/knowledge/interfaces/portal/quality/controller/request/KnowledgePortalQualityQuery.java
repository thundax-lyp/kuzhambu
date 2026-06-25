package com.thundax.kuzhambu.knowledge.interfaces.portal.quality.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KnowledgePortalQualityQuery {
    private String date;
    private String range;
    private String knowledgeBase;
}
