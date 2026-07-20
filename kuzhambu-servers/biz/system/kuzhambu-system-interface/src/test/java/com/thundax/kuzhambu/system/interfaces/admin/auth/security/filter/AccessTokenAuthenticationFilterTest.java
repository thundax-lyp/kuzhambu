package com.thundax.kuzhambu.system.interfaces.admin.auth.security.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.system.application.core.command.ChangeUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeUserStatusCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateUserCommand;
import com.thundax.kuzhambu.system.application.core.query.UserQuery;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.AdminAuthService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.PermissionService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.command.AdminAuthCommand;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.query.AdminAuthQuery;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthAccessTokenResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthTokenQueryResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthTokenRefreshResult;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AccessTokenAuthenticationFilterTest {

    @Test
    void shouldRejectTokenWhenUserWasDeleted() throws Exception {
        AccessTokenAuthenticationFilter filter = new AccessTokenAuthenticationFilter(
                Collections.emptyList(),
                new TokenAuthService(),
                new EmptyPermissionService(),
                new DeletedUserService(),
                new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sys/user/page");
        request.addHeader(AccessTokenNames.HEADER_TOKEN, "access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertTrue(response.getContentAsString().contains("未授权用户"));
        assertFalse(response.getContentAsString().isEmpty());
    }

    private static final class TokenAuthService implements AdminAuthService {

        @Override
        public AuthAccessTokenResult createAccessToken(AdminAuthCommand command) {
            return null;
        }

        @Override
        public AuthAccessTokenResult getAccessToken(AdminAuthQuery query) {
            PrincipalAccessToken accessToken = new PrincipalAccessToken();
            accessToken.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1L));
            return new AuthAccessTokenResult("access-token", null, accessToken);
        }

        @Override
        public int deleteAccessTokensByUserId(AdminAuthCommand command) {
            return 0;
        }

        @Override
        public boolean validateToken(AdminAuthCommand command) {
            return true;
        }

        @Override
        public void activeAccessToken(AdminAuthCommand command) {}

        @Override
        public void deleteAccessToken(AdminAuthCommand command) {}

        @Override
        public AuthTokenQueryResult getTokenInfo(AdminAuthQuery query) {
            return null;
        }

        @Override
        public AuthTokenRefreshResult refreshAccessToken(AdminAuthCommand command) {
            return null;
        }

        @Override
        public void invalidateSessionByToken(AdminAuthCommand command) {}

        @Override
        public int invalidateSessionsByUserId(AdminAuthCommand command) {
            return 0;
        }

        @Override
        public User authenticatePassword(AdminAuthCommand command) {
            return null;
        }

        @Override
        public User authenticateSms(AdminAuthCommand command) {
            return null;
        }

        @Override
        public User authenticateWecom(AdminAuthCommand command) {
            return null;
        }

        @Override
        public User authenticateGithub(AdminAuthCommand command) {
            return null;
        }

        @Override
        public void recordLoginFailed(AdminAuthCommand command) {}

        @Override
        public void validatePassword(AdminAuthCommand command) {}
    }

    private static final class EmptyPermissionService implements PermissionService {

        @Override
        public Set<String> createPermissions(String token, String userId) {
            return Collections.emptySet();
        }

        @Override
        public Set<String> getPermissions(String token) {
            return Collections.emptySet();
        }

        @Override
        public boolean isPermitted(String token, String permission) {
            return false;
        }
    }

    private static final class DeletedUserService implements UserApplicationService {

        @Override
        public User get(UserId id) {
            return null;
        }

        @Override
        public List<User> list(UserQuery query) {
            return Collections.emptyList();
        }

        @Override
        public PageResult<User> page(UserQuery query, PageQuery page) {
            return null;
        }

        @Override
        public boolean existsEmail(UserQuery query) {
            return false;
        }

        @Override
        public boolean existsMobile(UserQuery query) {
            return false;
        }

        @Override
        public UserId create(CreateUserCommand command) {
            return null;
        }

        @Override
        public void changeInfo(ChangeUserInfoCommand command) {}

        @Override
        public int remove(UserId id) {
            return 0;
        }

        @Override
        public int changeStatus(ChangeUserStatusCommand command) {
            return 0;
        }

        @Override
        public List<Role> listUserRoles(UserQuery query) {
            return Collections.emptyList();
        }
    }
}
