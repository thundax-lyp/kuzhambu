package com.thundax.kuzhambu.storage.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class StorageApplicationPageQueryAllowances {

    private static final String PAGE_QUERY_TYPE_DESCRIPTION =
            "Legacy storage application query is named as a page-specific Query instead of a business Query.";
    private static final String PAGE_FIELDS_DESCRIPTION =
            "Legacy storage application query declares raw pageNo/pageSize fields instead of using common PageQuery normalization.";
    private static final String REMEDIATION =
            "Rename the business query without Page in the type name, move pageNo/pageSize to common PageQuery passed as a separate ApplicationService parameter, update callers, then remove this allowance.";

    private StorageApplicationPageQueryAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                pageQueryType("com.thundax.kuzhambu.storage.application.query.StorageObjectPageQuery"),
                pageFields("com.thundax.kuzhambu.storage.application.query.StorageObjectPageQuery"));
    }

    private static ArchitectureRuleAllowance pageQueryType(String typeName) {
        return ArchitectureRuleAllowance.of("PAGE_QUERY_TYPE:" + typeName, PAGE_QUERY_TYPE_DESCRIPTION, REMEDIATION);
    }

    private static ArchitectureRuleAllowance pageFields(String typeName) {
        return ArchitectureRuleAllowance.of("PAGE_QUERY_FIELDS:" + typeName, PAGE_FIELDS_DESCRIPTION, REMEDIATION);
    }
}
