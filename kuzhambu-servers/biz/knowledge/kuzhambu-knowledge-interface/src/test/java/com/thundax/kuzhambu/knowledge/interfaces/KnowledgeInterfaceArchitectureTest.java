package com.thundax.kuzhambu.knowledge.interfaces;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

class KnowledgeInterfaceArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.knowledge";

    @Test
    void interfaceSpringBeansShouldDeclareSingleConstructor() {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".interfaces");

        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
    }
}
