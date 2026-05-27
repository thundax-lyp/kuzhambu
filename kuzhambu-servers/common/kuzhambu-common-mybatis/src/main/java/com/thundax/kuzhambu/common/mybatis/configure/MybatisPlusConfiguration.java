package com.thundax.kuzhambu.common.mybatis.configure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.thundax.kuzhambu.common.core.id.EntityId;
import com.thundax.kuzhambu.common.mybatis.typehandler.EntityIdTypeHandler;
import com.thundax.kuzhambu.common.mybatis.typehandler.StringListJsonTypeHandler;
import java.util.List;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class MybatisPlusConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(DataSourceProperties properties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(resolveDbType(properties)));
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean(name = "mybatisPlusTypeHandlerCustomizer")
    public ConfigurationCustomizer mybatisPlusTypeHandlerCustomizer() {
        return configuration -> registerDefaultTypeHandlers(configuration.getTypeHandlerRegistry());
    }

    @Bean
    @ConditionalOnMissingBean(name = "mybatisPlusBannerCustomizer")
    public MybatisPlusPropertiesCustomizer mybatisPlusBannerCustomizer() {
        return properties -> {
            GlobalConfig globalConfig = properties.getGlobalConfig();
            if (globalConfig == null) {
                globalConfig = new GlobalConfig();
                properties.setGlobalConfig(globalConfig);
            }
            globalConfig.setBanner(false);
        };
    }

    private void registerDefaultTypeHandlers(TypeHandlerRegistry registry) {
        registry.register(EntityId.class, EntityIdTypeHandler.class);
        registry.register(List.class, StringListJsonTypeHandler.class);
    }

    private DbType resolveDbType(DataSourceProperties properties) {
        if (properties.getUrl() == null && properties.getDriverClassName() == null) {
            throw new IllegalStateException(
                    "Missing datasource configuration. Configure spring.datasource.url or spring.datasource.driver-class-name.");
        }
        DbType dbType = resolveDbType(properties.getUrl());
        if (dbType != DbType.OTHER) {
            return dbType;
        }
        dbType = resolveDbType(properties.getDriverClassName());
        if (dbType != DbType.OTHER) {
            return dbType;
        }
        throw new IllegalStateException("Unsupported datasource type. Kuzhambu currently supports MYSQL and DM only.");
    }

    private DbType resolveDbType(String value) {
        if (value == null) {
            return DbType.OTHER;
        }
        String normalized = value.toLowerCase();
        if (normalized.contains(":mysql:") || normalized.contains("mysql")) {
            return DbType.MYSQL;
        }
        if (normalized.contains(":dm:") || normalized.contains("dm.jdbc")) {
            return DbType.DM;
        }
        return DbType.OTHER;
    }
}
