package com.thundax.kuzhambu.system.domain.audit.model.entity;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditMetaId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditMeta {

    private AuditMetaId id;
    private AuditObjectRef objectRef;
    private Long version;
    private AuditLogId lastLogId;
    private AuditAction lastAction;
    private AuditOperatorRef lastOperatorRef;
    private String lastOperatorName;
    private Instant lastOperatedAt;
    private AuditLogId createdLogId;
    private Instant createdAt;

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

    public AuditOperatorType getLastOperatorType() {
        return lastOperatorRef == null ? null : lastOperatorRef.getOperatorType();
    }

    public void setLastOperatorType(AuditOperatorType lastOperatorType) {
        ensureLastOperatorRef().setOperatorType(lastOperatorType);
    }

    public String getLastOperatorId() {
        return lastOperatorRef == null ? null : lastOperatorRef.getOperatorId();
    }

    public void setLastOperatorId(String lastOperatorId) {
        ensureLastOperatorRef().setOperatorId(lastOperatorId);
    }

    private AuditObjectRef ensureObjectRef() {
        if (objectRef == null) {
            objectRef = new AuditObjectRef();
        }
        return objectRef;
    }

    private AuditOperatorRef ensureLastOperatorRef() {
        if (lastOperatorRef == null) {
            lastOperatorRef = new AuditOperatorRef();
        }
        return lastOperatorRef;
    }
}
