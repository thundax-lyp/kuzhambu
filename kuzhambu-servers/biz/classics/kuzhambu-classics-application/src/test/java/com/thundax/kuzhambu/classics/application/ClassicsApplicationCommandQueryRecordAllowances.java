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
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageSortCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageUploadCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand"));
    }

    private static ArchitectureRuleAllowance legacy(String key) {
        return ArchitectureRuleAllowance.of(key, DESCRIPTION, REMEDIATION);
    }
}
