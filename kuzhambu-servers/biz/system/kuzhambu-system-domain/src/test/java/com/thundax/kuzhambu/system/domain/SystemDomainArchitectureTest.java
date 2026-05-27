package com.thundax.kuzhambu.system.domain;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SystemDomainArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.system";

    @Test
    void domainCodecShouldStayInDomainCodecPackage() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".domain");

        NamingArchitectureRuleSupport.assertCodecPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertBaseIdTypes(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertEntitySourcesDeclareOnlyRequiredAnnotations(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertDomainEnumPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertRepositoryPlacement(classes, BASE_PACKAGE);
    }
}
