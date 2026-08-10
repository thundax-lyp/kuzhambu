package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.knowledge.application.refinement.command.GenerateQualityReportCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ReextractLowQualityCategoryCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.LatestQualityReportQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityReportDetailQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityReportQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.ReportRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.ReextractLowQualityCategoryResult;
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
        assertPostMapping("create", "create", "knowledge:quality-report:generate");
        assertPostMapping("page", "page", "knowledge:quality-report:view");
        assertPostMapping("get", "get", "knowledge:quality-report:view");
        assertPostMapping("latest", "latest", "knowledge:quality-report:view");
        assertPostMapping("extractLowQualityCategory", "extract", "knowledge:graph:edit");
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

        var response = controller.create(request);

        ArgumentCaptor<GenerateQualityReportCommand> captor =
                ArgumentCaptor.forClass(GenerateQualityReportCommand.class);
        verify(service).generateReport(captor.capture());
        assertEquals(71L, captor.getValue().graphVersionId());
        assertEquals(1L, captor.getValue().generatedBy());
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
        when(service.pageReports(any(), any())).thenReturn(PageResult.of(1, 10, 1, List.of(reportRecord())));

        var response = controller.page(request);

        ArgumentCaptor<QualityReportQuery> queryCaptor = ArgumentCaptor.forClass(QualityReportQuery.class);
        ArgumentCaptor<PageQuery> pageCaptor = ArgumentCaptor.forClass(PageQuery.class);
        verify(service).pageReports(queryCaptor.capture(), pageCaptor.capture());
        assertEquals(71L, queryCaptor.getValue().graphVersionId());
        assertEquals("PUBLISHED", queryCaptor.getValue().reportStatus());
        assertEquals(1, pageCaptor.getValue().getPageNo());
        assertEquals(10, pageCaptor.getValue().getPageSize());
        assertEquals("KQR-71", response.getRecords().get(0).getReportNo());
    }

    @Test
    void detailShouldMapQueryAndResponse() {
        KnowledgeQualityReportApplicationService service = mock(KnowledgeQualityReportApplicationService.class);
        KnowledgeQualityReportController controller = new KnowledgeQualityReportController(service);
        QualityReportRequests.DetailRequest request = new QualityReportRequests.DetailRequest();
        request.setReportId(1001L);
        when(service.detail(any()))
                .thenReturn(new QualityReportDetailResult(reportRecord(), List.of(), List.of(), List.of()));

        var response = controller.get(request);

        ArgumentCaptor<QualityReportDetailQuery> captor = ArgumentCaptor.forClass(QualityReportDetailQuery.class);
        verify(service).detail(captor.capture());
        assertEquals(1001L, captor.getValue().reportId());
        assertEquals("KQR-71", response.getReport().getReportNo());
    }

    @Test
    void latestShouldMapQueryAndResponse() {
        KnowledgeQualityReportApplicationService service = mock(KnowledgeQualityReportApplicationService.class);
        KnowledgeQualityReportController controller = new KnowledgeQualityReportController(service);
        QualityReportRequests.LatestRequest request = new QualityReportRequests.LatestRequest();
        request.setGraphVersionId(71L);
        when(service.latest(any()))
                .thenReturn(new QualityReportDetailResult(reportRecord(), List.of(), List.of(), List.of()));

        var response = controller.latest(request);

        ArgumentCaptor<LatestQualityReportQuery> captor = ArgumentCaptor.forClass(LatestQualityReportQuery.class);
        verify(service).latest(captor.capture());
        assertEquals(71L, captor.getValue().graphVersionId());
        assertEquals("KQR-71", response.getReport().getReportNo());
    }

    @Test
    void latestShouldKeepEmptyReportDetailResponse() {
        KnowledgeQualityReportApplicationService service = mock(KnowledgeQualityReportApplicationService.class);
        KnowledgeQualityReportController controller = new KnowledgeQualityReportController(service);
        QualityReportRequests.LatestRequest request = new QualityReportRequests.LatestRequest();
        request.setGraphVersionId(71L);
        when(service.latest(any()))
                .thenReturn(new QualityReportDetailResult(null, List.of(), List.of(), List.of(), false, null, null));

        var response = controller.latest(request);

        ArgumentCaptor<LatestQualityReportQuery> captor = ArgumentCaptor.forClass(LatestQualityReportQuery.class);
        verify(service).latest(captor.capture());
        assertEquals(71L, captor.getValue().graphVersionId());
        assertNull(response.getReport());
        assertFalse(response.getStale());
    }

    @Test
    void reextractShouldMapRequestAndResponse() {
        KnowledgeQualityReportApplicationService service = mock(KnowledgeQualityReportApplicationService.class);
        KnowledgeQualityReportController controller = new KnowledgeQualityReportController(service);
        QualityReportRequests.ReextractRequest request = new QualityReportRequests.ReextractRequest();
        request.setReportId(1001L);
        request.setSourceCategoryCode("myth");
        request.setTaskType("GRAPH");
        request.setReplaceUnconfirmedOnly(true);
        request.setModelId(1L);
        request.setModelName("gpt-5.5");
        request.setPromptMessagesJson("[]");
        request.setInputPayloadJson("{}");
        request.setRequestedBy(9L);
        when(service.reextractLowQualityCategory(any()))
                .thenReturn(new ReextractLowQualityCategoryResult(
                        1001L,
                        "myth",
                        "神话",
                        "SANCAI_ENTRY",
                        2001L,
                        3001L,
                        4001L,
                        "GRAPH",
                        "QUALITY_REPORT",
                        "{\"sourceContentIds\":[2001]}",
                        true));

        var response = controller.extractLowQualityCategory(request);

        ArgumentCaptor<ReextractLowQualityCategoryCommand> captor =
                ArgumentCaptor.forClass(ReextractLowQualityCategoryCommand.class);
        verify(service).reextractLowQualityCategory(captor.capture());
        assertEquals(1001L, captor.getValue().reportId());
        assertEquals("myth", captor.getValue().sourceCategoryCode());
        assertEquals("GRAPH", captor.getValue().taskType());
        assertEquals(3001L, response.getTaskId());
        assertEquals("QUALITY_REPORT", response.getTriggerSource());
    }

    private static void assertPostMapping(String methodName, String path, String permission) throws Exception {
        Method method = KnowledgeQualityReportController.class.getDeclaredMethod(methodName, requestType(methodName));
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        HasPermission hasPermission = method.getAnnotation(HasPermission.class);
        assertEquals(path, postMapping.value()[0]);
        assertEquals(permission, hasPermission.value()[0]);
    }

    private static Class<?> requestType(String methodName) {
        return switch (methodName) {
            case "create" -> QualityReportRequests.GenerateRequest.class;
            case "page" -> QualityReportRequests.PageRequestBody.class;
            case "get" -> QualityReportRequests.DetailRequest.class;
            case "latest" -> QualityReportRequests.LatestRequest.class;
            case "extractLowQualityCategory" -> QualityReportRequests.ReextractRequest.class;
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
