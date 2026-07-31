package com.thundax.kuzhambu.operations.application.dashboard.support;

import com.thundax.kuzhambu.common.security.permission.PermissionAuthorizationService;
import org.springframework.stereotype.Component;

@Component
public class OperationsDashboardPermissionResolver {

    private static final String CLASSICS_CONTENT_PERMISSION = "classics:content:view";
    private static final String CLASSICS_SANC_AI_PERMISSION = "classics:sancai:view";
    private static final String CLASSICS_WANGQI_PERMISSION = "classics:wangqi:view";
    private static final String CLASSICS_MINGCUSTOMS_PERMISSION = "classics:mingcustoms:view";
    private static final String DISCOVERY_SEARCH_PERMISSION = "discovery:search:view";
    private static final String DISCOVERY_QA_PERMISSION = "discovery:qa:view";
    private static final String AI_INVOCATION_PERMISSION = "ai:invocation:view";
    private static final String KNOWLEDGE_TAXONOMY_PERMISSION = "knowledge:taxonomy:view";
    private static final String OPERATIONS_HEALTH_PERMISSION = "operations:health:view";
    private static final String OPERATIONS_TASK_PERMISSION = "operations:task:view";

    private final PermissionAuthorizationService permissionAuthorizationService;

    public OperationsDashboardPermissionResolver(PermissionAuthorizationService permissionAuthorizationService) {
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    public OperationsDashboardPermissionSnapshot resolve() {
        return new OperationsDashboardPermissionSnapshot(
                permissionAuthorizationService.isPermittedAny(
                        CLASSICS_CONTENT_PERMISSION,
                        CLASSICS_SANC_AI_PERMISSION,
                        CLASSICS_WANGQI_PERMISSION,
                        CLASSICS_MINGCUSTOMS_PERMISSION),
                permissionAuthorizationService.isPermittedAny(DISCOVERY_SEARCH_PERMISSION),
                permissionAuthorizationService.isPermittedAny(DISCOVERY_QA_PERMISSION),
                permissionAuthorizationService.isPermittedAny(AI_INVOCATION_PERMISSION),
                permissionAuthorizationService.isPermittedAny(KNOWLEDGE_TAXONOMY_PERMISSION),
                permissionAuthorizationService.isPermittedAny(OPERATIONS_HEALTH_PERMISSION),
                permissionAuthorizationService.isPermittedAny(OPERATIONS_TASK_PERMISSION));
    }
}
