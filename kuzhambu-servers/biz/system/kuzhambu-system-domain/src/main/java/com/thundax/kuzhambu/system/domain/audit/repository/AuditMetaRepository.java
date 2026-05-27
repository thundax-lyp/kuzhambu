package com.thundax.kuzhambu.system.domain.audit.repository;

import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditMeta;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditMetaId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;

public interface AuditMetaRepository {

    AuditMeta getByObjectRef(AuditObjectRef objectRef);

    AuditMetaId insert(AuditMeta meta);

    int update(AuditMeta meta);
}
