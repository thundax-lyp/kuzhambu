package com.thundax.kuzhambu.knowledge.interfaces.portal.quality.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "KnowledgePortalQualityRequest", description = "知识门户 quality 查询参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgePortalQualityRequest {
    private String date;
    private String range;
    private String knowledgeBase;
}
