package com.thundax.kuzhambu.system.application.auth.result;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminTokenQueryResult {
    private boolean active;
    private PrincipalAccessTokenCode token;
    private PrincipalAuthSession session;
    private PrincipalAccessToken principalAccessToken;
    private User user;
    private String username;

    public static AdminTokenQueryResult inactive(PrincipalAccessTokenCode token) {
        AdminTokenQueryResult result = new AdminTokenQueryResult();
        result.setToken(token);
        return result;
    }

    public static AdminTokenQueryResult active(
            PrincipalAccessTokenCode token, PrincipalAuthSession session, User user, String username) {
        AdminTokenQueryResult result = new AdminTokenQueryResult();
        result.setActive(true);
        result.setToken(token);
        result.setSession(session);
        result.setUser(user);
        result.setUsername(username);
        return result;
    }
}
