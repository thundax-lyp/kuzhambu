package com.thundax.kuzhambu.common.security.permission;

import java.util.Collection;

public class PrefixPermissionMatcher implements PermissionMatcher {

    private static final String PERMISSION_SEPARATOR = ":";

    @Override
    public boolean matches(Collection<String> permissions, String requiredPermission) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        for (String permission : permissions) {
            if (matches(permission, requiredPermission)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean matches(String permission, String requiredPermission) {
        if (isBlank(permission) || isBlank(requiredPermission)) {
            return false;
        }

        if (permission.equals(requiredPermission)) {
            return true;
        }

        return requiredPermission.startsWith(permission + PERMISSION_SEPARATOR);
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
