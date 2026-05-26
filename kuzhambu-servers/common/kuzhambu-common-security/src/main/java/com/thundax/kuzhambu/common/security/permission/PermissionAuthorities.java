package com.thundax.kuzhambu.common.security.permission;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;

public final class PermissionAuthorities {

    private PermissionAuthorities() {}

    public static Set<String> toPermissions(Collection<? extends GrantedAuthority> authorities) {
        Set<String> permissions = new LinkedHashSet<>();
        if (authorities == null || authorities.isEmpty()) {
            return permissions;
        }

        for (GrantedAuthority authority : authorities) {
            if (authority != null
                    && authority.getAuthority() != null
                    && !authority.getAuthority().trim().isEmpty()) {
                permissions.add(authority.getAuthority());
            }
        }

        return permissions;
    }
}
