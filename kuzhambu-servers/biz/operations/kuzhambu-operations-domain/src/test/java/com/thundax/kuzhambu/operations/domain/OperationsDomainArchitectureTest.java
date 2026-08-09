package com.thundax.kuzhambu.operations.domain;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class OperationsDomainArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.operations";

    @Test
    void valueObjectIdsShouldDeclareNoStaticMethods() {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".domain");

        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertDomainServiceSourcesUseRepositoryBoundary(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertRepositoryInterfaceMethodNames(
                classes,
                NamingArchitectureRuleSupport.legacyRepositoryInterfaceMethodNameAllowances(
                        "com.thundax.kuzhambu.operations.domain.cleanup.repository.CleanupJobRepository.deleteItemsByJobId",
                        "com.thundax.kuzhambu.operations.domain.health.repository.HealthAlertRepository.getOpenBySource"));
    }
}
