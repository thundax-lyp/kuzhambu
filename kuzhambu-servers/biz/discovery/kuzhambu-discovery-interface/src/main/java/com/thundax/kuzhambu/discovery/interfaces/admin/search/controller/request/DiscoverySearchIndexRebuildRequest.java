package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DiscoverySearchIndexRebuildRequest", description = "Discovery 搜索索引重建请求")
public class DiscoverySearchIndexRebuildRequest {

    @NotNull
    @Schema(name = "confirm", description = "确认执行全量重建", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean confirm;
}
