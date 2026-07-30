package com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupDetailResult;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupPageResult;
import com.thundax.kuzhambu.operations.application.cleanup.service.CleanupApplicationService;
import com.thundax.kuzhambu.operations.domain.cleanup.codec.CleanupItemIdCodec;
import com.thundax.kuzhambu.operations.domain.cleanup.codec.CleanupJobIdCodec;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request.OperationsCleanupDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request.OperationsCleanupExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request.OperationsCleanupPageRequest;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class OperationsCleanupAdminControllerTest {

    @Test
    void routesShouldKeepAdminCleanupApiPathsAndPermissions() throws Exception {
        assertRequestMapping(OperationsCleanupAdminController.class, "/api/operations/cleanup");
        assertPostMapping(
                OperationsCleanupAdminController.class,
                "execute",
                "execute",
                "operations:cleanup:execute",
                OperationsCleanupExecuteRequest.class);
        assertPostMapping(
                OperationsCleanupAdminController.class,
                "page",
                "page",
                "operations:cleanup:view",
                OperationsCleanupPageRequest.class);
        assertPostMapping(
                OperationsCleanupAdminController.class,
                "detail",
                "detail",
                "operations:cleanup:view",
                OperationsCleanupDetailRequest.class);
    }

    @Test
    void endpointsShouldDelegateToApplicationService() {
        CleanupApplicationService service = mock(CleanupApplicationService.class);
        OperationsCleanupAdminController controller = new OperationsCleanupAdminController(service);
        when(service.execute(any()))
                .thenReturn(new OperationsCleanupDetailResult(
                        CleanupJobIdCodec.toDomain(9101L),
                        "LONG_TASK",
                        "SUCCEEDED",
                        3,
                        3,
                        0,
                        null,
                        1001L,
                        Instant.ofEpochMilli(1_719_630_400_000L),
                        Instant.ofEpochMilli(1_719_630_500_000L),
                        List.of()));
        when(service.page(any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1L,
                        List.of(new OperationsCleanupPageResult(
                                CleanupJobIdCodec.toDomain(9101L),
                                "LONG_TASK",
                                "SUCCEEDED",
                                3,
                                3,
                                0,
                                null,
                                1001L,
                                Instant.ofEpochMilli(1_719_630_400_000L),
                                Instant.ofEpochMilli(1_719_630_500_000L)))));
        when(service.detail(any()))
                .thenReturn(new OperationsCleanupDetailResult(
                        CleanupJobIdCodec.toDomain(9101L),
                        "LONG_TASK",
                        "SUCCEEDED",
                        3,
                        3,
                        0,
                        null,
                        1001L,
                        Instant.ofEpochMilli(1_719_630_400_000L),
                        Instant.ofEpochMilli(1_719_630_500_000L),
                        List.of(new OperationsCleanupDetailResult.Item(
                                CleanupItemIdCodec.toDomain(9201L),
                                "share",
                                201L,
                                "FAILED",
                                "TARGET_NOT_FOUND",
                                Instant.ofEpochMilli(1_719_630_450_000L)))));

        OperationsCleanupExecuteRequest executeRequest = new OperationsCleanupExecuteRequest();
        executeRequest.setCleanupType("LONG_TASK");
        var executeResponse = controller.execute(executeRequest);
        assertEquals(9101L, executeResponse.getCleanupId());
        assertEquals("SUCCEEDED", executeResponse.getCleanupStatus());

        OperationsCleanupPageRequest pageRequest = new OperationsCleanupPageRequest();
        pageRequest.setCleanupType("LONG_TASK");
        pageRequest.setCleanupStatus("SUCCEEDED");
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(10);
        var pageResponse = controller.page(pageRequest);
        assertEquals(1L, pageResponse.getCount());
        assertEquals(9101L, pageResponse.getRecords().get(0).getCleanupId());

        OperationsCleanupDetailRequest detailRequest = new OperationsCleanupDetailRequest();
        detailRequest.setCleanupId(9101L);
        var detailResponse = controller.detail(detailRequest);
        assertEquals(9101L, detailResponse.getCleanupId());
        assertEquals(1, detailResponse.getItems().size());
        assertEquals(9201L, detailResponse.getItems().get(0).getCleanupItemId());
        assertEquals("share", detailResponse.getItems().get(0).getTargetType());
        assertEquals("TARGET_NOT_FOUND", detailResponse.getItems().get(0).getFailureReason());

        verify(service).execute(argThat(command -> command != null && "LONG_TASK".equals(command.getCleanupType())));
        verify(service)
                .page(
                        argThat(query -> query != null
                                && "LONG_TASK".equals(query.getCleanupType())
                                && "SUCCEEDED".equals(query.getCleanupStatus())),
                        argThat((PageQuery pageQuery) ->
                                pageQuery != null && pageQuery.getPageNo() == 1 && pageQuery.getPageSize() == 10));
        verify(service)
                .detail(argThat(query -> query != null
                        && query.getCleanupId() != null
                        && query.getCleanupId().value().equals(9101L)));
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
