package com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response;

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
@Schema(name = "AuditSnapshotResponse", description = "审计快照响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditSnapshotResponse implements Serializable {

    @Schema(name = "objectType", description = "对象类型")
    private String objectType;

    @Schema(name = "objectId", description = "对象ID")
    private String objectId;

    @Schema(name = "displayName", description = "展示名")
    private String displayName;

    @Schema(name = "fields", description = "字段")
    private List<AuditSnapshotFieldResponse> fields = new ArrayList<>();
}
