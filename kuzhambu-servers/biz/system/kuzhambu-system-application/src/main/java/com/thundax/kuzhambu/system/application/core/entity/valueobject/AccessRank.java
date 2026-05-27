package com.thundax.kuzhambu.system.application.core.entity.valueobject;

import java.util.Objects;

/**
 * 访问等级。
 */
public final class AccessRank implements Comparable<AccessRank> {

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 9;

    private final int value;

    private AccessRank(int value) {
        this.value = value;
    }

    public static AccessRank of(Integer value) {
        if (value == null || value < MIN_VALUE) {
            return new AccessRank(MIN_VALUE);
        }
        if (value >= MAX_VALUE) {
            return new AccessRank(MAX_VALUE);
        }
        return new AccessRank(value);
    }

    public int value() {
        return value;
    }

    public boolean canAccess(Integer targetRank) {
        return value >= of(targetRank).value();
    }

    public boolean canAccess(AccessRank targetRank) {
        return value >= (targetRank == null ? MIN_VALUE : targetRank.value());
    }

    @Override
    public int compareTo(AccessRank that) {
        return Integer.compare(value, that == null ? MIN_VALUE : that.value());
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof AccessRank)) {
            return false;
        }
        AccessRank accessRank = (AccessRank) that;
        return value == accessRank.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
