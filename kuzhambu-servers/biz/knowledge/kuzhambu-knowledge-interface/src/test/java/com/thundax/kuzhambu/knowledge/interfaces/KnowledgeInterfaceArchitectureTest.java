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
        ApiAnnotationArchitectureRuleSupport.assertControllerActionsUseVerbWhitelist(
                Path.of("src/main/java"), legacyActionVerbAllowances());
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsDoNotUsePathOrQueryParameters(
                Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.assembler.KnowledgeLineageInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.assembler.KnowledgeTaxonomyInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.assembler.KnowledgeGraphWorkbenchInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.assembler.KnowledgePortalAtlasInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.portal.home.assembler.KnowledgePortalHomeInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.assembler.KnowledgePortalLineageInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.portal.quality.assembler.KnowledgePortalQualityInterfaceAssembler"));
    }

    private static List<ArchitectureRuleAllowance> legacyRequestAnnotationAllowances() {
        return modelAnnotationAllowances(
                ModelAnnotationArchitectureRuleSupport.NAME_REQUEST_REQUIRED_ANNOTATIONS,
                "admin.lineage.controller.request.LineageCanvasRequest",
                "portal.atlas.controller.request.KnowledgePortalAtlasRequest",
                "portal.home.controller.request.KnowledgePortalHomeRequest",
                "portal.quality.controller.request.KnowledgePortalQualityRequest");
    }

    private static List<ArchitectureRuleAllowance> legacyActionVerbAllowances() {
        return List.of(
                actionVerbAllowance("KnowledgeGraphWorkbenchController"),
                actionVerbAllowance("KnowledgeLineageController"),
                actionVerbAllowance("KnowledgeTaxonomyController"));
    }

    private static List<ArchitectureRuleAllowance> legacyResponseAnnotationAllowances() {
        return modelAnnotationAllowances(
                ModelAnnotationArchitectureRuleSupport.NAME_RESPONSE_REQUIRED_ANNOTATIONS,
                "admin.lineage.controller.response.LineageCanvasResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$AvailableFiltersResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$EmptyResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$NodeResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$RelationResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$SourceRefResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$SummaryResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$VersionResponse",
                "admin.taxonomy.controller.response.TagAliasResponse",
                "admin.taxonomy.controller.response.TagBatchMergePreviewResponse",
                "admin.taxonomy.controller.response.TagCategoryResponse",
                "admin.taxonomy.controller.response.TagContentRefResponse",
                "admin.taxonomy.controller.response.TagDetailResponse",
                "admin.taxonomy.controller.response.TagExtractionResponse",
                "admin.taxonomy.controller.response.TagGovernanceMetricsResponse",
                "admin.taxonomy.controller.response.TagMergePreviewResponse",
                "admin.taxonomy.controller.response.TagResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$AvailableFiltersResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$BreadcrumbItemResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$CanvasEdgeResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$CanvasNodeResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$CanvasViewResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$CategoryEntityHighlightResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$CategoryViewResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$DetailViewResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$FocusNodeResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$OverviewCategoryCardResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$OverviewViewResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$RelatedTagResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$RelationGroupResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$RelationItemResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$SourceReferenceResponse",
                "portal.atlas.controller.response.KnowledgePortalAtlasResponse$TimelineItemResponse",
                "portal.home.controller.response.KnowledgePortalHomeResponse",
                "portal.home.controller.response.KnowledgePortalHomeResponse$PortalFeatureCollectionResponse",
                "portal.home.controller.response.KnowledgePortalHomeResponse$PortalQuickLinkResponse",
                "portal.home.controller.response.KnowledgePortalHomeResponse$PortalRecentUpdateResponse",
                "portal.home.controller.response.KnowledgePortalHomeResponse$PortalStatResponse",
                "portal.lineage.controller.response.KnowledgePortalLineageResponse",
                "portal.lineage.controller.response.KnowledgePortalLineageResponse$AvailableFiltersResponse",
                "portal.lineage.controller.response.KnowledgePortalLineageResponse$EmptyResponse",
                "portal.lineage.controller.response.KnowledgePortalLineageResponse$NodeResponse",
                "portal.lineage.controller.response.KnowledgePortalLineageResponse$RelationResponse",
                "portal.lineage.controller.response.KnowledgePortalLineageResponse$SourceRefResponse",
                "portal.lineage.controller.response.KnowledgePortalLineageResponse$SummaryResponse",
                "portal.lineage.controller.response.KnowledgePortalLineageResponse$VersionResponse",
                "portal.quality.controller.response.KnowledgePortalQualityResponse",
                "portal.quality.controller.response.KnowledgePortalQualityResponse$FocusIssueResponse",
                "portal.quality.controller.response.KnowledgePortalQualityResponse$QualityStatResponse",
                "portal.quality.controller.response.KnowledgePortalQualityResponse$SourceBreakdownResponse",
                "portal.quality.controller.response.KnowledgePortalQualityResponse$SourceDetailResponse",
                "portal.quality.controller.response.KnowledgePortalQualityResponse$TrendPointResponse",
                "portal.quality.controller.response.KnowledgePortalQualityResponse$TrendSeriesResponse");
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

    private static ArchitectureRuleAllowance actionVerbAllowance(String controller) {
        return ArchitectureRuleAllowance.of(
                "CONTROLLER_ACTION_VERB:*" + controller + ".java*",
                "Knowledge controller retains legacy action names or paths outside the shared verb whitelist.",
                "Rename the controller method and action path with a shared verb, update callers, then remove this allowance.");
    }
}
