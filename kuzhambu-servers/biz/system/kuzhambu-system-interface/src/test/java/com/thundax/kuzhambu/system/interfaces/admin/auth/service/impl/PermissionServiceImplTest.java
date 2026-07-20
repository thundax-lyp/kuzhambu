package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.system.application.core.service.CurrentUserApplicationService;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalAccessTokenRepository;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalAuthSessionRepository;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PermissionServiceImplTest {

    @Test
    void shouldReloadPermissionsWhenPermissionVersionChanged() {
        PrincipalAccessTokenRepository accessTokenRepository = mock(PrincipalAccessTokenRepository.class);
        PrincipalAuthSessionRepository authSessionRepository = mock(PrincipalAuthSessionRepository.class);
        UserApplicationService userService = mock(UserApplicationService.class);
        CurrentUserApplicationService currentUserService = mock(CurrentUserApplicationService.class);
        PermissionServiceImpl permissionService = new PermissionServiceImpl(
                accessTokenRepository, authSessionRepository, userService, currentUserService);

        PrincipalAuthSessionId sessionId = PrincipalAuthSessionId.of("session-1");
        PrincipalAuthSession session = PrincipalAuthSession.restore(
                sessionId,
                PrincipalKey.of(PrincipalType.USER, 1L),
                "admin-api",
                null,
                new Date(),
                new Date(),
                new Date(System.currentTimeMillis() + 60_000L));
        PrincipalAccessToken accessToken = new PrincipalAccessToken();
        accessToken.setSessionId(sessionId);
        accessToken.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1L));

        User user = new User();
        user.setId(UserId.of(1L));
        user.setStatus(UserStatus.ENABLED);

        when(accessTokenRepository.getByToken("token")).thenReturn(accessToken);
        when(authSessionRepository.getById(sessionId)).thenReturn(session);
        when(userService.get(UserId.of(1L))).thenReturn(user);
        when(currentUserService.listAccessibleMenus(any()))
                .thenReturn(List.of(menu("system:user:view")))
                .thenReturn(List.of(menu("system:role:view")));

        permissionService.createPermissions("token", "1");
        permissionService.onRoleCacheChanged();
        Set<String> permissions = permissionService.getPermissions("token");

        assertTrue(permissions.contains("system:role:view"));
        verify(currentUserService, times(2)).listAccessibleMenus(any());
        verify(authSessionRepository, times(2)).insert(any(PrincipalAuthSession.class), any(Integer.class));
    }

    private static Menu menu(String permission) {
        Menu menu = new Menu();
        menu.setId(MenuId.of(1L));
        menu.setPerms(permission);
        return menu;
    }
}
