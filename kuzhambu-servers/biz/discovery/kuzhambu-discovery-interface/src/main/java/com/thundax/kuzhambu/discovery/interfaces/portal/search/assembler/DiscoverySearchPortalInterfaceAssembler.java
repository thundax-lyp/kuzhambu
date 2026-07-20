package com.thundax.kuzhambu.discovery.interfaces.portal.search.assembler;

import com.thundax.kuzhambu.discovery.application.search.command.SearchClickEventCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPreviewQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchEventResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPreviewResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchClickEventRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchPreviewRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.response.DiscoverySearchGroupResponse;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.response.DiscoverySearchPreviewResponse;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.response.DiscoverySearchResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class DiscoverySearchPortalInterfaceAssembler {

    private static final List<String> PUBLIC_VISIBILITY_SCOPE = List.of("PUBLIC");
    private static final String PORTAL_OPERATOR_TYPE = "ANONYMOUS";

    private DiscoverySearchPortalInterfaceAssembler() {}

    public static SearchQuery toQuery(DiscoverySearchRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchQuery(
                normalizeQueryText(request.getQueryText()),
                request.getKnowledgeBases(),
                request.getCategoryCodes(),
                request.getTagNames(),
                request.getContentStatuses(),
                PUBLIC_VISIBILITY_SCOPE,
                parseDateFrom(request.getDateFrom()),
                parseDateTo(request.getDateTo()),
                request.getPageNo() == null ? 1 : request.getPageNo(),
                request.getPageSize() == null ? 20 : request.getPageSize(),
                PORTAL_OPERATOR_TYPE,
                null,
                newRequestId(),
                newTraceId());
    }

    public static SearchClickEventCreateCommand toCommand(DiscoverySearchClickEventRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchClickEventCreateCommand(
                request.getSearchEventId(),
                request.getContentDomain(),
                request.getContentType(),
                request.getContentId(),
                request.getContentTitle(),
                request.getResultGroupKey(),
                request.getResultRank(),
                request.getGroupRank(),
                request.getTargetPath(),
                PORTAL_OPERATOR_TYPE,
                null,
                newRequestId(),
                newTraceId());
    }

    public static SearchPreviewQuery toQuery(DiscoverySearchPreviewRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchPreviewQuery(
                request.getContentType(),
                request.getContentId(),
                PORTAL_OPERATOR_TYPE,
                null,
                newRequestId(),
                newTraceId());
    }

    public static DiscoverySearchResponse toResponse(SearchEventResult result) {
        if (result == null) {
            return null;
        }
        return DiscoverySearchResponse.builder()
                .searchEventId(result.getSearchEventId())
                .queryText(result.getQueryText())
                .displayQueryText(result.getDisplayQueryText())
                .totalCount(result.getResultTotalCount())
                .groupCount(result.getGroupTotalCount())
                .groups(toGroupResponses(result.getGroups()))
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
                .contentStatus(result.getContentStatus())
                .visibility(result.getVisibility())
                .sourceVersionNo(result.getSourceVersionNo())
                .publishedAt(result.getPublishedAt())
                .updatedAt(result.getUpdatedAt())
                .targetPath(result.getTargetPath())
                .build();
    }

    private static List<DiscoverySearchGroupResponse> toGroupResponses(List<SearchGroupResult> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        return groups.stream()
                .map(DiscoverySearchPortalInterfaceAssembler::toGroupResponse)
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
            return Collections.emptyList();
        }
        return items.stream()
                .map(DiscoverySearchPortalInterfaceAssembler::toItemResponse)
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

    private static Date parseDateFrom(String value) {
        return parseDate(value, false);
    }

    private static Date parseDateTo(String value) {
        return parseDate(value, true);
    }

    private static Date parseDate(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmedValue = value.trim();
        try {
            return Date.from(Instant.parse(trimmedValue));
        } catch (DateTimeParseException exception) {
            return parseLocalDate(trimmedValue, endOfDay);
        }
    }

    private static Date parseLocalDate(String value, boolean endOfDay) {
        try {
            LocalDate date = LocalDate.parse(value);
            Instant instant = endOfDay
                    ? date.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC)
                    : date.atStartOfDay().toInstant(ZoneOffset.UTC);
            return Date.from(instant);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private static String normalizeQueryText(String queryText) {
        return queryText == null ? "" : queryText;
    }

    private static String newRequestId() {
        return UUID.randomUUID().toString();
    }

    private static String newTraceId() {
        return UUID.randomUUID().toString();
    }
}
