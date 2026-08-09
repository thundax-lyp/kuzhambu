package com.thundax.kuzhambu.common.web;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.PathArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SourceHardRuleArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CommonWebArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.common.web";

    @Test
    void springBeansShouldDeclareSingleConstructor() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE);

        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        NamingArchitectureRuleSupport.assertConfigurationClassNames(classes);
        PathArchitectureRuleSupport.assertConfigurationClassPlacement(classes);
        SourceHardRuleArchitectureRuleSupport.assertConfigurationPropertiesDoNotDeclareBusinessControlFlow(
                Path.of("src/main/java"));
    }
}
