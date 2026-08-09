package com.thundax.kuzhambu.ai.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class AiApplicationCommandQueryRecordAllowances {

    private static final String DESCRIPTION =
            "Legacy application Command/Query is still a Lombok class instead of a record.";
    private static final String REMEDIATION =
            "Convert the contract to a Java record, remove Lombok annotations/imports, update callers, then remove this allowance.";
    private static final String CONSTRUCTION_DESCRIPTION =
            "Legacy production code constructs an application Command/Query outside *InterfaceAssembler, *FacadeAssembler, or ApplicationService orchestration.";
    private static final String CONSTRUCTION_REMEDIATION =
            "Move request or facade conversion into the matching assembler, or move internal downstream Command/Query construction into the calling ApplicationService, then remove this allowance.";

    private AiApplicationCommandQueryRecordAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.command.DiscoveryAiCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.command.PlatformAiInvokeCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.ai.application.scenario.command.SubmitAiRefinementTaskCommand"));
    }

    static List<ArchitectureRuleAllowance> constructionAllowances() {
        return List.of(
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver#ListAiBusinessConfigsQuery:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.invocation.support.AiWorkerModelConfigResolver#GetAiModelQuery:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.invocation.support.AiWorkerModelConfigResolver#ListAiBusinessConfigsQuery:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.facade.impl.AiFacadeImpl#AiReportSummaryQuery:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.facade.impl.AiFacadeImpl#GetAiBatchJobQuery:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.facade.impl.AiFacadeImpl#AiBatchJobCreateCommand:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.facade.impl.AiFacadeImpl#CanDispatchNextAiBatchUnitQuery:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.facade.impl.AiFacadeImpl#RecordAiBatchJobCommand:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.facade.impl.AiFacadeImpl#RecordAiBatchJobFailureCommand:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.facade.impl.AiFacadeImpl#CancelAiBatchJobCommand:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.facade.impl.AiFacadeImpl#RequireAiCandidateForApplyQuery:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.facade.impl.AiFacadeImpl#ApplyAiCandidateCommand:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.application.facade.impl.AiFacadeImpl#RejectAiCandidateCommand:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.AiRefinementTaskController#AiBatchJobCreateCommand:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.AiRefinementTaskController#GetAiBatchJobQuery:1"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.AiRefinementTaskController#GetAiBatchJobQuery:2"),
                construction(
                        "COMMAND_QUERY_CONSTRUCTION:com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.AiRefinementTaskController#CancelAiBatchJobCommand:1"));
    }

    static List<ArchitectureRuleAllowance> assemblerNullReturnAllowances() {
        return List.of();
    }

    private static ArchitectureRuleAllowance legacy(String key) {
        return ArchitectureRuleAllowance.of(key, DESCRIPTION, REMEDIATION);
    }

    private static ArchitectureRuleAllowance construction(String key) {
        return ArchitectureRuleAllowance.of(key, CONSTRUCTION_DESCRIPTION, CONSTRUCTION_REMEDIATION);
    }
}
