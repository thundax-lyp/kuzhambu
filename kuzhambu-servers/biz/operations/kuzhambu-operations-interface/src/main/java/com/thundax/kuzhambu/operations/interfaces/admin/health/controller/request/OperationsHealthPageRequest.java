package com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.support.EpochMillisOrInstantDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsHealthPageRequest", description = "Operations 健康记录分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsHealthPageRequest extends PageRequest {

    @Schema(name = "component", description = "组件筛选")
    @JsonProperty(value = "component")
    private String component;

    @Schema(name = "healthStatus", description = "健康状态筛选")
    @JsonProperty(value = "healthStatus")
    private String healthStatus;

    @Schema(name = "probeSource", description = "探针来源筛选")
    @JsonProperty(value = "probeSource")
    private String probeSource;

    @Schema(name = "probeTarget", description = "探针目标筛选")
    @JsonProperty(value = "probeTarget")
    private String probeTarget;

    @Schema(name = "checkedAtStart", description = "检查时间起点")
    @JsonProperty(value = "checkedAtStart")
    @JsonDeserialize(using = EpochMillisOrInstantDeserializer.class)
    private Instant checkedAtStart;

    @Schema(name = "checkedAtEnd", description = "检查时间终点")
    @JsonProperty(value = "checkedAtEnd")
    @JsonDeserialize(using = EpochMillisOrInstantDeserializer.class)
    private Instant checkedAtEnd;
}
