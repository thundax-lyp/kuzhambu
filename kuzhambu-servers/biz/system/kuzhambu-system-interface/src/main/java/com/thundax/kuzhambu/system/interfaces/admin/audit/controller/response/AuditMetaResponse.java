package com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AuditMetaResponse", description = "审计元数据响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditMetaResponse implements Serializable {

    @Schema(name = "id", description = "ID")
    private String id;

    @Schema(name = "objectType", description = "对象类型")
    private String objectType;

    @Schema(name = "objectId", description = "对象ID")
    private String objectId;

    @Schema(name = "version", description = "审计版本")
    private Long version;

    @Schema(name = "lastAction", description = "最后动作")
    private String lastAction;

    @Schema(name = "lastOperatorName", description = "最后操作者")
    private String lastOperatorName;

    @Schema(name = "lastOperatedAt", description = "最后操作时间")
    private Date lastOperatedAt;

    @Schema(name = "createdAt", description = "审计创建时间")
    private Date createdAt;
}
