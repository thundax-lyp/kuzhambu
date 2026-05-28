package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "SancaiCategorySortRequest", description = "三才图会门类排序请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SancaiCategorySortRequest {

    @Schema(name = "orderedIds", description = "排序实体ID序列")
    @JsonProperty("orderedIds")
    @NotEmpty(message = "orderedIds不能为空")
    private List<Long> orderedIds;

    @Schema(name = "sortDirection", description = "排序方向")
    @JsonProperty("sortDirection")
    private SortDirection sortDirection = SortDirection.ASC;
}
