package com.thundax.kuzhambu.discovery.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.ImplContractArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.LayerArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
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
        ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(classes, Collections.emptySet());
        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                classes, Collections.emptySet());
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesAreRecords(
                Path.of("src/main/java"), DiscoveryApplicationCommandQueryRecordAllowances.legacyAllowances());
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

    private static ArchitectureRuleAllowance rawParameters(String key) {
        return ArchitectureRuleAllowance.of(
                key,
                "Discovery ApplicationService method uses naked Long/String/Integer or scattered query parameters.",
                "Introduce a dedicated Query/Command or strong domain value object for the method input, then remove this allowance.");
    }
}
