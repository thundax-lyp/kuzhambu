package com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsContentRequest extends PageRequest {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("contentId")
    private Long contentId;

    @JsonProperty("tagId")
    private Long tagId;

    @JsonProperty("tagNameSnapshot")
    private String tagNameSnapshot;

    @JsonProperty("source")
    private String source;

    @JsonProperty("status")
    private String status;

    @JsonProperty("question")
    private String question;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("exportKind")
    private String exportKind;

    @JsonProperty("exportFormat")
    private String exportFormat;

    @JsonProperty("scopeType")
    private String scopeType;

    @JsonProperty("scopeJson")
    private String scopeJson;

    @JsonProperty("expiresAt")
    private Date expiresAt;
}
