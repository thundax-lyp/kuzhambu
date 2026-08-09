package com.thundax.kuzhambu.system.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.AnnotationBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.ImplContractArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.LayerArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SortableArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.TransactionArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class SystemApplicationArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.system";

    @Test
    void applicationLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".application");

        ModuleAndDependencyArchitectureRuleSupport.assertApplicationLayerBoundary(classes, BASE_PACKAGE);
        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "system");
        AnnotationBoundaryArchitectureRuleSupport.assertApplicationNoHttpAnnotations(classes, BASE_PACKAGE);
        TransactionArchitectureRuleSupport.assertTransactionalOnlyOnApplicationServiceUseCases(classes, BASE_PACKAGE);
        LayerArchitectureRuleSupport.assertServiceBoundaryTypesClean(classes);
        LayerArchitectureRuleSupport.assertApplicationServiceBoundaryClean(
                classes, legacyApplicationServiceBoundaryAllowances());
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(classes, Collections.emptySet());
        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                classes, Collections.emptySet());
        NamingArchitectureRuleSupport.assertApplicationServicesUseApplicationServiceSuffix(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertCodecPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesAreRecords(
                Path.of("src/main/java"), SystemApplicationCommandQueryRecordAllowances.legacyAllowances());
        NamingArchitectureRuleSupport.assertApplicationQueriesDoNotOwnPageState(
                Path.of("src/main/java"), Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")), Collections.emptyList());
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertServiceQueryObjectsUnderServiceQueryPackage(classes);
        SortableArchitectureRuleSupport.assertSortCommandsUseOrderedIdsOnly(Path.of("src/main/java"));
    }

    private static List<ArchitectureRuleAllowance> legacyApplicationServiceBoundaryAllowances() {
        return List.of(
                rawNoArg("METHOD_SHAPE:com.thundax.kuzhambu.system.application.auth.service."
                        + "PreAuthSessionApplicationService.countActiveSessions()"),
                rawNoArg("COUNT_RETURN:com.thundax.kuzhambu.system.application.auth.service."
                        + "PreAuthSessionApplicationService.countActiveSessions()"));
    }

    private static ArchitectureRuleAllowance rawNoArg(String key) {
        return ArchitectureRuleAllowance.of(
                key,
                "PreAuthSessionApplicationService.countActiveSessions is a legacy no-argument count method with an int return type.",
                "Change the return type to primitive long and decide whether the method should be renamed to a supported no-argument read shape or accept an explicit Query.");
    }
}
