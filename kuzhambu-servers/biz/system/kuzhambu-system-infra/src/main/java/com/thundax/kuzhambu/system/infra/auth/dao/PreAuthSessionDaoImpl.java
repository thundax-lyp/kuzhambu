package com.thundax.kuzhambu.system.infra.auth.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.thundax.kuzhambu.system.application.auth.dao.PreAuthSessionDao;
import com.thundax.kuzhambu.system.application.auth.entity.PreAuthSession;
import com.thundax.kuzhambu.system.application.auth.entity.PreAuthSession.PreAuthSessionValue;
import com.thundax.kuzhambu.system.application.auth.entity.PreAuthSession.RefreshTokenValue;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PreAuthSessionToken;
import com.thundax.kuzhambu.common.cache.CacheDTO;
import com.thundax.kuzhambu.common.cache.KuzhambuCacheNames;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@Profile("!test")
public class PreAuthSessionDaoImpl implements PreAuthSessionDao {

    private static final String CACHE_SECTION = KuzhambuCacheNames.PREFIX + "PRE_AUTH_SESSION_";
    private static final String SESSION_PREFIX = CACHE_SECTION + "SESSION_";
    private static final String TOKEN_PREFIX = CACHE_SECTION + "TOKEN_";
    private static final String REFRESH_TOKEN_PREFIX = CACHE_SECTION + "REFRESH_";
    private static final String ACTIVE_SESSION_KEY = CACHE_SECTION + "ACTIVE";
    private static final int SAFETY_SECONDS = 5;

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.BOTH)
    private Cache<String, Object> cache;

    private final RedisClient redisClient;
    private StatefulRedisConnection<String, String> redisConnection;

    public PreAuthSessionDaoImpl(@Value("${spring.redis.url:redis://127.0.0.1:6379/0}") String redisUrl) {
        this.redisClient = RedisClient.create(redisUrl);
    }

    @Override
    public int count() {
        redis().zremrangebyscore(ACTIVE_SESSION_KEY, 0, System.currentTimeMillis());
        return redis().zcard(ACTIVE_SESSION_KEY).intValue();
    }

    @Override
    public PreAuthSession getById(PreAuthSessionId id) {
        return id == null ? null : toEntity((PreAuthSessionCacheDTO) cache.get(SESSION_PREFIX + id.asString()));
    }

    @Override
    public PreAuthSessionId getByToken(PreAuthSessionToken token) {
        if (token == null) {
            return null;
        }
        return PreAuthSessionId.ofNullable((String) cache.get(TOKEN_PREFIX + token.asString()));
    }

    @Override
    public PreAuthSessionId getByRefreshToken(PreAuthSessionToken refreshToken) {
        if (refreshToken == null) {
            return null;
        }
        return PreAuthSessionId.ofNullable((String) cache.get(REFRESH_TOKEN_PREFIX + refreshToken.asString()));
    }

    @Override
    public void insert(PreAuthSession session) {
        Assert.notNull(session, "preAuthSession can not be null");
        putSession(session);
    }

    @Override
    public void update(PreAuthSession session) {
        Assert.notNull(session, "preAuthSession can not be null");
        PreAuthSession oldSession = getById(session.getId());
        if (oldSession != null) {
            removeIndexes(oldSession);
        }
        putSession(session);
    }

    @Override
    public void deleteById(PreAuthSessionId id) {
        PreAuthSession session = getById(id);
        if (session == null) {
            return;
        }
        cache.remove(SESSION_PREFIX + id.asString());
        removeIndexes(session);
        redis().zrem(ACTIVE_SESSION_KEY, id.asString());
    }

    private void putSession(PreAuthSession session) {
        long seconds = remainingSeconds(session);
        String sessionKey = SESSION_PREFIX + session.getId().asString();
        cache.put(sessionKey, toCacheDTO(session), seconds + SAFETY_SECONDS, TimeUnit.SECONDS);
        redis().zadd(ACTIVE_SESSION_KEY, session.getExpiredAt(), session.getId().asString());
        cache.put(TOKEN_PREFIX + session.getToken().asString(), session.getId().asString(), seconds, TimeUnit.SECONDS);
        for (RefreshTokenValue refreshToken : session.refreshTokenValues()) {
            long refreshTokenSeconds = ttlSeconds(refreshToken.getExpiredAt());
            if (refreshTokenSeconds <= 0) {
                continue;
            }
            cache.put(
                    REFRESH_TOKEN_PREFIX + refreshToken.getToken().asString(),
                    session.getId().asString(),
                    refreshTokenSeconds,
                    TimeUnit.SECONDS);
        }
    }

    private void removeIndexes(PreAuthSession session) {
        cache.remove(TOKEN_PREFIX + session.getToken().asString());
        for (RefreshTokenValue refreshToken : session.refreshTokenValues()) {
            cache.remove(REFRESH_TOKEN_PREFIX + refreshToken.getToken().asString());
        }
    }

    private long remainingSeconds(PreAuthSession session) {
        return remainingSeconds(session.getExpiredAt());
    }

    private long remainingSeconds(long expiredAt) {
        return Math.max(1L, ttlSeconds(expiredAt));
    }

    private long ttlSeconds(long expiredAt) {
        long remainingMillis = expiredAt - System.currentTimeMillis();
        return remainingMillis <= 0 ? 0L : (remainingMillis + 999L) / 1000L;
    }

    private static PreAuthSession toEntity(PreAuthSessionCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        List<RefreshTokenValue> refreshTokens = new ArrayList<>();
        for (RefreshTokenCacheDTO refreshToken : cacheDTO.refreshTokens) {
            refreshTokens.add(RefreshTokenValue.of(PreAuthSessionToken.of(refreshToken.token), refreshToken.expiredAt));
        }
        Map<String, PreAuthSessionValue> items = new LinkedHashMap<>();
        for (Map.Entry<String, PreAuthSessionValueCacheDTO> entry : cacheDTO.items.entrySet()) {
            items.put(entry.getKey(), PreAuthSessionValue.of(entry.getValue().value, entry.getValue().expiredAt));
        }
        return PreAuthSession.restore(
                PreAuthSessionId.of(cacheDTO.id),
                PreAuthSessionToken.of(cacheDTO.token),
                refreshTokens,
                cacheDTO.expiredAt,
                items);
    }

    private static PreAuthSessionCacheDTO toCacheDTO(PreAuthSession session) {
        PreAuthSessionCacheDTO cacheDTO = new PreAuthSessionCacheDTO();
        cacheDTO.id = session.getId().asString();
        cacheDTO.token = session.getToken().asString();
        cacheDTO.expiredAt = session.getExpiredAt();
        cacheDTO.refreshTokens = new ArrayList<>();
        for (RefreshTokenValue refreshToken : session.refreshTokenValues()) {
            RefreshTokenCacheDTO refreshTokenDTO = new RefreshTokenCacheDTO();
            refreshTokenDTO.token = refreshToken.getToken().asString();
            refreshTokenDTO.expiredAt = refreshToken.getExpiredAt();
            cacheDTO.refreshTokens.add(refreshTokenDTO);
        }
        cacheDTO.items = new LinkedHashMap<>();
        for (Map.Entry<String, PreAuthSessionValue> entry : session.itemValues().entrySet()) {
            PreAuthSessionValueCacheDTO valueDTO = new PreAuthSessionValueCacheDTO();
            valueDTO.value = entry.getValue().getValue();
            valueDTO.expiredAt = entry.getValue().getExpiredAt();
            cacheDTO.items.put(entry.getKey(), valueDTO);
        }
        return cacheDTO;
    }

    private RedisCommands<String, String> redis() {
        if (redisConnection == null) {
            synchronized (this) {
                if (redisConnection == null) {
                    redisConnection = redisClient.connect();
                }
            }
        }
        return redisConnection.sync();
    }

    @PreDestroy
    public void destroy() {
        if (redisConnection != null) {
            redisConnection.close();
        }
        redisClient.shutdown();
    }

    private static class PreAuthSessionCacheDTO implements CacheDTO {
        private String id;
        private String token;
        private long expiredAt;
        private List<RefreshTokenCacheDTO> refreshTokens = new ArrayList<>();
        private Map<String, PreAuthSessionValueCacheDTO> items = new LinkedHashMap<>();
    }

    private static class RefreshTokenCacheDTO implements CacheDTO {
        private String token;
        private long expiredAt;
    }

    private static class PreAuthSessionValueCacheDTO implements CacheDTO {
        private String value;
        private long expiredAt;
    }
}
