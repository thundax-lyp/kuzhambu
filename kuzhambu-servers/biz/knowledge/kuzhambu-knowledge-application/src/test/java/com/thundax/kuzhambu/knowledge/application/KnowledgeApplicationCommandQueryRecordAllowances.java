package com.thundax.kuzhambu.knowledge.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class KnowledgeApplicationCommandQueryRecordAllowances {

    private static final String DESCRIPTION =
            "Legacy application Command/Query is still a Lombok class instead of a record.";
    private static final String REMEDIATION =
            "Convert the contract to a Java record, remove Lombok annotations/imports, update callers, then remove this allowance.";

    private KnowledgeApplicationCommandQueryRecordAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.graph.command.RegenerateGraphExtractionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.lineage.query.LineageCanvasQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.portal.query.KnowledgePortalAtlasQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementEntityCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementLineageNodeCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementLineageRelationCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementRelationCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteQualityAnnotationCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementEntityCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementLineageNodeCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementLineageRelationCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementRelationCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.GenerateQualityReportCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.ReextractLowQualityCategoryCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertQualityAnnotationCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementEntityCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementLineageNodeCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementLineageRelationCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementRelationCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.refinement.query.RefinementDetailQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasCreateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasRemoveCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchDeprecateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchMergeCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchReviewCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCandidateApplyCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryCreateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryStatusCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryUpdateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCreateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagDeprecateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagExtractionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagMergeCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagReviewCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagStatusCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagUpdateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagBatchMergePreviewQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagCategoryQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagGovernanceMetricsQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagMergePreviewQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagReviewQuery"));
    }

    private static ArchitectureRuleAllowance legacy(String key) {
        return ArchitectureRuleAllowance.of(key, DESCRIPTION, REMEDIATION);
    }
}
