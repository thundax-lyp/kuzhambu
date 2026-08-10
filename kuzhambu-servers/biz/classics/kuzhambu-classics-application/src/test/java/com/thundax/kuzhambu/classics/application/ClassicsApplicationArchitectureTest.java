package com.thundax.kuzhambu.classics.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.AnnotationBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.ImplContractArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.LayerArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.PathArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SortableArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.TransactionArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsApplicationArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.classics";

    @Test
    void applicationLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".application");

        ModuleAndDependencyArchitectureRuleSupport.assertApplicationLayerBoundary(classes, BASE_PACKAGE);
        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "classics");
        AnnotationBoundaryArchitectureRuleSupport.assertApplicationNoHttpAnnotations(classes, BASE_PACKAGE);
        TransactionArchitectureRuleSupport.assertTransactionalOnlyOnApplicationServiceUseCases(classes, BASE_PACKAGE);
        LayerArchitectureRuleSupport.assertApplicationServiceBoundaryClean(
                classes, legacyApplicationServiceBoundaryAllowances());
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(classes, Collections.emptyList());
        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                classes, Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationServicesUseApplicationServiceSuffix(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertConfigurationClassNames(classes);
        PathArchitectureRuleSupport.assertConfigurationClassPlacement(classes);
        NamingArchitectureRuleSupport.assertCodecPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesAreRecords(
                Path.of("src/main/java"), ClassicsApplicationCommandQueryRecordAllowances.legacyAllowances());
        NamingArchitectureRuleSupport.assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-classics-interface/src/main/java")),
                legacyCommandQueryConstructionAllowances());
        NamingArchitectureRuleSupport.assertAssemblersDoNotReturnNullApplicationCommandOrQuery(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-classics-interface/src/main/java")),
                legacyAssemblerNullReturnAllowances());
        NamingArchitectureRuleSupport.assertApplicationQueriesDoNotOwnPageState(
                Path.of("src/main/java"), Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.classics.application.facade.assembler.ClassicsFacadeAssembler",
                        "com.thundax.kuzhambu.classics.application.sancai.assembler.SancaiApplicationFacadeAssembler"));
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        SortableArchitectureRuleSupport.assertSortCommandsUseOrderedIdsOnly(Path.of("src/main/java"));
    }

    private static List<ArchitectureRuleAllowance> legacyApplicationServiceBoundaryAllowances() {
        return List.of(
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.cleanup.service."
                                + "ClassicsCleanupApplicationService.listTargets(java.lang.String, java.time.Instant, java.lang.Integer, java.lang.Integer)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.classics.application.cleanup.service."
                        + "ClassicsCleanupApplicationService.executeTarget(java.lang.String, java.lang.Long)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.report.service."
                                + "ClassicsReportApplicationService.summary(java.time.Instant, java.time.Instant, java.lang.String)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.search.service."
                                + "ClassicsSearchContentApplicationService.getPublicContent(java.lang.String, java.lang.String)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.search.service."
                                + "ClassicsSearchContentApplicationService.getWorkbenchContent(java.lang.String, java.lang.String)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.search.service."
                                + "ClassicsSearchContentApplicationService.listWorkbenchContents(java.lang.String, java.lang.String)"));
    }

    private static List<ArchitectureRuleAllowance> legacyCommandQueryConstructionAllowances() {
        return List.of();
    }

    private static ArchitectureRuleAllowance constructionViolation(String ownerAndType) {
        return ArchitectureRuleAllowance.of(
                "COMMAND_QUERY_CONSTRUCTION:" + ownerAndType,
                "Classics support or controller code directly constructs an application Command/Query.",
                "Move request conversion into an InterfaceAssembler or application facade, then remove this allowance.");
    }

    private static List<ArchitectureRuleAllowance> legacyAssemblerNullReturnAllowances() {
        return List.of();
    }

    private static ArchitectureRuleAllowance rawParameters(String key) {
        return ArchitectureRuleAllowance.of(
                key,
                "Classics ApplicationService method uses naked values, domain entities, collections, or scattered query parameters.",
                "Introduce a dedicated Query/Command or strong domain value object for the method input, then remove this allowance.");
    }
}
