package com.thundax.kuzhambu.operations.domain.report.codec;

import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;

public final class ReportIdCodec {

    private ReportIdCodec() {}

    public static ReportId toDomain(Long value) {
        return value == null ? null : new ReportId(value);
    }

    public static Long toValue(ReportId id) {
        return id == null ? null : id.value();
    }
}
