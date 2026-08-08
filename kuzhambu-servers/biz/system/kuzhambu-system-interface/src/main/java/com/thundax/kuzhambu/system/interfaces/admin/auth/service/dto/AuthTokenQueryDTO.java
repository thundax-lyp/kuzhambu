package com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAuthSession;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthTokenQueryDTO {
    private boolean active;
    private String token;
    private PrincipalAuthSession session;
    private PrincipalAccessToken principalAccessToken;
    private User user;
    private String username;

    public static AuthTokenQueryDTO inactive(String token) {
        AuthTokenQueryDTO result = new AuthTokenQueryDTO();
        result.setToken(token);
        return result;
    }

    public static AuthTokenQueryDTO active(String token, PrincipalAuthSession session, User user, String username) {
        AuthTokenQueryDTO result = new AuthTokenQueryDTO();
        result.setActive(true);
        result.setToken(token);
        result.setSession(session);
        result.setUser(user);
        result.setUsername(username);
        return result;
    }

    public static AuthTokenQueryDTO active(
            String token, PrincipalAccessToken principalAccessToken, User user, String username) {
        return active(token, principalAccessToken, null, user, username);
    }

    public static AuthTokenQueryDTO active(
            String token,
            PrincipalAccessToken principalAccessToken,
            PrincipalAuthSession session,
            User user,
            String username) {
        AuthTokenQueryDTO result = new AuthTokenQueryDTO();
        result.setActive(true);
        result.setToken(token);
        result.setPrincipalAccessToken(principalAccessToken);
        result.setSession(session);
        result.setUser(user);
        result.setUsername(username);
        return result;
    }
}
