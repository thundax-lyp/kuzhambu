package com.thundax.kuzhambu.classics.application.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.facade.assembler.ClassicsFacadeAssembler;
import com.thundax.kuzhambu.classics.application.report.result.ClassicsReportSummaryResult;
import com.thundax.kuzhambu.classics.application.report.service.ClassicsReportApplicationService;
import com.thundax.kuzhambu.classics.application.search.result.ClassicsSearchSourceContent;
import com.thundax.kuzhambu.classics.application.search.service.ClassicsSearchContentApplicationService;
import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsFacadeImplTest {

    @Test
    void summaryShouldDelegateAndMapFacadeResponse() {
        ClassicsReportApplicationService classicsReportApplicationService =
                mock(ClassicsReportApplicationService.class);
        Date periodStart = new Date(1_735_689_600_000L);
        Date periodEnd = new Date(1_735_776_000_000L);
        when(classicsReportApplicationService.summary(periodStart, periodEnd, "WEEK"))
                .thenReturn(new ClassicsReportSummaryResult(
                        periodStart,
                        periodEnd,
                        8L,
                        5L,
                        4L,
                        3L,
                        21L,
                        List.of(new ClassicsReportSummaryResult.TopContentResult(1001L, "SANCAI_ENTRY", "青花龙纹", 12L)),
                        List.of(new ClassicsReportSummaryResult.ContentGrowthPointResult("2025-W01", 2L))));
        ClassicsFacadeImpl facade =
                newFacade(classicsReportApplicationService, mock(ClassicsSearchContentApplicationService.class));

        var response = facade.summary(ClassicsSummaryFacadeRequest.builder()
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .bucketType("WEEK")
                .build());

        assertEquals(periodStart, response.getPeriodStart());
        assertEquals(periodEnd, response.getPeriodEnd());
        assertEquals(8L, response.getContentCount());
        assertEquals(5L, response.getTranslatedContentCount());
        assertEquals(4L, response.getImageReadyContentCount());
        assertEquals(3L, response.getVisualAssetReadyContentCount());
        assertEquals(21L, response.getShareVisitCount());
        assertEquals("青花龙纹", response.getTopContents().get(0).getTitle());
        assertEquals("2025-W01", response.getContentGrowthSeries().get(0).getBucket());
    }

    @Test
    void publicContentReadMethodsShouldDelegateAndMapFacadeResponses() {
        ClassicsSearchContentApplicationService classicsSearchContentApplicationService =
                mock(ClassicsSearchContentApplicationService.class);
        ClassicsSearchSourceContent sourceContent = new ClassicsSearchSourceContent(
                "SANCAI_ENTRY",
                "1001",
                "SANCAI",
                "TIANWEN",
                "天文",
                "青花龙纹",
                "摘要",
                List.of("原文", "译文"),
                List.of("礼制", "器物"),
                "PUBLISHED",
                "PUBLIC",
                7,
                new Date(1_735_689_600_000L),
                new Date(1_735_776_000_000L));
        when(classicsSearchContentApplicationService.listPublicContents()).thenReturn(List.of(sourceContent));
        when(classicsSearchContentApplicationService.getPublicContent("SANCAI_ENTRY", "1001"))
                .thenReturn(sourceContent);
        ClassicsFacadeImpl facade =
                newFacade(mock(ClassicsReportApplicationService.class), classicsSearchContentApplicationService);
        ClassicsPublicContentFacadeRequest request = ClassicsPublicContentFacadeRequest.builder()
                .contentType("SANCAI_ENTRY")
                .contentId("1001")
                .build();

        var listResponse = facade.listPublicContents();
        var getResponse = facade.getPublicContent(request);

        assertEquals(1, listResponse.getContents().size());
        assertEquals("SANCAI_ENTRY", listResponse.getContents().get(0).getContentType());
        assertEquals(7, listResponse.getContents().get(0).getCurrentVersionNo());
        assertEquals("青花龙纹", getResponse.getContent().getTitle());
        assertEquals(List.of("礼制", "器物"), getResponse.getContent().getTagNames());
        verify(classicsSearchContentApplicationService).listPublicContents();
        verify(classicsSearchContentApplicationService).getPublicContent("SANCAI_ENTRY", "1001");
    }

    @Test
    void nullRequestsShouldKeepFacadeBoundaryStable() {
        ClassicsReportApplicationService classicsReportApplicationService =
                mock(ClassicsReportApplicationService.class);
        ClassicsSearchContentApplicationService classicsSearchContentApplicationService =
                mock(ClassicsSearchContentApplicationService.class);
        ClassicsFacadeImpl facade =
                newFacade(classicsReportApplicationService, classicsSearchContentApplicationService);

        assertNull(facade.summary(null));
        assertNull(facade.getPublicContent(null));

        verifyNoInteractions(classicsReportApplicationService, classicsSearchContentApplicationService);
    }

    private ClassicsFacadeImpl newFacade(
            ClassicsReportApplicationService classicsReportApplicationService,
            ClassicsSearchContentApplicationService classicsSearchContentApplicationService) {
        return new ClassicsFacadeImpl(
                classicsReportApplicationService,
                classicsSearchContentApplicationService,
                new ClassicsFacadeAssembler());
    }
}
