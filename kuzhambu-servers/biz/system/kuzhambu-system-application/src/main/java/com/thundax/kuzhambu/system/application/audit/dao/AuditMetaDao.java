package com.thundax.kuzhambu.system.application.audit.dao;

import com.thundax.kuzhambu.system.application.audit.entity.AuditMeta;
import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditMetaId;
import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditObjectRef;

public interface AuditMetaDao {

    AuditMeta getByObjectRef(AuditObjectRef objectRef);

    AuditMetaId insert(AuditMeta meta);

    int update(AuditMeta meta);
}
