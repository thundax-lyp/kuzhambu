package com.thundax.kuzhambu.classics.application.report.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.report.result.ClassicsReportSummaryResult;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsSharePortalListItem;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.repository.ClassicsSharingRepository;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsReportApplicationServiceImplTest {

    @Test
    void summaryShouldAggregatePublicContentAndDailyGrowth() {
        SancaiRepository sancaiRepository = mock(SancaiRepository.class);
        WangqiDocumentRepository wangqiDocumentRepository = mock(WangqiDocumentRepository.class);
        MingCustomsRepository mingCustomsRepository = mock(MingCustomsRepository.class);
        ClassicsSharingRepository classicsSharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsReportApplicationServiceImpl service = new ClassicsReportApplicationServiceImpl(
                sancaiRepository, wangqiDocumentRepository, mingCustomsRepository, classicsSharingRepository);

        when(sancaiRepository.listEntries(
                        null,
                        null,
                        null,
                        null,
                        SancaiEntryVisibility.PUBLIC.value(),
                        null,
                        null,
                        null,
                        null,
                        SortDirection.ASC))
                .thenReturn(List.of(
                        sancaiEntry(
                                "青花龙纹",
                                SancaiEntryTranslationStatus.READY,
                                SancaiEntryImageStatus.READY,
                                SancaiEntryVisualAssetStatus.READY,
                                instant(1_718_000_000_000L)),
                        sancaiEntry(
                                "孔雀蓝釉",
                                SancaiEntryTranslationStatus.MISSING,
                                SancaiEntryImageStatus.MISSING,
                                SancaiEntryVisualAssetStatus.MISSING,
                                instant(1_718_086_400_000L))));
        when(wangqiDocumentRepository.listTimeline(null, WangqiDocumentVisibility.PUBLIC.value(), SortDirection.ASC))
                .thenReturn(List.of(wangqiDocument("王圻图谱", instant(1_718_086_400_000L))));
        when(mingCustomsRepository.list(
                        null, null, null, null, null, MingCustomsVisibility.PUBLIC.value(), SortDirection.ASC))
                .thenReturn(List.of(mingCustomsEntry("明礼汇编", instant(1_718_172_800_000L))));

        PageResult<ClassicsShareLink> sharePage = new PageResult<>();
        sharePage.setRecords(List.of(shareLink(10L), shareLink(20L)));
        when(classicsSharingRepository.pageLinks(null, ClassicsShareVisibility.PUBLIC.value(), 1, 10_000))
                .thenReturn(sharePage);
        when(classicsSharingRepository.listTopPortalShares(eq(ClassicsShareVisibility.PUBLIC.value()), eq(10)))
                .thenReturn(List.of(topShare(1001L, "SANCAI_ENTRY", "青花龙纹", 21L)));

        ClassicsReportSummaryResult result = service.summary(date(1_718_000_000_000L), date(1_718_259_200_000L), "DAY");

        assertEquals(4L, result.getContentCount());
        assertEquals(1L, result.getTranslatedContentCount());
        assertEquals(1L, result.getImageReadyContentCount());
        assertEquals(1L, result.getVisualAssetReadyContentCount());
        assertEquals(30L, result.getShareVisitCount());
        assertEquals("青花龙纹", result.getTopContents().get(0).getTitle());
        assertEquals(21L, result.getTopContents().get(0).getVisitCount());
        assertEquals("2024-06-10", result.getContentGrowthSeries().get(0).getBucket());
        assertEquals(1L, result.getContentGrowthSeries().get(0).getCreatedCount());
        assertEquals("2024-06-11", result.getContentGrowthSeries().get(1).getBucket());
        assertEquals(2L, result.getContentGrowthSeries().get(1).getCreatedCount());
    }

    private static SancaiEntry sancaiEntry(
            String title,
            SancaiEntryTranslationStatus translationStatus,
            SancaiEntryImageStatus imageStatus,
            SancaiEntryVisualAssetStatus visualAssetStatus,
            Instant contentUpdatedAt) {
        SancaiEntry entry = new SancaiEntry();
        entry.setTitle(title);
        entry.setLifecycleStatus(SancaiEntryLifecycleStatus.PUBLISHED);
        entry.setVisibility(SancaiEntryVisibility.PUBLIC);
        entry.setTranslationStatus(translationStatus);
        entry.setImageStatus(imageStatus);
        entry.setVisualAssetStatus(visualAssetStatus);
        entry.setRefinementStatus(SancaiEntryRefinementStatus.RAW);
        entry.setContentUpdatedAt(contentUpdatedAt);
        return entry;
    }

    private static WangqiDocument wangqiDocument(String title, Instant contentUpdatedAt) {
        WangqiDocument document = new WangqiDocument();
        document.setTitle(title);
        document.setContentFormat(WangqiContentFormat.MARKDOWN);
        document.setVisibility(WangqiDocumentVisibility.PUBLIC);
        document.setContentUpdatedAt(contentUpdatedAt);
        return document;
    }

    private static MingCustomsEntry mingCustomsEntry(String title, Instant contentUpdatedAt) {
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setTitle(title);
        entry.setVisibility(MingCustomsVisibility.PUBLIC);
        entry.setContentUpdatedAt(contentUpdatedAt);
        return entry;
    }

    private static ClassicsShareLink shareLink(long accessCount) {
        ClassicsShareLink link = new ClassicsShareLink();
        link.setAccessCount(accessCount);
        return link;
    }

    private static ClassicsSharePortalListItem topShare(
            long contentId, String contentType, String titleSnapshot, long accessCount) {
        ClassicsSharePortalListItem item = new ClassicsSharePortalListItem();
        item.setContentId(ClassicsContentIdCodec.toDomain(contentId));
        item.setContentType(ClassicsContentType.valueOf(contentType));
        item.setTitleSnapshot(titleSnapshot);
        item.setAccessCount(accessCount);
        return item;
    }

    private static Date date(long epochMillis) {
        return new Date(epochMillis);
    }

    private static Instant instant(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis);
    }
}
