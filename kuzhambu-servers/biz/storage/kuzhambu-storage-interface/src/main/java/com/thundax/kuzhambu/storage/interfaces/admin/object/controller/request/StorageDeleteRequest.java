package com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request;

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
@Schema(name = "StorageDeleteRequest", description = "存储对象删除请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageDeleteRequest {

    @Schema(name = "ids", description = "存储对象ID集合")
    @JsonProperty("ids")
    @NotEmpty(message = "ids不能为空")
    private List<Long> ids;
}
