package com.thundax.kuzhambu.classics.interfaces;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.InterfaceBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModelAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsInterfaceArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.classics";

    @Test
    void interfaceLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".interfaces");

        ModelAnnotationArchitectureRuleSupport.assertRequestClassAnnotationsRequired(
                classes, BASE_PACKAGE, legacyRequestAnnotationAllowances());
        ModelAnnotationArchitectureRuleSupport.assertResponseClassAnnotationsRequired(
                classes, BASE_PACKAGE, legacyResponseAnnotationAllowances());
        ModuleAndDependencyArchitectureRuleSupport.assertInterfaceLayerBoundary(classes, BASE_PACKAGE);
        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "classics");
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceNoPersistenceDependency(classes, BASE_PACKAGE);
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceOnlyCallsApplicationServices(classes, BASE_PACKAGE);
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceProtocolModelsStayInSameSubdomain(
                Path.of("src/main/java"));
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceProtocolsDoNotExposeDomainModels(
                Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        ApiAnnotationArchitectureRuleSupport.assertAdminControllersDeclareRequiredClassAnnotations(
                Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertAdminControllerMethodsDeclareRequiredAnnotations(
                Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertControllerActionsUseVerbWhitelist(Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsUseRequestResponseShape(Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsDoNotUsePathOrQueryParameters(
                Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.classics.interfaces.admin.publication.assembler.ClassicsPublicationInterfaceAssembler",
                        "com.thundax.kuzhambu.classics.interfaces.portal.sancai.assembler.SancaiPortalInterfaceAssembler"));
    }

    private static List<ArchitectureRuleAllowance> legacyRequestAnnotationAllowances() {
        return modelAnnotationAllowances(ModelAnnotationArchitectureRuleSupport.NAME_REQUEST_REQUIRED_ANNOTATIONS);
    }

    private static List<ArchitectureRuleAllowance> legacyResponseAnnotationAllowances() {
        return modelAnnotationAllowances(ModelAnnotationArchitectureRuleSupport.NAME_RESPONSE_REQUIRED_ANNOTATIONS);
    }

    private static List<ArchitectureRuleAllowance> modelAnnotationAllowances(String ruleName, String... classNames) {
        return java.util.Arrays.stream(classNames)
                .map(
                        className -> ArchitectureRuleAllowance.of(
                                ruleName + ":" + BASE_PACKAGE + ".interfaces." + className,
                                "Classics legacy API model is pending annotation normalization.",
                                "Add the required model annotations or migrate the protocol shape, then remove this allowance."))
                .toList();
    }
}
