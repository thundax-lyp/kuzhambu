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
import com.thundax.kuzhambu.system.application.core.command.RemoveUserCommand;
import com.thundax.kuzhambu.system.application.core.query.GetUserQuery;
import com.thundax.kuzhambu.system.application.core.query.UserQuery;
import com.thundax.kuzhambu.system.application.core.service.UserManagementApplicationService;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.AdminAuthService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.PermissionService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthAccessTokenDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthTokenQueryDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthTokenRefreshDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.support.AdminAuthLookup;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.support.AdminAuthOperation;
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
        public AuthAccessTokenDTO createAccessToken(AdminAuthOperation command) {
            return null;
        }

        @Override
        public AuthAccessTokenDTO getAccessToken(AdminAuthLookup query) {
            PrincipalAccessToken accessToken = new PrincipalAccessToken();
            accessToken.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1L));
            return new AuthAccessTokenDTO("access-token", null, accessToken);
        }

        @Override
        public int deleteAccessTokensByUserId(AdminAuthOperation command) {
            return 0;
        }

        @Override
        public boolean validateToken(AdminAuthOperation command) {
            return true;
        }

        @Override
        public void activeAccessToken(AdminAuthOperation command) {}

        @Override
        public void deleteAccessToken(AdminAuthOperation command) {}

        @Override
        public AuthTokenQueryDTO getTokenInfo(AdminAuthLookup query) {
            return null;
        }

        @Override
        public AuthTokenRefreshDTO refreshAccessToken(AdminAuthOperation command) {
            return null;
        }

        @Override
        public void invalidateSessionByToken(AdminAuthOperation command) {}

        @Override
        public int invalidateSessionsByUserId(AdminAuthOperation command) {
            return 0;
        }

        @Override
        public User authenticatePassword(AdminAuthOperation command) {
            return null;
        }

        @Override
        public User authenticateSms(AdminAuthOperation command) {
            return null;
        }

        @Override
        public User authenticateWecom(AdminAuthOperation command) {
            return null;
        }

        @Override
        public User authenticateGithub(AdminAuthOperation command) {
            return null;
        }

        @Override
        public void recordLoginFailed(AdminAuthOperation command) {}

        @Override
        public void validatePassword(AdminAuthOperation command) {}
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

    private static final class DeletedUserService implements UserManagementApplicationService {

        @Override
        public User get(GetUserQuery query) {
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
        public int remove(RemoveUserCommand command) {
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
