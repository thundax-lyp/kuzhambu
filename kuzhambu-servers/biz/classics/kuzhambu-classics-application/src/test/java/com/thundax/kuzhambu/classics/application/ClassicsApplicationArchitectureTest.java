package com.thundax.kuzhambu.classics.application;

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
        NamingArchitectureRuleSupport.assertCodecPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesAreRecords(
                Path.of("src/main/java"), ClassicsApplicationCommandQueryRecordAllowances.legacyAllowances());
        NamingArchitectureRuleSupport.assertApplicationQueriesDoNotOwnPageState(
                Path.of("src/main/java"), Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        SortableArchitectureRuleSupport.assertSortCommandsUseOrderedIdsOnly(Path.of("src/main/java"));
    }

    private static List<ArchitectureRuleAllowance> legacyApplicationServiceBoundaryAllowances() {
        return List.of(
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationCleanupApplicationService.claimEs(com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob, java.lang.String, java.time.Instant, java.time.Instant)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationCleanupApplicationService.claimFastGpt(com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob, java.lang.String, java.time.Instant, java.time.Instant)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationCleanupApplicationService.qualify(com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob, java.lang.String, boolean)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationCleanupApplicationService.fail(com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob, java.lang.String, boolean, java.lang.String)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationCleanupApplicationService.complete(com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob, java.lang.String, boolean)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                        + "ClassicsPublicationApplicationService.createBatch(java.util.List)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationExecutionApplicationService.releaseClaim(com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId, com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationExecutionApplicationService.claim(com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId, com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken, java.time.Instant, java.time.Instant)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationExecutionApplicationService.retry(com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId, com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken, java.time.Instant, java.lang.String, java.lang.String)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationExecutionApplicationService.fail(com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId, com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken, java.time.Instant, java.lang.String, java.lang.String)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationExecutionApplicationService.start(com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId, com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken, java.time.Instant, java.time.Instant)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.sancai.service."
                                + "SancaiAssetApplicationService.useImage(com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId, com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.sancai.service."
                                + "SancaiAssetApplicationService.useVisualAsset(com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId, com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.sancai.service."
                                + "SancaiAssetApplicationService.getImageContent(com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId, com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.sancai.service."
                                + "SancaiAssetApplicationService.pageShowcases(java.lang.String, java.lang.String, java.lang.String, java.time.Instant, java.time.Instant, com.thundax.kuzhambu.common.core.page.PageQuery)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.sancai.service."
                                + "SancaiAssetApplicationService.applyFusionDescription(com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId, com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId, java.lang.String)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.sancai.service."
                                + "SancaiAssetApplicationService.createGeneratedVisualAssetVersion(com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId, com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId, com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.sancai.service."
                                + "SancaiAssetApplicationService.getVisualAssetSourceContent(com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId, com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.sancai.service."
                                + "SancaiAssetApplicationService.updateVisualAsset(com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.sancai.service."
                                + "SancaiAssetApplicationService.pageShowcases(java.lang.String, com.thundax.kuzhambu.common.core.page.PageQuery)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.sancai.service."
                                + "SancaiAssetApplicationService.getVisualAssetGeneratedContent(com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId, com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.cleanup.service."
                                + "ClassicsCleanupApplicationService.listTargets(java.lang.String, java.time.Instant, java.lang.Integer, java.lang.Integer)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.classics.application.cleanup.service."
                        + "ClassicsCleanupApplicationService.executeTarget(java.lang.String, java.lang.Long)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.report.service."
                                + "ClassicsReportApplicationService.summary(java.time.Instant, java.time.Instant, java.lang.String)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationReconcileApplicationService.reconcileFailure(com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationReconcileApplicationService.succeed(com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob, java.time.Instant)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.wangqi.service."
                                + "WangqiDocumentApplicationService.changeStorageObject(com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId, com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationSnapshotBindApplicationService.bind(com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob, com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.search.service."
                                + "ClassicsSearchContentApplicationService.getPublicContent(java.lang.String, java.lang.String)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.search.service."
                                + "ClassicsSearchContentApplicationService.getWorkbenchContent(java.lang.String, java.lang.String)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.search.service."
                                + "ClassicsSearchContentApplicationService.listWorkbenchContents(java.lang.String, java.lang.String)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.content.service."
                                + "ClassicsContentApplicationService.sortQaPairs(java.lang.String, com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId, com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.content.service."
                                + "ClassicsContentApplicationService.deleteVersions(java.lang.String, com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.content.service."
                                + "ClassicsContentApplicationService.pageExportJobs(java.lang.String, java.lang.String, java.lang.String, com.thundax.kuzhambu.common.core.page.PageQuery)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.content.service."
                                + "ClassicsContentApplicationService.listTags(java.lang.String, com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.content.service."
                                + "ClassicsContentApplicationService.ensureVersioned(com.thundax.kuzhambu.classics.domain.content.model.Versionable, com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType, java.lang.String)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.content.service."
                                + "ClassicsContentApplicationService.listVersions(java.lang.String, com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.content.service."
                                + "ClassicsContentApplicationService.listQaPairs(java.lang.String, com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.content.service."
                                + "ClassicsContentApplicationService.applyAiResult(com.thundax.kuzhambu.classics.domain.content.model.Versionable, java.lang.String)"),
                publicationWorkflow(
                        "METHOD_SHAPE:com.thundax.kuzhambu.classics.application.publication.service."
                                + "ClassicsPublicationContentCommitApplicationService.commit(com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob, com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken)"));
    }

    private static ArchitectureRuleAllowance publicationWorkflow(String key) {
        return ArchitectureRuleAllowance.of(
                key,
                "Classics publication internal workflow service exposes entity/token/time parameters instead of a Command.",
                "Move the workflow method behind support/executor semantics or introduce a dedicated publication Command object, then remove this allowance.");
    }

    private static ArchitectureRuleAllowance rawParameters(String key) {
        return ArchitectureRuleAllowance.of(
                key,
                "Classics ApplicationService method uses naked values, domain entities, collections, or scattered query parameters.",
                "Introduce a dedicated Query/Command or strong domain value object for the method input, then remove this allowance.");
    }
}
