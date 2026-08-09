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
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsDoNotUsePathOrQueryParameters(
                Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler.KnowledgeGraphExtractionInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.assembler.KnowledgeLineageInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.assembler.KnowledgeGraphRefinementInterfaceAssembler",
                        "com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.assembler.KnowledgeQualityReportInterfaceAssembler",
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
                "admin.graph.controller.request.GraphExtractionRequests$BatchCancelRequest",
                "admin.graph.controller.request.GraphExtractionRequests$CreateRequest",
                "admin.graph.controller.request.GraphExtractionRequests$EntityIdRequest",
                "admin.graph.controller.request.GraphExtractionRequests$EntityPageRequest",
                "admin.graph.controller.request.GraphExtractionRequests$LineageNodeIdRequest",
                "admin.graph.controller.request.GraphExtractionRequests$LineageNodePageRequest",
                "admin.graph.controller.request.GraphExtractionRequests$LineageRelationIdRequest",
                "admin.graph.controller.request.GraphExtractionRequests$LineageRelationPageRequest",
                "admin.graph.controller.request.GraphExtractionRequests$PageTaskRequest",
                "admin.graph.controller.request.GraphExtractionRequests$RegenerateRequest",
                "admin.graph.controller.request.GraphExtractionRequests$RelationIdRequest",
                "admin.graph.controller.request.GraphExtractionRequests$RelationPageRequest",
                "admin.graph.controller.request.GraphExtractionRequests$TaskIdRequest",
                "admin.graph.controller.request.GraphExtractionRequests$VersionIdRequest",
                "admin.graph.controller.request.GraphExtractionRequests$VersionPageRequest",
                "admin.lineage.controller.request.LineageCanvasRequest",
                "admin.refinement.controller.request.QualityReportRequests$DetailRequest",
                "admin.refinement.controller.request.QualityReportRequests$GenerateRequest",
                "admin.refinement.controller.request.QualityReportRequests$LatestRequest",
                "admin.refinement.controller.request.QualityReportRequests$ReextractRequest",
                "admin.refinement.controller.request.RefinementRequests$AnnotationDeleteRequest",
                "admin.refinement.controller.request.RefinementRequests$AnnotationPageRequest",
                "admin.refinement.controller.request.RefinementRequests$AnnotationUpsertRequest",
                "admin.refinement.controller.request.RefinementRequests$EntityConfirmRequest",
                "admin.refinement.controller.request.RefinementRequests$EntityDeleteRequest",
                "admin.refinement.controller.request.RefinementRequests$EntityUpsertRequest",
                "admin.refinement.controller.request.RefinementRequests$LineageNodeConfirmRequest",
                "admin.refinement.controller.request.RefinementRequests$LineageNodeDeleteRequest",
                "admin.refinement.controller.request.RefinementRequests$LineageNodeUpsertRequest",
                "admin.refinement.controller.request.RefinementRequests$LineageRelationConfirmRequest",
                "admin.refinement.controller.request.RefinementRequests$LineageRelationDeleteRequest",
                "admin.refinement.controller.request.RefinementRequests$LineageRelationUpsertRequest",
                "admin.refinement.controller.request.RefinementRequests$QualitySummaryRequest",
                "admin.refinement.controller.request.RefinementRequests$RelationConfirmRequest",
                "admin.refinement.controller.request.RefinementRequests$RelationDeleteRequest",
                "admin.refinement.controller.request.RefinementRequests$RelationUpsertRequest",
                "admin.refinement.controller.request.RefinementRequests$TaskApplyRequest",
                "admin.refinement.controller.request.RefinementRequests$TaskDetailRequest",
                "admin.refinement.controller.request.RefinementRequests$TaskOpenRequest",
                "admin.refinement.controller.request.RefinementRequests$TaskPageRequest",
                "portal.atlas.controller.request.KnowledgePortalAtlasRequest",
                "portal.home.controller.request.KnowledgePortalHomeRequest",
                "portal.quality.controller.request.KnowledgePortalQualityRequest");
    }

    private static List<ArchitectureRuleAllowance> legacyResponseAnnotationAllowances() {
        return modelAnnotationAllowances(
                ModelAnnotationArchitectureRuleSupport.NAME_RESPONSE_REQUIRED_ANNOTATIONS,
                "admin.graph.controller.response.GraphExtractionResponses$BatchCancelResponse",
                "admin.graph.controller.response.GraphExtractionResponses$EntityResponse",
                "admin.graph.controller.response.GraphExtractionResponses$LineageNodeResponse",
                "admin.graph.controller.response.GraphExtractionResponses$LineageRelationResponse",
                "admin.graph.controller.response.GraphExtractionResponses$RelationResponse",
                "admin.graph.controller.response.GraphExtractionResponses$TaskResponse",
                "admin.graph.controller.response.GraphExtractionResponses$VersionResponse",
                "admin.lineage.controller.response.LineageCanvasResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$AvailableFiltersResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$EmptyResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$NodeResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$RelationResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$SourceRefResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$SummaryResponse",
                "admin.lineage.controller.response.LineageCanvasResponse$VersionResponse",
                "admin.refinement.controller.response.QualityReportResponses$AnnotationResponse",
                "admin.refinement.controller.response.QualityReportResponses$DetailResponse",
                "admin.refinement.controller.response.QualityReportResponses$IssueResponse",
                "admin.refinement.controller.response.QualityReportResponses$ReextractResponse",
                "admin.refinement.controller.response.QualityReportResponses$ReportResponse",
                "admin.refinement.controller.response.QualityReportResponses$SourceDetailResponse",
                "admin.refinement.controller.response.RefinementResponses$AnnotationResponse",
                "admin.refinement.controller.response.RefinementResponses$ApplyResponse",
                "admin.refinement.controller.response.RefinementResponses$DetailResponse",
                "admin.refinement.controller.response.RefinementResponses$EntityOptionResponse",
                "admin.refinement.controller.response.RefinementResponses$EntityResponse",
                "admin.refinement.controller.response.RefinementResponses$LineageNodeResponse",
                "admin.refinement.controller.response.RefinementResponses$LineageRelationResponse",
                "admin.refinement.controller.response.RefinementResponses$ProgressSummaryResponse",
                "admin.refinement.controller.response.RefinementResponses$QualitySummaryResponse",
                "admin.refinement.controller.response.RefinementResponses$RelationResponse",
                "admin.refinement.controller.response.RefinementResponses$WorkbenchItemResponse",
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
}
