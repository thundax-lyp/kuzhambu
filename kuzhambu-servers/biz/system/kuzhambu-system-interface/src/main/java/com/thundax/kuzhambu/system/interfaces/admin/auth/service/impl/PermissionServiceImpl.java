package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import com.thundax.kuzhambu.system.application.auth.command.CreatePermissionsCommand;
import com.thundax.kuzhambu.system.application.auth.query.PermissionQuery;
import com.thundax.kuzhambu.system.application.auth.service.PermissionApplicationService;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.PermissionCode;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.PermissionService;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionApplicationService permissionApplicationService;

    public PermissionServiceImpl(PermissionApplicationService permissionApplicationService) {
        this.permissionApplicationService = permissionApplicationService;
    }

    @Override
    public Set<String> createPermissions(String token, String userId) {
        return toStringSet(permissionApplicationService.createPermissions(
                new CreatePermissionsCommand(accessTokenCode(token), UserIdCodec.toDomain(userId))));
    }

    @Override
    public Set<String> getPermissions(String token) {
        return toStringSet(permissionApplicationService.getPermissions(permissionQuery(token)));
    }

    @Override
    public boolean isPermitted(String token, String permission) {
        return permissionApplicationService.isPermitted(
                new PermissionQuery(accessTokenCode(token), PermissionCode.ofNullable(permission)));
    }

    private PermissionQuery permissionQuery(String token) {
        return new PermissionQuery(accessTokenCode(token), null);
    }

    private PrincipalAccessTokenCode accessTokenCode(String token) {
        return PrincipalAccessTokenCode.ofNullable(token);
    }

    private Set<String> toStringSet(Set<PermissionCode> permissions) {
        if (permissions == null) {
            return null;
        }
        Set<String> values = new HashSet<>();
        for (PermissionCode permission : permissions) {
            if (permission != null) {
                values.add(permission.asString());
            }
        }
        return values;
    }
}
