package com.thundax.kuzhambu.biz.core.codec;

import com.thundax.kuzhambu.biz.core.entity.valueobject.AccessRank;

public final class AccessRankCodec {

    private AccessRankCodec() {}

    public static AccessRank toDomain(Integer value) {
        return AccessRank.of(value);
    }

    public static Integer toValue(AccessRank rank) {
        return rank == null ? AccessRank.MIN_VALUE : rank.value();
    }
}
