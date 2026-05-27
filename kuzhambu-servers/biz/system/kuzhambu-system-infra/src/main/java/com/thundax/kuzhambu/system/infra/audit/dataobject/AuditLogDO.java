package com.thundax.kuzhambu.system.infra.audit.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_audit_log")
public class AuditLogDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long metaId;
    private String objectType;
    private String objectId;
    private Long version;
    private Long previousVersion;
    private String action;
    private String idempotencyKey;
    private String operatorType;
    private String operatorId;
    private String operatorName;
    private String source;
    private String requestId;
    private String traceId;
    private String remoteAddr;
    private String summary;
    private Integer snapshotSchemaVersion;
    private String beforeSnapshot;
    private String afterSnapshot;
    private String changedFields;
    private Date occurredAt;
}
