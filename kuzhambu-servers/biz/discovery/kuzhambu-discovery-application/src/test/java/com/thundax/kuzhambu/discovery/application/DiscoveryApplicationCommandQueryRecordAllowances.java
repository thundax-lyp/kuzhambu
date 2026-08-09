package com.thundax.kuzhambu.discovery.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class DiscoveryApplicationCommandQueryRecordAllowances {

    private static final String DESCRIPTION =
            "Legacy application Command/Query is still a Lombok class instead of a record.";
    private static final String REMEDIATION =
            "Convert the contract to a Java record, remove Lombok annotations/imports, update callers, then remove this allowance.";

    private DiscoveryApplicationCommandQueryRecordAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.qa.command.AskQuestionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.qa.command.ExportQaSessionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.qa.command.SyncKnowledgeContentCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.qa.query.KnowledgeSyncItemPageQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.qa.query.QaSessionPageQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.search.command.SearchClickEventCreateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationPrepareCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationReferenceCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.search.query.SearchEventPageQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.search.query.SearchPreviewQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.search.query.SearchPublicationCandidatePageQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.search.query.SearchPublicationCategoryAggregationQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.search.query.SearchQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.discovery.application.search.query.SearchStatisticsSummaryQuery"));
    }

    private static ArchitectureRuleAllowance legacy(String key) {
        return ArchitectureRuleAllowance.of(key, DESCRIPTION, REMEDIATION);
    }
}
