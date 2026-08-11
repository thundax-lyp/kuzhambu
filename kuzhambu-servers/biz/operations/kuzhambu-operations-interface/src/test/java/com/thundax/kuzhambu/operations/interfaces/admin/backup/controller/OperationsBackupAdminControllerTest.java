package com.thundax.kuzhambu.operations.interfaces.admin.backup.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupDetailResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupExecuteResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupPageResult;
import com.thundax.kuzhambu.operations.application.backup.service.BackupApplicationService;
import com.thundax.kuzhambu.operations.domain.backup.codec.BackupIdCodec;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupPageRequest;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class OperationsBackupAdminControllerTest {

    @Test
    void routesShouldKeepAdminBackupApiPathsAndPermissions() throws Exception {
        assertRequestMapping(OperationsBackupAdminController.class, "/api/operations/backup");
        assertPostMapping(
                OperationsBackupAdminController.class,
                "create",
                "create",
                "operations:backup:execute",
                OperationsBackupExecuteRequest.class);
        assertPostMapping(
                OperationsBackupAdminController.class,
                "page",
                "page",
                "operations:backup:view",
                OperationsBackupPageRequest.class);
        assertPostMapping(
                OperationsBackupAdminController.class,
                "getDetail",
                "get",
                "operations:backup:view",
                OperationsBackupDetailRequest.class);
    }

    @Test
    void endpointsShouldDelegateToApplicationService() {
        BackupApplicationService service = mock(BackupApplicationService.class);
        OperationsBackupAdminController controller = new OperationsBackupAdminController(service);
        when(service.execute(any()))
                .thenReturn(new OperationsBackupExecuteResult(
                        BackupIdCodec.toDomain(9001L),
                        "MANUAL",
                        "SUCCEEDED",
                        "backup_20260629-120000.sql",
                        4096L,
                        "sha256-backup",
                        null,
                        Instant.ofEpochMilli(1_719_630_400_000L),
                        Instant.ofEpochMilli(1_719_630_500_000L),
                        Instant.ofEpochMilli(1_722_222_400_000L)));
        when(service.page(any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1L,
                        List.of(new OperationsBackupPageResult(
                                BackupIdCodec.toDomain(9001L),
                                "MANUAL",
                                "SUCCEEDED",
                                "backup_20260629-120000.sql",
                                4096L,
                                "sha256-backup",
                                null,
                                1001L,
                                Instant.ofEpochMilli(1_719_630_400_000L),
                                Instant.ofEpochMilli(1_719_630_500_000L),
                                Instant.ofEpochMilli(1_722_222_400_000L)))));
        when(service.detail(any()))
                .thenReturn(new OperationsBackupDetailResult(
                        BackupIdCodec.toDomain(9001L),
                        "MANUAL",
                        "SUCCEEDED",
                        null,
                        "backup_20260629-120000.sql",
                        4096L,
                        "sha256-backup",
                        null,
                        1001L,
                        Instant.ofEpochMilli(1_719_630_400_000L),
                        Instant.ofEpochMilli(1_719_630_500_000L),
                        Instant.ofEpochMilli(1_722_222_400_000L)));

        var executeResponse = controller.create(new OperationsBackupExecuteRequest());
        assertEquals(9001L, executeResponse.getBackupId());
        assertEquals("SUCCEEDED", executeResponse.getBackupStatus());

        OperationsBackupPageRequest pageRequest = new OperationsBackupPageRequest();
        pageRequest.setBackupType("MANUAL");
        pageRequest.setBackupStatus("SUCCEEDED");
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(10);
        var pageResponse = controller.page(pageRequest);
        assertEquals(1L, pageResponse.getCount());
        assertEquals(9001L, pageResponse.getRecords().get(0).getBackupId());

        OperationsBackupDetailRequest detailRequest = new OperationsBackupDetailRequest();
        detailRequest.setBackupId(9001L);
        var detailResponse = controller.getDetail(detailRequest);
        assertEquals(9001L, detailResponse.getBackupId());

        verify(service).execute(any());
        verify(service)
                .page(
                        argThat(query -> query != null
                                && "MANUAL".equals(query.backupType())
                                && "SUCCEEDED".equals(query.backupStatus())),
                        argThat((PageQuery pageQuery) ->
                                pageQuery != null && pageQuery.getPageNo() == 1 && pageQuery.getPageSize() == 10));
        verify(service)
                .detail(argThat(query -> query != null
                        && query.backupId() != null
                        && query.backupId().value().equals(9001L)));
    }

    @Test
    void detailShouldPreserveNullResultForMissingBackup() {
        BackupApplicationService service = mock(BackupApplicationService.class);
        OperationsBackupAdminController controller = new OperationsBackupAdminController(service);
        OperationsBackupDetailRequest request = new OperationsBackupDetailRequest();
        request.setBackupId(9001L);

        var response = controller.getDetail(request);

        assertNull(response);
        verify(service).detail(any());
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
