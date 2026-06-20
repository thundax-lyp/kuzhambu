package com.thundax.kuzhambu.starter.portal;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.StarterArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

class PortalStarterArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.starter.portal";

    @Test
    void starterShouldContainRuntimeAssemblyOnly() {
        JavaClasses classes = importPackages(BASE_PACKAGE);

        StarterArchitectureRuleSupport.assertStarterContainsOnlyRuntimeAssembly(classes, BASE_PACKAGE);
    }

    @Test
    void portalStarterShouldScanPortalPackagesAndDisableDefaultSecurity() {
        SpringBootApplication application = KuzhambuPortalApplication.class.getAnnotation(SpringBootApplication.class);
        MapperScan mapperScan = KuzhambuPortalApplication.class.getAnnotation(MapperScan.class);

        Assertions.assertThat(application.exclude()).contains(SecurityAutoConfiguration.class);
        Assertions.assertThat(application.scanBasePackages())
                .contains(
                        "com.thundax.kuzhambu.system.application",
                        "com.thundax.kuzhambu.system.infra",
                        "com.thundax.kuzhambu.storage.application",
                        "com.thundax.kuzhambu.storage.infra",
                        "com.thundax.kuzhambu.classics.application",
                        "com.thundax.kuzhambu.classics.infra",
                        "com.thundax.kuzhambu.classics.interfaces.portal");
        Assertions.assertThat(Arrays.asList(mapperScan.value()))
                .contains(
                        "com.thundax.kuzhambu.system.infra.core.persistence.mapper",
                        "com.thundax.kuzhambu.system.infra.auth.persistence.mapper",
                        "com.thundax.kuzhambu.storage.infra.object.persistence.mapper",
                        "com.thundax.kuzhambu.classics.infra.sharing.persistence.mapper");
    }
}
