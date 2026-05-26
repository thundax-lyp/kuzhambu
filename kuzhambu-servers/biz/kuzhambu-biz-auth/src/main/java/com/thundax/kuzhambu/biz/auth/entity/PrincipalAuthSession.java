package com.thundax.kuzhambu.biz.auth.entity;

import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalAuthSessionId;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalKey;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PrincipalAuthSession {
    private static final SnowflakeIdGenerator ID_GENERATOR = new SnowflakeIdGenerator();

    private PrincipalAuthSessionId id;
    private PrincipalKey principalKey;
    private String clientId;
    private Map<String, Object> values = new LinkedHashMap<>();
    private Date issuedAt;
    private Date lastAccessTime;
    private Date expireAt;

    public static PrincipalAuthSession create(
            PrincipalKey principalKey, String clientId, Date issuedAt, long ttlSeconds) {
        if (principalKey == null) {
            throw new IllegalArgumentException("principalKey can not be null");
        }
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("clientId can not be blank");
        }
        if (issuedAt == null) {
            throw new IllegalArgumentException("issuedAt can not be null");
        }
        if (ttlSeconds <= 0L) {
            throw new IllegalArgumentException("ttlSeconds must be greater than 0");
        }
        return new PrincipalAuthSession(
                PrincipalAuthSessionId.of(nextHexSnowflakeId()),
                principalKey,
                clientId,
                new LinkedHashMap<>(),
                issuedAt,
                issuedAt,
                new Date(issuedAt.getTime() + ttlSeconds * 1000L));
    }

    public static PrincipalAuthSession restore(
            PrincipalAuthSessionId id,
            PrincipalKey principalKey,
            String clientId,
            Map<String, Object> values,
            Date issuedAt,
            Date lastAccessTime,
            Date expireAt) {
        if (id == null || principalKey == null || StringUtils.isBlank(clientId)) {
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

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }

    public int remainingSeconds(Date now) {
        if (expireAt == null || now == null) {
            return 0;
        }
        long remainingMillis = expireAt.getTime() - now.getTime();
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

    private static String nextHexSnowflakeId() {
        return Long.toHexString(ID_GENERATOR.nextId().value());
    }
}
