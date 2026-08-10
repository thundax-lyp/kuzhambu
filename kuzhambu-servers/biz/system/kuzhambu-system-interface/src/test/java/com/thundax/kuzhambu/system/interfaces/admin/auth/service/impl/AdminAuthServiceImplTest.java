package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.web.exception.KuzhambuException;
import com.thundax.kuzhambu.system.application.auth.command.RefreshAdminAccessTokenCommand;
import com.thundax.kuzhambu.system.application.auth.query.AdminAccessTokenQuery;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenQueryResult;
import com.thundax.kuzhambu.system.application.auth.service.AdminSessionTokenApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalAuthenticationApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.core.service.UserManagementApplicationService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.PermissionService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthTokenQueryDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.support.AdminAuthLookup;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.support.AdminAuthOperation;
import com.thundax.kuzhambu.system.interfaces.admin.configure.LoginProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AdminAuthServiceImplTest {

    @Test
    void shouldTreatBlankAccessTokenAsInactiveToken() {
        AdminSessionTokenApplicationService adminTokenService = mock(AdminSessionTokenApplicationService.class);
        AdminAuthServiceImpl authService = authService(adminTokenService);
        when(adminTokenService.getTokenInfo(any(AdminAccessTokenQuery.class)))
                .thenReturn(AdminTokenQueryResult.inactive(null));
        AdminAuthLookup query = new AdminAuthLookup();
        query.setToken("   ");

        AuthTokenQueryDTO result = authService.getTokenInfo(query);

        assertFalse(result.isActive());
        assertNull(result.getToken());
        ArgumentCaptor<AdminAccessTokenQuery> captor = ArgumentCaptor.forClass(AdminAccessTokenQuery.class);
        verify(adminTokenService).getTokenInfo(captor.capture());
        assertNull(captor.getValue().token());
    }

    @Test
    void shouldMapBlankRefreshTokenToInvalidTokenResponse() {
        AdminSessionTokenApplicationService adminTokenService = mock(AdminSessionTokenApplicationService.class);
        AdminAuthServiceImpl authService = authService(adminTokenService);
        when(adminTokenService.refreshAccessToken(any(RefreshAdminAccessTokenCommand.class)))
                .thenThrow(new BizException("invalid token"));
        AdminAuthOperation command = new AdminAuthOperation();
        command.setRefreshToken("   ");

        KuzhambuException exception =
                assertThrows(KuzhambuException.class, () -> authService.refreshAccessToken(command));

        assertEquals("AUTH-00006", exception.getCode());
        ArgumentCaptor<RefreshAdminAccessTokenCommand> captor =
                ArgumentCaptor.forClass(RefreshAdminAccessTokenCommand.class);
        verify(adminTokenService).refreshAccessToken(captor.capture());
        assertNull(captor.getValue().refreshToken());
    }

    private AdminAuthServiceImpl authService(AdminSessionTokenApplicationService adminTokenService) {
        return new AdminAuthServiceImpl(
                mock(LoginProperties.class),
                mock(PermissionService.class),
                adminTokenService,
                mock(PrincipalAuthenticationApplicationService.class),
                mock(PrincipalIdentityApplicationService.class),
                mock(UserManagementApplicationService.class));
    }
}
