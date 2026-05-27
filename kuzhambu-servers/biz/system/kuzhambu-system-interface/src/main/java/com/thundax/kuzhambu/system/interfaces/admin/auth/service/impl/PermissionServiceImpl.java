package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import static com.thundax.kuzhambu.system.domain.model.valueobject.PermissionCode.ADMIN;
import static com.thundax.kuzhambu.system.domain.model.valueobject.PermissionCode.SEPARATOR;
import static com.thundax.kuzhambu.system.domain.model.valueobject.PermissionCode.SUPER;
import static com.thundax.kuzhambu.system.domain.model.valueobject.PermissionCode.USER;

import com.thundax.kuzhambu.common.core.arch.OneLineMethodAllowed;
import com.thundax.kuzhambu.common.security.permission.PermissionMatcher;
import com.thundax.kuzhambu.common.security.permission.PrefixPermissionMatcher;
import com.thundax.kuzhambu.system.application.auth.dao.PrincipalAccessTokenDao;
import com.thundax.kuzhambu.system.application.auth.dao.PrincipalAuthSessionDao;
import com.thundax.kuzhambu.system.application.core.service.CurrentUserService;
import com.thundax.kuzhambu.system.application.core.service.UserService;
import com.thundax.kuzhambu.system.application.core.service.query.CurrentUserQuery;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.model.entity.PrincipalAuthSession;
import com.thundax.kuzhambu.system.domain.model.entity.User;
import com.thundax.kuzhambu.system.domain.model.valueobject.PermissionCode;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.PermissionService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class PermissionServiceImpl implements PermissionService {

    private static final String SESSION_VALUE_PERMISSIONS = "PERMISSIONS";
    private static final int SAFETY_SECONDS = 10;

    private final PrincipalAccessTokenDao principalAccessTokenDao;
    private final PrincipalAuthSessionDao principalAuthSessionDao;
    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final PermissionMatcher permissionMatcher = new PrefixPermissionMatcher();

    public PermissionServiceImpl(
            PrincipalAccessTokenDao principalAccessTokenDao,
            PrincipalAuthSessionDao principalAuthSessionDao,
            UserService userService,
            CurrentUserService currentUserService) {
        this.principalAccessTokenDao = principalAccessTokenDao;
        this.principalAuthSessionDao = principalAuthSessionDao;
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @Override
    public Set<String> createPermissions(String token, String userId) {
        Assert.hasText(token, "token can not be empty");
        Assert.hasText(userId, "userId can not be empty");

        PrincipalAuthSession session = getActiveSession(token);
        if (session == null) {
            return Collections.emptySet();
        }
        Set<String> permissions = new HashSet<>(loadPermissions(userId));
        session.getValues().put(SESSION_VALUE_PERMISSIONS, new HashSet<>(permissions));
        principalAuthSessionDao.insert(session, expiredSeconds(session));
        return new HashSet<>(permissions);
    }

    @Override
    public Set<String> getPermissions(String token) {
        PrincipalAuthSession session = getActiveSession(token);
        if (session == null) {
            return null;
        }
        return toPermissionSet(session.getValues().get(SESSION_VALUE_PERMISSIONS));
    }

    @Override
    public boolean isPermitted(String token, String permission) {
        return permissionMatcher.matches(getPermissions(token), permission);
    }

    private Set<String> loadPermissions(String userId) {
        User user = userService.get(UserIdCodec.toDomain(Long.valueOf(userId)));
        Assert.notNull(user, "user can not be null");

        Set<String> permissions = new HashSet<>();
        CurrentUserQuery currentUserQuery =
                new CurrentUserQuery(user.getId(), user.getPrivilege(), user.getStatus(), user.getRank());
        List<Menu> menuList = currentUserService.listAccessibleMenus(currentUserQuery);
        if (menuList != null && !menuList.isEmpty()) {
            menuList.forEach(menu -> {
                if (StringUtils.isNotBlank(menu.getPerms())) {
                    for (String permission : StringUtils.split(menu.getPerms(), SEPARATOR)) {
                        if (!PermissionCode.isBuiltIn(permission)) {
                            permissions.add(permission);
                        }
                    }
                }
            });
        }

        permissions.add(USER);
        if (user.isSuper()) {
            permissions.add(SUPER);
            permissions.add(ADMIN);
        } else if (user.isAdmin()) {
            permissions.add(ADMIN);
        }

        return permissions;
    }

    private PrincipalAuthSession getActiveSession(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        PrincipalAccessToken accessToken = principalAccessTokenDao.getByToken(token);
        if (accessToken == null || accessToken.getSessionId() == null || !accessToken.canAccess(new Date())) {
            return null;
        }
        PrincipalAuthSession session = principalAuthSessionDao.getById(accessToken.getSessionId());
        if (session == null || session.isExpired(new Date())) {
            return null;
        }
        return session;
    }

    private Set<String> toPermissionSet(Object value) {
        if (!(value instanceof Collection)) {
            return null;
        }
        Set<String> permissions = new HashSet<>();
        for (Object item : snapshotCollection((Collection<?>) value)) {
            if (item != null) {
                permissions.add(String.valueOf(item));
            }
        }
        return permissions;
    }

    private Collection<?> snapshotCollection(Collection<?> source) {
        for (int index = 0; index < 3; index++) {
            try {
                return new ArrayList<>(source);
            } catch (ConcurrentModificationException ignored) {
                Thread.yield();
            }
        }
        synchronized (source) {
            return new ArrayList<>(source);
        }
    }

    @OneLineMethodAllowed(reason = "表达权限会话缓存 TTL 的安全余量边界")
    private int expiredSeconds(PrincipalAuthSession session) {
        return session.remainingSeconds(new Date()) + SAFETY_SECONDS;
    }
}
