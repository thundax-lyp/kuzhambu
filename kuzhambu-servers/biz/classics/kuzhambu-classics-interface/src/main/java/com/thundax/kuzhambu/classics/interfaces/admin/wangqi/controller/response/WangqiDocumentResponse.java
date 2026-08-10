package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "王圻文档响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WangqiDocumentResponse implements Serializable {
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

    @JsonProperty("lifecycleStatus")
    private String lifecycleStatus;

    @JsonProperty("transitionStatus")
    private String transitionStatus;

    @JsonProperty("currentPublicationJobId")
    private Long currentPublicationJobId;

    @JsonProperty("events")
    private List<WangqiDocumentEventResponse> events;
}
