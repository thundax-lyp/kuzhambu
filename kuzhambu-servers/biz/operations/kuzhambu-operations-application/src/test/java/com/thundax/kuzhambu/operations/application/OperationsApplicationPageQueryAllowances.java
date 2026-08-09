package com.thundax.kuzhambu.operations.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class OperationsApplicationPageQueryAllowances {

    private static final String DESCRIPTION =
            "Legacy operations application query is named as a page-specific Query instead of a business Query.";
    private static final String REMEDIATION =
            "Rename the business query without Page in the type name, keep pagination in common PageQuery, update callers, then remove this allowance.";

    private OperationsApplicationPageQueryAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                legacy("com.thundax.kuzhambu.operations.application.restore.query.OperationsRestorePageQuery"),
                legacy("com.thundax.kuzhambu.operations.application.report.query.OperationsReportPageQuery"),
                legacy("com.thundax.kuzhambu.operations.application.health.query.OperationsHealthPageQuery"),
                legacy("com.thundax.kuzhambu.operations.application.health.query.OperationsHealthAlertPageQuery"),
                legacy("com.thundax.kuzhambu.operations.application.task.query.OperationsTaskPageQuery"));
    }

    private static ArchitectureRuleAllowance legacy(String typeName) {
        return ArchitectureRuleAllowance.of("PAGE_QUERY_TYPE:" + typeName, DESCRIPTION, REMEDIATION);
    }
}
