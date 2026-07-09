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
public class MingCustomsTagCloudItemResponse implements Serializable {

    @JsonProperty("tagId")
    private Long tagId;

    @JsonProperty("tagNameSnapshot")
    private String tagNameSnapshot;

    @JsonProperty("count")
    private Long count;
}
