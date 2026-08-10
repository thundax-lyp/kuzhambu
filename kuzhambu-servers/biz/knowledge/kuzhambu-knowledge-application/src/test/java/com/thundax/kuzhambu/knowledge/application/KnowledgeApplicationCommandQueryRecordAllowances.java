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
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.portal.query.KnowledgePortalAtlasQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchMergeCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCandidateApplyCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagExtractionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagMergeCommand"),
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
