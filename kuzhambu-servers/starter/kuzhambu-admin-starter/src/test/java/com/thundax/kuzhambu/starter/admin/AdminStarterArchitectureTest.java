package com.thundax.kuzhambu.starter.admin;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.StarterArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
    }

    @Test
    void adminStarterShouldScanSystemStorageAndDiscoveryRuntimePackages() {
        SpringBootApplication application = KuzhambuAdminApplication.class.getAnnotation(SpringBootApplication.class);
        MapperScan mapperScan = KuzhambuAdminApplication.class.getAnnotation(MapperScan.class);

        Assertions.assertThat(application.scanBasePackages())
                .contains(
                        "com.thundax.kuzhambu.system.application",
                        "com.thundax.kuzhambu.system.infra",
                        "com.thundax.kuzhambu.system.interfaces.admin",
                        "com.thundax.kuzhambu.storage.application",
                        "com.thundax.kuzhambu.storage.infra",
                        "com.thundax.kuzhambu.storage.interfaces.admin",
                        "com.thundax.kuzhambu.discovery.application",
                        "com.thundax.kuzhambu.discovery.infra",
                        "com.thundax.kuzhambu.discovery.interfaces.admin");
        Assertions.assertThat(Arrays.asList(mapperScan.value()))
                .contains(
                        "com.thundax.kuzhambu.system.infra.core.persistence.mapper",
                        "com.thundax.kuzhambu.system.infra.auth.persistence.mapper",
                        "com.thundax.kuzhambu.system.infra.audit.persistence.mapper",
                        "com.thundax.kuzhambu.storage.infra.object.persistence.mapper",
                        "com.thundax.kuzhambu.discovery.infra.search.persistence.mapper");
    }

    @Test
    void adminStarterShouldEnableSchedulingForRuntimeCleanupTasks() {
        Assertions.assertThat(KuzhambuAdminApplication.class.getAnnotation(EnableScheduling.class))
                .isNotNull();
    }

    @Test
    void adminStarterShouldExposeOperationsBackupScheduleConfiguration() throws IOException {
        String applicationYaml = loadApplicationYaml();

        Assertions.assertThat(applicationYaml)
                .contains("root-path: ${KUZHAMBU_BACKUP_ROOT_PATH:/backup/kuzhambu}")
                .contains("enabled: ${KUZHAMBU_OPERATIONS_BACKUP_SCHEDULE_ENABLED:true}")
                .contains("startup-enabled: ${KUZHAMBU_OPERATIONS_BACKUP_STARTUP_ENABLED:true}")
                .contains("daily-cron: ${KUZHAMBU_OPERATIONS_BACKUP_DAILY_CRON:0 0 2 * * ?}");
    }

    @Test
    void adminStarterShouldExposeOperationsCleanupScheduleConfiguration() throws IOException {
        String applicationYaml = loadApplicationYaml();

        Assertions.assertThat(applicationYaml)
                .contains("enabled: ${KUZHAMBU_OPERATIONS_CLEANUP_SCHEDULE_ENABLED:true}")
                .contains("startup-enabled: ${KUZHAMBU_OPERATIONS_CLEANUP_STARTUP_ENABLED:false}")
                .contains("daily-cron: ${KUZHAMBU_OPERATIONS_CLEANUP_DAILY_CRON:0 30 3 * * ?}")
                .contains("default-limit: ${KUZHAMBU_OPERATIONS_CLEANUP_DEFAULT_LIMIT:200}")
                .contains("enabled: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_BACKUP_ENABLED:true}")
                .contains("retention-days: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_BACKUP_RETENTION_DAYS:30}")
                .contains("limit: ${KUZHAMBU_OPERATIONS_CLEANUP_EXPIRED_BACKUP_LIMIT:200}");
    }

    private String loadApplicationYaml() throws IOException {
        try (InputStream inputStream = KuzhambuAdminApplication.class.getResourceAsStream("/application.yml")) {
            Assertions.assertThat(inputStream).isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
