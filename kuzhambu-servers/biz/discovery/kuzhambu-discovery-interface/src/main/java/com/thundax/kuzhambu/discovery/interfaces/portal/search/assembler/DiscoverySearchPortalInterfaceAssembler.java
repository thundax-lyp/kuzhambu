package com.thundax.kuzhambu.discovery.interfaces.portal.search.assembler;

import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import com.thundax.kuzhambu.discovery.application.search.command.SearchClickEventCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPreviewQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchEventResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPreviewResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.interfaces.common.DiscoveryInterfaceIdCodec;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchClickEventRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchPreviewRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.response.DiscoverySearchGroupResponse;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.response.DiscoverySearchPreviewResponse;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.response.DiscoverySearchResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.lang.NonNull;

public final class DiscoverySearchPortalInterfaceAssembler {

    private static final String PORTAL_OPERATOR_TYPE = "ANONYMOUS";

    private DiscoverySearchPortalInterfaceAssembler() {}

    public static @NonNull SearchQuery toQuery(@NonNull DiscoverySearchRequest request) {
        Objects.requireNonNull(request, "request");
        return new SearchQuery(
                normalizeQueryText(request.getQueryText()),
                request.getKnowledgeBases(),
                request.getCategoryCodes(),
                request.getTagNames(),
                parseDateFrom(request.getDateFrom()),
                parseDateTo(request.getDateTo()),
                PORTAL_OPERATOR_TYPE,
                null,
                RequestIdCodec.toDomain(newRequestId()),
                TraceIdCodec.toDomain(newTraceId()));
    }

    public static @NonNull SearchClickEventCreateCommand toCommand(@NonNull DiscoverySearchClickEventRequest request) {
        Objects.requireNonNull(request, "request");
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
                PORTAL_OPERATOR_TYPE,
                null,
                RequestIdCodec.toDomain(newRequestId()),
                TraceIdCodec.toDomain(newTraceId()));
    }

    public static @NonNull SearchPreviewQuery toQuery(@NonNull DiscoverySearchPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new SearchPreviewQuery(
                request.getContentType(),
                request.getContentId(),
                PORTAL_OPERATOR_TYPE,
                null,
                newRequestId(),
                newTraceId());
    }

    public static @NonNull DiscoverySearchResponse toResponse(@NonNull SearchEventResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoverySearchResponse.builder()
                .id(SearchEventIdCodec.toStringValue(result.getId()))
                .queryText(result.getQueryText())
                .displayQueryText(result.getDisplayQueryText())
                .totalCount(result.getResultTotalCount())
                .groupCount(result.getGroupTotalCount())
                .groups(toGroupResponses(result.getGroups()))
                .build();
    }

    public static @NonNull DiscoverySearchPreviewResponse toResponse(@NonNull SearchPreviewResult result) {
        Objects.requireNonNull(result, "result");
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

    private static Instant parseDateFrom(String value) {
        return parseDate(value, false);
    }

    private static Instant parseDateTo(String value) {
        return parseDate(value, true);
    }

    private static Instant parseDate(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmedValue = value.trim();
        try {
            return Instant.parse(trimmedValue);
        } catch (DateTimeParseException exception) {
            return parseLocalDate(trimmedValue, endOfDay);
        }
    }

    private static Instant parseLocalDate(String value, boolean endOfDay) {
        try {
            LocalDate date = LocalDate.parse(value);
            return endOfDay
                    ? date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusMillis(1)
                    : date.atStartOfDay().toInstant(ZoneOffset.UTC);
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
