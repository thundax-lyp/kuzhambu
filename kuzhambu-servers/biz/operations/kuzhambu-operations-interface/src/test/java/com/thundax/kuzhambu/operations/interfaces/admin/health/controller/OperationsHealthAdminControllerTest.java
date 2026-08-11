package com.thundax.kuzhambu.operations.interfaces.admin.health.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthPageResult;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthSummaryResult;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthTrendResult;
import com.thundax.kuzhambu.operations.application.health.service.HealthCheckApplicationService;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthCheckIdCodec;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthSummaryRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthTrendRequest;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class OperationsHealthAdminControllerTest {

    @Test
    void routesShouldKeepAdminHealthApiPathsAndPermissions() throws Exception {
        assertRequestMapping(OperationsHealthAdminController.class, "/api/operations/health");
        assertPostMapping(
                OperationsHealthAdminController.class,
                "listSummary",
                "list",
                "operations:health:view",
                OperationsHealthSummaryRequest.class);
        assertPostMapping(
                OperationsHealthAdminController.class,
                "page",
                "page",
                "operations:health:view",
                OperationsHealthPageRequest.class);
        assertPostMapping(
                OperationsHealthAdminController.class,
                "getTrend",
                "get",
                "operations:health:view",
                OperationsHealthTrendRequest.class);
    }

    @Test
    void endpointsShouldDelegateToApplicationService() {
        HealthCheckApplicationService service = mock(HealthCheckApplicationService.class);
        OperationsHealthAdminController controller = new OperationsHealthAdminController(service);
        when(service.summary())
                .thenReturn(List.of(new OperationsHealthSummaryResult(
                        HealthCheckIdCodec.toDomain(9101L),
                        "db-master",
                        "UP",
                        16,
                        "ping ok",
                        "DATABASE",
                        "primary",
                        Instant.ofEpochMilli(1_719_630_400_000L))));
        when(service.page(any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1L,
                        List.of(new OperationsHealthPageResult(
                                HealthCheckIdCodec.toDomain(9101L),
                                "db-master",
                                "UP",
                                16,
                                "ping ok",
                                "DATABASE",
                                "primary",
                                "{\"pool\":\"ok\"}",
                                Instant.ofEpochMilli(1_719_630_400_000L)))));
        when(service.trend(any())).thenReturn(List.of(new OperationsHealthTrendResult("2026-07-06", 2L, 1L, 0L, 12L)));

        var summaryResponse = controller.listSummary(new OperationsHealthSummaryRequest());
        assertEquals(1, summaryResponse.size());
        assertEquals(9101L, summaryResponse.get(0).getCheckId());
        assertEquals("DATABASE", summaryResponse.get(0).getProbeSource());

        OperationsHealthPageRequest pageRequest = new OperationsHealthPageRequest();
        pageRequest.setComponent("db-master");
        pageRequest.setHealthStatus("UP");
        pageRequest.setProbeSource("HTTP");
        pageRequest.setProbeTarget("http://127.0.0.1:8080/internal/health");
        pageRequest.setCheckedAtStart(Instant.ofEpochMilli(1_719_630_400_000L));
        pageRequest.setCheckedAtEnd(Instant.ofEpochMilli(1_719_716_800_000L));
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(10);
        var pageResponse = controller.page(pageRequest);
        assertEquals(1L, pageResponse.getCount());
        assertEquals(9101L, pageResponse.getRecords().get(0).getCheckId());
        assertEquals("primary", pageResponse.getRecords().get(0).getProbeTarget());
        assertEquals("{\"pool\":\"ok\"}", pageResponse.getRecords().get(0).getDetailsJson());

        OperationsHealthTrendRequest trendRequest = new OperationsHealthTrendRequest();
        trendRequest.setComponent("db-master");
        trendRequest.setProbeSource("DATABASE");
        trendRequest.setPeriodStart(Instant.ofEpochMilli(1_719_630_400_000L));
        trendRequest.setPeriodEnd(Instant.ofEpochMilli(1_719_716_800_000L));
        trendRequest.setBucketType("DAY");
        var trendResponse = controller.getTrend(trendRequest);
        assertEquals(1, trendResponse.size());
        assertEquals("2026-07-06", trendResponse.get(0).getBucket());
        assertEquals(2L, trendResponse.get(0).getUpCount());
        assertEquals(12L, trendResponse.get(0).getAvgLatencyMs());

        verify(service).summary();
        verify(service)
                .page(
                        argThat(query -> query != null
                                && "db-master".equals(query.component())
                                && "UP".equals(query.healthStatus())
                                && "HTTP".equals(query.probeSource())
                                && "http://127.0.0.1:8080/internal/health".equals(query.probeTarget())
                                && Instant.ofEpochMilli(1_719_630_400_000L).equals(query.checkedAtStart())
                                && Instant.ofEpochMilli(1_719_716_800_000L).equals(query.checkedAtEnd())),
                        argThat((PageQuery pageQuery) ->
                                pageQuery != null && pageQuery.getPageNo() == 1 && pageQuery.getPageSize() == 10));
        verify(service)
                .trend(argThat(query -> query != null
                        && "db-master".equals(query.component())
                        && "DATABASE".equals(query.probeSource())
                        && "DAY".equals(query.bucketType())));
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
