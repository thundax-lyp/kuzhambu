package com.thundax.kuzhambu.ai.infra;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.AnnotationBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AiInfraArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.ai";

    @Test
    void infraLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".infra");

        ModuleAndDependencyArchitectureRuleSupport.assertInfraLayerBoundary(classes, BASE_PACKAGE);
        ModuleAndDependencyArchitectureRuleSupport.assertPersistenceMappersOnlyCalledByRepositoryImpl(
                classes, BASE_PACKAGE);
        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "ai");
        AnnotationBoundaryArchitectureRuleSupport.assertInfraAnnotationBoundary(classes, BASE_PACKAGE);
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertRepositoryImplPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertPersistenceMapperPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertPersistenceDataObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertPersistenceAssemblerPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertPersistenceAssemblersDeclareStaticConversionMethods(classes);
        NamingArchitectureRuleSupport.assertMapperSourcesDeclareOnlyMapperAnnotation(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertDataObjectSourcesDeclareOnlyRequiredLombokAnnotations(
                Path.of("src/main/java"));
    }
}
