package com.thundax.kuzhambu.operations.interfaces.admin.report.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportDetailResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportDownloadResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportGenerateResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportPageResult;
import com.thundax.kuzhambu.operations.application.report.service.ReportApplicationService;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportGenerateRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportPageRequest;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class OperationsReportAdminControllerTest {

    @Test
    void routesShouldKeepAdminReportApiPathsAndPermissions() throws Exception {
        assertRequestMapping(OperationsReportAdminController.class, "/api/operations/report");
        assertPostMapping(
                OperationsReportAdminController.class,
                "generate",
                "generate",
                "operations:report:generate",
                OperationsReportGenerateRequest.class);
        assertPostMapping(
                OperationsReportAdminController.class,
                "page",
                "page",
                "operations:report:view",
                OperationsReportPageRequest.class);
        assertPostMapping(
                OperationsReportAdminController.class,
                "detail",
                "detail",
                "operations:report:view",
                OperationsReportDetailRequest.class);
        assertGetMapping(
                OperationsReportAdminController.class,
                "content",
                "{reportId}/content",
                "operations:report:view",
                Long.class,
                Boolean.class,
                jakarta.servlet.http.HttpServletResponse.class);
    }

    @Test
    void endpointsShouldDelegateToApplicationService() throws Exception {
        ReportApplicationService service = mock(ReportApplicationService.class);
        OperationsReportAdminController controller = new OperationsReportAdminController(service);
        when(service.generate(any())).thenReturn(new OperationsReportGenerateResult(ReportId.of(9001L), "PENDING"));
        when(service.page(any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1L,
                        List.of(new OperationsReportPageResult(
                                ReportId.of(9001L),
                                "WEEKLY",
                                "PDF",
                                new Date(1_718_000_000_000L),
                                new Date(1_718_086_400_000L),
                                3001L,
                                "weekly-report.pdf",
                                "SUCCEEDED",
                                null,
                                1001L,
                                new Date(1_718_086_500_000L),
                                new Date(1_718_086_600_000L)))));
        when(service.detail(any()))
                .thenReturn(new OperationsReportDetailResult(
                        ReportId.of(9001L),
                        "WEEKLY",
                        "PDF",
                        new Date(1_718_000_000_000L),
                        new Date(1_718_086_400_000L),
                        "req-1",
                        "trace-1",
                        "2026.06.26",
                        3001L,
                        "weekly-report.pdf",
                        "SUCCEEDED",
                        null,
                        1001L,
                        new Date(1_718_086_500_000L),
                        new Date(1_718_086_600_000L)));
        when(service.download(any()))
                .thenReturn(new OperationsReportDownloadResult(
                        ReportId.of(9001L),
                        "PDF",
                        "weekly-report.pdf",
                        "application/pdf",
                        11L,
                        "storage-report.pdf",
                        new ByteArrayInputStream("pdf-content".getBytes())));

        OperationsReportGenerateRequest generateRequest = new OperationsReportGenerateRequest();
        generateRequest.setReportType("WEEKLY");
        generateRequest.setFormat("PDF");
        generateRequest.setPeriodStart(new Date(1_718_000_000_000L));
        generateRequest.setPeriodEnd(new Date(1_718_086_400_000L));
        var generateResponse = controller.generate(generateRequest);
        assertEquals(9001L, generateResponse.getReportId());
        assertEquals("PENDING", generateResponse.getReportStatus());

        OperationsReportPageRequest pageRequest = new OperationsReportPageRequest();
        pageRequest.setReportType("WEEKLY");
        pageRequest.setFormat("PDF");
        pageRequest.setReportStatus("SUCCEEDED");
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(10);
        var pageResponse = controller.page(pageRequest);
        assertEquals(1L, pageResponse.getCount());
        assertEquals(9001L, pageResponse.getRecords().get(0).getReportId());

        OperationsReportDetailRequest detailRequest = new OperationsReportDetailRequest();
        detailRequest.setReportId(9001L);
        var detailResponse = controller.detail(detailRequest);
        assertEquals(9001L, detailResponse.getReportId());
        assertEquals("req-1", detailResponse.getRequestId());

        MockHttpServletResponse contentResponse = new MockHttpServletResponse();
        controller.content(9001L, true, contentResponse);
        assertEquals("application/pdf", contentResponse.getContentType());
        assertEquals(11, contentResponse.getContentLength());
        assertEquals("pdf-content", contentResponse.getContentAsString());
        assertEquals(
                "attachment; filename=\"weekly-report.pdf\"; filename*=UTF-8''weekly-report.pdf",
                contentResponse.getHeader("Content-Disposition"));

        verify(service)
                .generate(argThat(command -> command != null
                        && "WEEKLY".equals(command.getReportType())
                        && "PDF".equals(command.getFormat())));
        verify(service)
                .page(
                        argThat(query -> query != null
                                && "WEEKLY".equals(query.getReportType())
                                && "PDF".equals(query.getFormat())
                                && "SUCCEEDED".equals(query.getReportStatus())),
                        argThat((PageQuery pageQuery) ->
                                pageQuery != null && pageQuery.getPageNo() == 1 && pageQuery.getPageSize() == 10));
        verify(service)
                .detail(argThat(query -> query != null
                        && query.getReportId() != null
                        && query.getReportId().value().equals(9001L)));
        verify(service)
                .download(argThat(query -> query != null
                        && query.getReportId() != null
                        && query.getReportId().value().equals(9001L)));
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

    private void assertGetMapping(
            Class<?> type, String methodName, String expectedPath, String expectedPermission, Class<?>... parameters)
            throws Exception {
        Method method = type.getDeclaredMethod(methodName, parameters);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of(expectedPermission), List.of(permission.value()));
    }
}
