package com.thundax.kuzhambu.starter.portal;

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
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

class PortalStarterArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.starter.portal";

    @Test
    void starterShouldContainRuntimeAssemblyOnly() {
        JavaClasses classes = importPackages(BASE_PACKAGE);

        StarterArchitectureRuleSupport.assertStarterContainsOnlyRuntimeAssembly(classes, BASE_PACKAGE);
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
    }

    @Test
    void portalStarterShouldScanPortalPackagesAndDisableDefaultSecurity() {
        SpringBootApplication application = KuzhambuPortalApplication.class.getAnnotation(SpringBootApplication.class);
        MapperScan mapperScan = KuzhambuPortalApplication.class.getAnnotation(MapperScan.class);

        Assertions.assertThat(application.exclude())
                .contains(SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class);
        Assertions.assertThat(application.scanBasePackages())
                .contains(
                        "com.thundax.kuzhambu.system.application",
                        "com.thundax.kuzhambu.system.infra",
                        "com.thundax.kuzhambu.storage.application",
                        "com.thundax.kuzhambu.storage.infra",
                        "com.thundax.kuzhambu.classics.application",
                        "com.thundax.kuzhambu.classics.infra",
                        "com.thundax.kuzhambu.classics.interfaces.portal",
                        "com.thundax.kuzhambu.knowledge.domain",
                        "com.thundax.kuzhambu.knowledge.application",
                        "com.thundax.kuzhambu.knowledge.infra",
                        "com.thundax.kuzhambu.knowledge.interfaces.portal",
                        "com.thundax.kuzhambu.ai.domain",
                        "com.thundax.kuzhambu.ai.application",
                        "com.thundax.kuzhambu.ai.infra",
                        "com.thundax.kuzhambu.discovery.application",
                        "com.thundax.kuzhambu.discovery.infra",
                        "com.thundax.kuzhambu.discovery.interfaces.portal");
        Assertions.assertThat(Arrays.asList(mapperScan.value()))
                .contains(
                        "com.thundax.kuzhambu.system.infra.core.persistence.mapper",
                        "com.thundax.kuzhambu.system.infra.auth.persistence.mapper",
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
    void portalStarterShouldNotRunClassicsPublicationSchedules() throws IOException {
        Assertions.assertThat(KuzhambuPortalApplication.class.getAnnotation(EnableScheduling.class))
                .isNull();
        Assertions.assertThat(loadApplicationYaml())
                .contains("enabled: ${KUZHAMBU_CLASSICS_PUBLICATION_ENABLED:false}")
                .contains("rocketmq:", "enabled: false");
    }

    private static String loadApplicationYaml() throws IOException {
        try (InputStream input =
                PortalStarterArchitectureTest.class.getClassLoader().getResourceAsStream("application.yml")) {
            Assertions.assertThat(input).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
