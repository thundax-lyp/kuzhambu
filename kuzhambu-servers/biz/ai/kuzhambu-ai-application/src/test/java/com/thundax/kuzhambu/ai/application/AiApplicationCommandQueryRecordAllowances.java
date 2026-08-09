package com.thundax.kuzhambu.ai.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class AiApplicationCommandQueryRecordAllowances {

    private static final String DESCRIPTION =
            "Legacy application Command/Query is still a Lombok class instead of a record.";
    private static final String REMEDIATION =
            "Convert the contract to a Java record, remove Lombok annotations/imports, update callers, then remove this allowance.";

    private AiApplicationCommandQueryRecordAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.command.BuildPromptOptimizationSuggestionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.command.CreateAiBusinessConfigCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.command.CreateAiModelCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.command.PromptTemplateSaveCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.command.RollbackPromptVersionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.command.UpdateAiBusinessConfigCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.command.UpdateAiModelCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.command.ValidatePromptVariablesCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.query.ListAiBusinessConfigsQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.query.ListAiCapabilitiesQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.query.ListAiModelsQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.query.ListPromptVariablesQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.query.ListPromptVersionsQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.query.ListPromptsQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.config.query.PromptVersionCompareQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.command.ApplyAiCandidateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.command.CancelAiBatchJobCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.command.ExpireRunningAiBatchJobsCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobFailureCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.command.RejectAiCandidateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.query.AiReportSummaryQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.query.CanDispatchNextAiBatchUnitQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.query.GetAiBatchJobQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.query.PageAiBatchJobsByCapabilitiesQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.query.PageAiBatchJobsQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.query.RequireAiCandidateForApplyQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.command.CancelAiRefinementTaskCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.command.DiscoveryAiCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.command.PlatformAiInvokeCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.command.SubmitAiRefinementTaskCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.query.GetAiRefinementTaskQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.query.PageAiRefinementTasksQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.query.SubscribeAiRefinementTaskEventsQuery"));
    }

    private static ArchitectureRuleAllowance legacy(String key) {
        return ArchitectureRuleAllowance.of(key, DESCRIPTION, REMEDIATION);
    }
}
