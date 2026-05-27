package com.thundax.kuzhambu.system.infra.auth.repository.impl;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.thundax.kuzhambu.common.cache.CacheDTO;
import com.thundax.kuzhambu.common.cache.KuzhambuCacheNames;
import com.thundax.kuzhambu.common.core.crypto.Sha256Digest;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalAccessTokenIdCodec;
import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalAuthSessionIdCodec;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalAccessTokenRepository;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@Profile("!test")
public class PrincipalAccessTokenRepositoryImpl implements PrincipalAccessTokenRepository {

    private static final String CACHE_SECTION = KuzhambuCacheNames.PREFIX + "PRINCIPAL_ACCESS_TOKEN_";
    private static final String TOKEN_HASH_PREFIX = CACHE_SECTION + "HASH_";
    private static final String TOKEN_CODE_PREFIX = CACHE_SECTION + "TOKEN_CODE_";
    private static final String PRINCIPAL_INDEX_PREFIX = CACHE_SECTION + "PRINCIPAL_";
    private static final String CLIENT_INDEX_PREFIX = CACHE_SECTION + "CLIENT_";
    private static final int SAFETY_SECONDS = 5;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();
    private final RedisClient redisClient;
    private StatefulRedisConnection<String, String> redisConnection;

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.BOTH)
    private Cache<String, Object> cache;

    public PrincipalAccessTokenRepositoryImpl(@Value("${spring.redis.url:redis://127.0.0.1:6379/0}") String redisUrl) {
        this.redisClient = RedisClient.create(redisUrl);
    }

    @Override
    public PrincipalAccessToken getById(PrincipalAccessTokenId id) {
        if (id == null) {
            return null;
        }
        String tokenHash = (String) cache.get(TOKEN_CODE_PREFIX + PrincipalAccessTokenIdCodec.toValue(id));
        return getByTokenHash(tokenHash);
    }

    @Override
    public PrincipalAccessToken getByTokenCode(PrincipalAccessTokenCode tokenCode) {
        if (tokenCode == null) {
            return null;
        }
        String tokenHash = (String) cache.get(TOKEN_CODE_PREFIX + tokenCode.value());
        return getByTokenHash(tokenHash);
    }

    @Override
    public PrincipalAccessToken getByToken(String token) {
        return getByTokenHash(tokenHash(token));
    }

    @Override
    public List<PrincipalAccessToken> listByPrincipalKeyAndClientIdAndStatus(
            PrincipalKey principalKey, String clientId, PrincipalTokenStatus status) {
        if (principalKey == null || status == null) {
            return new ArrayList<>();
        }
        String indexKey = principalIndexKey(principalKey, clientId, status);
        removeExpired(indexKey);
        List<String> tokenHashes = redis().zrange(indexKey, 0, -1);
        List<PrincipalAccessToken> tokens = new ArrayList<>();
        for (String tokenHash : tokenHashes) {
            PrincipalAccessToken token = getByTokenHash(tokenHash);
            if (token != null) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    @Override
    public int countByClientIdAndStatus(String clientId, PrincipalTokenStatus status) {
        if (status == null) {
            return 0;
        }
        String indexKey = clientIndexKey(clientId, status);
        removeExpired(indexKey);
        return redis().zcard(indexKey).intValue();
    }

    @Override
    public PrincipalAccessTokenId insert(PrincipalAccessToken accessToken, String token) {
        Assert.notNull(accessToken, "accessToken can not be null");
        Assert.hasText(token, "token can not be blank");
        Assert.notNull(accessToken.getTokenCode(), "tokenCode can not be null");
        Assert.notNull(accessToken.getPrincipalKey(), "principalKey can not be null");
        Assert.notNull(accessToken.getExpireAt(), "expireAt can not be null");
        Assert.notNull(accessToken.getStatus(), "status can not be null");
        if (accessToken.getId() == null) {
            accessToken.setId(PrincipalAccessTokenIdCodec.nextId(idGenerator));
        }
        putToken(accessToken, tokenHash(token));
        return accessToken.getId();
    }

    @Override
    public int updateStatus(PrincipalAccessToken accessToken) {
        Assert.notNull(accessToken, "accessToken can not be null");
        String tokenHash = tokenHashById(accessToken.getId());
        PrincipalAccessToken oldToken = getByTokenHash(tokenHash);
        if (oldToken != null) {
            removeIndex(oldToken);
        }
        Assert.notNull(accessToken.getExpireAt(), "expireAt can not be null");
        Assert.notNull(accessToken.getStatus(), "status can not be null");
        Assert.hasText(tokenHash, "tokenHash can not be blank");
        putToken(accessToken, tokenHash);
        return 1;
    }

    private PrincipalAccessToken getByTokenHash(String tokenHash) {
        return StringUtils.isBlank(tokenHash)
                ? null
                : toDomain((PrincipalAccessTokenCacheDTO) cache.get(TOKEN_HASH_PREFIX + tokenHash));
    }

    private void putToken(PrincipalAccessToken accessToken, String tokenHash) {
        long seconds = remainingSeconds(accessToken);
        cache.put(TOKEN_HASH_PREFIX + tokenHash, toCacheDTO(accessToken), seconds + SAFETY_SECONDS, TimeUnit.SECONDS);
        cache.put(TOKEN_CODE_PREFIX + accessToken.getTokenCode().value(), tokenHash, seconds, TimeUnit.SECONDS);
        cache.put(
                TOKEN_CODE_PREFIX + PrincipalAccessTokenIdCodec.toValue(accessToken.getId()),
                tokenHash,
                seconds,
                TimeUnit.SECONDS);
        redis().zadd(principalIndexKey(accessToken), accessToken.getExpireAt().getTime(), tokenHash);
        redis().zadd(clientIndexKey(accessToken), accessToken.getExpireAt().getTime(), tokenHash);
    }

    private void removeIndex(PrincipalAccessToken accessToken) {
        String tokenHash = tokenHashById(accessToken.getId());
        redis().zrem(principalIndexKey(accessToken), tokenHash);
        redis().zrem(clientIndexKey(accessToken), tokenHash);
    }

    private long remainingSeconds(PrincipalAccessToken accessToken) {
        Date expireAt = accessToken.getExpireAt();
        if (expireAt == null) {
            return 1L;
        }
        long remainingMillis = expireAt.getTime() - System.currentTimeMillis();
        return remainingMillis <= 0 ? 1L : (remainingMillis + 999L) / 1000L;
    }

    private void removeExpired(String indexKey) {
        redis().zremrangebyscore(indexKey, 0, System.currentTimeMillis());
    }

    private String tokenHashById(PrincipalAccessTokenId id) {
        return id == null ? null : (String) cache.get(TOKEN_CODE_PREFIX + PrincipalAccessTokenIdCodec.toValue(id));
    }

    private String tokenHash(String token) {
        return StringUtils.isBlank(token) ? null : Sha256Digest.hashBase64Url(token);
    }

    private String principalIndexKey(PrincipalAccessToken accessToken) {
        return principalIndexKey(accessToken.getPrincipalKey(), accessToken.getClientId(), accessToken.getStatus());
    }

    private String clientIndexKey(PrincipalAccessToken accessToken) {
        return clientIndexKey(accessToken.getClientId(), accessToken.getStatus());
    }

    private String clientIndexKey(String clientId, PrincipalTokenStatus status) {
        return CLIENT_INDEX_PREFIX + StringUtils.defaultIfBlank(clientId, "DEFAULT") + "_" + status.value();
    }

    private String principalIndexKey(PrincipalKey principalKey, String clientId, PrincipalTokenStatus status) {
        return PRINCIPAL_INDEX_PREFIX
                + principalKey.getPrincipalType().value()
                + "_"
                + principalKey.getPrincipalId()
                + "_"
                + StringUtils.defaultIfBlank(clientId, "DEFAULT")
                + "_"
                + status.value();
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

    private static PrincipalAccessToken toDomain(PrincipalAccessTokenCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        PrincipalAccessToken accessToken = new PrincipalAccessToken();
        accessToken.setId(PrincipalAccessTokenIdCodec.toDomain(cacheDTO.id));
        accessToken.setTokenCode(PrincipalAccessTokenCode.ofNullable(cacheDTO.tokenCode));
        accessToken.setClientId(cacheDTO.clientId);
        accessToken.setSessionId(PrincipalAuthSessionIdCodec.toDomain(cacheDTO.sessionId));
        accessToken.setPrincipalKey(PrincipalKey.of(PrincipalType.from(cacheDTO.principalType), cacheDTO.principalId));
        accessToken.setScopes(new LinkedHashSet<>(cacheDTO.scopes));
        accessToken.setIssuedAt(cacheDTO.issuedAt);
        accessToken.setExpireAt(cacheDTO.expireAt);
        accessToken.setStatus(PrincipalTokenStatus.from(cacheDTO.status));
        return accessToken;
    }

    private static PrincipalAccessTokenCacheDTO toCacheDTO(PrincipalAccessToken accessToken) {
        PrincipalAccessTokenCacheDTO cacheDTO = new PrincipalAccessTokenCacheDTO();
        cacheDTO.id = PrincipalAccessTokenIdCodec.toValue(accessToken.getId());
        cacheDTO.tokenCode = accessToken.getTokenCode() == null
                ? null
                : accessToken.getTokenCode().value();
        cacheDTO.clientId = accessToken.getClientId();
        cacheDTO.sessionId = PrincipalAuthSessionIdCodec.toValue(accessToken.getSessionId());
        cacheDTO.principalType =
                accessToken.getPrincipalKey().getPrincipalType().value();
        cacheDTO.principalId = accessToken.getPrincipalKey().getPrincipalId();
        cacheDTO.scopes =
                accessToken.getScopes() == null ? new ArrayList<>() : new ArrayList<>(accessToken.getScopes());
        cacheDTO.issuedAt = accessToken.getIssuedAt();
        cacheDTO.expireAt = accessToken.getExpireAt();
        cacheDTO.status = accessToken.getStatus().value();
        return cacheDTO;
    }

    private static class PrincipalAccessTokenCacheDTO implements CacheDTO {
        private String id;
        private String tokenCode;
        private String clientId;
        private String sessionId;
        private String principalType;
        private Long principalId;
        private List<String> scopes = new ArrayList<>();
        private Date issuedAt;
        private Date expireAt;
        private String status;
    }
}
