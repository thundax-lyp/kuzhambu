package com.thundax.kuzhambu.knowledge.interfaces;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.ModelAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeInterfaceArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.knowledge";

    @Test
    void interfaceSpringBeansShouldDeclareSingleConstructor() {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".interfaces");

        ModelAnnotationArchitectureRuleSupport.assertRequestClassAnnotationsRequired(
                classes, BASE_PACKAGE, legacyRequestAnnotationAllowances());
        ModelAnnotationArchitectureRuleSupport.assertResponseClassAnnotationsRequired(
                classes, BASE_PACKAGE, legacyResponseAnnotationAllowances());
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
    }

    @Test
    void interfaceApiAnnotationsShouldKeepContractShape() throws Exception {
        ApiAnnotationArchitectureRuleSupport.assertControllerActionsAvoidAmbiguousVerbs(Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsDoNotUsePathOrQueryParameters(
                Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.assembler.KnowledgeGraphWorkbenchInterfaceAssembler"));
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
                                "Knowledge legacy API model is pending annotation normalization.",
                                "Add the required model annotations or migrate the protocol shape, then remove this allowance."))
                .toList();
    }
}
