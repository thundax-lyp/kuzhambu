package com.thundax.kuzhambu.system.interfaces.admin.core.controller.request;

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
@Schema(name = "RoleSortRequest", description = "角色排序请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleSortRequest {

    @Schema(name = "orderedIds", description = "排序实体ID序列")
    @JsonProperty(value = "orderedIds")
    @NotEmpty(message = "orderedIds不能为空")
    private List<String> orderedIds;

    @Schema(name = "sortDirection", description = "排序方向")
    @JsonProperty(value = "sortDirection")
    private SortDirection sortDirection = SortDirection.ASC;
}
