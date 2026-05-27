package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WangqiDocumentRequest extends PageRequest {
    @JsonProperty("id") private Long id;
    @JsonProperty("title") private String title;
    @JsonProperty("summary") private String summary;
    @JsonProperty("contentFormat") private String contentFormat;
    @JsonProperty("content") private String content;
    @JsonProperty("documentTime") private LocalDateTime documentTime;
    @JsonProperty("storageObjectId") private Long storageObjectId;
    @JsonProperty("visibility") private String visibility;
    @JsonProperty("keyword") private String keyword;
    @JsonProperty("sortDirection") private String sortDirection;
}
