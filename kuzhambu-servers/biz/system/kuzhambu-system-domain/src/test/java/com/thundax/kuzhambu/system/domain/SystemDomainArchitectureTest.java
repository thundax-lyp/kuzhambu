package com.thundax.kuzhambu.system.domain;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

class SystemDomainArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.system";

    @Test
    void domainCodecShouldStayInDomainCodecPackage() {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".domain");

        NamingArchitectureRuleSupport.assertCodecPlacement(classes, BASE_PACKAGE);
    }
}
