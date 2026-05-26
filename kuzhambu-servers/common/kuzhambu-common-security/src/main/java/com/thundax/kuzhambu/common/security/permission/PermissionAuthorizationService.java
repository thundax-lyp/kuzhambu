package com.thundax.kuzhambu.common.security.permission;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import java.util.Arrays;

public class PermissionAuthorizationService {

    private final PermissionMatcher permissionMatcher;

    public PermissionAuthorizationService(PermissionMatcher permissionMatcher) {
        this.permissionMatcher = permissionMatcher;
    }

    public boolean isPermitted(String permission) {
        return isPermittedAny(permission);
    }

    public boolean isPermittedAny(String... permissions) {
        KuzhambuSubject subject = KuzhambuContextHolder.currentSubject();
        if (subject == null || !subject.isAuthenticated() || permissions == null || permissions.length == 0) {
            return false;
        }
        return Arrays.stream(permissions)
                .filter(permission -> permission != null && !permission.trim().isEmpty())
                .anyMatch(permission -> permissionMatcher.matches(subject.getAuthorities(), permission));
    }
}
