package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "MingCustomsKeywordSortRequest", description = "明代习俗关键词排序请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MingCustomsKeywordSortRequest {

    @Schema(name = "orderedIds", description = "排序实体ID序列")
    @JsonProperty("orderedIds")
    @NotEmpty(message = "orderedIds不能为空")
    private List<Long> orderedIds;
}
