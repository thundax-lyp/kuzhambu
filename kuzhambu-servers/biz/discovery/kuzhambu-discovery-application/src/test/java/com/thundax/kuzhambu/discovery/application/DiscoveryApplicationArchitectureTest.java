package com.thundax.kuzhambu.discovery.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.ImplContractArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.LayerArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.PathArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiscoveryApplicationArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.discovery";

    @Test
    void applicationCommandAndQuerySourcesShouldDeclareFieldsOnly() {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".application");

        LayerArchitectureRuleSupport.assertApplicationServiceBoundaryClean(
                classes, legacyApplicationServiceBoundaryAllowances());
        NamingArchitectureRuleSupport.assertConfigurationClassNames(classes);
        PathArchitectureRuleSupport.assertConfigurationClassPlacement(classes);
        ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(classes, Collections.emptySet());
        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                classes, Collections.emptySet());
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesAreRecords(
                Path.of("src/main/java"), DiscoveryApplicationCommandQueryRecordAllowances.legacyAllowances());
        NamingArchitectureRuleSupport.assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-discovery-interface/src/main/java")),
                legacyCommandQueryConstructionAllowances());
        NamingArchitectureRuleSupport.assertAssemblersDoNotReturnNullApplicationCommandOrQuery(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-discovery-interface/src/main/java")),
                legacyAssemblerNullReturnAllowances());
        NamingArchitectureRuleSupport.assertApplicationQueriesDoNotOwnPageState(
                Path.of("src/main/java"), Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.discovery.application.facade.assembler.DiscoveryFacadeAssembler",
                        "com.thundax.kuzhambu.discovery.application.facade.assembler.DiscoverySearchPublicationFacadeAssembler"));
    }

    private static List<ArchitectureRuleAllowance> legacyApplicationServiceBoundaryAllowances() {
        return List.of(
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.discovery.application.search.service."
                        + "SearchApplicationService.getEvent(java.lang.Long)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.discovery.application.qa.service."
                                + "QaApplicationService.getPortalSessionDetail(java.lang.Long, java.lang.String, java.lang.String)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.discovery.application.qa.service."
                        + "QaApplicationService.getSessionDetail(java.lang.Long)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.discovery.application.qa.service."
                                + "QaApplicationService.listPortalSessions(java.lang.String, java.lang.String, java.lang.Integer)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.discovery.application.qa.service."
                        + "QaApplicationService.listSourcesByMessageId(java.lang.Long)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.discovery.application.qa.service."
                        + "QaApplicationService.getTraceByTraceId(java.lang.Long)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.discovery.application.search.service."
                                + "SearchIndexSyncApplicationService.syncUpsert(java.lang.String, java.lang.String, java.lang.Integer)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.discovery.application.search.service."
                                + "SearchIndexSyncApplicationService.syncDelete(java.lang.String, java.lang.String, java.lang.Integer, java.time.Instant)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.discovery.application.search.service."
                        + "SearchIndexCleanupApplicationService.cleanupDeletedDocuments(int)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.discovery.application.report.service."
                                + "DiscoveryReportApplicationService.summary(java.time.Instant, java.time.Instant, java.lang.String)"));
    }

    private static List<ArchitectureRuleAllowance> legacyCommandQueryConstructionAllowances() {
        return List.of(
                constructionViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.DiscoveryQaAdminController#SyncKnowledgeContentCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.DiscoveryQaAdminController#KnowledgeSyncItemQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.DiscoveryQaAdminController#KnowledgeSyncItemQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.DiscoveryQaAdminController#DeleteQaSessionCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.DiscoveryQaAdminController#QaSessionQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.DiscoveryQaAdminController#QaSessionQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.DiscoveryQaAdminController#ExportQaSessionCommand:1"));
    }

    private static ArchitectureRuleAllowance constructionViolation(String ownerAndType) {
        return ArchitectureRuleAllowance.of(
                "COMMAND_QUERY_CONSTRUCTION:" + ownerAndType,
                "Discovery QA admin controller directly constructs an application Command/Query.",
                "Move request conversion into DiscoveryQaInterfaceAssembler, then remove this allowance.");
    }

    private static List<ArchitectureRuleAllowance> legacyAssemblerNullReturnAllowances() {
        return List.of(
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler.DiscoveryQaPortalInterfaceAssembler#toOpenSessionCommand:OpenQaSessionCommand:1"),
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler.DiscoveryQaPortalInterfaceAssembler#toChatCompletionCommand:ChatCompletionCommand:1"),
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler.DiscoveryQaPortalInterfaceAssembler#toDeleteSessionCommand:DeleteQaSessionCommand:1"),
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler.DiscoveryQaPortalInterfaceAssembler#toExportSessionCommand:ExportQaSessionCommand:1"),
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.portal.search.assembler.DiscoverySearchPortalInterfaceAssembler#toQuery:SearchQuery:1"),
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.portal.search.assembler.DiscoverySearchPortalInterfaceAssembler#toCommand:SearchClickEventCreateCommand:1"),
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.portal.search.assembler.DiscoverySearchPortalInterfaceAssembler#toQuery:SearchPreviewQuery:1"),
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler.DiscoverySearchStatisticsInterfaceAssembler#toQuery:SearchQuery:1"),
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler.DiscoverySearchStatisticsInterfaceAssembler#toQuery:SearchEventQuery:1"),
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler.DiscoverySearchStatisticsInterfaceAssembler#toCommand:SearchClickEventCreateCommand:1"),
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler.DiscoverySearchStatisticsInterfaceAssembler#toQuery:SearchPreviewQuery:1"),
                nullReturnViolation(
                        "com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler.DiscoverySearchStatisticsInterfaceAssembler#toQuery:SearchStatisticsSummaryQuery:1"));
    }

    private static ArchitectureRuleAllowance nullReturnViolation(String ownerMethodAndType) {
        return ArchitectureRuleAllowance.of(
                "COMMAND_QUERY_ASSEMBLER_NULL_RETURN:" + ownerMethodAndType,
                "Discovery interface assembler returns null for an application Command/Query on null input.",
                "Validate inputs in the caller or model the absence explicitly, then return a concrete application contract.");
    }

    private static ArchitectureRuleAllowance rawParameters(String key) {
        return ArchitectureRuleAllowance.of(
                key,
                "Discovery ApplicationService method uses naked Long/String/Integer or scattered query parameters.",
                "Introduce a dedicated Query/Command or strong domain value object for the method input, then remove this allowance.");
    }
}
