package com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.discovery.application.search.query.SearchAnalysisSummaryQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchLogPageQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchAnalysisSummaryResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchLogResult;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchAnalysisSummaryRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchLogPageRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchAnalysisSummaryResponse;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchLogDetailResponse;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchLogResponse;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

public final class DiscoverySearchAdminInterfaceAssembler {

    private DiscoverySearchAdminInterfaceAssembler() {}

    public static SearchLogPageQuery toQuery(DiscoverySearchLogPageRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchLogPageQuery(
                request.getQueryText(),
                request.getIntentTypes(),
                request.getSearchStatuses(),
                request.getOperatorId(),
                parseDate(request.getDateFrom(), "dateFrom"),
                parseDate(request.getDateTo(), "dateTo"),
                request.getPageNo() == null ? 1 : request.getPageNo(),
                request.getPageSize() == null ? 20 : request.getPageSize());
    }

    public static SearchAnalysisSummaryQuery toQuery(DiscoverySearchAnalysisSummaryRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchAnalysisSummaryQuery(
                parseDate(request.getDateFrom(), "dateFrom"), parseDate(request.getDateTo(), "dateTo"));
    }

    public static DiscoverySearchLogResponse toResponse(SearchLogResult result) {
        if (result == null) {
            return null;
        }
        return DiscoverySearchLogResponse.builder()
                .searchLogId(result.getSearchLogId())
                .queryText(result.getQueryText())
                .displayQueryText(result.getDisplayQueryText())
                .intentType(result.getIntentType())
                .resultTotalCount(result.getResultTotalCount())
                .groupTotalCount(result.getGroupTotalCount())
                .searchStatus(result.getSearchStatus())
                .operatorId(result.getOperatorId())
                .createdAt(toDate(result.getCreatedAt()))
                .build();
    }

    public static DiscoverySearchLogDetailResponse toDetailResponse(SearchLogResult result) {
        if (result == null) {
            return null;
        }
        return DiscoverySearchLogDetailResponse.builder()
                .searchLogId(result.getSearchLogId())
                .queryText(result.getQueryText())
                .normalizedQueryText(result.getNormalizedQueryText())
                .displayQueryText(result.getDisplayQueryText())
                .intentType(result.getIntentType())
                .searchScopesJson(result.getSearchScopesJson())
                .resultTotalCount(result.getResultTotalCount())
                .groupTotalCount(result.getGroupTotalCount())
                .searchStatus(result.getSearchStatus())
                .failureCode(result.getFailureCode())
                .failureMessage(result.getFailureMessage())
                .operatorId(result.getOperatorId())
                .requestId(result.getRequestId())
                .traceId(result.getTraceId())
                .createdAt(toDate(result.getCreatedAt()))
                .build();
    }

    public static DiscoverySearchAnalysisSummaryResponse toResponse(SearchAnalysisSummaryResult result) {
        if (result == null) {
            return null;
        }
        return DiscoverySearchAnalysisSummaryResponse.builder()
                .searchCount(result.getSearchCount())
                .failedSearchCount(result.getFailedSearchCount())
                .zeroResultSearchCount(result.getZeroResultSearchCount())
                .clickCount(result.getClickCount())
                .topQueries(toTopQueryResponses(result.getTopQueries()))
                .build();
    }

    public static String firstValue(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    private static Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }

    private static List<DiscoverySearchAnalysisSummaryResponse.TopQueryResponse> toTopQueryResponses(
            List<SearchAnalysisSummaryResult.TopQuery> topQueries) {
        if (topQueries == null) {
            return List.of();
        }
        return topQueries.stream()
                .map(topQuery -> DiscoverySearchAnalysisSummaryResponse.TopQueryResponse.builder()
                        .queryText(topQuery.getQueryText())
                        .count(topQuery.getCount())
                        .build())
                .toList();
    }

    private static Date parseDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Date.from(Instant.parse(value));
        } catch (DateTimeParseException exception) {
            throw new BizException(
                    "DISCOVERY-40002",
                    "discovery.search.admin.invalid-date",
                    fieldName + " must be ISO-8601 format",
                    exception);
        }
    }
}
