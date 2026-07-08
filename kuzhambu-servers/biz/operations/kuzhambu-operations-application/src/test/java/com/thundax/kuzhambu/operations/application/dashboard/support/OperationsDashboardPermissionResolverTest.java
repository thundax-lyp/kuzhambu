package com.thundax.kuzhambu.operations.application.dashboard.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.common.security.permission.PermissionAuthorizationService;
import com.thundax.kuzhambu.common.security.permission.PrefixPermissionMatcher;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class OperationsDashboardPermissionResolverTest {

    @AfterEach
    void tearDown() {
        KuzhambuContextHolder.clear();
    }

    @Test
    void resolveShouldMapExactPermissions() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "user-1", KuzhambuSubjectType.ADMIN_USER, "Admin", "token", List.of("operations:health:view")));
        OperationsDashboardPermissionResolver resolver = new OperationsDashboardPermissionResolver(
                new PermissionAuthorizationService(new PrefixPermissionMatcher()));

        OperationsDashboardPermissionSnapshot snapshot = resolver.resolve();

        assertNotNull(snapshot);
        assertTrue(snapshot.canViewHealthSummary());
        assertFalse(snapshot.canViewClassicsContentSummary());
        assertFalse(snapshot.canViewAiInvocationSummary());
        assertFalse(snapshot.canLoadKnowledgeSummary());
        assertTrue(snapshot.hasAnyChartPermission());
    }

    @Test
    void resolveShouldMapParentPermissions() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "user-1", KuzhambuSubjectType.ADMIN_USER, "Admin", "token", List.of("classics", "discovery")));
        OperationsDashboardPermissionResolver resolver = new OperationsDashboardPermissionResolver(
                new PermissionAuthorizationService(new PrefixPermissionMatcher()));

        OperationsDashboardPermissionSnapshot snapshot = resolver.resolve();

        assertTrue(snapshot.canViewClassicsContentSummary());
        assertTrue(snapshot.canViewDiscoverySearchSummary());
        assertTrue(snapshot.canViewDiscoveryQaSummary());
        assertFalse(snapshot.canViewAiInvocationSummary());
        assertFalse(snapshot.canViewKnowledgeTaxonomySummary());
        assertFalse(snapshot.canViewTaskSummary());
    }

    @Test
    void resolveShouldMapSuperPermissionAsAllAllowed() {
        KuzhambuContextHolder.setSubject(
                new KuzhambuSubject("user-1", KuzhambuSubjectType.ADMIN_USER, "Admin", "token", List.of("super")));
        OperationsDashboardPermissionResolver resolver = new OperationsDashboardPermissionResolver(
                new PermissionAuthorizationService(new PrefixPermissionMatcher()));

        OperationsDashboardPermissionSnapshot snapshot = resolver.resolve();

        assertTrue(snapshot.canLoadClassicsSummary());
        assertTrue(snapshot.canLoadDiscoverySummary());
        assertTrue(snapshot.canLoadAiSummary());
        assertTrue(snapshot.canLoadKnowledgeSummary());
        assertTrue(snapshot.canViewHealthSummary());
        assertTrue(snapshot.canViewTaskSummary());
    }
}
