package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
    private Date documentTime;

    @JsonProperty("storageObjectId")
    private Long storageObjectId;

    @JsonProperty("visibility")
    private String visibility;
}
