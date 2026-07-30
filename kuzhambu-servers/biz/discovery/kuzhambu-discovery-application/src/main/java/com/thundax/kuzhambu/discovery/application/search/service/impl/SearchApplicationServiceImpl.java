package com.thundax.kuzhambu.discovery.application.search.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import com.thundax.kuzhambu.discovery.application.search.command.SearchClickEventCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchEventPageQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPreviewQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchStatisticsSummaryQuery;
import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchEventResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPageResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPreviewResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchStatisticsSummaryResult;
import com.thundax.kuzhambu.discovery.application.search.service.QueryUnderstandingApplicationService;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import com.thundax.kuzhambu.discovery.application.search.support.SearchTimeObjectMapperFactory;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClickEvent;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchEvent;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchClickEventRepository;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchEventRepository;
import com.thundax.kuzhambu.discovery.domain.service.SearchDomainService;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class SearchApplicationServiceImpl implements SearchApplicationService {

    private static final ObjectMapper OBJECT_MAPPER = SearchTimeObjectMapperFactory.create();
    private static final String PUBLIC_VISIBILITY = "PUBLIC";

    private final SearchEventRepository searchEventRepository;
    private final SearchClickEventRepository searchClickEventRepository;
    private final SearchDomainService searchDomainService;
    private final SearchIndexGateway searchIndexGateway;
    private final QueryUnderstandingApplicationService queryUnderstandingApplicationService;

    public SearchApplicationServiceImpl(
            SearchEventRepository searchEventRepository,
            SearchClickEventRepository searchClickEventRepository,
            SearchDomainService searchDomainService,
            SearchIndexGateway searchIndexGateway,
            QueryUnderstandingApplicationService queryUnderstandingApplicationService) {
        this.searchEventRepository = searchEventRepository;
        this.searchClickEventRepository = searchClickEventRepository;
        this.searchDomainService = searchDomainService;
        this.searchIndexGateway = searchIndexGateway;
        this.queryUnderstandingApplicationService = queryUnderstandingApplicationService;
    }

    @Override
    public SearchEventResult search(SearchQuery query) {
        validateSearchQuery(query);
        normalizeSearchQuery(query);
        long startNanos = System.nanoTime();
        QueryUnderstandingResult understandingResult = null;
        String normalizedQueryText =
                searchDomainService.normalizeKeyword(query.getQueryText()).getNormalizedText();
        SearchScope scope = searchDomainService.normalizeScope(toSearchScope(query));
        try {
            understandingResult = queryUnderstandingApplicationService.understand(query);
            var keyword = searchDomainService.normalizeKeyword(resolveSearchText(query, understandingResult));
            normalizedQueryText = keyword.getNormalizedText();
            scope = searchDomainService.normalizeScope(toSearchScope(query));
            int pageNo = searchDomainService.normalizePageNo(query.getPageNo());
            int pageSize = searchDomainService.normalizePageSize(query.getPageSize());
            SearchPageResult searchPage = searchIndexGateway.search(keyword, scope, pageNo, pageSize);
            List<SearchGroupResult> groups = searchPage.safeGroups();
            SearchEvent searchEvent = buildSucceededSearchEvent(
                    query,
                    understandingResult,
                    normalizedQueryText,
                    scope,
                    searchPage.getTotalCount(),
                    groups,
                    elapsedMillis(startNanos));
            searchEvent.setId(searchEventRepository.save(searchEvent));
            return toSearchResult(searchEvent, groups);
        } catch (BizException exception) {
            searchEventRepository.save(buildFailedSearchEvent(
                    query, understandingResult, normalizedQueryText, scope, elapsedMillis(startNanos), exception));
            throw exception;
        } catch (UnsupportedOperationException exception) {
            searchEventRepository.save(buildFailedSearchEvent(
                    query, understandingResult, normalizedQueryText, scope, elapsedMillis(startNanos), exception));
            throw new BizException(
                    "DISCOVERY-20001",
                    "discovery.search.backend.not-implemented",
                    "Search backend is not implemented",
                    exception);
        } catch (RuntimeException exception) {
            searchEventRepository.save(buildFailedSearchEvent(
                    query, understandingResult, normalizedQueryText, scope, elapsedMillis(startNanos), exception));
            throw exception;
        }
    }

    @Override
    public SearchPreviewResult getPreview(SearchPreviewQuery query) {
        validatePreviewQuery(query);
        SearchPreviewResult result = searchIndexGateway.getPreview(query.getContentType(), query.getContentId());
        if (result == null) {
            throw new BizException(
                    "DISCOVERY-20003",
                    "discovery.search.preview.not-found",
                    "Search preview does not exist or is not visible");
        }
        return result;
    }

    @Override
    public Boolean recordClick(SearchClickEventCreateCommand command) {
        validateClickCommand(command);
        SearchEventId searchEventId = command.getSearchEventId();
        SearchEvent searchEvent = searchEventRepository.getById(searchEventId);
        if (searchEvent == null) {
            throw new BizException(
                    "DISCOVERY-20002", "discovery.search.click.search-event-not-found", "Search event does not exist");
        }
        searchClickEventRepository.save(new SearchClickEvent(
                null,
                searchEventId,
                command.getContentDomain(),
                command.getContentType(),
                command.getContentId(),
                command.getContentTitle(),
                command.getResultGroupKey(),
                command.getResultRank(),
                command.getGroupRank(),
                command.getTargetPath(),
                command.getOperatorType(),
                command.getOperatorId(),
                RequestIdCodec.toValue(command.getRequestId()),
                TraceIdCodec.toValue(command.getTraceId()),
                Instant.now()));
        return Boolean.TRUE;
    }

    @Override
    public PageResult<SearchEventResult> pageEvents(SearchEventPageQuery query) {
        if (query == null) {
            throw new BizException("Search event page query is required");
        }
        int pageNo = searchDomainService.normalizePageNo(query.getPageNo());
        int pageSize = searchDomainService.normalizePageSize(query.getPageSize());
        String intentType = firstOrNull(query.getIntentTypes());
        String searchStatus = firstOrNull(query.getSearchStatuses());
        PageResult<SearchEvent> pageResult = searchEventRepository.page(
                query.getQueryText(), intentType, searchStatus, query.getOperatorId(), pageNo, pageSize);
        List<SearchEventResult> records = pageResult.getRecords() == null
                ? Collections.emptyList()
                : pageResult.getRecords().stream()
                        .map(this::toSearchEventResult)
                        .toList();
        return PageResult.of(pageResult.getPageNo(), pageResult.getPageSize(), pageResult.getTotalCount(), records);
    }

    @Override
    public SearchEventResult getEvent(Long id) {
        if (id == null) {
            throw new BizException("Search event id is required");
        }
        return toSearchEventResult(searchEventRepository.getById(SearchEventIdCodec.toDomain(id)));
    }

    @Override
    public SearchStatisticsSummaryResult getStatisticsSummary(SearchStatisticsSummaryQuery query) {
        Instant dateFrom = query == null ? null : query.getDateFrom();
        Instant dateTo = query == null ? null : query.getDateTo();
        List<SearchEvent> searchEvents = searchEventRepository.listByCreatedAtRange(dateFrom, dateTo);
        List<SearchEvent> logs = searchEvents == null ? Collections.emptyList() : searchEvents;
        long clickCount = searchClickEventRepository.countByCreatedAtRange(dateFrom, dateTo);
        return new SearchStatisticsSummaryResult(
                logs.size(),
                logs.stream().filter(this::isFailedSearch).count(),
                logs.stream().filter(this::isZeroResultSucceededSearch).count(),
                clickCount,
                topQueries(logs));
    }

    private SearchEvent buildSucceededSearchEvent(
            SearchQuery query,
            QueryUnderstandingResult understandingResult,
            String normalizedQueryText,
            SearchScope searchScope,
            int totalCount,
            List<SearchGroupResult> groups,
            Long searchLatencyMs) {
        return new SearchEvent(
                null,
                query.getQueryText(),
                normalizedQueryText,
                resolveDisplayQueryText(understandingResult, normalizedQueryText),
                resolveIntentType(understandingResult),
                searchScope,
                totalCount,
                groups == null ? 0 : groups.size(),
                searchLatencyMs,
                "SUCCEEDED",
                null,
                null,
                query.getOperatorType(),
                query.getOperatorId(),
                RequestIdCodec.toValue(query.getRequestId()),
                TraceIdCodec.toValue(query.getTraceId()),
                Instant.now());
    }

    private SearchEvent buildFailedSearchEvent(
            SearchQuery query,
            QueryUnderstandingResult understandingResult,
            String normalizedQueryText,
            SearchScope searchScope,
            Long searchLatencyMs,
            RuntimeException exception) {
        String failureCode = exception instanceof BizException bizException && !isBlank(bizException.getCode())
                ? bizException.getCode()
                : "DISCOVERY-20001";
        return new SearchEvent(
                null,
                query.getQueryText(),
                normalizedQueryText,
                resolveDisplayQueryText(understandingResult, normalizedQueryText),
                resolveIntentType(understandingResult),
                searchScope,
                0,
                0,
                searchLatencyMs,
                "FAILED",
                failureCode,
                exception.getMessage(),
                query.getOperatorType(),
                query.getOperatorId(),
                RequestIdCodec.toValue(query.getRequestId()),
                TraceIdCodec.toValue(query.getTraceId()),
                Instant.now());
    }

    private SearchEventResult toSearchResult(SearchEvent searchEvent, List<SearchGroupResult> groups) {
        return new SearchEventResult(
                searchEvent.getId(),
                searchEvent.getQueryText(),
                searchEvent.getNormalizedQueryText(),
                searchEvent.getDisplayQueryText(),
                searchEvent.getIntentType() == null
                        ? null
                        : searchEvent.getIntentType().value(),
                writeScope(searchEvent.getSearchScope()),
                searchEvent.getResultTotalCount() == null ? 0 : searchEvent.getResultTotalCount(),
                searchEvent.getGroupTotalCount() == null ? 0 : searchEvent.getGroupTotalCount(),
                searchEvent.getSearchStatus(),
                searchEvent.getFailureCode(),
                searchEvent.getFailureMessage(),
                searchEvent.getOperatorId(),
                searchEvent.getRequestId(),
                searchEvent.getTraceId(),
                searchEvent.getCreatedAt() == null
                        ? null
                        : searchEvent.getCreatedAt().toEpochMilli(),
                groups);
    }

    private SearchScope toSearchScope(SearchQuery query) {
        SearchScope scope = new SearchScope(
                query.getKnowledgeBases(),
                query.getCategoryCodes(),
                query.getTagNames(),
                query.getContentStatuses(),
                List.of(PUBLIC_VISIBILITY),
                Collections.emptyList(),
                query.getDateFrom(),
                query.getDateTo());
        return scope;
    }

    private String resolveSearchText(SearchQuery query, QueryUnderstandingResult understandingResult) {
        if (understandingResult == null) {
            return query.getQueryText();
        }
        if (!isBlank(understandingResult.getRewrittenQueryText())) {
            return understandingResult.getRewrittenQueryText();
        }
        if (!isBlank(understandingResult.getNormalizedQueryText())) {
            return understandingResult.getNormalizedQueryText();
        }
        return query.getQueryText();
    }

    private String resolveDisplayQueryText(QueryUnderstandingResult understandingResult, String fallbackText) {
        if (understandingResult == null) {
            return fallbackText;
        }
        if (!isBlank(understandingResult.getRewrittenQueryText())) {
            return understandingResult.getRewrittenQueryText();
        }
        if (!isBlank(understandingResult.getNormalizedQueryText())) {
            return understandingResult.getNormalizedQueryText();
        }
        return fallbackText;
    }

    private SearchIntentType resolveIntentType(QueryUnderstandingResult understandingResult) {
        if (understandingResult == null || isBlank(understandingResult.getIntent())) {
            return SearchIntentType.KEYWORD_SEARCH;
        }
        try {
            return SearchIntentType.from(understandingResult.getIntent());
        } catch (RuntimeException exception) {
            return SearchIntentType.UNKNOWN;
        }
    }

    private SearchEventResult toSearchEventResult(SearchEvent entity) {
        if (entity == null) {
            return null;
        }
        return new SearchEventResult(
                entity.getId(),
                entity.getQueryText(),
                entity.getNormalizedQueryText(),
                entity.getDisplayQueryText(),
                entity.getIntentType() == null ? null : entity.getIntentType().value(),
                writeScope(entity.getSearchScope()),
                entity.getResultTotalCount() == null ? 0 : entity.getResultTotalCount(),
                entity.getGroupTotalCount() == null ? 0 : entity.getGroupTotalCount(),
                entity.getSearchStatus(),
                entity.getFailureCode(),
                entity.getFailureMessage(),
                entity.getOperatorId(),
                entity.getRequestId(),
                entity.getTraceId(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toEpochMilli(),
                Collections.emptyList());
    }

    private void validateSearchQuery(SearchQuery query) {
        if (query == null) {
            throw new BizException("Search query is required");
        }
    }

    private void normalizeSearchQuery(SearchQuery query) {
        if (query.getQueryText() == null) {
            query.setQueryText("");
        }
        query.setVisibilityScopes(List.of(PUBLIC_VISIBILITY));
    }

    private void validatePreviewQuery(SearchPreviewQuery query) {
        if (query == null || isBlank(query.getContentType()) || isBlank(query.getContentId())) {
            throw new BizException("Search preview query is incomplete");
        }
    }

    private void validateClickCommand(SearchClickEventCreateCommand command) {
        if (command == null
                || command.getSearchEventId() == null
                || isBlank(command.getContentDomain())
                || isBlank(command.getContentType())
                || isBlank(command.getContentId())
                || isBlank(command.getResultGroupKey())) {
            throw new BizException("Search click event command is incomplete");
        }
    }

    private String firstOrNull(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private boolean isFailedSearch(SearchEvent searchEvent) {
        return searchEvent != null && "FAILED".equals(searchEvent.getSearchStatus());
    }

    private boolean isZeroResultSucceededSearch(SearchEvent searchEvent) {
        return searchEvent != null
                && "SUCCEEDED".equals(searchEvent.getSearchStatus())
                && searchEvent.getResultTotalCount() != null
                && searchEvent.getResultTotalCount() == 0;
    }

    private List<SearchStatisticsSummaryResult.TopQueryItem> topQueries(List<SearchEvent> searchEvents) {
        Map<String, Long> countByQueryText = searchEvents.stream()
                .map(SearchEvent::getQueryText)
                .filter(queryText -> !isBlank(queryText))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return countByQueryText.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue)
                        .reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(10)
                .map(entry -> new SearchStatisticsSummaryResult.TopQueryItem(entry.getKey(), entry.getValue()))
                .toList();
    }

    private String writeScope(SearchScope searchScope) {
        if (searchScope == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(searchScope);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }
}
