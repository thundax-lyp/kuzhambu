package com.thundax.kuzhambu.system.infra.auth.repository.impl;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.apache.commons.lang3.StringUtils;

final class AuthRedisClientFactory {

    private AuthRedisClientFactory() {}

    static RedisClient create(String redisUrl, String redisPassword) {
        RedisURI redisUri = RedisURI.create(redisUrl);
        if (StringUtils.isNotBlank(redisPassword) && redisUri.getPassword() == null) {
            redisUri.setPassword(redisPassword);
        }
        return RedisClient.create(redisUri);
    }
}
