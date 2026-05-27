package com.thundax.kuzhambu.system.infra.auth.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.thundax.kuzhambu.common.cache.CacheDTO;
import com.thundax.kuzhambu.common.cache.KuzhambuCacheNames;
import com.thundax.kuzhambu.common.core.crypto.Sha256Digest;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.application.auth.dao.PrincipalRefreshTokenDao;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalRefreshToken;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalAccessTokenIdCodec;
import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalAuthSessionIdCodec;
import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalRefreshTokenIdCodec;
import com.thundax.kuzhambu.system.domain.auth.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalRefreshTokenCode;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalRefreshTokenId;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@Profile("!test")
public class PrincipalRefreshTokenDaoImpl implements PrincipalRefreshTokenDao {

    private static final String CACHE_SECTION = KuzhambuCacheNames.PREFIX + "PRINCIPAL_REFRESH_TOKEN_";
    private static final String TOKEN_HASH_PREFIX = CACHE_SECTION + "HASH_";
    private static final String TOKEN_CODE_PREFIX = CACHE_SECTION + "TOKEN_CODE_";
    private static final String PRINCIPAL_INDEX_PREFIX = CACHE_SECTION + "PRINCIPAL_";
    private static final int SAFETY_SECONDS = 5;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();
    private final RedisClient redisClient;
    private StatefulRedisConnection<String, String> redisConnection;

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.BOTH)
    private Cache<String, Object> cache;

    public PrincipalRefreshTokenDaoImpl(@Value("${spring.redis.url:redis://127.0.0.1:6379/0}") String redisUrl) {
        this.redisClient = RedisClient.create(redisUrl);
    }

    @Override
    public PrincipalRefreshToken getById(PrincipalRefreshTokenId id) {
        if (id == null) {
            return null;
        }
        String tokenHash = (String) cache.get(TOKEN_CODE_PREFIX + PrincipalRefreshTokenIdCodec.toValue(id));
        return getByTokenHash(tokenHash);
    }

    @Override
    public PrincipalRefreshToken getByTokenCode(PrincipalRefreshTokenCode tokenCode) {
        if (tokenCode == null) {
            return null;
        }
        String tokenHash = (String) cache.get(TOKEN_CODE_PREFIX + tokenCode.value());
        return getByTokenHash(tokenHash);
    }

    @Override
    public PrincipalRefreshToken getByToken(String token) {
        return getByTokenHash(tokenHash(token));
    }

    private PrincipalRefreshToken getByTokenHash(String tokenHash) {
        return StringUtils.isBlank(tokenHash)
                ? null
                : toEntity((PrincipalRefreshTokenCacheDTO) cache.get(TOKEN_HASH_PREFIX + tokenHash));
    }

    @Override
    public List<PrincipalRefreshToken> listByPrincipalKeyAndClientIdAndStatus(
            PrincipalKey principalKey, String clientId, PrincipalTokenStatus status) {
        if (principalKey == null || status == null) {
            return new ArrayList<>();
        }
        String indexKey = principalIndexKey(principalKey, clientId, status);
        redis().zremrangebyscore(indexKey, 0, System.currentTimeMillis());
        List<String> tokenHashes = redis().zrange(indexKey, 0, -1);
        List<PrincipalRefreshToken> tokens = new ArrayList<>();
        for (String tokenHash : tokenHashes) {
            PrincipalRefreshToken token = getByTokenHash(tokenHash);
            if (token != null) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    @Override
    public PrincipalRefreshTokenId insert(PrincipalRefreshToken refreshToken, String token) {
        Assert.notNull(refreshToken, "refreshToken can not be null");
        Assert.notNull(refreshToken.getTokenCode(), "tokenCode can not be null");
        Assert.hasText(token, "token can not be blank");
        Assert.notNull(refreshToken.getPrincipalKey(), "principalKey can not be null");
        Assert.notNull(refreshToken.getExpireAt(), "expireAt can not be null");
        Assert.notNull(refreshToken.getStatus(), "status can not be null");
        if (refreshToken.getId() == null) {
            refreshToken.setId(PrincipalRefreshTokenIdCodec.nextId(idGenerator));
        }
        putToken(refreshToken, tokenHash(token));
        return refreshToken.getId();
    }

    @Override
    public int updateStatus(PrincipalRefreshToken refreshToken) {
        Assert.notNull(refreshToken, "refreshToken can not be null");
        String tokenHash = tokenHashById(refreshToken.getId());
        PrincipalRefreshToken oldToken = getByTokenHash(tokenHash);
        if (oldToken != null) {
            redis().zrem(principalIndexKey(oldToken), tokenHashById(oldToken.getId()));
        }
        Assert.notNull(refreshToken.getExpireAt(), "expireAt can not be null");
        Assert.notNull(refreshToken.getStatus(), "status can not be null");
        putToken(refreshToken, tokenHash);
        return 1;
    }

    private void putToken(PrincipalRefreshToken refreshToken, String tokenHash) {
        Assert.hasText(tokenHash, "tokenHash can not be blank");
        long seconds = remainingSeconds(refreshToken);
        cache.put(TOKEN_HASH_PREFIX + tokenHash, toCacheDTO(refreshToken), seconds + SAFETY_SECONDS, TimeUnit.SECONDS);
        cache.put(TOKEN_CODE_PREFIX + refreshToken.getTokenCode().value(), tokenHash, seconds, TimeUnit.SECONDS);
        cache.put(
                TOKEN_CODE_PREFIX + PrincipalRefreshTokenIdCodec.toValue(refreshToken.getId()),
                tokenHash,
                seconds,
                TimeUnit.SECONDS);
        redis().zadd(principalIndexKey(refreshToken), refreshToken.getExpireAt().getTime(), tokenHash);
    }

    private String tokenHashById(PrincipalRefreshTokenId id) {
        if (id == null) {
            return null;
        }
        return (String) cache.get(TOKEN_CODE_PREFIX + PrincipalRefreshTokenIdCodec.toValue(id));
    }

    private long remainingSeconds(PrincipalRefreshToken refreshToken) {
        Date expireAt = refreshToken.getExpireAt();
        if (expireAt == null) {
            return 1L;
        }
        long remainingMillis = expireAt.getTime() - System.currentTimeMillis();
        return remainingMillis <= 0 ? 1L : (remainingMillis + 999L) / 1000L;
    }

    private String tokenHash(String token) {
        return StringUtils.isBlank(token) ? null : Sha256Digest.hashBase64Url(token);
    }

    private String principalIndexKey(PrincipalRefreshToken refreshToken) {
        return principalIndexKey(refreshToken.getPrincipalKey(), refreshToken.getClientId(), refreshToken.getStatus());
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

    private static PrincipalRefreshToken toEntity(PrincipalRefreshTokenCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        PrincipalRefreshToken refreshToken = new PrincipalRefreshToken();
        refreshToken.setId(PrincipalRefreshTokenIdCodec.toDomain(cacheDTO.id));
        refreshToken.setTokenCode(PrincipalRefreshTokenCode.ofNullable(cacheDTO.tokenCode));
        refreshToken.setAccessTokenId(PrincipalAccessTokenIdCodec.toDomain(cacheDTO.accessTokenId));
        refreshToken.setClientId(cacheDTO.clientId);
        refreshToken.setSessionId(PrincipalAuthSessionIdCodec.toDomain(cacheDTO.sessionId));
        refreshToken.setPrincipalKey(PrincipalKey.of(PrincipalType.from(cacheDTO.principalType), cacheDTO.principalId));
        refreshToken.setIssuedAt(cacheDTO.issuedAt);
        refreshToken.setExpireAt(cacheDTO.expireAt);
        refreshToken.setStatus(PrincipalTokenStatus.from(cacheDTO.status));
        return refreshToken;
    }

    private static PrincipalRefreshTokenCacheDTO toCacheDTO(PrincipalRefreshToken refreshToken) {
        PrincipalRefreshTokenCacheDTO cacheDTO = new PrincipalRefreshTokenCacheDTO();
        cacheDTO.id = PrincipalRefreshTokenIdCodec.toValue(refreshToken.getId());
        cacheDTO.tokenCode = refreshToken.getTokenCode().value();
        cacheDTO.accessTokenId = PrincipalAccessTokenIdCodec.toValue(refreshToken.getAccessTokenId());
        cacheDTO.clientId = refreshToken.getClientId();
        cacheDTO.sessionId = PrincipalAuthSessionIdCodec.toValue(refreshToken.getSessionId());
        cacheDTO.principalType =
                refreshToken.getPrincipalKey().getPrincipalType().value();
        cacheDTO.principalId = refreshToken.getPrincipalKey().getPrincipalId();
        cacheDTO.issuedAt = refreshToken.getIssuedAt();
        cacheDTO.expireAt = refreshToken.getExpireAt();
        cacheDTO.status = refreshToken.getStatus().value();
        return cacheDTO;
    }

    private static class PrincipalRefreshTokenCacheDTO implements CacheDTO {
        private String id;
        private String tokenCode;
        private String accessTokenId;
        private String clientId;
        private String sessionId;
        private String principalType;
        private Long principalId;
        private Date issuedAt;
        private Date expireAt;
        private String status;
    }
}
