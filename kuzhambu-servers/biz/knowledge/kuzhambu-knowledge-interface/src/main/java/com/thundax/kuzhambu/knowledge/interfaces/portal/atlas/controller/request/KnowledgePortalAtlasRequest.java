package com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "KnowledgePortalAtlasRequest", description = "知识门户 atlas 查询参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgePortalAtlasRequest {
    private String level;
    private String categoryCode;
    private Long entityId;
    private String knowledgeBase;
    private String keyword;
    private String tag;
    private String timeRange;
}
