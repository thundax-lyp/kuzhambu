package com.thundax.kuzhambu.system.infra.core.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.thundax.kuzhambu.common.cache.CacheDTO;
import com.thundax.kuzhambu.common.cache.KuzhambuCacheNames;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.entity.enums.UserPrivilege;
import com.thundax.kuzhambu.system.application.core.entity.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.codec.AccessRankCodec;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class UserCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final String CACHE_SECTION = KuzhambuCacheNames.PREFIX + "sys.user";
    private static final String ROLES_PREFIX = ".roles_";
    private static final String KEY_INDEX = "keys";

    @CreateCache(
            name = CACHE_SECTION,
            cacheType = CacheType.BOTH,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Object> cache;

    @CreateCache(
            name = CACHE_SECTION + ".keys.",
            cacheType = CacheType.BOTH,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Set<String>> keyIndexCache;

    public User getById(Long id) {
        return toDomain((UserCacheDTO) cache.get(String.valueOf(id)));
    }

    public void putById(User user) {
        if (user != null && UserIdCodec.toValue(user.getId()) != null) {
            String key = String.valueOf(UserIdCodec.toValue(user.getId()));
            cache.put(key, toCacheDTO(user), OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
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
    public List<Long> getUserRoleIds(Long userId) {
        return (List<Long>) cache.get(userRoleIdsKey(userId));
    }

    public void putUserRoleIds(Long userId, List<Long> roleIds) {
        String key = userRoleIdsKey(userId);
        cache.put(key, roleIds);
        rememberKey(key);
    }

    public void removeUserRoleIds(Long userId) {
        String key = userRoleIdsKey(userId);
        cache.remove(key);
        forgetKey(key);
    }

    private String userRoleIdsKey(Long userId) {
        return ROLES_PREFIX + userId;
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

    private static User toDomain(UserCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        User user = new User();
        user.setId(UserIdCodec.toDomain(cacheDTO.id));
        user.setDepartmentId(DepartmentIdCodec.toDomain(cacheDTO.departmentId));
        user.setEmail(cacheDTO.email);
        user.setMobile(cacheDTO.mobile);
        user.setTel(cacheDTO.tel);
        user.setName(cacheDTO.name);
        user.setRank(AccessRankCodec.toDomain(cacheDTO.rank));
        user.setPrivilege(cacheDTO.privilege == null ? null : UserPrivilege.from(cacheDTO.privilege));
        user.setStatus(cacheDTO.status == null ? null : UserStatus.from(cacheDTO.status));
        user.setRemarks(cacheDTO.remarks);
        return user;
    }

    private static UserCacheDTO toCacheDTO(User user) {
        UserCacheDTO cacheDTO = new UserCacheDTO();
        cacheDTO.id = UserIdCodec.toValue(user.getId());
        cacheDTO.departmentId = DepartmentIdCodec.toValue(user.getDepartmentId());
        cacheDTO.email = user.getEmail();
        cacheDTO.mobile = user.getMobile();
        cacheDTO.tel = user.getTel();
        cacheDTO.name = user.getName();
        cacheDTO.rank = AccessRankCodec.toValue(user.getRank());
        cacheDTO.privilege =
                user.getPrivilege() == null ? null : user.getPrivilege().value();
        cacheDTO.status = user.getStatus() == null ? null : user.getStatus().value();
        cacheDTO.remarks = user.getRemarks();
        return cacheDTO;
    }

    private static class UserCacheDTO implements CacheDTO {
        private Long id;
        private Long departmentId;
        private String email;
        private String mobile;
        private String tel;
        private String name;
        private Integer rank;
        private String privilege;
        private String status;
        private String remarks;
    }
}
