package com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response;

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
public class ClassicsContentResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("contentId")
    private Long contentId;

    @JsonProperty("tagNameSnapshot")
    private String tagNameSnapshot;

    @JsonProperty("question")
    private String question;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("status")
    private String status;
}
