package com.thundax.kuzhambu.classics.application.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.cleanup.service.ClassicsCleanupApplicationService;
import com.thundax.kuzhambu.classics.application.content.query.ContentObjectQuery;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.facade.assembler.ClassicsFacadeAssembler;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.application.report.result.ClassicsReportSummaryResult;
import com.thundax.kuzhambu.classics.application.report.service.ClassicsReportApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.application.search.result.ClassicsSearchSourceContent;
import com.thundax.kuzhambu.classics.application.search.service.ClassicsSearchContentApplicationService;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.facade.request.ClassicsCleanupTargetsFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsFacadeImplTest {

    @Test
    void summaryShouldDelegateAndMapFacadeResponse() {
        ClassicsReportApplicationService classicsReportApplicationService =
                mock(ClassicsReportApplicationService.class);
        Instant periodStart = Instant.ofEpochMilli(1_735_689_600_000L);
        Instant periodEnd = Instant.ofEpochMilli(1_735_776_000_000L);
        when(classicsReportApplicationService.summary(argThat(query -> periodStart.equals(query.periodStart())
                        && periodEnd.equals(query.periodEnd())
                        && "WEEK".equals(query.bucketType()))))
                .thenReturn(new ClassicsReportSummaryResult(
                        periodStart,
                        periodEnd,
                        8L,
                        5L,
                        4L,
                        3L,
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
                Instant.ofEpochMilli(1_735_689_600_000L),
                Instant.ofEpochMilli(1_735_776_000_000L));
        when(classicsSearchContentApplicationService.listPublicContents()).thenReturn(List.of(sourceContent));
        when(classicsSearchContentApplicationService.getPublicContent(argThat(
                        query -> "SANCAI_ENTRY".equals(query.contentType()) && "1001".equals(query.contentId()))))
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
        verify(classicsSearchContentApplicationService)
                .getPublicContent(argThat(
                        query -> "SANCAI_ENTRY".equals(query.contentType()) && "1001".equals(query.contentId())));
    }

    @Test
    void getQaKnowledgeShouldMapSancaiContent() {
        ClassicsContentApplicationService classicsContentApplicationService =
                mock(ClassicsContentApplicationService.class);
        ClassicsSearchContentApplicationService classicsSearchContentApplicationService =
                mock(ClassicsSearchContentApplicationService.class);
        SancaiApplicationService sancaiApplicationService = mock(SancaiApplicationService.class);
        ClassicsFacadeImpl facade = newFacade(
                mock(ClassicsReportApplicationService.class),
                classicsSearchContentApplicationService,
                classicsContentApplicationService,
                sancaiApplicationService,
                mock(WangqiDocumentApplicationService.class),
                mock(MingCustomsApplicationService.class));
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
                Instant.ofEpochMilli(1_735_689_600_000L),
                Instant.ofEpochMilli(1_735_776_000_000L));
        SancaiEntry entry = new SancaiEntry();
        entry.setId(com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec.toDomain(1001L));
        entry.setOriginalText("原文");
        entry.setTranslationText("译文");
        when(classicsSearchContentApplicationService.getPublicContent(argThat(
                        query -> "SANCAI_ENTRY".equals(query.contentType()) && "1001".equals(query.contentId()))))
                .thenReturn(sourceContent);
        when(sancaiApplicationService.getEntry(
                        com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec.toDomain(1001L)))
                .thenReturn(entry);
        ContentObjectQuery contentQuery =
                new ContentObjectQuery("SANCAI_ENTRY", ClassicsContentIdCodec.toDomain(1001L));
        when(classicsContentApplicationService.listTags(contentQuery))
                .thenReturn(List.of(confirmedTag("礼制"), removedTag("失效"), confirmedTag("仪制")));
        when(classicsContentApplicationService.listQaPairs(contentQuery))
                .thenReturn(List.of(new ClassicsContentQaPair(
                        null,
                        ClassicsContentType.SANCAI_ENTRY,
                        ClassicsContentIdCodec.toDomain(1001L),
                        "Q1",
                        "A1",
                        ClassicsContentSource.MANUAL,
                        1)));

        var response = facade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
                .contentType("SANCAI_ENTRY")
                .contentId("1001")
                .build());
        assertEquals("/classics/sancai/1001", response.getKnowledge().getSourcePath());
        assertEquals("1001", response.getKnowledge().getContentId());
        assertEquals("青花龙纹", response.getKnowledge().getTitle());
        assertEquals("原文", response.getKnowledge().getOriginalText());
        assertEquals("译文", response.getKnowledge().getTranslationText());
        assertEquals(List.of("礼制", "仪制"), response.getKnowledge().getTags());
        assertEquals(1, response.getKnowledge().getQaPairs().size());
        assertEquals("Q1", response.getKnowledge().getQaPairs().get(0).getQuestion());
        verify(classicsContentApplicationService).listTags(contentQuery);
        verify(classicsContentApplicationService).listQaPairs(contentQuery);
    }

    @Test
    void getQaKnowledgeShouldMapWangqiContent() {
        ClassicsContentApplicationService classicsContentApplicationService =
                mock(ClassicsContentApplicationService.class);
        ClassicsSearchContentApplicationService classicsSearchContentApplicationService =
                mock(ClassicsSearchContentApplicationService.class);
        WangqiDocumentApplicationService wangqiDocumentApplicationService =
                mock(WangqiDocumentApplicationService.class);
        ClassicsFacadeImpl facade = newFacade(
                mock(ClassicsReportApplicationService.class),
                classicsSearchContentApplicationService,
                classicsContentApplicationService,
                mock(SancaiApplicationService.class),
                wangqiDocumentApplicationService,
                mock(MingCustomsApplicationService.class));
        ClassicsSearchSourceContent sourceContent = new ClassicsSearchSourceContent(
                "WANGQI_DOCUMENT",
                "2001",
                "WANGQI",
                "WANGQI",
                "碑刻",
                "五经",
                "摘要",
                List.of("段落"),
                List.of("碑刻"),
                "PUBLISHED",
                "PUBLIC",
                9,
                Instant.ofEpochMilli(1_735_689_600_000L),
                Instant.ofEpochMilli(1_735_776_000_000L));
        WangqiDocument document = new WangqiDocument();
        document.setId(com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec.toDomain(2001L));
        document.setContent("内容正文");
        when(classicsSearchContentApplicationService.getPublicContent(argThat(
                        query -> "WANGQI_DOCUMENT".equals(query.contentType()) && "2001".equals(query.contentId()))))
                .thenReturn(sourceContent);
        when(wangqiDocumentApplicationService.get(
                        com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec.toDomain(2001L)))
                .thenReturn(document);
        ContentObjectQuery contentQuery =
                new ContentObjectQuery("WANGQI_DOCUMENT", ClassicsContentIdCodec.toDomain(2001L));
        when(classicsContentApplicationService.listTags(contentQuery)).thenReturn(List.of(confirmedTag("碑文")));
        when(classicsContentApplicationService.listQaPairs(contentQuery))
                .thenReturn(List.of(new ClassicsContentQaPair(
                        null,
                        ClassicsContentType.WANGQI_DOCUMENT,
                        ClassicsContentIdCodec.toDomain(2001L),
                        "Q2",
                        "A2",
                        ClassicsContentSource.AI,
                        1)));

        var response = facade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
                .contentType("WANGQI_DOCUMENT")
                .contentId("2001")
                .build());
        assertEquals("/classics/wangqi/2001", response.getKnowledge().getSourcePath());
        assertEquals("内容正文", response.getKnowledge().getBody());
        assertNull(response.getKnowledge().getOriginalText());
        assertNull(response.getKnowledge().getTranslationText());
        assertEquals(List.of("碑文"), response.getKnowledge().getTags());
        assertEquals("A2", response.getKnowledge().getQaPairs().get(0).getAnswer());
        verify(classicsContentApplicationService).listTags(contentQuery);
        verify(classicsContentApplicationService).listQaPairs(contentQuery);
    }

    @Test
    void getQaKnowledgeShouldMapMingCustomsContent() {
        ClassicsContentApplicationService classicsContentApplicationService =
                mock(ClassicsContentApplicationService.class);
        ClassicsSearchContentApplicationService classicsSearchContentApplicationService =
                mock(ClassicsSearchContentApplicationService.class);
        MingCustomsApplicationService mingCustomsApplicationService = mock(MingCustomsApplicationService.class);
        ClassicsFacadeImpl facade = newFacade(
                mock(ClassicsReportApplicationService.class),
                classicsSearchContentApplicationService,
                classicsContentApplicationService,
                mock(SancaiApplicationService.class),
                mock(WangqiDocumentApplicationService.class),
                mingCustomsApplicationService);
        ClassicsSearchSourceContent sourceContent = new ClassicsSearchSourceContent(
                "MING_CUSTOMS",
                "3001",
                "MING",
                "CUSTOMS",
                "礼制",
                "明史",
                "摘要",
                List.of("节录"),
                List.of("制度"),
                "PUBLISHED",
                "PUBLIC",
                10,
                Instant.ofEpochMilli(1_735_689_600_000L),
                Instant.ofEpochMilli(1_735_776_000_000L));
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec.toDomain(3001L));
        entry.setContent("正文");
        entry.setOriginalExcerpts("原典");
        when(classicsSearchContentApplicationService.getPublicContent(argThat(
                        query -> "MING_CUSTOMS".equals(query.contentType()) && "3001".equals(query.contentId()))))
                .thenReturn(sourceContent);
        when(mingCustomsApplicationService.get(
                        com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec.toDomain(3001L)))
                .thenReturn(entry);
        ContentObjectQuery contentQuery =
                new ContentObjectQuery("MING_CUSTOMS", ClassicsContentIdCodec.toDomain(3001L));
        when(classicsContentApplicationService.listTags(contentQuery)).thenReturn(List.of(confirmedTag("礼节")));
        when(classicsContentApplicationService.listQaPairs(contentQuery))
                .thenReturn(List.of(new ClassicsContentQaPair(
                        null,
                        ClassicsContentType.MING_CUSTOMS,
                        ClassicsContentIdCodec.toDomain(3001L),
                        "Q3",
                        "A3",
                        ClassicsContentSource.AI,
                        1)));

        var response = facade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
                .contentType("MING_CUSTOMS")
                .contentId("3001")
                .build());
        assertEquals("/classics/ming-customs/3001", response.getKnowledge().getSourcePath());
        assertEquals("正文", response.getKnowledge().getBody());
        assertEquals("原典", response.getKnowledge().getOriginalExcerpts());
        assertEquals(List.of("礼节"), response.getKnowledge().getTags());
        assertEquals("Q3", response.getKnowledge().getQaPairs().get(0).getQuestion());
        verify(classicsContentApplicationService).listTags(contentQuery);
        verify(classicsContentApplicationService).listQaPairs(contentQuery);
    }

    @Test
    void nullRequestsShouldKeepFacadeBoundaryStable() {
        ClassicsReportApplicationService classicsReportApplicationService =
                mock(ClassicsReportApplicationService.class);
        ClassicsContentApplicationService classicsContentApplicationService =
                mock(ClassicsContentApplicationService.class);
        ClassicsSearchContentApplicationService classicsSearchContentApplicationService =
                mock(ClassicsSearchContentApplicationService.class);
        ClassicsFacadeImpl facade = newFacade(
                classicsReportApplicationService,
                classicsSearchContentApplicationService,
                classicsContentApplicationService,
                mock(SancaiApplicationService.class),
                mock(WangqiDocumentApplicationService.class),
                mock(MingCustomsApplicationService.class));

        assertNull(facade.summary(null));
        assertNull(facade.getPublicContent(null));
        assertNull(facade.getQaKnowledge(null));
        assertNull(facade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
                .contentType("")
                .contentId("1001")
                .build()));
        assertNull(facade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
                .contentType("SANCAI_ENTRY")
                .contentId("")
                .build()));

        verifyNoInteractions(
                classicsReportApplicationService,
                classicsContentApplicationService,
                classicsSearchContentApplicationService);
    }

    @Test
    void cleanupTargetsShouldForwardPolicyParametersAndExposeTargetIds() {
        ClassicsCleanupApplicationService cleanupApplicationService = mock(ClassicsCleanupApplicationService.class);
        Instant requestedAt = Instant.ofEpochMilli(1_735_689_600_000L);
        when(cleanupApplicationService.listTargets(argThat(query -> "EXPIRED_SHARE".equals(query.cleanupType())
                        && requestedAt.equals(query.requestedAt())
                        && Integer.valueOf(90).equals(query.retentionDays())
                        && Integer.valueOf(25).equals(query.maxTargets()))))
                .thenReturn(List.of(
                        ClassicsCleanupApplicationService.CleanupTarget.builder()
                                .targetType("share")
                                .targetId(11L)
                                .build(),
                        ClassicsCleanupApplicationService.CleanupTarget.builder()
                                .targetType("share")
                                .targetId(12L)
                                .build()));
        ClassicsFacadeImpl facade = newFacade(
                cleanupApplicationService,
                mock(ClassicsReportApplicationService.class),
                mock(ClassicsSearchContentApplicationService.class));

        var response = facade.listCleanupTargets(ClassicsCleanupTargetsFacadeRequest.builder()
                .cleanupType("expired_share")
                .requestedAt(requestedAt)
                .retentionDays(90)
                .limit(25)
                .build());

        assertEquals("EXPIRED_SHARE", response.getCleanupType());
        assertEquals(true, response.isSupported());
        assertEquals(2, response.getTargets().size());
        assertEquals("share", response.getTargets().get(0).getTargetType());
        assertEquals(11L, response.getTargets().get(0).getTargetId());
    }

    @Test
    void cleanupExecutionShouldReturnFailureForUnsupportedTypeWithoutThrowing() {
        ClassicsFacadeImpl facade = newFacade(
                mock(ClassicsReportApplicationService.class), mock(ClassicsSearchContentApplicationService.class));

        var response = facade.executeCleanupTargets(ClassicsCleanupTargetsFacadeRequest.builder()
                .cleanupType("UNKNOWN")
                .targetIds(List.of(1L))
                .build());

        assertEquals("UNKNOWN", response.getCleanupType());
        assertEquals(false, response.isSupported());
        assertEquals("UNSUPPORTED_CLEANUP_TYPE", response.getFailureReason());
        assertEquals(0, response.getItemResults().size());
    }

    private ClassicsContentTag confirmedTag(String tagName) {
        ClassicsContentTag tag = new ClassicsContentTag();
        tag.setTagNameSnapshot(tagName);
        tag.setStatus(ClassicsContentTagStatus.ACTIVE);
        return tag;
    }

    private ClassicsContentTag removedTag(String tagName) {
        ClassicsContentTag tag = new ClassicsContentTag();
        tag.setTagNameSnapshot(tagName);
        tag.setStatus(ClassicsContentTagStatus.REMOVED);
        return tag;
    }

    private ClassicsFacadeImpl newFacade(
            ClassicsReportApplicationService classicsReportApplicationService,
            ClassicsSearchContentApplicationService classicsSearchContentApplicationService,
            ClassicsContentApplicationService classicsContentApplicationService,
            SancaiApplicationService sancaiApplicationService,
            WangqiDocumentApplicationService wangqiDocumentApplicationService,
            MingCustomsApplicationService mingCustomsApplicationService) {
        return newFacade(
                mock(ClassicsCleanupApplicationService.class),
                classicsReportApplicationService,
                classicsSearchContentApplicationService,
                classicsContentApplicationService,
                sancaiApplicationService,
                wangqiDocumentApplicationService,
                mingCustomsApplicationService);
    }

    private ClassicsFacadeImpl newFacade(
            ClassicsCleanupApplicationService classicsCleanupApplicationService,
            ClassicsReportApplicationService classicsReportApplicationService,
            ClassicsSearchContentApplicationService classicsSearchContentApplicationService,
            ClassicsContentApplicationService classicsContentApplicationService,
            SancaiApplicationService sancaiApplicationService,
            WangqiDocumentApplicationService wangqiDocumentApplicationService,
            MingCustomsApplicationService mingCustomsApplicationService) {
        return new ClassicsFacadeImpl(
                classicsCleanupApplicationService,
                classicsContentApplicationService,
                classicsReportApplicationService,
                classicsSearchContentApplicationService,
                sancaiApplicationService,
                wangqiDocumentApplicationService,
                mingCustomsApplicationService,
                new ClassicsFacadeAssembler());
    }

    private ClassicsFacadeImpl newFacade(
            ClassicsCleanupApplicationService classicsCleanupApplicationService,
            ClassicsReportApplicationService classicsReportApplicationService,
            ClassicsSearchContentApplicationService classicsSearchContentApplicationService) {
        return newFacade(
                classicsCleanupApplicationService,
                classicsReportApplicationService,
                classicsSearchContentApplicationService,
                mock(ClassicsContentApplicationService.class),
                mock(SancaiApplicationService.class),
                mock(WangqiDocumentApplicationService.class),
                mock(MingCustomsApplicationService.class));
    }

    private ClassicsFacadeImpl newFacade(
            ClassicsReportApplicationService classicsReportApplicationService,
            ClassicsSearchContentApplicationService classicsSearchContentApplicationService) {
        return newFacade(
                classicsReportApplicationService,
                classicsSearchContentApplicationService,
                mock(ClassicsContentApplicationService.class),
                mock(SancaiApplicationService.class),
                mock(WangqiDocumentApplicationService.class),
                mock(MingCustomsApplicationService.class));
    }
}
