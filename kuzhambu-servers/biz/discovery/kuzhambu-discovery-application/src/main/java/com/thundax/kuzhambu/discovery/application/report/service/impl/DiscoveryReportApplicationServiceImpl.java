package com.thundax.kuzhambu.discovery.application.report.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult;
import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult.QaTrendPointResult;
import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult.SearchTrendPointResult;
import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult.TopQueryResult;
import com.thundax.kuzhambu.discovery.application.report.service.DiscoveryReportApplicationService;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchEvent;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchEventRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class DiscoveryReportApplicationServiceImpl implements DiscoveryReportApplicationService {

    private static final int TOP_QUERY_LIMIT = 10;
    private static final ZoneId REPORT_ZONE = ZoneId.of("Asia/Shanghai");

    private final SearchEventRepository searchEventRepository;
    private final QaSessionRepository qaSessionRepository;

    public DiscoveryReportApplicationServiceImpl(
            SearchEventRepository searchEventRepository, QaSessionRepository qaSessionRepository) {
        this.searchEventRepository = searchEventRepository;
        this.qaSessionRepository = qaSessionRepository;
    }

    @Override
    public DiscoveryReportSummaryResult summary(Instant periodStart, Instant periodEnd, String bucketType) {
        List<SearchEvent> searchEvents = searchEventRepository.listByCreatedAtRange(periodStart, periodEnd);
        List<QaSession> qaSessions = qaSessionRepository.listByOpenedAtRange(periodStart, periodEnd);
        return new DiscoveryReportSummaryResult(
                periodStart,
                periodEnd,
                (long) searchEvents.size(),
                (long) qaSessions.size(),
                averageSearchLatencyMs(searchEvents),
                buildTopQueries(searchEvents),
                buildSearchTrendSeries(periodStart, periodEnd, bucketType, searchEvents),
                buildQaTrendSeries(periodStart, periodEnd, bucketType, qaSessions));
    }

    private Long averageSearchLatencyMs(List<SearchEvent> searchEvents) {
        return Math.round(searchEvents.stream()
                .map(SearchEvent::getSearchLatencyMs)
                .filter(latencyMs -> latencyMs != null)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0D));
    }

    private List<TopQueryResult> buildTopQueries(List<SearchEvent> searchEvents) {
        return searchEvents.stream()
                .map(this::resolveQueryText)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.groupingBy(queryText -> queryText, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(TOP_QUERY_LIMIT)
                .map(entry -> new TopQueryResult(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<SearchTrendPointResult> buildSearchTrendSeries(
            Instant periodStart, Instant periodEnd, String bucketType, List<SearchEvent> searchEvents) {
        Map<String, Long> bucketCounts = new LinkedHashMap<>();
        for (SearchEvent searchEvent : searchEvents) {
            Instant createdAt = searchEvent == null ? null : searchEvent.getCreatedAt();
            if (createdAt == null || outOfRange(createdAt, periodStart, periodEnd)) {
                continue;
            }
            String bucket = toBucket(createdAt, bucketType);
            bucketCounts.put(bucket, bucketCounts.getOrDefault(bucket, 0L) + 1L);
        }
        List<SearchTrendPointResult> points = new ArrayList<>();
        for (Map.Entry<String, Long> entry : bucketCounts.entrySet()) {
            points.add(new SearchTrendPointResult(entry.getKey(), entry.getValue()));
        }
        return points;
    }

    private List<QaTrendPointResult> buildQaTrendSeries(
            Instant periodStart, Instant periodEnd, String bucketType, List<QaSession> qaSessions) {
        Map<String, Long> bucketCounts = new LinkedHashMap<>();
        for (QaSession qaSession : qaSessions) {
            Instant openedAt = qaSession == null ? null : qaSession.getOpenedAt();
            if (openedAt == null || outOfRange(openedAt, periodStart, periodEnd)) {
                continue;
            }
            String bucket = toBucket(openedAt, bucketType);
            bucketCounts.put(bucket, bucketCounts.getOrDefault(bucket, 0L) + 1L);
        }
        List<QaTrendPointResult> points = new ArrayList<>();
        for (Map.Entry<String, Long> entry : bucketCounts.entrySet()) {
            points.add(new QaTrendPointResult(entry.getKey(), entry.getValue()));
        }
        return points;
    }

    private boolean outOfRange(Instant value, Instant periodStart, Instant periodEnd) {
        return (periodStart != null && value.isBefore(periodStart)) || (periodEnd != null && value.isAfter(periodEnd));
    }

    private String resolveQueryText(SearchEvent searchEvent) {
        if (searchEvent == null) {
            return null;
        }
        if (StringUtils.isNotBlank(searchEvent.getDisplayQueryText())) {
            return searchEvent.getDisplayQueryText();
        }
        if (StringUtils.isNotBlank(searchEvent.getNormalizedQueryText())) {
            return searchEvent.getNormalizedQueryText();
        }
        return searchEvent.getQueryText();
    }

    private String toBucket(Instant value, String bucketType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                        StringUtils.equalsIgnoreCase(bucketType, "WEEK") ? "yyyy-'W'ww" : "yyyy-MM-dd")
                .withZone(REPORT_ZONE);
        return formatter.format(value);
    }
}
