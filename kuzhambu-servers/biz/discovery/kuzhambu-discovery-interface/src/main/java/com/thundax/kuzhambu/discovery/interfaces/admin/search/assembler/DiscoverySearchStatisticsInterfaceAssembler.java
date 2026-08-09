package com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.discovery.application.search.command.SearchClickEventCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchEventQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPreviewQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchStatisticsSummaryQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchEventResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPreviewResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchStatisticsSummaryResult;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchClickEventRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchEventPageRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchPreviewRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchStatisticsSummaryRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchEventDetailResponse;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchEventResponse;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchGroupResponse;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchPreviewResponse;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchResponse;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchStatisticsSummaryResponse;
import com.thundax.kuzhambu.discovery.interfaces.common.DiscoveryInterfaceIdCodec;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

public final class DiscoverySearchStatisticsInterfaceAssembler {

    private static final String ADMIN_OPERATOR_TYPE = "ADMIN";

    private DiscoverySearchStatisticsInterfaceAssembler() {}

    public static SearchQuery toQuery(DiscoverySearchRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchQuery(
                request.getQueryText(),
                request.getKnowledgeBases(),
                request.getCategoryCodes(),
                request.getTagNames(),
                parseDate(request.getDateFrom(), "dateFrom"),
                parseDate(request.getDateTo(), "dateTo"),
                ADMIN_OPERATOR_TYPE,
                KuzhambuContextHolder.currentSubjectId(),
                RequestIdCodec.toDomain(newRequestId()),
                TraceIdCodec.toDomain(newTraceId()));
    }

    public static SearchEventQuery toQuery(DiscoverySearchEventPageRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchEventQuery(
                request.getQueryText(),
                request.getIntentTypes(),
                request.getSearchStatuses(),
                request.getOperatorId(),
                parseDate(request.getDateFrom(), "dateFrom"),
                parseDate(request.getDateTo(), "dateTo"));
    }

    public static SearchClickEventCreateCommand toCommand(DiscoverySearchClickEventRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchClickEventCreateCommand(
                SearchEventIdCodec.toDomain(DiscoveryInterfaceIdCodec.toLongValue(request.getSearchEventId())),
                request.getContentDomain(),
                request.getContentType(),
                request.getContentId(),
                request.getContentTitle(),
                request.getResultGroupKey(),
                request.getResultRank(),
                request.getGroupRank(),
                request.getTargetPath(),
                ADMIN_OPERATOR_TYPE,
                KuzhambuContextHolder.currentSubjectId(),
                RequestIdCodec.toDomain(newRequestId()),
                TraceIdCodec.toDomain(newTraceId()));
    }

    public static SearchPreviewQuery toQuery(DiscoverySearchPreviewRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchPreviewQuery(
                request.getContentType(),
                request.getContentId(),
                ADMIN_OPERATOR_TYPE,
                KuzhambuContextHolder.currentSubjectId(),
                newRequestId(),
                newTraceId());
    }

    public static SearchStatisticsSummaryQuery toQuery(DiscoverySearchStatisticsSummaryRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchStatisticsSummaryQuery(
                parseDate(request.getDateFrom(), "dateFrom"), parseDate(request.getDateTo(), "dateTo"));
    }

    public static DiscoverySearchEventResponse toResponse(SearchEventResult result) {
        if (result == null) {
            return null;
        }
        return DiscoverySearchEventResponse.builder()
                .id(SearchEventIdCodec.toStringValue(result.getId()))
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

    public static DiscoverySearchEventDetailResponse toDetailResponse(SearchEventResult result) {
        if (result == null) {
            return null;
        }
        return DiscoverySearchEventDetailResponse.builder()
                .id(SearchEventIdCodec.toStringValue(result.getId()))
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

    public static DiscoverySearchResponse toSearchResponse(SearchEventResult result) {
        if (result == null) {
            return null;
        }
        return DiscoverySearchResponse.builder()
                .id(SearchEventIdCodec.toStringValue(result.getId()))
                .queryText(result.getQueryText())
                .displayQueryText(result.getDisplayQueryText())
                .totalCount(result.getResultTotalCount())
                .groupCount(result.getGroupTotalCount())
                .groups(toGroupResponses(result.getGroups()))
                .build();
    }

    public static DiscoverySearchStatisticsSummaryResponse toResponse(SearchStatisticsSummaryResult result) {
        if (result == null) {
            return null;
        }
        return DiscoverySearchStatisticsSummaryResponse.builder()
                .searchCount(result.getSearchCount())
                .failedSearchCount(result.getFailedSearchCount())
                .zeroResultSearchCount(result.getZeroResultSearchCount())
                .clickCount(result.getClickCount())
                .topQueries(toTopQueryResponses(result.getTopQueries()))
                .build();
    }

    public static DiscoverySearchPreviewResponse toResponse(SearchPreviewResult result) {
        if (result == null) {
            return null;
        }
        return DiscoverySearchPreviewResponse.builder()
                .contentDomain(result.getContentDomain())
                .contentType(result.getContentType())
                .contentId(result.getContentId())
                .knowledgeBase(result.getKnowledgeBase())
                .categoryCode(result.getCategoryCode())
                .categoryName(result.getCategoryName())
                .title(result.getTitle())
                .summary(result.getSummary())
                .bodyText(result.getBodyText())
                .tagNames(result.getTagNames())
                .sourceVersionNo(result.getSourceVersionNo())
                .publishedAt(result.getPublishedAt())
                .updatedAt(result.getUpdatedAt())
                .targetPath(result.getTargetPath())
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

    private static Instant toDate(Long value) {
        return value == null ? null : Instant.ofEpochMilli(value);
    }

    private static List<DiscoverySearchGroupResponse> toGroupResponses(List<SearchGroupResult> groups) {
        if (groups == null || groups.isEmpty()) {
            return List.of();
        }
        return groups.stream()
                .map(DiscoverySearchStatisticsInterfaceAssembler::toGroupResponse)
                .toList();
    }

    private static DiscoverySearchGroupResponse toGroupResponse(SearchGroupResult group) {
        if (group == null) {
            return null;
        }
        return DiscoverySearchGroupResponse.builder()
                .groupKey(group.getGroupKey())
                .groupTitle(group.getGroupTitle())
                .count(group.getCount())
                .items(toItemResponses(group.getItems()))
                .build();
    }

    private static List<DiscoverySearchGroupResponse.DiscoverySearchItemResponse> toItemResponses(
            List<SearchResult> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .map(DiscoverySearchStatisticsInterfaceAssembler::toItemResponse)
                .toList();
    }

    private static DiscoverySearchGroupResponse.DiscoverySearchItemResponse toItemResponse(SearchResult item) {
        if (item == null) {
            return null;
        }
        return DiscoverySearchGroupResponse.DiscoverySearchItemResponse.builder()
                .contentDomain(item.getContentDomain())
                .contentType(item.getContentType())
                .contentId(item.getContentId())
                .title(item.getTitle())
                .summary(item.getSummary())
                .highlightText(item.getHighlightText())
                .resultRank(item.getResultRank())
                .groupRank(item.getGroupRank())
                .targetPath(item.getTargetPath())
                .build();
    }

    private static List<DiscoverySearchStatisticsSummaryResponse.TopQueryResponse> toTopQueryResponses(
            List<SearchStatisticsSummaryResult.TopQueryItem> topQueries) {
        if (topQueries == null) {
            return List.of();
        }
        return topQueries.stream()
                .map(topQuery -> DiscoverySearchStatisticsSummaryResponse.TopQueryResponse.builder()
                        .queryText(topQuery.getQueryText())
                        .count(topQuery.getCount())
                        .build())
                .toList();
    }

    private static Instant parseDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            throw new BizException(
                    "DISCOVERY-40002",
                    "discovery.search.admin.invalid-date",
                    fieldName + " must be ISO-8601 format",
                    exception);
        }
    }

    private static String newRequestId() {
        return UUID.randomUUID().toString();
    }

    private static String newTraceId() {
        return UUID.randomUUID().toString();
    }
}
