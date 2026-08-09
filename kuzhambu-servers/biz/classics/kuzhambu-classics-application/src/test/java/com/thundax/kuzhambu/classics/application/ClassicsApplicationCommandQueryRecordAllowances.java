package com.thundax.kuzhambu.classics.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class ClassicsApplicationCommandQueryRecordAllowances {

    private static final String DESCRIPTION =
            "Legacy application Command/Query is still a Lombok class instead of a record.";
    private static final String REMEDIATION =
            "Convert the contract to a Java record, remove Lombok annotations/imports, update callers, then remove this allowance.";

    private ClassicsApplicationCommandQueryRecordAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchApplyContentCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchRejectContentCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.content.command.ContentTagSortCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordSortCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiCategoryCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiCategorySortCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageSortCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageUploadCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySortCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiVolumeCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiVolumeSortCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSourceFileCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery"));
    }

    private static ArchitectureRuleAllowance legacy(String key) {
        return ArchitectureRuleAllowance.of(key, DESCRIPTION, REMEDIATION);
    }
}
