package com.thundax.kuzhambu.interfaces.admin.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AuditOptionsResponse", description = "审计筛选选项响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditOptionsResponse implements Serializable {

    @Schema(name = "objectTypes", description = "对象类型")
    private List<AuditOptionResponse> objectTypes = new ArrayList<>();

    @Schema(name = "actions", description = "审计动作")
    private List<AuditOptionResponse> actions = new ArrayList<>();

    @Schema(name = "operatorTypes", description = "操作者类型")
    private List<AuditOptionResponse> operatorTypes = new ArrayList<>();
}
