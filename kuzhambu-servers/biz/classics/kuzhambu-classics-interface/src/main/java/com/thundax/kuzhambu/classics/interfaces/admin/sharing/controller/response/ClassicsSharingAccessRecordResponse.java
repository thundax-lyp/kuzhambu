package com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response;

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
public class ClassicsSharingAccessRecordResponse implements Serializable {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("shareLinkId")
    private Long shareLinkId;

    @JsonProperty("shareTargetId")
    private Long shareTargetId;

    @JsonProperty("accessedAt")
    private Instant accessedAt;

    @JsonProperty("accessResult")
    private String accessResult;

    @JsonProperty("clientSnapshot")
    private String clientSnapshot;
}
