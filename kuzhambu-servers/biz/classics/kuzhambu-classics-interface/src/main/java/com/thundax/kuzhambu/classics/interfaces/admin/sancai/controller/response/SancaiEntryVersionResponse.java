package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SancaiEntryVersionResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("contentId")
    private Long contentId;

    @JsonProperty("versionNo")
    private Integer versionNo;

    @JsonProperty("versionedAt")
    private Instant versionedAt;

    @JsonProperty("snapshotJson")
    private String snapshotJson;

    @JsonProperty("changeType")
    private String changeType;

    @JsonProperty("changeSummary")
    private String changeSummary;
}
