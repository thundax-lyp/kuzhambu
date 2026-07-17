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
public class WangqiDocumentEventResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("documentId")
    private Long documentId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("occurredAt")
    private Date occurredAt;

    @JsonProperty("occurredLabel")
    private String occurredLabel;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("priority")
    private Integer priority;
}
