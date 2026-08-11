package com.thundax.kuzhambu.ai.domain;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.AnnotationBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class AiDomainArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.ai";

    @Test
    void domainLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".domain");

        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "ai");
        AnnotationBoundaryArchitectureRuleSupport.assertDomainSpringAndPersistenceFree(classes, BASE_PACKAGE);
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        NamingArchitectureRuleSupport.assertCodecPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBaseIdTypes(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertEntitySourcesDeclareOnlyRequiredAnnotations(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertDomainEnumPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertDomainServiceSourcesUseRepositoryBoundary(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertRepositoryPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertRepositoryInterfaceMethodNames(classes, Collections.emptyList());
    }
}
