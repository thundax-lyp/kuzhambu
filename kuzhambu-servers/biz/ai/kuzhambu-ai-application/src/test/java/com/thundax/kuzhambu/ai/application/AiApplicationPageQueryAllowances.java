package com.thundax.kuzhambu.ai.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class AiApplicationPageQueryAllowances {

    private static final String PAGE_QUERY_TYPE_DESCRIPTION =
            "Legacy AI application query is named as a page-specific Query instead of a business Query.";
    private static final String EMBEDDED_PAGE_QUERY_DESCRIPTION =
            "Legacy AI application query embeds PageQuery instead of receiving PageQuery as a separate ApplicationService parameter.";
    private static final String REMEDIATION =
            "Rename the business query without Page in the type name, pass common PageQuery as a separate ApplicationService parameter, update callers, then remove this allowance.";

    private AiApplicationPageQueryAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                pageQueryType("com.thundax.kuzhambu.ai.application.scenario.query.PageAiRefinementTasksQuery"),
                embeddedPageQuery("com.thundax.kuzhambu.ai.application.scenario.query.PageAiRefinementTasksQuery"),
                pageQueryType("com.thundax.kuzhambu.ai.application.invocation.query.PageAiBatchJobsQuery"),
                embeddedPageQuery("com.thundax.kuzhambu.ai.application.invocation.query.PageAiBatchJobsQuery"),
                pageQueryType(
                        "com.thundax.kuzhambu.ai.application.invocation.query.PageAiBatchJobsByCapabilitiesQuery"),
                embeddedPageQuery(
                        "com.thundax.kuzhambu.ai.application.invocation.query.PageAiBatchJobsByCapabilitiesQuery"));
    }

    private static ArchitectureRuleAllowance pageQueryType(String typeName) {
        return ArchitectureRuleAllowance.of("PAGE_QUERY_TYPE:" + typeName, PAGE_QUERY_TYPE_DESCRIPTION, REMEDIATION);
    }

    private static ArchitectureRuleAllowance embeddedPageQuery(String typeName) {
        return ArchitectureRuleAllowance.of(
                "PAGE_QUERY_EMBEDDED:" + typeName, EMBEDDED_PAGE_QUERY_DESCRIPTION, REMEDIATION);
    }
}
