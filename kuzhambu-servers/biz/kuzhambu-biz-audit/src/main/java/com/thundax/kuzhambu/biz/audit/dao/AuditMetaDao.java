package com.thundax.kuzhambu.biz.audit.dao;

import com.thundax.kuzhambu.biz.audit.entity.AuditMeta;
import com.thundax.kuzhambu.biz.audit.entity.valueobject.AuditMetaId;
import com.thundax.kuzhambu.biz.audit.entity.valueobject.AuditObjectRef;

public interface AuditMetaDao {

    AuditMeta getByObjectRef(AuditObjectRef objectRef);

    AuditMetaId insert(AuditMeta meta);

    int update(AuditMeta meta);
}
