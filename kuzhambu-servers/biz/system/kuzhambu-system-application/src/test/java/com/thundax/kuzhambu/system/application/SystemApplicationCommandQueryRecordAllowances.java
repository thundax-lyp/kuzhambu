package com.thundax.kuzhambu.system.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class SystemApplicationCommandQueryRecordAllowances {

    private static final String DESCRIPTION =
            "Legacy application Command/Query is still a Lombok class instead of a record.";
    private static final String REMEDIATION =
            "Convert the contract to a Java record, remove Lombok annotations/imports, update callers, then remove this allowance.";

    private SystemApplicationCommandQueryRecordAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of();
    }

    private static ArchitectureRuleAllowance legacy(String key) {
        return ArchitectureRuleAllowance.of(key, DESCRIPTION, REMEDIATION);
    }
}
