package com.thundax.kuzhambu.classics.interfaces;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.InterfaceBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ClassicsInterfaceArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.classics";

    @Test
    void interfaceLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".interfaces");

        ModuleAndDependencyArchitectureRuleSupport.assertInterfaceLayerBoundary(classes, BASE_PACKAGE);
        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "classics");
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceNoPersistenceDependency(classes, BASE_PACKAGE);
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceOnlyCallsApplicationServices(classes, BASE_PACKAGE);
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceProtocolModelsStayInSameSubdomain(
                Path.of("src/main/java"));
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceProtocolsDoNotExposeDomainModels(
                Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        ApiAnnotationArchitectureRuleSupport.assertAdminControllersDeclareRequiredClassAnnotations(
                Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertAdminControllerMethodsDeclareRequiredAnnotations(
                Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsUseRequestResponseShape(Path.of("src/main/java"));
    }
}
