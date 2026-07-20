package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.system.application.auth.service.PermissionApplicationService;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PermissionServiceImplTest {

    @Test
    void shouldDelegatePermissionLookupToApplicationService() {
        PermissionApplicationService permissionApplicationService = mock(PermissionApplicationService.class);
        PermissionServiceImpl permissionService = new PermissionServiceImpl(permissionApplicationService);

        when(permissionApplicationService.getPermissions("token")).thenReturn(Set.of("system:role:view"));

        Set<String> permissions = permissionService.getPermissions("token");

        assertTrue(permissions.contains("system:role:view"));
        verify(permissionApplicationService).getPermissions("token");
    }
}
