package com.thundax.kuzhambu.classics.application.report.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.report.result.ClassicsReportSummaryResult;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsReportApplicationServiceImplTest {

    @Test
    void summaryShouldAggregatePublicContentAndDailyGrowth() {
        SancaiRepository sancaiRepository = mock(SancaiRepository.class);
        WangqiDocumentRepository wangqiDocumentRepository = mock(WangqiDocumentRepository.class);
        MingCustomsRepository mingCustomsRepository = mock(MingCustomsRepository.class);
        ClassicsReportApplicationServiceImpl service = new ClassicsReportApplicationServiceImpl(
                sancaiRepository, wangqiDocumentRepository, mingCustomsRepository);

        when(sancaiRepository.listEntries(
                        null,
                        null,
                        null,
                        SancaiEntryLifecycleStatus.PUBLISHED.value(),
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
        when(wangqiDocumentRepository.listTimeline(null, SortDirection.ASC))
                .thenReturn(List.of(wangqiDocument("王圻图谱", instant(1_718_086_400_000L))));
        when(mingCustomsRepository.list(
                        null, null, null, null, null, MingCustomsVisibility.PUBLIC.value(), SortDirection.ASC))
                .thenReturn(List.of(mingCustomsEntry("明礼汇编", instant(1_718_172_800_000L))));

        ClassicsReportSummaryResult result = service.summary(date(1_718_000_000_000L), date(1_718_259_200_000L), "DAY");

        assertEquals(4L, result.getContentCount());
        assertEquals(1L, result.getTranslatedContentCount());
        assertEquals(1L, result.getImageReadyContentCount());
        assertEquals(1L, result.getVisualAssetReadyContentCount());
        assertEquals(0, result.getTopContents().size());
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

    private static Instant date(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis);
    }

    private static Instant instant(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis);
    }
}
