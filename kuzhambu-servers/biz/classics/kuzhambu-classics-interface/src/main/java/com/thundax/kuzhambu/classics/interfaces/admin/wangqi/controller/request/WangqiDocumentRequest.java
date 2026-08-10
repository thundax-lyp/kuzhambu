package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "王圻文档管理请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WangqiDocumentRequest extends PageRequest {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("contentFormat")
    private String contentFormat;

    @JsonProperty("content")
    private String content;

    @JsonProperty("documentTime")
    private Instant documentTime;

    @JsonProperty("storageObjectId")
    private Long storageObjectId;

    @JsonProperty("keyword")
    private String keyword;

    @JsonProperty("sortDirection")
    private String sortDirection;
}
