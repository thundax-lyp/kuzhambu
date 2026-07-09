package com.thundax.kuzhambu.discovery.interfaces.portal.search.assembler;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.discovery.application.search.command.SearchClickCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchLogResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchClickRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.response.DiscoverySearchGroupResponse;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.response.DiscoverySearchResponse;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class DiscoverySearchPortalInterfaceAssembler {

    private static final String PORTAL_OPERATOR_TYPE = "ANONYMOUS";

    private DiscoverySearchPortalInterfaceAssembler() {}

    public static SearchQuery toQuery(DiscoverySearchRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchQuery(
                request.getQueryText(),
                request.getKnowledgeBases(),
                request.getCategoryCodes(),
                request.getTagNames(),
                request.getContentStatuses(),
                request.getVisibilityScopes(),
                parseDate(request.getDateFrom(), "dateFrom"),
                parseDate(request.getDateTo(), "dateTo"),
                request.getPageNo() == null ? 1 : request.getPageNo(),
                request.getPageSize() == null ? 20 : request.getPageSize(),
                PORTAL_OPERATOR_TYPE,
                null,
                newRequestId(),
                newTraceId());
    }

    public static SearchClickCreateCommand toCommand(DiscoverySearchClickRequest request) {
        if (request == null) {
            return null;
        }
        return new SearchClickCreateCommand(
                request.getSearchLogId(),
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

    public static DiscoverySearchResponse toResponse(SearchLogResult result) {
        if (result == null) {
            return null;
        }
        return DiscoverySearchResponse.builder()
                .searchLogId(result.getSearchLogId())
                .queryText(result.getQueryText())
                .displayQueryText(result.getDisplayQueryText())
                .totalCount(result.getResultTotalCount())
                .groupCount(result.getGroupTotalCount())
                .groups(toGroupResponses(result.getGroups()))
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

    private static Date parseDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Date.from(Instant.parse(value));
        } catch (DateTimeParseException exception) {
            throw new BizException(
                    "DISCOVERY-40001",
                    "discovery.search.request.invalid-date",
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
