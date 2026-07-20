package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import com.thundax.kuzhambu.system.application.auth.service.PermissionApplicationService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.PermissionService;
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
        return permissionApplicationService.createPermissions(token, userId);
    }

    @Override
    public Set<String> getPermissions(String token) {
        return permissionApplicationService.getPermissions(token);
    }

    @Override
    public boolean isPermitted(String token, String permission) {
        return permissionApplicationService.isPermitted(token, permission);
    }
}
