package com.thundax.kuzhambu.system.domain.audit.model.entity;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditChangedField;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditMetaId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import java.time.Instant;
import java.util.ArrayList;
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
    private AuditObjectRef objectRef;
    private Long version;
    private Long previousVersion;
    private AuditAction action;
    private String idempotencyKey;
    private AuditOperatorRef operatorRef;
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
    private Instant occurredAt;

    public String getObjectType() {
        return objectRef == null ? null : objectRef.getObjectType();
    }

    public void setObjectType(String objectType) {
        ensureObjectRef().setObjectType(objectType);
    }

    public String getObjectId() {
        return objectRef == null ? null : objectRef.getObjectId();
    }

    public void setObjectId(String objectId) {
        ensureObjectRef().setObjectId(objectId);
    }

    public AuditOperatorType getOperatorType() {
        return operatorRef == null ? null : operatorRef.getOperatorType();
    }

    public void setOperatorType(AuditOperatorType operatorType) {
        ensureOperatorRef().setOperatorType(operatorType);
    }

    public String getOperatorId() {
        return operatorRef == null ? null : operatorRef.getOperatorId();
    }

    public void setOperatorId(String operatorId) {
        ensureOperatorRef().setOperatorId(operatorId);
    }

    private AuditObjectRef ensureObjectRef() {
        if (objectRef == null) {
            objectRef = new AuditObjectRef();
        }
        return objectRef;
    }

    private AuditOperatorRef ensureOperatorRef() {
        if (operatorRef == null) {
            operatorRef = new AuditOperatorRef();
        }
        return operatorRef;
    }
}
