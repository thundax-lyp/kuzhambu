package com.thundax.kuzhambu.system.domain.core.model.valueobject;

import java.util.Objects;

public final class PermissionCode {

    public static final String SEPARATOR = ",";
    public static final String USER = "user";
    public static final String ADMIN = "admin";
    public static final String SUPER = "super";

    private final String value;

    private PermissionCode(String value) {
        this.value = value;
    }

    public static PermissionCode of(String value) {
        return new PermissionCode(value);
    }

    public static PermissionCode ofNullable(String value) {
        return value == null ? null : of(value);
    }

    public String value() {
        return value;
    }

    public String asString() {
        return value;
    }

    public static boolean isBuiltIn(String permission) {
        return USER.equals(permission) || ADMIN.equals(permission) || SUPER.equals(permission);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PermissionCode)) {
            return false;
        }
        PermissionCode that = (PermissionCode) other;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
