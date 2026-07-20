package com.thundax.kuzhambu.common.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

public class IntegrationRedisCleanerTest {

    @Test
    public void shouldCleanKeysByScanInsteadOfKeysCommand() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        @SuppressWarnings("unchecked")
        Cursor<byte[]> cursor = mock(Cursor.class);
        byte[] key = "it:user:1".getBytes(StandardCharsets.UTF_8);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(key);
        when(connection.del(any(byte[][].class))).thenReturn(1L);

        long deleted = new IntegrationRedisCleaner(connectionFactory).cleanByPrefix("it:");

        assertEquals(1L, deleted);
        verify(connection, never()).keys(any(byte[].class));
        verify(connection).close();
    }
}
