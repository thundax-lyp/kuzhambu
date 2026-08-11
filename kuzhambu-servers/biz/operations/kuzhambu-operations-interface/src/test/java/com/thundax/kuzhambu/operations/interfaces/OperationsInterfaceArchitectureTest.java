package com.thundax.kuzhambu.operations.interfaces;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.ModelAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.PathArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class OperationsInterfaceArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.operations";

    @Test
    void interfaceApiSurfaceShouldKeepContractShape() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".interfaces");

        ApiAnnotationArchitectureRuleSupport.requestClassAnnotationsRequired(BASE_PACKAGE)
                .check(classes);
        ModelAnnotationArchitectureRuleSupport.assertResponseClassAnnotationsRequired(
                classes, BASE_PACKAGE, legacyResponseAnnotationAllowances());
        NamingArchitectureRuleSupport.assertConfigurationClassNames(classes);
        PathArchitectureRuleSupport.assertConfigurationClassPlacement(classes);
        ApiAnnotationArchitectureRuleSupport.assertControllerActionsUseVerbWhitelist(
                Path.of("src/main/java"), Collections.emptyList());
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsUseRequestResponseShape(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.operations.interfaces.admin.report.assembler.OperationsReportInterfaceAssembler",
                        "com.thundax.kuzhambu.operations.interfaces.admin.restore.assembler.OperationsRestoreInterfaceAssembler",
                        "com.thundax.kuzhambu.operations.interfaces.admin.task.assembler.OperationsTaskInterfaceAssembler"));
    }

    private static List<ArchitectureRuleAllowance> legacyResponseAnnotationAllowances() {
        return java.util.Arrays.stream(new String[] {})
                .map(
                        className -> ArchitectureRuleAllowance.of(
                                ModelAnnotationArchitectureRuleSupport.NAME_RESPONSE_REQUIRED_ANNOTATIONS
                                        + ":"
                                        + BASE_PACKAGE
                                        + ".interfaces."
                                        + className,
                                "Operations legacy API model is pending annotation normalization.",
                                "Add the required model annotations or migrate the protocol shape, then remove this allowance."))
                .toList();
    }
}
