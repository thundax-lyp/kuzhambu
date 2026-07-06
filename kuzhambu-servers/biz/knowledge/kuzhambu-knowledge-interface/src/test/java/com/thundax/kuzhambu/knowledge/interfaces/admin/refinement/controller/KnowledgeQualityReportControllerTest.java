package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.knowledge.application.refinement.command.GenerateQualityReportCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityReportPageQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.ReportRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.service.KnowledgeQualityReportApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.request.QualityReportRequests;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class KnowledgeQualityReportControllerTest {

    @Test
    void routesAndPermissionsShouldRemainStable() throws Exception {
        RequestMapping root = KnowledgeQualityReportController.class.getAnnotation(RequestMapping.class);
        assertEquals("/api/knowledge/quality/report", root.value()[0]);
        assertPostMapping("generate", "generate", "knowledge:quality-report:generate");
        assertPostMapping("page", "page", "knowledge:quality-report:view");
        assertPostMapping("detail", "detail", "knowledge:quality-report:view");
        assertPostMapping("latest", "latest", "knowledge:quality-report:view");
    }

    @Test
    void generateShouldMapRequestAndDetailResponse() {
        KnowledgeQualityReportApplicationService service = mock(KnowledgeQualityReportApplicationService.class);
        KnowledgeQualityReportController controller = new KnowledgeQualityReportController(service);
        QualityReportRequests.GenerateRequest request = new QualityReportRequests.GenerateRequest();
        request.setGraphVersionId(71L);
        request.setGeneratedBy(1L);
        when(service.generateReport(any()))
                .thenReturn(new QualityReportDetailResult(reportRecord(), List.of(), List.of(), List.of()));

        var response = controller.generate(request);

        ArgumentCaptor<GenerateQualityReportCommand> captor =
                ArgumentCaptor.forClass(GenerateQualityReportCommand.class);
        verify(service).generateReport(captor.capture());
        assertEquals(71L, captor.getValue().getGraphVersionId());
        assertEquals(1L, captor.getValue().getGeneratedBy());
        assertEquals("KQR-71", response.getReport().getReportNo());
    }

    @Test
    void pageShouldMapQueryAndPageResponse() {
        KnowledgeQualityReportApplicationService service = mock(KnowledgeQualityReportApplicationService.class);
        KnowledgeQualityReportController controller = new KnowledgeQualityReportController(service);
        QualityReportRequests.PageRequestBody request = new QualityReportRequests.PageRequestBody();
        request.setPageNo(1);
        request.setPageSize(10);
        request.setGraphVersionId(71L);
        request.setReportStatus("PUBLISHED");
        when(service.pageReports(any())).thenReturn(PageResult.of(1, 10, 1, List.of(reportRecord())));

        var response = controller.page(request);

        ArgumentCaptor<QualityReportPageQuery> captor = ArgumentCaptor.forClass(QualityReportPageQuery.class);
        verify(service).pageReports(captor.capture());
        assertEquals(71L, captor.getValue().getGraphVersionId());
        assertEquals("PUBLISHED", captor.getValue().getReportStatus());
        assertEquals("KQR-71", response.getRecords().get(0).getReportNo());
    }

    private static void assertPostMapping(String methodName, String path, String permission) throws Exception {
        Method method = KnowledgeQualityReportController.class.getDeclaredMethods().length == 0
                ? null
                : KnowledgeQualityReportController.class.getDeclaredMethod(methodName, requestType(methodName));
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        HasPermission hasPermission = method.getAnnotation(HasPermission.class);
        assertEquals(path, postMapping.value()[0]);
        assertEquals(permission, hasPermission.value());
    }

    private static Class<?> requestType(String methodName) {
        return switch (methodName) {
            case "generate" -> QualityReportRequests.GenerateRequest.class;
            case "page" -> QualityReportRequests.PageRequestBody.class;
            case "detail" -> QualityReportRequests.DetailRequest.class;
            case "latest" -> QualityReportRequests.LatestRequest.class;
            default -> throw new IllegalArgumentException(methodName);
        };
    }

    private static ReportRecord reportRecord() {
        return new ReportRecord(
                1001L,
                "KQR-71",
                71L,
                "SANCAI_ENTRY",
                1001L,
                "myth",
                "神话",
                "PUBLISHED",
                2L,
                1L,
                1L,
                1L,
                0L,
                0L,
                new BigDecimal("0.5000"),
                BigDecimal.ONE,
                BigDecimal.ZERO,
                new BigDecimal("0.6667"),
                1L,
                2L,
                1L,
                null,
                null);
    }
}
