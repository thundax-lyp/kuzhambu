package com.thundax.kuzhambu.common.mybatis.configure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.thundax.kuzhambu.common.core.id.EntityId;
import com.thundax.kuzhambu.common.mybatis.typehandler.EntityIdTypeHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class MybatisPlusConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MybatisPlusConfiguration.class))
            .withPropertyValues("spring.datasource.url=jdbc:mysql://127.0.0.1:3306/kuzhambu");

    @Test
    public void shouldRegisterMybatisPlusBeans() {
        contextRunner.run(context -> {
            context.getBean(MybatisPlusInterceptor.class);
            context.getBean(ConfigurationCustomizer.class);
        });
    }

    @Test
    public void shouldRegisterOptimisticLockerBeforePagination() {
        contextRunner.run(context -> {
            MybatisPlusInterceptor interceptor = context.getBean(MybatisPlusInterceptor.class);

            assertEquals(2, interceptor.getInterceptors().size());
            assertTrue(interceptor.getInterceptors().get(0) instanceof OptimisticLockerInnerInterceptor);
            assertTrue(interceptor.getInterceptors().get(1) instanceof PaginationInnerInterceptor);
        });
    }

    @Test
    public void shouldInferPaginationDbTypeFromDatasourceUrl() {
        contextRunner
                .withPropertyValues("spring.datasource.url=jdbc:dm://127.0.0.1:5236/KUZHAMBU")
                .run(context -> {
                    MybatisPlusInterceptor interceptor = context.getBean(MybatisPlusInterceptor.class);
                    PaginationInnerInterceptor pagination = (PaginationInnerInterceptor)
                            interceptor.getInterceptors().get(1);

                    assertEquals(DbType.DM, pagination.getDbType());
                });
    }

    @Test
    public void shouldInferPaginationDbTypeFromDatasourceDriverWhenUrlMissing() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MybatisPlusConfiguration.class))
                .withPropertyValues("spring.datasource.driver-class-name=dm.jdbc.driver.DmDriver");

        runner.run(context -> {
            MybatisPlusInterceptor interceptor = context.getBean(MybatisPlusInterceptor.class);
            PaginationInnerInterceptor pagination =
                    (PaginationInnerInterceptor) interceptor.getInterceptors().get(1);

            assertEquals(DbType.DM, pagination.getDbType());
        });
    }

    @Test
    public void shouldRejectUnsupportedDatasourceType() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MybatisPlusConfiguration.class))
                .withPropertyValues("spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/kuzhambu");

        runner.run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
    }

    @Test
    public void shouldRejectMissingDatasourceConfiguration() {
        ApplicationContextRunner runner =
                new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(MybatisPlusConfiguration.class));

        runner.run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
    }

    @Test
    public void shouldRegisterDefaultTypeHandlers() {
        contextRunner.run(context -> {
            MybatisConfiguration configuration = new MybatisConfiguration();

            context.getBean(ConfigurationCustomizer.class).customize(configuration);

            assertNotNull(configuration.getTypeHandlerRegistry().getTypeHandler(EntityId.class));
            assertTrue(
                    configuration.getTypeHandlerRegistry().getTypeHandler(EntityId.class)
                            instanceof EntityIdTypeHandler);
        });
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable cause = throwable;
        while (cause != null) {
            if (causeType.isInstance(cause)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
