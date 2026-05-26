package com.thundax.kuzhambu.common.cache.configure;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
@EnableMethodCache(basePackages = "com.thundax.kuzhambu")
@EnableCreateCacheAnnotation
public class JetCacheConfiguration {

    static final String BASE_PACKAGE = "com.thundax.kuzhambu";
    static final int DEFAULT_STAT_INTERVAL_MINUTES = 15;

    @Bean
    public static RedisConfigurationValidator redisConfigurationValidator(Environment environment) {
        return new RedisConfigurationValidator(environment);
    }

    @Bean
    public static BeanPostProcessor jetCacheGlobalConfigBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof GlobalCacheConfig) {
                    GlobalCacheConfig globalCacheConfig = (GlobalCacheConfig) bean;
                    globalCacheConfig.setHiddenPackages(new String[] {BASE_PACKAGE});
                    globalCacheConfig.setAreaInCacheName(true);
                    globalCacheConfig.setPenetrationProtect(true);
                    if (globalCacheConfig.getStatIntervalMinutes() <= 0) {
                        globalCacheConfig.setStatIntervalMinutes(DEFAULT_STAT_INTERVAL_MINUTES);
                    }
                }
                return bean;
            }
        };
    }

    public static class RedisConfigurationValidator implements InitializingBean {

        private final Environment environment;

        public RedisConfigurationValidator(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void afterPropertiesSet() {
            String remoteType = environment.getProperty("jetcache.remote.default.type");
            if (remoteType != null && !remoteType.toLowerCase().contains("redis")) {
                return;
            }
            String jetcacheUri = environment.getProperty("jetcache.remote.default.uri");
            String springRedisUrl = environment.getProperty("spring.redis.url");
            if (!StringUtils.hasText(jetcacheUri) && !StringUtils.hasText(springRedisUrl)) {
                throw new IllegalStateException(
                        "Missing Redis configuration. Configure jetcache.remote.default.uri or spring.redis.url.");
            }
        }
    }
}
