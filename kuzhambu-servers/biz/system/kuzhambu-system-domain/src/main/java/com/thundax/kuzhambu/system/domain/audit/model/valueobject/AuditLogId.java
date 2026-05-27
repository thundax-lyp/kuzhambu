package com.thundax.kuzhambu.system.domain.audit.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class AuditLogId extends BaseLongId {

    private AuditLogId(Long value) {
        super(value);
    }

    public static AuditLogId of(Long value) {
        return new AuditLogId(value);
    }

    public static AuditLogId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
