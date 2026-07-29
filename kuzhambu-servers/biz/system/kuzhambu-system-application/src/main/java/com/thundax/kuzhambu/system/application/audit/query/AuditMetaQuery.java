package com.thundax.kuzhambu.system.application.audit.query;

import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditMetaQuery {

    private AuditObjectRef objectRef;

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

    private AuditObjectRef ensureObjectRef() {
        if (objectRef == null) {
            objectRef = new AuditObjectRef();
        }
        return objectRef;
    }
}
