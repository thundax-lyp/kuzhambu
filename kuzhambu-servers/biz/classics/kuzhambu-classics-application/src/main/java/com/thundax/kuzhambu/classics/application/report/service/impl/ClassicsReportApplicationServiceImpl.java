package com.thundax.kuzhambu.classics.application.report.service.impl;

import com.thundax.kuzhambu.classics.application.report.result.ClassicsReportSummaryResult;
import com.thundax.kuzhambu.classics.application.report.result.ClassicsReportSummaryResult.ContentGrowthPointResult;
import com.thundax.kuzhambu.classics.application.report.service.ClassicsReportApplicationService;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class ClassicsReportApplicationServiceImpl implements ClassicsReportApplicationService {

    private static final ZoneId REPORT_ZONE = ZoneId.of("Asia/Shanghai");

    private final SancaiRepository sancaiRepository;
    private final WangqiDocumentRepository wangqiDocumentRepository;
    private final MingCustomsRepository mingCustomsRepository;

    public ClassicsReportApplicationServiceImpl(
            SancaiRepository sancaiRepository,
            WangqiDocumentRepository wangqiDocumentRepository,
            MingCustomsRepository mingCustomsRepository) {
        this.sancaiRepository = sancaiRepository;
        this.wangqiDocumentRepository = wangqiDocumentRepository;
        this.mingCustomsRepository = mingCustomsRepository;
    }

    @Override
    public ClassicsReportSummaryResult summary(Instant periodStart, Instant periodEnd, String bucketType) {
        List<SancaiEntry> publicSancaiEntries = sancaiRepository.listEntries(
                null,
                null,
                null,
                SancaiEntryLifecycleStatus.PUBLISHED.value(),
                null,
                null,
                null,
                null,
                SortDirection.ASC);
        List<WangqiDocument> publicWangqiDocuments = wangqiDocumentRepository.listTimeline(null, SortDirection.ASC);
        List<MingCustomsEntry> publicMingCustomsEntries = mingCustomsRepository.list(
                null, null, null, null, null, MingCustomsVisibility.PUBLIC.value(), SortDirection.ASC);

        long translatedContentCount = publicSancaiEntries.stream()
                .filter(entry -> entry != null && entry.getTranslationStatus() == SancaiEntryTranslationStatus.READY)
                .count();
        long imageReadyContentCount = publicSancaiEntries.stream()
                .filter(entry -> entry != null && entry.getImageStatus() == SancaiEntryImageStatus.READY)
                .count();
        long visualAssetReadyContentCount = publicSancaiEntries.stream()
                .filter(entry -> entry != null && entry.getVisualAssetStatus() == SancaiEntryVisualAssetStatus.READY)
                .count();
        List<Instant> growthDates = new ArrayList<>();
        publicSancaiEntries.stream().map(SancaiEntry::getContentUpdatedAt).forEach(growthDates::add);
        publicWangqiDocuments.stream().map(WangqiDocument::getContentUpdatedAt).forEach(growthDates::add);
        publicMingCustomsEntries.stream()
                .map(MingCustomsEntry::getContentUpdatedAt)
                .forEach(growthDates::add);

        return new ClassicsReportSummaryResult(
                periodStart,
                periodEnd,
                (long) (publicSancaiEntries.size() + publicWangqiDocuments.size() + publicMingCustomsEntries.size()),
                translatedContentCount,
                imageReadyContentCount,
                visualAssetReadyContentCount,
                List.of(),
                buildGrowthSeries(periodStart, periodEnd, bucketType, growthDates));
    }

    private List<ContentGrowthPointResult> buildGrowthSeries(
            Instant periodStart, Instant periodEnd, String bucketType, List<Instant> contentUpdatedDates) {
        if (periodStart == null || periodEnd == null || StringUtils.isBlank(bucketType)) {
            return List.of();
        }
        Map<String, Long> bucketCounts = new LinkedHashMap<>();
        for (Instant contentUpdatedAt : contentUpdatedDates) {
            if (contentUpdatedAt == null
                    || contentUpdatedAt.isBefore(periodStart)
                    || contentUpdatedAt.isAfter(periodEnd)) {
                continue;
            }
            String bucket = toBucket(contentUpdatedAt, bucketType);
            bucketCounts.put(bucket, bucketCounts.getOrDefault(bucket, 0L) + 1L);
        }
        List<ContentGrowthPointResult> points = new ArrayList<>();
        for (Map.Entry<String, Long> entry : bucketCounts.entrySet()) {
            points.add(new ContentGrowthPointResult(entry.getKey(), entry.getValue()));
        }
        return points;
    }

    private String toBucket(Instant value, String bucketType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                        StringUtils.equalsIgnoreCase(bucketType, "WEEK") ? "yyyy-'W'ww" : "yyyy-MM-dd")
                .withZone(REPORT_ZONE);
        return formatter.format(value);
    }
}
