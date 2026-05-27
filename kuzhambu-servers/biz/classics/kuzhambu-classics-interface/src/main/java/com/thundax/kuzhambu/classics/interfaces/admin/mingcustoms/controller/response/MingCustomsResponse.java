package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response;

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
public class MingCustomsResponse implements Serializable {
    @JsonProperty("id") private Long id;
    @JsonProperty("title") private String title;
    @JsonProperty("category") private String category;
    @JsonProperty("chapter") private String chapter;
    @JsonProperty("section") private String section;
    @JsonProperty("summary") private String summary;
    @JsonProperty("contentFormat") private String contentFormat;
    @JsonProperty("content") private String content;
    @JsonProperty("originalExcerpts") private String originalExcerpts;
    @JsonProperty("visibility") private String visibility;
}
