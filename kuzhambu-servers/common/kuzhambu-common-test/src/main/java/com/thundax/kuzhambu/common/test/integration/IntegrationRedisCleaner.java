package com.thundax.kuzhambu.common.test.integration;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

public class IntegrationRedisCleaner {

    private final RedisConnectionFactory connectionFactory;

    public IntegrationRedisCleaner(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public long cleanByPrefix(String prefix) {
        if (isBlank(prefix)) {
            throw new IllegalArgumentException("Redis integration key prefix must not be blank.");
        }
        RedisConnection connection = connectionFactory.getConnection();
        try {
            Set<byte[]> keys = connection.keys((prefix + "*").getBytes(StandardCharsets.UTF_8));
            if (keys == null || keys.isEmpty()) {
                return 0L;
            }
            return connection.del(keys.toArray(new byte[keys.size()][]));
        } finally {
            connection.close();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
