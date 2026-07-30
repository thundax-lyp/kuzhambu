package com.thundax.kuzhambu.system.domain.auth.model.entity;

import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalAuthSessionIdCodec;
import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalClientIdCodec;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalClientId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalAuthSession {
    private static final SnowflakeIdGenerator ID_GENERATOR = new SnowflakeIdGenerator();

    private PrincipalAuthSessionId id;
    private PrincipalKey principalKey;
    private PrincipalClientId clientId;
    private Map<String, Object> values = new LinkedHashMap<>();
    private Instant issuedAt;
    private Instant lastAccessTime;
    private Instant expireAt;

    public static PrincipalAuthSession create(
            PrincipalKey principalKey, PrincipalClientId clientId, Instant issuedAt, long ttlSeconds) {
        if (principalKey == null) {
            throw new IllegalArgumentException("principalKey can not be null");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("clientId can not be blank");
        }
        if (issuedAt == null) {
            throw new IllegalArgumentException("issuedAt can not be null");
        }
        if (ttlSeconds <= 0L) {
            throw new IllegalArgumentException("ttlSeconds must be greater than 0");
        }
        return new PrincipalAuthSession(
                PrincipalAuthSessionIdCodec.toDomain(nextHexSnowflakeId()),
                principalKey,
                clientId,
                new LinkedHashMap<>(),
                issuedAt,
                issuedAt,
                issuedAt.plusSeconds(ttlSeconds));
    }

    public static PrincipalAuthSession create(
            PrincipalKey principalKey, String clientId, Instant issuedAt, long ttlSeconds) {
        return create(principalKey, PrincipalClientIdCodec.toDomain(clientId), issuedAt, ttlSeconds);
    }

    public static PrincipalAuthSession restore(
            PrincipalAuthSessionId id,
            PrincipalKey principalKey,
            PrincipalClientId clientId,
            Map<String, Object> values,
            Instant issuedAt,
            Instant lastAccessTime,
            Instant expireAt) {
        if (id == null || principalKey == null || clientId == null) {
            throw new IllegalArgumentException("principal auth session state can not be null");
        }
        return new PrincipalAuthSession(
                id,
                principalKey,
                clientId,
                values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values),
                issuedAt,
                lastAccessTime,
                expireAt);
    }

    public static PrincipalAuthSession restore(
            PrincipalAuthSessionId id,
            PrincipalKey principalKey,
            String clientId,
            Map<String, Object> values,
            Instant issuedAt,
            Instant lastAccessTime,
            Instant expireAt) {
        return restore(
                id,
                principalKey,
                PrincipalClientIdCodec.toDomain(clientId),
                values,
                issuedAt,
                lastAccessTime,
                expireAt);
    }

    public boolean isExpired(Instant now) {
        return expireAt != null && now != null && !expireAt.isAfter(now);
    }

    public int remainingSeconds(Instant now) {
        if (expireAt == null || now == null) {
            return 0;
        }
        long remainingMillis = Duration.between(now, expireAt).toMillis();
        if (remainingMillis <= 0L) {
            return 0;
        }
        return (int) Math.max(1L, remainingMillis / 1000L);
    }

    public Map<String, Object> getValues() {
        if (values == null) {
            values = new LinkedHashMap<>();
        }
        return values;
    }

    public void setClientId(String clientId) {
        this.clientId = PrincipalClientIdCodec.toDomain(clientId);
    }

    public void setClientId(PrincipalClientId clientId) {
        this.clientId = clientId;
    }

    private static String nextHexSnowflakeId() {
        return Long.toHexString(ID_GENERATOR.nextId().value());
    }
}
