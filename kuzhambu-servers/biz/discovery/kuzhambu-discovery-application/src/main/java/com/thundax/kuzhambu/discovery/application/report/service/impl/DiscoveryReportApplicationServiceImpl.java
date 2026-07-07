package com.thundax.kuzhambu.discovery.application.report.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult;
import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult.QaTrendPointResult;
import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult.SearchTrendPointResult;
import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult.TopQueryResult;
import com.thundax.kuzhambu.discovery.application.report.service.DiscoveryReportApplicationService;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchLog;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchLogRepository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class DiscoveryReportApplicationServiceImpl implements DiscoveryReportApplicationService {

    private static final int TOP_QUERY_LIMIT = 10;

    private final SearchLogRepository searchLogRepository;
    private final QaSessionRepository qaSessionRepository;

    public DiscoveryReportApplicationServiceImpl(
            SearchLogRepository searchLogRepository, QaSessionRepository qaSessionRepository) {
        this.searchLogRepository = searchLogRepository;
        this.qaSessionRepository = qaSessionRepository;
    }

    @Override
    public DiscoveryReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType) {
        List<SearchLog> searchLogs = searchLogRepository.listByCreatedAtRange(periodStart, periodEnd);
        List<QaSession> qaSessions = qaSessionRepository.listByOpenedAtRange(periodStart, periodEnd);
        return new DiscoveryReportSummaryResult(
                periodStart,
                periodEnd,
                (long) searchLogs.size(),
                (long) qaSessions.size(),
                averageSearchLatencyMs(searchLogs),
                buildTopQueries(searchLogs),
                buildSearchTrendSeries(periodStart, periodEnd, bucketType, searchLogs),
                buildQaTrendSeries(periodStart, periodEnd, bucketType, qaSessions));
    }

    private Long averageSearchLatencyMs(List<SearchLog> searchLogs) {
        return Math.round(searchLogs.stream()
                .map(SearchLog::getSearchLatencyMs)
                .filter(latencyMs -> latencyMs != null)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0D));
    }

    private List<TopQueryResult> buildTopQueries(List<SearchLog> searchLogs) {
        return searchLogs.stream()
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
            Date periodStart, Date periodEnd, String bucketType, List<SearchLog> searchLogs) {
        Map<String, Long> bucketCounts = new LinkedHashMap<>();
        for (SearchLog searchLog : searchLogs) {
            Date createdAt = searchLog == null ? null : searchLog.getCreatedAt();
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
            Date periodStart, Date periodEnd, String bucketType, List<QaSession> qaSessions) {
        Map<String, Long> bucketCounts = new LinkedHashMap<>();
        for (QaSession qaSession : qaSessions) {
            Date openedAt = qaSession == null ? null : qaSession.getOpenedAt();
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

    private boolean outOfRange(Date value, Date periodStart, Date periodEnd) {
        return (periodStart != null && value.before(periodStart)) || (periodEnd != null && value.after(periodEnd));
    }

    private String resolveQueryText(SearchLog searchLog) {
        if (searchLog == null) {
            return null;
        }
        if (StringUtils.isNotBlank(searchLog.getDisplayQueryText())) {
            return searchLog.getDisplayQueryText();
        }
        if (StringUtils.isNotBlank(searchLog.getNormalizedQueryText())) {
            return searchLog.getNormalizedQueryText();
        }
        return searchLog.getQueryText();
    }

    private String toBucket(Date value, String bucketType) {
        SimpleDateFormat formatter =
                new SimpleDateFormat(StringUtils.equalsIgnoreCase(bucketType, "WEEK") ? "yyyy-'W'ww" : "yyyy-MM-dd");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return formatter.format(value);
    }
}
