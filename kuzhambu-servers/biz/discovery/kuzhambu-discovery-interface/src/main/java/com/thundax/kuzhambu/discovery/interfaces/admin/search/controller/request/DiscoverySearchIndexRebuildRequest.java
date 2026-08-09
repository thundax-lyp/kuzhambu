package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DiscoverySearchIndexRebuildRequest", description = "Discovery 搜索索引重建请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchIndexRebuildRequest {

    @NotNull
    @Schema(name = "confirm", description = "确认执行全量重建", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean confirm;

    public DiscoverySearchIndexRebuildRequest() {}

    public DiscoverySearchIndexRebuildRequest(Boolean confirm) {
        this.confirm = confirm;
    }
}
