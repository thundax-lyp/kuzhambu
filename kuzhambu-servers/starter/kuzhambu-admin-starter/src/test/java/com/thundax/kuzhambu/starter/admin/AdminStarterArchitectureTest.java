package com.thundax.kuzhambu.starter.admin;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.StarterArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
                        "com.thundax.kuzhambu.knowledge.domain",
                        "com.thundax.kuzhambu.knowledge.application",
                        "com.thundax.kuzhambu.knowledge.infra",
                        "com.thundax.kuzhambu.knowledge.interfaces.admin",
                        "com.thundax.kuzhambu.ai.domain",
                        "com.thundax.kuzhambu.ai.application",
                        "com.thundax.kuzhambu.ai.infra",
                        "com.thundax.kuzhambu.ai.interfaces.admin",
                        "com.thundax.kuzhambu.discovery.application",
                        "com.thundax.kuzhambu.discovery.infra",
                        "com.thundax.kuzhambu.discovery.interfaces.admin");
        Assertions.assertThat(Arrays.asList(mapperScan.value()))
                .contains(
                        "com.thundax.kuzhambu.system.infra.core.persistence.mapper",
                        "com.thundax.kuzhambu.system.infra.auth.persistence.mapper",
                        "com.thundax.kuzhambu.system.infra.audit.persistence.mapper",
                        "com.thundax.kuzhambu.storage.infra.object.persistence.mapper",
                        "com.thundax.kuzhambu.classics.infra.publication.persistence.mapper",
                        "com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper",
                        "com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper",
                        "com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper",
                        "com.thundax.kuzhambu.ai.infra.batch.persistence.mapper",
                        "com.thundax.kuzhambu.ai.infra.capability.persistence.mapper",
                        "com.thundax.kuzhambu.ai.infra.config.persistence.mapper",
                        "com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper",
                        "com.thundax.kuzhambu.ai.infra.model.persistence.mapper",
                        "com.thundax.kuzhambu.ai.infra.prompt.persistence.mapper",
                        "com.thundax.kuzhambu.ai.infra.refinement.persistence.mapper",
                        "com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper",
                        "com.thundax.kuzhambu.discovery.infra.search.persistence.mapper");
    }

    @Test
    void adminStarterShouldEnableSchedulingForRuntimeCleanupTasks() {
        Assertions.assertThat(KuzhambuAdminApplication.class.getAnnotation(EnableScheduling.class))
                .isNotNull();
    }

    @Test
    void adminStarterShouldOwnClassicsPublicationSchedules() throws IOException {
        String applicationYaml = loadApplicationYaml();

        Assertions.assertThat(applicationYaml)
                .contains("enabled: ${KUZHAMBU_CLASSICS_PUBLICATION_ENABLED:true}")
                .contains("dispatch-fixed-delay: ${KUZHAMBU_CLASSICS_PUBLICATION_DISPATCH_FIXED_DELAY:5s}")
                .contains(
                        "success-reconcile-fixed-delay: ${KUZHAMBU_CLASSICS_PUBLICATION_SUCCESS_RECONCILE_FIXED_DELAY:30s}")
                .contains(
                        "failure-reconcile-fixed-delay: ${KUZHAMBU_CLASSICS_PUBLICATION_FAILURE_RECONCILE_FIXED_DELAY:30s}")
                .contains("es-cleanup-fixed-delay: ${KUZHAMBU_CLASSICS_PUBLICATION_ES_CLEANUP_FIXED_DELAY:60s}")
                .contains(
                        "fastgpt-cleanup-fixed-delay: ${KUZHAMBU_CLASSICS_PUBLICATION_FASTGPT_CLEANUP_FIXED_DELAY:60s}");
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

    @Test
    void adminStarterShouldExposeOperationsExternalHealthProbeConfiguration() throws IOException {
        String applicationYaml = loadApplicationYaml();

        Assertions.assertThat(applicationYaml)
                .contains("enabled: ${KUZHAMBU_OPERATIONS_HEALTH_PROBES_ENABLED:false}")
                .contains("timeout-ms: ${KUZHAMBU_OPERATIONS_HEALTH_PROBES_TIMEOUT_MS:3000}")
                .contains("enabled: ${KUZHAMBU_OPERATIONS_HEALTH_PROBES_TARGETS_0_ENABLED:true}")
                .contains("component: ${KUZHAMBU_OPERATIONS_HEALTH_PROBES_TARGETS_0_COMPONENT:admin-starter}")
                .contains("url: ${KUZHAMBU_OPERATIONS_HEALTH_PROBES_TARGETS_0_URL:")
                .contains("expected-status: ${KUZHAMBU_OPERATIONS_HEALTH_PROBES_TARGETS_0_EXPECTED_STATUS:200}")
                .contains(
                        "degraded-latency-ms: ${KUZHAMBU_OPERATIONS_HEALTH_PROBES_TARGETS_0_DEGRADED_LATENCY_MS:1000}");
    }

    @Test
    void systemSeedShouldExposeOperationsHealthPageMenu() throws IOException {
        Path repoRoot = findRepoRoot();
        String systemJson = Files.readString(repoRoot.resolve("db/data-source/system.json"));

        Assertions.assertThat(systemJson)
                .contains("\"name\": \"健康检查\"")
                .contains("\"operations:health:view\"")
                .contains("\"url\": \"/operations/health\"");
    }

    @Test
    void systemSeedShouldExposeAiGovernanceMenus() throws IOException {
        Path repoRoot = findRepoRoot();
        String systemJson = Files.readString(repoRoot.resolve("db/data-source/system.json"));

        Assertions.assertThat(systemJson)
                .contains("\"url\": \"/ai/models\"")
                .contains("\"url\": \"/ai/prompts\"")
                .contains("\"url\": \"/ai/business-configs\"")
                .contains("\"url\": \"/ai/invocations\"")
                .doesNotContain("\"url\": \"/ai/action-status\"")
                .doesNotContain("\"url\": \"/ai/services\"")
                .doesNotContain("\"url\": \"/ai/capability-mappings\"");
    }

    private String loadApplicationYaml() throws IOException {
        try (InputStream inputStream = KuzhambuAdminApplication.class.getResourceAsStream("/application.yml")) {
            Assertions.assertThat(inputStream).isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Path findRepoRoot() {
        Path currentPath = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (currentPath != null) {
            if (Files.exists(currentPath.resolve("db/data-source/system.json"))) {
                return currentPath;
            }
            currentPath = currentPath.getParent();
        }
        throw new IllegalStateException("Cannot locate repository root from user.dir");
    }
}
