package com.thundax.kuzhambu.classics.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.AnnotationBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ImplContractArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SortableArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.TransactionArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsApplicationArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.classics";
    private static final List<String> LEGACY_IMPL_CLASSES = List.of(
            "com.thundax.kuzhambu.classics.application.publication.service.impl"
                    + ".ClassicsPublicationCleanupApplicationServiceImpl",
            "com.thundax.kuzhambu.classics.application.publication.service.impl"
                    + ".ClassicsPublicationReconcileApplicationServiceImpl");
    private static final List<String> LEGACY_IMPL_DEPENDENCIES = List.of(
            ImplContractArchitectureRuleSupport.dependency(
                    "com.thundax.kuzhambu.classics.application.publication.scheduler"
                            + ".ClassicsPublicationEsCleanupScheduler",
                    "com.thundax.kuzhambu.classics.application.publication.service.impl"
                            + ".ClassicsPublicationCleanupApplicationServiceImpl"),
            ImplContractArchitectureRuleSupport.dependency(
                    "com.thundax.kuzhambu.classics.application.publication.scheduler"
                            + ".ClassicsPublicationFailureReconcileScheduler",
                    "com.thundax.kuzhambu.classics.application.publication.service.impl"
                            + ".ClassicsPublicationReconcileApplicationServiceImpl"),
            ImplContractArchitectureRuleSupport.dependency(
                    "com.thundax.kuzhambu.classics.application.publication.scheduler"
                            + ".ClassicsPublicationFastGptCleanupScheduler",
                    "com.thundax.kuzhambu.classics.application.publication.service.impl"
                            + ".ClassicsPublicationCleanupApplicationServiceImpl"),
            ImplContractArchitectureRuleSupport.dependency(
                    "com.thundax.kuzhambu.classics.application.publication.scheduler"
                            + ".ClassicsPublicationSuccessReconcileScheduler",
                    "com.thundax.kuzhambu.classics.application.publication.service.impl"
                            + ".ClassicsPublicationReconcileApplicationServiceImpl"));

    @Test
    void applicationLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".application");

        ModuleAndDependencyArchitectureRuleSupport.assertApplicationLayerBoundary(classes, BASE_PACKAGE);
        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "classics");
        AnnotationBoundaryArchitectureRuleSupport.assertApplicationNoHttpAnnotations(classes, BASE_PACKAGE);
        TransactionArchitectureRuleSupport.assertTransactionalOnlyOnApplicationServiceUseCases(classes, BASE_PACKAGE);
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(classes, LEGACY_IMPL_CLASSES);
        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                classes, LEGACY_IMPL_DEPENDENCIES);
        NamingArchitectureRuleSupport.assertApplicationServicesUseApplicationServiceSuffix(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertCodecPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        SortableArchitectureRuleSupport.assertSortCommandsUseOrderedIdsOnly(Path.of("src/main/java"));
    }
}
