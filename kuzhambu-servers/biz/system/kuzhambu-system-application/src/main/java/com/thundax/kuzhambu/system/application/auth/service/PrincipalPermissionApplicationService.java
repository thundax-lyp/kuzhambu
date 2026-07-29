package com.thundax.kuzhambu.system.application.auth.service;

import com.thundax.kuzhambu.system.application.auth.command.CreatePermissionsCommand;
import com.thundax.kuzhambu.system.application.auth.query.PermissionQuery;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.PermissionCode;
import java.util.Set;

public interface PrincipalPermissionApplicationService {

    Set<PermissionCode> createPermissions(CreatePermissionsCommand command);

    Set<PermissionCode> getPermissions(PermissionQuery query);

    boolean isPermitted(PermissionQuery query);
}
