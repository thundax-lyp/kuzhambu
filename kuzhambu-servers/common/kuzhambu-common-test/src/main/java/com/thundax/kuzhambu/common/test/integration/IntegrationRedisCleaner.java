package com.thundax.kuzhambu.common.test.integration;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

public class IntegrationRedisCleaner {

    private static final int SCAN_COUNT = 1000;
    private static final int DELETE_BATCH_SIZE = 500;

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
            ScanOptions options = ScanOptions.scanOptions()
                    .match(prefix + "*")
                    .count(SCAN_COUNT)
                    .build();
            long deleted = 0L;
            List<byte[]> batch = new ArrayList<byte[]>();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    batch.add(cursor.next());
                    if (batch.size() >= DELETE_BATCH_SIZE) {
                        deleted += deleteBatch(connection, batch);
                    }
                }
            }
            return deleted + deleteBatch(connection, batch);
        } finally {
            connection.close();
        }
    }

    private long deleteBatch(RedisConnection connection, List<byte[]> batch) {
        if (batch.isEmpty()) {
            return 0L;
        }
        long deleted = connection.del(batch.toArray(new byte[batch.size()][]));
        batch.clear();
        return deleted;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
