package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import com.thundax.kuzhambu.system.application.auth.query.PermissionQuery;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalPermissionApplicationService;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.PermissionCode;
import com.thundax.kuzhambu.system.interfaces.admin.auth.assembler.AuthInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.PermissionService;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PrincipalPermissionApplicationService permissionApplicationService;

    public PermissionServiceImpl(PrincipalPermissionApplicationService permissionApplicationService) {
        this.permissionApplicationService = permissionApplicationService;
    }

    @Override
    public Set<String> createPermissions(String token, String userId) {
        PrincipalAccessTokenCode tokenCode = accessTokenCode(token);
        return toStringSet(permissionApplicationService.createPermissions(
                tokenCode == null
                        ? AuthInterfaceAssembler.emptyCreatePermissionsCommand()
                        : AuthInterfaceAssembler.toCreatePermissionsCommand(tokenCode, UserIdCodec.toDomain(userId))));
    }

    @Override
    public Set<String> getPermissions(String token) {
        return toStringSet(permissionApplicationService.getPermissions(permissionQuery(token)));
    }

    @Override
    public boolean isPermitted(String token, String permission) {
        PrincipalAccessTokenCode tokenCode = accessTokenCode(token);
        PermissionCode permissionCode = PermissionCode.ofNullable(permission);
        return permissionApplicationService.isPermitted(
                tokenCode == null || permissionCode == null
                        ? AuthInterfaceAssembler.emptyPermissionQuery()
                        : AuthInterfaceAssembler.toPermissionQuery(tokenCode, permissionCode));
    }

    private PermissionQuery permissionQuery(String token) {
        PrincipalAccessTokenCode tokenCode = accessTokenCode(token);
        return tokenCode == null
                ? AuthInterfaceAssembler.emptyPermissionQuery()
                : AuthInterfaceAssembler.toPermissionQuery(tokenCode);
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
