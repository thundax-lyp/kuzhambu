package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.system.application.auth.query.PermissionQuery;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalPermissionApplicationService;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.PermissionCode;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PermissionServiceImplTest {

    @Test
    void shouldDelegatePermissionLookupToApplicationService() {
        PrincipalPermissionApplicationService permissionApplicationService =
                mock(PrincipalPermissionApplicationService.class);
        PermissionServiceImpl permissionService = new PermissionServiceImpl(permissionApplicationService);

        when(permissionApplicationService.getPermissions(any(PermissionQuery.class)))
                .thenReturn(Set.of(PermissionCode.of("system:role:view")));

        Set<String> permissions = permissionService.getPermissions("token");

        assertTrue(permissions.contains("system:role:view"));
        verify(permissionApplicationService).getPermissions(any(PermissionQuery.class));
    }
}
