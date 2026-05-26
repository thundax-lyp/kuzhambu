package com.thundax.kuzhambu.biz.core.entity.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class DepartmentId extends BaseLongId {

    private DepartmentId(Long value) {
        super(value);
    }

    public static DepartmentId of(Long value) {
        return new DepartmentId(value);
    }

    public static DepartmentId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
