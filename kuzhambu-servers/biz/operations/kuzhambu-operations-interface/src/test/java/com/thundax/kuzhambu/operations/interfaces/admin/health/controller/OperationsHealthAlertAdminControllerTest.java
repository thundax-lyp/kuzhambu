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
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthAlertPageResult;
import com.thundax.kuzhambu.operations.application.health.service.HealthAlertApplicationService;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertAckRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertRecoverRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class OperationsHealthAlertAdminControllerTest {

    @Test
    void routesShouldKeepAdminHealthAlertApiPathsAndPermissions() throws Exception {
        assertRequestMapping(OperationsHealthAlertAdminController.class, "/api/operations/health/alerts");
        assertPostMapping(
                OperationsHealthAlertAdminController.class,
                "page",
                "page",
                "operations:health:view",
                OperationsHealthAlertPageRequest.class);
        assertPostMapping(
                OperationsHealthAlertAdminController.class,
                "ack",
                "ack",
                "operations:health:manage",
                OperationsHealthAlertAckRequest.class);
        assertPostMapping(
                OperationsHealthAlertAdminController.class,
                "recover",
                "recover",
                "operations:health:manage",
                OperationsHealthAlertRecoverRequest.class);
    }

    @Test
    void endpointsShouldDelegateToApplicationService() {
        HealthAlertApplicationService service = mock(HealthAlertApplicationService.class);
        OperationsHealthAlertAdminController controller = new OperationsHealthAlertAdminController(service);
        when(service.page(any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1L,
                        List.of(new OperationsHealthAlertPageResult(
                                HealthAlertId.of(9201L),
                                "database",
                                "HEALTH_DOWN",
                                "CRITICAL",
                                "ACTIVE",
                                "HEALTH",
                                null,
                                HealthCheckId.of(9101L),
                                "database down",
                                "check database",
                                "OPEN_HEALTH_DETAIL",
                                "{\"route\":\"/operations/dashboard\"}",
                                new Date(1_719_630_400_000L),
                                new Date(1_719_630_500_000L),
                                null,
                                null,
                                null,
                                "probe timeout"))));

        OperationsHealthAlertPageRequest pageRequest = new OperationsHealthAlertPageRequest();
        pageRequest.setComponent("database");
        pageRequest.setAlertLevel("CRITICAL");
        pageRequest.setAlertStatus("ACTIVE");
        pageRequest.setSourceRefType("HEALTH");
        pageRequest.setSourceRefId(9001L);
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(10);
        var pageResponse = controller.page(pageRequest);
        assertEquals(1L, pageResponse.getCount());
        assertEquals(9201L, pageResponse.getRecords().get(0).getAlertId());
        assertEquals("HEALTH_DOWN", pageResponse.getRecords().get(0).getAlertType());
        assertEquals("OPEN_HEALTH_DETAIL", pageResponse.getRecords().get(0).getRecoveryAction());
        assertEquals("probe timeout", pageResponse.getRecords().get(0).getFailureReason());

        OperationsHealthAlertAckRequest ackRequest = new OperationsHealthAlertAckRequest();
        ackRequest.setAlertId(9201L);
        controller.ack(ackRequest);

        OperationsHealthAlertRecoverRequest recoverRequest = new OperationsHealthAlertRecoverRequest();
        recoverRequest.setAlertId(9201L);
        controller.recover(recoverRequest);

        verify(service)
                .page(
                        argThat(query -> query != null
                                && "database".equals(query.getComponent())
                                && "CRITICAL".equals(query.getAlertLevel())
                                && "ACTIVE".equals(query.getAlertStatus())
                                && "HEALTH".equals(query.getSourceRefType())
                                && Long.valueOf(9001L).equals(query.getSourceRefId())),
                        argThat((PageQuery pageQuery) ->
                                pageQuery != null && pageQuery.getPageNo() == 1 && pageQuery.getPageSize() == 10));
        verify(service)
                .ack(argThat(command ->
                        command != null && command.getAlertId().value().equals(9201L)));
        verify(service)
                .recover(argThat(command ->
                        command != null && command.getAlertId().value().equals(9201L)));
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
