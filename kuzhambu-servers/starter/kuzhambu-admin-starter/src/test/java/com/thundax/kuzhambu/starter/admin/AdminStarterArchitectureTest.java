package com.thundax.kuzhambu.starter.admin;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.StarterArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

class AdminStarterArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.starter.admin";

    @Test
    void starterShouldContainRuntimeAssemblyOnly() {
        JavaClasses classes = importPackages(BASE_PACKAGE);

        StarterArchitectureRuleSupport.assertStarterContainsOnlyRuntimeAssembly(classes, BASE_PACKAGE);
    }

    @Test
    void adminStarterShouldScanSystemAndStorageRuntimePackages() {
        SpringBootApplication application = KuzhambuAdminApplication.class.getAnnotation(SpringBootApplication.class);
        MapperScan mapperScan = KuzhambuAdminApplication.class.getAnnotation(MapperScan.class);

        Assertions.assertThat(application.scanBasePackages())
                .contains(
                        "com.thundax.kuzhambu.system.application",
                        "com.thundax.kuzhambu.system.infra",
                        "com.thundax.kuzhambu.system.interfaces.admin",
                        "com.thundax.kuzhambu.storage.application",
                        "com.thundax.kuzhambu.storage.infra",
                        "com.thundax.kuzhambu.storage.interfaces.admin");
        Assertions.assertThat(Arrays.asList(mapperScan.value()))
                .contains(
                        "com.thundax.kuzhambu.system.infra.core.persistence.mapper",
                        "com.thundax.kuzhambu.system.infra.auth.persistence.mapper",
                        "com.thundax.kuzhambu.system.infra.audit.persistence.mapper",
                        "com.thundax.kuzhambu.storage.infra.object.persistence.mapper");
    }

    @Test
    void adminStarterShouldEnableSchedulingForRuntimeCleanupTasks() {
        Assertions.assertThat(KuzhambuAdminApplication.class.getAnnotation(EnableScheduling.class))
                .isNotNull();
    }
}
