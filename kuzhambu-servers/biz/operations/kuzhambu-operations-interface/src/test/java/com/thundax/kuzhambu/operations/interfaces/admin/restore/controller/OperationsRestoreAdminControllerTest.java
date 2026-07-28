package com.thundax.kuzhambu.operations.interfaces.admin.restore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreDetailResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreExecuteResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestorePageResult;
import com.thundax.kuzhambu.operations.application.restore.service.RestoreApplicationService;
import com.thundax.kuzhambu.operations.domain.restore.codec.RestoreIdCodec;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestoreDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestoreExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestorePageRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class OperationsRestoreAdminControllerTest {

    @Test
    void routesShouldKeepAdminRestoreApiPathsAndPermissions() throws Exception {
        assertRequestMapping(OperationsRestoreAdminController.class, "/api/operations/restore");
        assertPostMapping(
                OperationsRestoreAdminController.class,
                "execute",
                "execute",
                "operations:restore:execute",
                OperationsRestoreExecuteRequest.class);
        assertPostMapping(
                OperationsRestoreAdminController.class,
                "page",
                "page",
                "operations:restore:view",
                OperationsRestorePageRequest.class);
        assertPostMapping(
                OperationsRestoreAdminController.class,
                "detail",
                "detail",
                "operations:restore:view",
                OperationsRestoreDetailRequest.class);
    }

    @Test
    void endpointsShouldDelegateToApplicationService() {
        RestoreApplicationService service = mock(RestoreApplicationService.class);
        OperationsRestoreAdminController controller = new OperationsRestoreAdminController(service);
        when(service.execute(any()))
                .thenReturn(new OperationsRestoreExecuteResult(
                        RestoreIdCodec.toDomain(9101L),
                        9001L,
                        9201L,
                        "DRILL",
                        "SUCCEEDED",
                        Boolean.TRUE,
                        new Date(1_719_630_410_000L),
                        new Date(1_719_630_490_000L),
                        null,
                        new Date(1_719_630_400_000L),
                        new Date(1_719_630_500_000L)));
        when(service.page(any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1L,
                        List.of(new OperationsRestorePageResult(
                                RestoreIdCodec.toDomain(9101L),
                                9001L,
                                9201L,
                                "DRILL",
                                "SUCCEEDED",
                                Boolean.TRUE,
                                new Date(1_719_630_410_000L),
                                new Date(1_719_630_490_000L),
                                null,
                                1001L,
                                new Date(1_719_630_400_000L),
                                new Date(1_719_630_500_000L)))));
        when(service.detail(any()))
                .thenReturn(new OperationsRestoreDetailResult(
                        RestoreIdCodec.toDomain(9101L),
                        9001L,
                        9201L,
                        "DRILL",
                        "SUCCEEDED",
                        Boolean.TRUE,
                        new Date(1_719_630_410_000L),
                        new Date(1_719_630_490_000L),
                        null,
                        1001L,
                        new Date(1_719_630_400_000L),
                        new Date(1_719_630_500_000L)));

        OperationsRestoreExecuteRequest executeRequest = new OperationsRestoreExecuteRequest();
        executeRequest.setBackupId(9001L);
        executeRequest.setRestoreMode("DRILL");
        var executeResponse = controller.execute(executeRequest);
        assertEquals(9101L, executeResponse.getRestoreId());
        assertEquals("DRILL", executeResponse.getRestoreMode());
        assertEquals("SUCCEEDED", executeResponse.getRestoreStatus());
        assertEquals(new Date(1_719_630_410_000L), executeResponse.getWriteBlockStartedAt());
        assertEquals(new Date(1_719_630_490_000L), executeResponse.getWriteBlockReleasedAt());

        OperationsRestorePageRequest pageRequest = new OperationsRestorePageRequest();
        pageRequest.setBackupId(9001L);
        pageRequest.setRestoreMode("DRILL");
        pageRequest.setRestoreStatus("SUCCEEDED");
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(10);
        var pageResponse = controller.page(pageRequest);
        assertEquals(1L, pageResponse.getCount());
        assertEquals(9101L, pageResponse.getRecords().get(0).getRestoreId());

        OperationsRestoreDetailRequest detailRequest = new OperationsRestoreDetailRequest();
        detailRequest.setRestoreId(9101L);
        var detailResponse = controller.detail(detailRequest);
        assertEquals(9101L, detailResponse.getRestoreId());

        verify(service)
                .execute(argThat(command -> command != null
                        && command.getBackupId() != null
                        && command.getBackupId().value().equals(9001L)
                        && "DRILL".equals(command.getRestoreMode())));
        verify(service)
                .page(
                        argThat(query -> query != null
                                && Long.valueOf(9001L).equals(query.getBackupId())
                                && "DRILL".equals(query.getRestoreMode())
                                && "SUCCEEDED".equals(query.getRestoreStatus())),
                        argThat((PageQuery pageQuery) ->
                                pageQuery != null && pageQuery.getPageNo() == 1 && pageQuery.getPageSize() == 10));
        verify(service)
                .detail(argThat(query -> query != null
                        && query.getRestoreId() != null
                        && query.getRestoreId().value().equals(9101L)));
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
