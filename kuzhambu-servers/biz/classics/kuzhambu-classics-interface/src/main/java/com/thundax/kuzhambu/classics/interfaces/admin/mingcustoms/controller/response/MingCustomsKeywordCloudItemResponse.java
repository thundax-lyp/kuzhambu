package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "明代习俗关键词云项响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MingCustomsKeywordCloudItemResponse implements Serializable {

    @JsonProperty("keyword")
    private String keyword;

    @JsonProperty("count")
    private Long count;
}
