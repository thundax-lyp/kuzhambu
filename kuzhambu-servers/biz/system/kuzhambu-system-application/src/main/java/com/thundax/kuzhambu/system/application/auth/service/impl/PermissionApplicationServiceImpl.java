package com.thundax.kuzhambu.system.application.auth.service.impl;

import static com.thundax.kuzhambu.system.domain.core.model.valueobject.PermissionCode.ADMIN;
import static com.thundax.kuzhambu.system.domain.core.model.valueobject.PermissionCode.SEPARATOR;
import static com.thundax.kuzhambu.system.domain.core.model.valueobject.PermissionCode.SUPER;
import static com.thundax.kuzhambu.system.domain.core.model.valueobject.PermissionCode.USER;

import com.thundax.kuzhambu.common.core.arch.OneLineMethodAllowed;
import com.thundax.kuzhambu.common.security.permission.PermissionMatcher;
import com.thundax.kuzhambu.common.security.permission.PrefixPermissionMatcher;
import com.thundax.kuzhambu.system.application.auth.command.CreatePermissionsCommand;
import com.thundax.kuzhambu.system.application.auth.query.PermissionQuery;
import com.thundax.kuzhambu.system.application.auth.service.PermissionApplicationService;
import com.thundax.kuzhambu.system.application.core.query.CurrentUserQuery;
import com.thundax.kuzhambu.system.application.core.service.CurrentUserApplicationService;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.application.core.service.impl.MenuApplicationServiceImpl;
import com.thundax.kuzhambu.system.application.core.service.impl.RoleApplicationServiceImpl;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAuthSession;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalAccessTokenRepository;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalAuthSessionRepository;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.PermissionCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class PermissionApplicationServiceImpl
        implements PermissionApplicationService,
                RoleApplicationServiceImpl.CacheChangedListener,
                MenuApplicationServiceImpl.CacheChangedListener {

    private static final String SESSION_VALUE_PERMISSIONS = "PERMISSIONS";
    private static final String SESSION_VALUE_PERMISSION_VERSION = "PERMISSION_VERSION";
    private static final int SAFETY_SECONDS = 10;

    private final PrincipalAccessTokenRepository principalAccessTokenRepository;
    private final PrincipalAuthSessionRepository principalAuthSessionRepository;
    private final UserApplicationService userService;
    private final CurrentUserApplicationService currentUserService;
    private final PermissionMatcher permissionMatcher = new PrefixPermissionMatcher();
    private final AtomicLong permissionVersion = new AtomicLong();

    public PermissionApplicationServiceImpl(
            PrincipalAccessTokenRepository principalAccessTokenRepository,
            PrincipalAuthSessionRepository principalAuthSessionRepository,
            UserApplicationService userService,
            CurrentUserApplicationService currentUserService) {
        this.principalAccessTokenRepository = principalAccessTokenRepository;
        this.principalAuthSessionRepository = principalAuthSessionRepository;
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @Override
    public Set<PermissionCode> createPermissions(CreatePermissionsCommand command) {
        String token = tokenValue(command);
        String userId = UserIdCodec.toStringValue(command == null ? null : command.getUserId());
        Assert.hasText(token, "token can not be empty");
        Assert.hasText(userId, "userId can not be empty");

        PrincipalAuthSession session = getActiveSession(token);
        if (session == null) {
            return Collections.emptySet();
        }
        Set<PermissionCode> permissions = new HashSet<>(loadPermissions(userId));
        savePermissions(session, permissions);
        return new HashSet<>(permissions);
    }

    @Override
    public Set<PermissionCode> getPermissions(PermissionQuery query) {
        String token = tokenValue(query);
        PrincipalAuthSession session = getActiveSession(token);
        if (session == null) {
            return null;
        }
        if (!isCurrentPermissionVersion(session)) {
            return refreshPermissions(session);
        }
        return toPermissionSet(session.getValues().get(SESSION_VALUE_PERMISSIONS));
    }

    @Override
    public boolean isPermitted(PermissionQuery query) {
        return permissionMatcher.matches(
                toPermissionValues(getPermissions(query)),
                query == null || query.getPermission() == null
                        ? null
                        : query.getPermission().asString());
    }

    @Override
    public void onRoleCacheChanged() {
        permissionVersion.incrementAndGet();
    }

    @Override
    public void onMenuCacheChanged() {
        permissionVersion.incrementAndGet();
    }

    private Set<PermissionCode> refreshPermissions(PrincipalAuthSession session) {
        if (session.getPrincipalKey() == null || session.getPrincipalKey().getPrincipalId() == null) {
            return Collections.emptySet();
        }
        Set<PermissionCode> permissions = new HashSet<>(
                loadPermissions(String.valueOf(session.getPrincipalKey().getPrincipalId())));
        savePermissions(session, permissions);
        return new HashSet<>(permissions);
    }

    private void savePermissions(PrincipalAuthSession session, Set<PermissionCode> permissions) {
        session.getValues().put(SESSION_VALUE_PERMISSIONS, toPermissionValues(permissions));
        session.getValues().put(SESSION_VALUE_PERMISSION_VERSION, permissionVersion.get());
        principalAuthSessionRepository.insert(session, expiredSeconds(session));
    }

    private boolean isCurrentPermissionVersion(PrincipalAuthSession session) {
        Object sessionVersion = session.getValues().get(SESSION_VALUE_PERMISSION_VERSION);
        return sessionVersion instanceof Number && ((Number) sessionVersion).longValue() == permissionVersion.get();
    }

    private Set<PermissionCode> loadPermissions(String userId) {
        User user = userService.get(UserIdCodec.toDomain(Long.valueOf(userId)));
        Assert.notNull(user, "user can not be null");

        Set<PermissionCode> permissions = new HashSet<>();
        CurrentUserQuery currentUserQuery =
                new CurrentUserQuery(user.getId(), user.getPrivilege(), user.getStatus(), user.getRank());
        List<Menu> menuList = currentUserService.listAccessibleMenus(currentUserQuery);
        if (menuList != null && !menuList.isEmpty()) {
            menuList.forEach(menu -> {
                if (StringUtils.isNotBlank(menu.getPerms())) {
                    for (String permission : StringUtils.split(menu.getPerms(), SEPARATOR)) {
                        if (!PermissionCode.isBuiltIn(permission)) {
                            permissions.add(PermissionCode.of(permission));
                        }
                    }
                }
            });
        }

        permissions.add(PermissionCode.of(USER));
        if (user.isSuper()) {
            permissions.add(PermissionCode.of(SUPER));
            permissions.add(PermissionCode.of(ADMIN));
        } else if (user.isAdmin()) {
            permissions.add(PermissionCode.of(ADMIN));
        }

        return permissions;
    }

    private PrincipalAuthSession getActiveSession(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        PrincipalAccessToken accessToken = principalAccessTokenRepository.getByToken(token);
        if (accessToken == null || accessToken.getSessionId() == null || !accessToken.canAccess(new Date())) {
            return null;
        }
        PrincipalAuthSession session = principalAuthSessionRepository.getById(accessToken.getSessionId());
        if (session == null || session.isExpired(new Date())) {
            return null;
        }
        return session;
    }

    private Set<PermissionCode> toPermissionSet(Object value) {
        if (!(value instanceof Collection)) {
            return null;
        }
        Set<PermissionCode> permissions = new HashSet<>();
        for (Object item : snapshotCollection((Collection<?>) value)) {
            if (item != null) {
                permissions.add(PermissionCode.of(String.valueOf(item)));
            }
        }
        return permissions;
    }

    private Set<String> toPermissionValues(Set<PermissionCode> permissions) {
        if (permissions == null) {
            return null;
        }
        Set<String> values = new HashSet<>();
        for (PermissionCode permission : permissions) {
            if (permission != null) {
                values.add(permission.asString());
            }
        }
        return values;
    }

    private String tokenValue(CreatePermissionsCommand command) {
        if (command == null || command.getToken() == null) {
            return null;
        }
        return command.getToken().asString();
    }

    private String tokenValue(PermissionQuery query) {
        if (query == null || query.getToken() == null) {
            return null;
        }
        return query.getToken().asString();
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
