package com.thundax.kuzhambu.classics.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class ClassicsApplicationPageQueryAllowances {

    private static final String DESCRIPTION =
            "Legacy classics application query is named as a page-specific Query instead of a business Query.";
    private static final String REMEDIATION =
            "Rename the business query without Page in the type name, keep pagination in common PageQuery, update callers, then remove this allowance.";

    private ClassicsApplicationPageQueryAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                legacy("com.thundax.kuzhambu.classics.application.publication.query.ClassicsPublicationJobPageQuery"));
    }

    private static ArchitectureRuleAllowance legacy(String typeName) {
        return ArchitectureRuleAllowance.of("PAGE_QUERY_TYPE:" + typeName, DESCRIPTION, REMEDIATION);
    }
}
