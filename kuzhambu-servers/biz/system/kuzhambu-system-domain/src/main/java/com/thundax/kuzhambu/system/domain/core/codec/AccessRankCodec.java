package com.thundax.kuzhambu.system.domain.core.codec;

import com.thundax.kuzhambu.system.domain.core.valueobject.AccessRank;

public final class AccessRankCodec {

    private AccessRankCodec() {}

    public static AccessRank toDomain(Integer value) {
        return AccessRank.of(value);
    }

    public static Integer toValue(AccessRank rank) {
        return rank == null ? AccessRank.MIN_VALUE : rank.value();
    }
}
