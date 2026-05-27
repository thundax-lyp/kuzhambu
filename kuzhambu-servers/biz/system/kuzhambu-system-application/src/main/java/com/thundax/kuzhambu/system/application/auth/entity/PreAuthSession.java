package com.thundax.kuzhambu.system.application.auth.entity;

import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PreAuthSessionToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PreAuthSession {
    private static final int MAX_REFRESH_TOKEN_SIZE = 5;
    private static final SnowflakeIdGenerator ID_GENERATOR = new SnowflakeIdGenerator();

    @Getter
    private PreAuthSessionId id;

    @Getter
    private PreAuthSessionToken token;

    @Getter
    private long expiredAt;

    private List<RefreshTokenValue> refreshTokens = new ArrayList<>();
    private Map<String, PreAuthSessionValue> items = new LinkedHashMap<>();

    public static PreAuthSession create(int expiredSeconds) {
        if (expiredSeconds <= 0) {
            throw new IllegalArgumentException("expiredSeconds must be greater than 0");
        }

        PreAuthSessionToken refreshToken = PreAuthSessionToken.of(nextHexSnowflakeId());
        long expiredAt = expiredAt(expiredSeconds);
        return new PreAuthSession(
                PreAuthSessionId.of(nextHexSnowflakeId()),
                PreAuthSessionToken.of(nextHexSnowflakeId()),
                expiredAt,
                new ArrayList<>(Collections.singleton(new RefreshTokenValue(refreshToken, expiredAt))),
                new LinkedHashMap<>());
    }

    public static PreAuthSession restore(
            PreAuthSessionId id, PreAuthSessionToken token, List<RefreshTokenValue> refreshTokens, long expiredAt) {
        return restore(id, token, refreshTokens, expiredAt, new LinkedHashMap<>());
    }

    public static PreAuthSession restore(
            PreAuthSessionId id,
            PreAuthSessionToken token,
            List<RefreshTokenValue> refreshTokens,
            long expiredAt,
            Map<String, PreAuthSessionValue> items) {
        if (id == null || token == null || refreshTokens == null || refreshTokens.isEmpty()) {
            throw new IllegalArgumentException("pre-auth session state can not be null");
        }
        return new PreAuthSession(id, token, expiredAt, new ArrayList<>(refreshTokens), new LinkedHashMap<>(items));
    }

    public void refresh(int expiredSeconds, int refreshTokenGraceSeconds) {
        if (expiredSeconds <= 0) {
            throw new IllegalArgumentException("expiredSeconds must be greater than 0");
        }
        if (refreshTokenGraceSeconds <= 0) {
            throw new IllegalArgumentException("refreshTokenGraceSeconds must be greater than 0");
        }
        long now = System.currentTimeMillis();
        token = PreAuthSessionToken.of(nextHexSnowflakeId());
        PreAuthSessionToken refreshToken = PreAuthSessionToken.of(nextHexSnowflakeId());
        expiredAt = expiredAt(expiredSeconds, now);
        List<RefreshTokenValue> acceptedRefreshTokens = new ArrayList<>();
        acceptedRefreshTokens.add(new RefreshTokenValue(refreshToken, expiredAt));
        long graceExpiredAt = expiredAt(refreshTokenGraceSeconds, now);
        for (RefreshTokenValue oldRefreshToken : refreshTokens) {
            if (oldRefreshToken.getExpiredAt() > now) {
                acceptedRefreshTokens.add(new RefreshTokenValue(
                        oldRefreshToken.getToken(), Math.min(oldRefreshToken.getExpiredAt(), graceExpiredAt)));
            }
        }
        refreshTokens = acceptedRefreshTokens;
        while (refreshTokens.size() > MAX_REFRESH_TOKEN_SIZE) {
            refreshTokens.remove(refreshTokens.size() - 1);
        }
    }

    public String findValue(String name) {
        PreAuthSessionValue item = items.get(name);
        if (item == null || item.getExpiredAt() <= System.currentTimeMillis()) {
            return null;
        }
        return item.getValue();
    }

    public void upsertValue(String name, String value, long expiredAt) {
        items.put(name, new PreAuthSessionValue(value, expiredAt));
    }

    public boolean isExpired() {
        return expiredAt <= System.currentTimeMillis();
    }

    public PreAuthSessionToken getRefreshToken() {
        return refreshTokens.get(0).getToken();
    }

    public List<RefreshTokenValue> refreshTokenValues() {
        return Collections.unmodifiableList(refreshTokens);
    }

    public Map<String, PreAuthSessionValue> itemValues() {
        return Collections.unmodifiableMap(items);
    }

    private static String nextHexSnowflakeId() {
        return Long.toHexString(ID_GENERATOR.nextId().value());
    }

    private static long expiredAt(int expiredSeconds) {
        return expiredAt(expiredSeconds, System.currentTimeMillis());
    }

    private static long expiredAt(int expiredSeconds, long now) {
        return now + expiredSeconds * 1000L;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RefreshTokenValue {
        private final PreAuthSessionToken token;
        private final long expiredAt;

        public static RefreshTokenValue of(PreAuthSessionToken token, long expiredAt) {
            return new RefreshTokenValue(token, expiredAt);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PreAuthSessionValue {
        private final String value;
        private final long expiredAt;

        public static PreAuthSessionValue of(String value, long expiredAt) {
            return new PreAuthSessionValue(value, expiredAt);
        }
    }
}
