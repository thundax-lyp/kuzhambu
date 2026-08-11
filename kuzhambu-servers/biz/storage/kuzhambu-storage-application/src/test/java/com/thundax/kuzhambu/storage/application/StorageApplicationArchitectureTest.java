package com.thundax.kuzhambu.storage.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.AnnotationBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.CrossApplicationIsolationArchitectureRuleSupport;
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

class StorageApplicationArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.storage";

    @Test
    void applicationLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".application");

        ModuleAndDependencyArchitectureRuleSupport.assertApplicationLayerBoundary(classes, BASE_PACKAGE);
        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "storage");
        CrossApplicationIsolationArchitectureRuleSupport.assertNoUnexpectedCrossApplicationDependency(
                classes, "storage");
        AnnotationBoundaryArchitectureRuleSupport.assertApplicationNoHttpAnnotations(classes, BASE_PACKAGE);
        TransactionArchitectureRuleSupport.assertTransactionalOnlyOnApplicationServiceUseCases(classes, BASE_PACKAGE);
        LayerArchitectureRuleSupport.assertServiceBoundaryTypesClean(classes);
        LayerArchitectureRuleSupport.assertApplicationServiceUseCaseMethodShapeClean(classes);
        LayerArchitectureRuleSupport.assertApplicationServiceBoundaryClean(classes, Collections.emptyList());
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(classes, Collections.emptyList());
        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                classes, Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationServicesUseApplicationServiceSuffix(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertCodecPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesAreRecords(
                Path.of("src/main/java"), StorageApplicationCommandQueryRecordAllowances.legacyAllowances());
        NamingArchitectureRuleSupport.assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-storage-interface/src/main/java")),
                legacyCommandQueryConstructionAllowances());
        NamingArchitectureRuleSupport.assertAssemblersDoNotReturnNullApplicationCommandOrQuery(
                java.util.List.of(Path.of("src/main/java"), Path.of("../kuzhambu-storage-interface/src/main/java")),
                Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationQueriesDoNotOwnPageState(
                Path.of("src/main/java"), Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")), Collections.emptyList());
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        SortableArchitectureRuleSupport.assertSortCommandsUseOrderedIdsOnly(Path.of("src/main/java"));
    }

    private static List<ArchitectureRuleAllowance> legacyCommandQueryConstructionAllowances() {
        return List.of(
                constructionViolation(
                        "com.thundax.kuzhambu.storage.application.facade.impl.StorageFacadeImpl#RemoveStorageObjectCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.storage.application.facade.impl.StorageFacadeImpl#GetStorageObjectQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.storage.application.facade.impl.StorageFacadeImpl#ListStorageReferencesQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController#StorageSortCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController#GetStorageObjectQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController#RemoveStorageObjectCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController#UploadStorageObjectCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController#InitMultipartUploadCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController#UploadMultipartPartCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController#CompleteMultipartUploadCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController#AbortMultipartUploadCommand:1"));
    }

    private static ArchitectureRuleAllowance constructionViolation(String ownerAndType) {
        return ArchitectureRuleAllowance.of(
                "COMMAND_QUERY_CONSTRUCTION:" + ownerAndType,
                "Storage legacy facade or controller constructs an application Command/Query directly.",
                "Move request or facade DTO conversion into the corresponding InterfaceAssembler or FacadeAssembler, then remove this allowance.");
    }
}
