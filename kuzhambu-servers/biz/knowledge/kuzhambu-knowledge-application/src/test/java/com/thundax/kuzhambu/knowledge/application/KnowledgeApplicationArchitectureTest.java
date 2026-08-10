package com.thundax.kuzhambu.knowledge.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.ImplContractArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.LayerArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.PathArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeApplicationArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.knowledge";

    @Test
    void applicationSpringBeansShouldDeclareSingleConstructor() {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".application");

        LayerArchitectureRuleSupport.assertApplicationServiceBoundaryClean(
                classes, legacyApplicationServiceBoundaryAllowances());
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        NamingArchitectureRuleSupport.assertConfigurationClassNames(classes);
        PathArchitectureRuleSupport.assertConfigurationClassPlacement(classes);
        ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(classes, Collections.emptySet());
        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                classes, Collections.emptySet());
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesAreRecords(
                Path.of("src/main/java"), KnowledgeApplicationCommandQueryRecordAllowances.legacyAllowances());
        NamingArchitectureRuleSupport.assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-knowledge-interface/src/main/java")),
                Collections.emptyList());
        NamingArchitectureRuleSupport.assertAssemblersDoNotReturnNullApplicationCommandOrQuery(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-knowledge-interface/src/main/java")),
                Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationQueriesDoNotOwnPageState(
                Path.of("src/main/java"), Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.knowledge.application.facade.assembler.KnowledgeFacadeAssembler",
                        "com.thundax.kuzhambu.knowledge.application.taxonomy.assembler.TaxonomyApplicationAssembler"));
    }

    private static List<ArchitectureRuleAllowance> legacyApplicationServiceBoundaryAllowances() {
        return List.of(
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.workbench.service."
                        + "KnowledgeGraphWorkbenchApplicationService.getManuscript(java.lang.String, java.lang.Long)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.workbench.service."
                        + "KnowledgeGraphWorkbenchApplicationService.applyCandidate(java.lang.Long, java.lang.String)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.workbench.service."
                                + "KnowledgeGraphWorkbenchApplicationService.listManuscriptTree(java.lang.String, java.lang.String, java.lang.String, java.lang.String)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.workbench.service."
                                + "KnowledgeGraphWorkbenchApplicationService.extractManuscript(java.lang.String, java.lang.Long, java.lang.String, java.lang.Long)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.workbench.service."
                                + "KnowledgeGraphWorkbenchApplicationService.getLatestCandidate(java.lang.String, java.lang.Long, java.lang.String)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.workbench.service."
                        + "KnowledgeGraphWorkbenchApplicationService.applyCandidate(java.lang.Long)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.refinement.service."
                        + "KnowledgeQualityReportApplicationService.detail(java.lang.Long)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.refinement.service."
                        + "KnowledgeQualityReportApplicationService.latest(java.lang.Long)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.refinement.service."
                        + "KnowledgeGraphRefinementApplicationService.openTask(java.lang.Long, java.lang.Long)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.refinement.service."
                        + "KnowledgeGraphRefinementApplicationService.qualitySummary(java.lang.Long)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.refinement.service."
                        + "KnowledgeGraphRefinementApplicationService.applyTask(java.lang.Long, java.lang.Long)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.graph.service."
                        + "KnowledgeGraphExtractionApplicationService.getLineageRelationDetail(java.lang.Long)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.graph.service."
                                + "KnowledgeGraphExtractionApplicationService.pageEntities(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, com.thundax.kuzhambu.common.core.page.PageQuery)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.graph.service."
                        + "KnowledgeGraphExtractionApplicationService.getLineageNodeDetail(java.lang.Long)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.graph.service."
                        + "KnowledgeGraphExtractionApplicationService.getEntityDetail(java.lang.Long)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.graph.service."
                                + "KnowledgeGraphExtractionApplicationService.pageRelations(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, com.thundax.kuzhambu.common.core.page.PageQuery)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.graph.service."
                        + "KnowledgeGraphExtractionApplicationService.getRelationDetail(java.lang.Long)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.graph.service."
                                + "KnowledgeGraphExtractionApplicationService.pageLineageRelations(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, com.thundax.kuzhambu.common.core.page.PageQuery)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.graph.service."
                                + "KnowledgeGraphExtractionApplicationService.pageLineageNodes(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, com.thundax.kuzhambu.common.core.page.PageQuery)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.taxonomy.service."
                        + "KnowledgeTaxonomyReadApplicationService.getTagHint(java.lang.String)"),
                rawParameters("METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.taxonomy.service."
                        + "KnowledgeTaxonomyReadApplicationService.listEntityHints(java.lang.String)"),
                rawParameters(
                        "METHOD_SHAPE:com.thundax.kuzhambu.knowledge.application.report.service."
                                + "KnowledgeReportApplicationService.summary(java.time.Instant, java.time.Instant, java.lang.String)"));
    }

    private static ArchitectureRuleAllowance rawParameters(String key) {
        return ArchitectureRuleAllowance.of(
                key,
                "Knowledge ApplicationService method uses naked Long/String values or scattered query parameters.",
                "Introduce a dedicated Query/Command or strong domain value object for the method input, then remove this allowance.");
    }
}
