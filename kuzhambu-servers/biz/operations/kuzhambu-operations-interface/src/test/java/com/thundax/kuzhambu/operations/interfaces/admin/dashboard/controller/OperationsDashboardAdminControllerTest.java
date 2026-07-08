package com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.operations.application.dashboard.query.OperationsDashboardOverviewQuery;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TaskStatusSummaryResult;
import com.thundax.kuzhambu.operations.application.dashboard.service.OperationsDashboardApplicationService;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthSummaryResult;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.request.OperationsDashboardOverviewRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class OperationsDashboardAdminControllerTest {

    @Test
    void routesShouldKeepAdminDashboardApiPathsAndPermissions() throws Exception {
        assertRequestMapping(OperationsDashboardAdminController.class, "/api/operations/dashboard");
        assertPostMapping(
                OperationsDashboardAdminController.class,
                "overview",
                "overview",
                "operations:dashboard:view",
                OperationsDashboardOverviewRequest.class);
    }

    @Test
    void overviewShouldDelegateToApplicationServiceAndMapStableResponse() {
        OperationsDashboardApplicationService service = mock(OperationsDashboardApplicationService.class);
        OperationsDashboardAdminController controller = new OperationsDashboardAdminController(service);
        Date periodStart = new Date(1_719_630_400_000L);
        Date periodEnd = new Date(1_719_716_800_000L);
        when(service.overview(argThat(query -> query != null && "CUSTOM".equals(query.getPeriodType()))))
                .thenReturn(new OperationsDashboardOverviewResult(
                        periodStart,
                        periodEnd,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        3L,
                        2L,
                        1L,
                        BigDecimal.valueOf(120),
                        BigDecimal.valueOf(8),
                        4L,
                        5L,
                        BigDecimal.valueOf(16),
                        BigDecimal.ZERO,
                        1,
                        2,
                        1,
                        0,
                        0,
                        0,
                        null,
                        null,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(new OperationsHealthSummaryResult(
                                HealthCheckId.of(9101L),
                                "admin-server",
                                "UP",
                                12,
                                "ok",
                                "LOCAL",
                                "admin-server",
                                periodEnd)),
                        List.of(new TaskStatusSummaryResult("RUNNING", 2L)),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of()));

        OperationsDashboardOverviewRequest request = new OperationsDashboardOverviewRequest();
        request.setPeriodType("CUSTOM");
        request.setPeriodStart(periodStart);
        request.setPeriodEnd(periodEnd);
        var response = controller.overview(request);

        assertEquals(periodStart, response.getPeriodStart());
        assertEquals(periodEnd, response.getPeriodEnd());
        assertEquals(3L, response.getAiInvocationCount());
        assertEquals(1, response.getHealthSummaries().size());
        assertEquals(9101L, response.getHealthSummaries().get(0).getCheckId());
        assertEquals(1, response.getTaskStatusSummaries().size());
        assertEquals("RUNNING", response.getTaskStatusSummaries().get(0).getTaskStatus());

        verify(service)
                .overview(argThat((OperationsDashboardOverviewQuery query) -> query != null
                        && "CUSTOM".equals(query.getPeriodType())
                        && periodStart.equals(query.getPeriodStart())
                        && periodEnd.equals(query.getPeriodEnd())));
    }

    @Test
    void overviewShouldKeepNullCollectionsFromApplicationResult() {
        OperationsDashboardApplicationService service = mock(OperationsDashboardApplicationService.class);
        OperationsDashboardAdminController controller = new OperationsDashboardAdminController(service);
        Date periodStart = new Date(1_719_630_400_000L);
        Date periodEnd = new Date(1_719_716_800_000L);
        when(service.overview(argThat(query -> query != null && "WEEK".equals(query.getPeriodType()))))
                .thenReturn(new OperationsDashboardOverviewResult(
                        periodStart,
                        periodEnd,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "CRITICAL",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));

        OperationsDashboardOverviewRequest request = new OperationsDashboardOverviewRequest();
        request.setPeriodType("WEEK");
        var response = controller.overview(request);

        assertEquals(periodStart, response.getPeriodStart());
        assertEquals(periodEnd, response.getPeriodEnd());
        assertEquals("CRITICAL", response.getHighestAlertLevel());
        assertNull(response.getLatestAlert());
        assertNull(response.getContentGrowthSeries());
        assertNull(response.getSearchTrendSeries());
        assertNull(response.getHealthSummaries());
        assertNull(response.getTaskStatusSummaries());
        assertNull(response.getTopContents());
        assertNull(response.getTopQueries());
        assertNull(response.getTopTags());
        assertNull(response.getTopAiCapabilities());
    }

    private void assertRequestMapping(Class<?> type, String expectedPath) {
        RequestMapping mapping = type.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private void assertPostMapping(
            Class<?> type, String methodName, String expectedPath, String expectedPermission, Class<?>... parameters)
            throws Exception {
        Method method = type.getDeclaredMethod(methodName, parameters);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of(expectedPermission), List.of(permission.value()));
    }
}
