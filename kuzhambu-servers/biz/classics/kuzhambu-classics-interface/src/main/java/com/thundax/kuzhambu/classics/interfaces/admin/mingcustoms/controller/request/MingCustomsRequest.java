package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "明代习俗管理请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MingCustomsRequest extends PageRequest {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("category")
    private String category;

    @JsonProperty("chapter")
    private String chapter;

    @JsonProperty("section")
    private String section;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("contentFormat")
    private String contentFormat;

    @JsonProperty("content")
    private String content;

    @JsonProperty("originalExcerpts")
    private String originalExcerpts;

    @JsonProperty("keyword")
    private String keyword;

    @JsonProperty("tagName")
    private String tagName;

    @JsonProperty("tagId")
    private Long tagId;

    @JsonProperty("tagNameSnapshot")
    private String tagNameSnapshot;

    @JsonProperty("sortDirection")
    private String sortDirection;
}
