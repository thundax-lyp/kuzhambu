package com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "三才图会门户搜索请求")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SancaiPortalEntrySearchRequest {
    private Long id;
    private Long categoryId;
    private Long volumeId;
    private String keyword;
    private Integer pageNo;
    private Integer pageSize;
}
