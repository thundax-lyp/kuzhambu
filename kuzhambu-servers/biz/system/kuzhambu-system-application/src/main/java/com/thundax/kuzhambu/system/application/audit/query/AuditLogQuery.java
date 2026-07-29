package com.thundax.kuzhambu.system.application.audit.query;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogQuery {

    private AuditObjectRef objectRef;
    private AuditAction action;
    private AuditOperatorRef operatorRef;
    private String source;
    private String requestId;
    private Date beginDate;
    private Date endDate;

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
