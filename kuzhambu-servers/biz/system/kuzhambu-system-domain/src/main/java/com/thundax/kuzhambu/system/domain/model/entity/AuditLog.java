package com.thundax.kuzhambu.system.domain.model.entity;

import com.thundax.kuzhambu.system.domain.model.enums.AuditAction;
import com.thundax.kuzhambu.system.domain.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.model.valueobject.AuditChangedField;
import com.thundax.kuzhambu.system.domain.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.model.valueobject.AuditMetaId;
import com.thundax.kuzhambu.system.domain.model.valueobject.AuditSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private AuditLogId id;
    private AuditMetaId metaId;
    private String objectType;
    private String objectId;
    private Long version;
    private Long previousVersion;
    private AuditAction action;
    private String idempotencyKey;
    private AuditOperatorType operatorType;
    private String operatorId;
    private String operatorName;
    private String source;
    private String requestId;
    private String traceId;
    private String remoteAddr;
    private String summary;
    private Integer snapshotSchemaVersion = 1;
    private AuditSnapshot beforeSnapshot;
    private AuditSnapshot afterSnapshot;
    private List<AuditChangedField> changedFields = new ArrayList<>();
    private Date occurredAt;
}
