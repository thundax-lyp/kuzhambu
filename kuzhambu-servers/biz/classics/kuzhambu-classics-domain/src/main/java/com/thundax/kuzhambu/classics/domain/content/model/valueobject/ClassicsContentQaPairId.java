package com.thundax.kuzhambu.classics.domain.content.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class ClassicsContentQaPairId extends BaseLongId {

    private ClassicsContentQaPairId(Long value) {
        super(value);
    }

    public static ClassicsContentQaPairId of(Long value) {
        return new ClassicsContentQaPairId(value);
    }

    public static ClassicsContentQaPairId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
