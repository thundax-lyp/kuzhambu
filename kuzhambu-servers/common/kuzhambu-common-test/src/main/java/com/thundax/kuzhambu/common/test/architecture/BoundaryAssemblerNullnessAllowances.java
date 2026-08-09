package com.thundax.kuzhambu.common.test.architecture;

import java.util.Arrays;
import java.util.List;

public final class BoundaryAssemblerNullnessAllowances {

    private static final String DESCRIPTION =
            "Legacy boundary assembler public methods still accept nullable inputs or return null.";
    private static final String REMEDIATION =
            "Add Objects.requireNonNull guards for non-null inputs, replace null returns with concrete command/query/response/result values or caller-side branching, then remove this allowance.";

    private BoundaryAssemblerNullnessAllowances() {}

    public static List<ArchitectureRuleAllowance> legacyClasses(String... classNames) {
        return Arrays.stream(classNames)
                .map(BoundaryAssemblerNullnessAllowances::legacyClass)
                .toList();
    }

    private static ArchitectureRuleAllowance legacyClass(String className) {
        return ArchitectureRuleAllowance.of("BOUNDARY_ASSEMBLER_NULL_*:" + className + "#*", DESCRIPTION, REMEDIATION);
    }
}
