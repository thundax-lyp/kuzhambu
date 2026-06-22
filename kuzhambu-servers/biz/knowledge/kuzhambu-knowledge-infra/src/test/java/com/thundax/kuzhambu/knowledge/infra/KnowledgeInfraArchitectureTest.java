package com.thundax.kuzhambu.knowledge.infra;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

class KnowledgeInfraArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.knowledge";

    @Test
    void infraSpringBeansShouldDeclareSingleConstructor() {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".infra");

        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
    }
}
