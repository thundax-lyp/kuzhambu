package com.thundax.kuzhambu.common.cache.configure;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.mock.env.MockEnvironment;

public class JetCacheConfigurationTest {

    @Test
    public void shouldCustomizeGlobalCacheConfigWithProjectDefaults() {
        BeanPostProcessor beanPostProcessor = JetCacheConfiguration.jetCacheGlobalConfigBeanPostProcessor();
        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();

        Object processed = beanPostProcessor.postProcessAfterInitialization(globalCacheConfig, "globalCacheConfig");

        assertSame(globalCacheConfig, processed);
        assertArrayEquals(new String[] {JetCacheConfiguration.BASE_PACKAGE}, globalCacheConfig.getHiddenPackages());
        assertTrue(globalCacheConfig.isAreaInCacheName());
        assertTrue(globalCacheConfig.isPenetrationProtect());
        assertEquals(JetCacheConfiguration.DEFAULT_STAT_INTERVAL_MINUTES, globalCacheConfig.getStatIntervalMinutes());
    }

    @Test
    public void shouldKeepExplicitStatIntervalMinutes() {
        BeanPostProcessor beanPostProcessor = JetCacheConfiguration.jetCacheGlobalConfigBeanPostProcessor();
        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setStatIntervalMinutes(5);

        beanPostProcessor.postProcessAfterInitialization(globalCacheConfig, "globalCacheConfig");

        assertEquals(5, globalCacheConfig.getStatIntervalMinutes());
    }

    @Test
    public void shouldAcceptRedisUriConfiguration() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("jetcache.remote.default.type", "redis.lettuce")
                .withProperty("jetcache.remote.default.uri", "redis://127.0.0.1:6379/0");

        new JetCacheConfiguration.RedisConfigurationValidator(environment).afterPropertiesSet();
    }

    @Test
    public void shouldRejectMissingRedisUriConfiguration() throws Exception {
        MockEnvironment environment =
                new MockEnvironment().withProperty("jetcache.remote.default.type", "redis.lettuce");

        assertThrows(
                IllegalStateException.class,
                () -> new JetCacheConfiguration.RedisConfigurationValidator(environment).afterPropertiesSet());
    }
}
