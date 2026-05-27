package com.thundax.kuzhambu.system.domain.core.valueobject;

public final class PermissionCode {

    public static final String SEPARATOR = ",";
    public static final String USER = "user";
    public static final String ADMIN = "admin";
    public static final String SUPER = "super";

    private PermissionCode() {}

    public static boolean isBuiltIn(String permission) {
        return USER.equals(permission) || ADMIN.equals(permission) || SUPER.equals(permission);
    }
}
