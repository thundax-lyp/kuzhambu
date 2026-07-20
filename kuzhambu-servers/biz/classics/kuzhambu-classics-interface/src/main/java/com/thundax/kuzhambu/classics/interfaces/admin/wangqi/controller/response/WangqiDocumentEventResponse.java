package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "WangqiDocumentEventResponse", description = "王圻文档历史事件响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WangqiDocumentEventResponse implements Serializable {
    @Schema(name = "id", description = "事件ID")
    @JsonProperty("id")
    private Long id;

    @Schema(name = "documentId", description = "王圻文档ID")
    @JsonProperty("documentId")
    private Long documentId;

    @Schema(name = "title", description = "事件标题")
    @JsonProperty("title")
    private String title;

    @Schema(name = "occurredAt", description = "历史事件发生时间")
    @JsonProperty("occurredAt")
    private Date occurredAt;

    @Schema(name = "occurredLabel", description = "历史事件发生时间展示文本")
    @JsonProperty("occurredLabel")
    private String occurredLabel;

    @Schema(name = "summary", description = "事件摘要")
    @JsonProperty("summary")
    private String summary;
}
