package com.thundax.kuzhambu.system.infra.core.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.thundax.kuzhambu.common.cache.CacheDTO;
import com.thundax.kuzhambu.common.cache.KuzhambuCacheNames;
import com.thundax.kuzhambu.system.application.core.entity.Role;
import com.thundax.kuzhambu.system.application.core.entity.enums.RolePrivilege;
import com.thundax.kuzhambu.system.application.core.entity.enums.RoleStatus;
import com.thundax.kuzhambu.system.domain.core.codec.RoleIdCodec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class RoleCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final String CACHE_SECTION = KuzhambuCacheNames.PREFIX + "sys.role.";
    private static final String USERS_PREFIX = "users_";
    private static final String MENUS_PREFIX = "menus_";
    private static final String KEY_INDEX = "keys";

    @CreateCache(
            name = CACHE_SECTION,
            cacheType = CacheType.BOTH,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Object> cache;

    @CreateCache(
            name = CACHE_SECTION + "keys.",
            cacheType = CacheType.BOTH,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Set<String>> keyIndexCache;

    public Role getById(Long id) {
        return toDomain((RoleCacheDTO) cache.get(String.valueOf(id)));
    }

    public void putById(Role role) {
        if (role != null && RoleIdCodec.toValue(role.getId()) != null) {
            String key = String.valueOf(RoleIdCodec.toValue(role.getId()));
            cache.put(key, toCacheDTO(role), OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
            rememberKey(key);
        }
    }

    public void removeById(Long id) {
        String key = String.valueOf(id);
        cache.remove(key);
        forgetKey(key);
    }

    public void removeAll() {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys != null && !keys.isEmpty()) {
            cache.removeAll(keys);
        }
        keyIndexCache.remove(KEY_INDEX);
    }

    @SuppressWarnings("unchecked")
    public List<Long> getRoleUserIds(Long roleId) {
        return (List<Long>) cache.get(roleUserIdsKey(roleId));
    }

    public void putRoleUserIds(Long roleId, List<Long> userIds) {
        String key = roleUserIdsKey(roleId);
        cache.put(key, userIds);
        rememberKey(key);
    }

    public void removeRoleUserIds(Long roleId) {
        String key = roleUserIdsKey(roleId);
        cache.remove(key);
        forgetKey(key);
    }

    @SuppressWarnings("unchecked")
    public List<Long> getRoleMenuIds(Long roleId) {
        return (List<Long>) cache.get(roleMenuIdsKey(roleId));
    }

    public void putRoleMenuIds(Long roleId, List<Long> menuIds) {
        String key = roleMenuIdsKey(roleId);
        cache.put(key, menuIds);
        rememberKey(key);
    }

    public void removeRoleMenuIds(Long roleId) {
        String key = roleMenuIdsKey(roleId);
        cache.remove(key);
        forgetKey(key);
    }

    private String roleUserIdsKey(Long roleId) {
        return USERS_PREFIX + roleId;
    }

    private String roleMenuIdsKey(Long roleId) {
        return MENUS_PREFIX + roleId;
    }

    private void rememberKey(String key) {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys == null) {
            keys = new HashSet<>();
        }
        if (keys.add(key)) {
            keyIndexCache.put(KEY_INDEX, keys, OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void forgetKey(String key) {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys == null) {
            return;
        }
        if (keys.remove(key)) {
            keyIndexCache.put(KEY_INDEX, keys, OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
    }

    private static Role toDomain(RoleCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        Role role = new Role();
        role.setId(RoleIdCodec.toDomain(cacheDTO.id));
        role.setName(cacheDTO.name);
        role.setPrivilege(cacheDTO.privilege == null ? null : RolePrivilege.from(cacheDTO.privilege));
        role.setStatus(cacheDTO.status == null ? null : RoleStatus.from(cacheDTO.status));
        role.setPriority(cacheDTO.priority == null ? 0 : cacheDTO.priority);
        role.setRemarks(cacheDTO.remarks);
        return role;
    }

    private static RoleCacheDTO toCacheDTO(Role role) {
        RoleCacheDTO cacheDTO = new RoleCacheDTO();
        cacheDTO.id = RoleIdCodec.toValue(role.getId());
        cacheDTO.name = role.getName();
        cacheDTO.privilege =
                role.getPrivilege() == null ? null : role.getPrivilege().value();
        cacheDTO.status = role.getStatus() == null ? null : role.getStatus().value();
        cacheDTO.priority = role.getPriority();
        cacheDTO.remarks = role.getRemarks();
        return cacheDTO;
    }

    private static class RoleCacheDTO implements CacheDTO {
        private Long id;
        private String name;
        private String privilege;
        private String status;
        private Integer priority;
        private String remarks;
    }
}
