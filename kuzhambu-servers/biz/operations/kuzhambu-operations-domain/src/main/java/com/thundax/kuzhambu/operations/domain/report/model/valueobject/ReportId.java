package com.thundax.kuzhambu.operations.domain.report.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class ReportId extends BaseLongId {

    private ReportId(Long value) {
        super(value);
    }

    public static ReportId of(Long value) {
        return new ReportId(value);
    }

    public static ReportId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
