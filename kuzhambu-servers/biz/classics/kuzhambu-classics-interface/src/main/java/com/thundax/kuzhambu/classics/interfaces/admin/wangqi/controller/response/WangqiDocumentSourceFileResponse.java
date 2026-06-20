package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WangqiDocumentSourceFileResponse implements Serializable {
    @JsonProperty("documentId")
    private Long documentId;

    @JsonProperty("storageObjectId")
    private Long storageObjectId;

    @JsonProperty("originalFilename")
    private String originalFilename;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("size")
    private Long size;

    @JsonProperty("contentUrl")
    private String contentUrl;
}
