package com.thundax.kuzhambu.operations.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class OperationsApplicationArchitectureTest extends AbstractArchitectureTest {

    @Test
    void applicationContractsShouldStayInDedicatedPackages() {
        JavaClasses classes = importPackages("com.thundax.kuzhambu.operations.application");

        NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(Path.of("src/main/java"));
    }
}
