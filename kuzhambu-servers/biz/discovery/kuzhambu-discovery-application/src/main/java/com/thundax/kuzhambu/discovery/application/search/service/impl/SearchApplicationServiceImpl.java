package com.thundax.kuzhambu.discovery.application.search.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.search.command.SearchClickCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchLogPageQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchLogResult;
import com.thundax.kuzhambu.discovery.application.search.service.QueryUnderstandingApplicationService;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import com.thundax.kuzhambu.discovery.application.search.support.SearchPermissionFilter;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClick;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchLog;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchClickRepository;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchLogRepository;
import com.thundax.kuzhambu.discovery.domain.service.SearchDomainService;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class SearchApplicationServiceImpl implements SearchApplicationService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SearchLogRepository searchLogRepository;
    private final SearchClickRepository searchClickRepository;
    private final SearchDomainService searchDomainService;
    private final SearchIndexGateway searchIndexGateway;
    private final SearchPermissionFilter searchPermissionFilter;
    private final QueryUnderstandingApplicationService queryUnderstandingApplicationService;

    public SearchApplicationServiceImpl(
            SearchLogRepository searchLogRepository,
            SearchClickRepository searchClickRepository,
            SearchDomainService searchDomainService,
            SearchIndexGateway searchIndexGateway,
            SearchPermissionFilter searchPermissionFilter,
            QueryUnderstandingApplicationService queryUnderstandingApplicationService) {
        this.searchLogRepository = searchLogRepository;
        this.searchClickRepository = searchClickRepository;
        this.searchDomainService = searchDomainService;
        this.searchIndexGateway = searchIndexGateway;
        this.searchPermissionFilter = searchPermissionFilter;
        this.queryUnderstandingApplicationService = queryUnderstandingApplicationService;
    }

    @Override
    public SearchLogResult search(SearchQuery query) {
        validateSearchQuery(query);
        QueryUnderstandingResult understandingResult = queryUnderstandingApplicationService.understand(query);
        var keyword = searchDomainService.normalizeKeyword(resolveSearchText(query, understandingResult));
        var scope = searchDomainService.normalizeScope(toSearchScope(query));
        int pageNo = searchDomainService.normalizePageNo(query.getPageNo());
        int pageSize = searchDomainService.normalizePageSize(query.getPageSize());
        try {
            List<SearchGroupResult> groups = searchIndexGateway.search(keyword, scope, pageNo, pageSize);
            List<SearchGroupResult> filteredGroups = searchPermissionFilter.filter(query, groups);
            SearchLog searchLog = buildSucceededSearchLog(
                    query, understandingResult, keyword.getNormalizedText(), scope, filteredGroups);
            searchLogRepository.save(searchLog);
            return toSearchResult(searchLog, filteredGroups);
        } catch (BizException exception) {
            searchLogRepository.save(
                    buildFailedSearchLog(query, understandingResult, keyword.getNormalizedText(), scope, exception));
            throw exception;
        } catch (UnsupportedOperationException exception) {
            searchLogRepository.save(
                    buildFailedSearchLog(query, understandingResult, keyword.getNormalizedText(), scope, exception));
            throw new BizException(
                    "DISCOVERY-20001",
                    "discovery.search.backend.not-implemented",
                    "Search backend is not implemented",
                    exception);
        }
    }

    @Override
    public Boolean recordClick(SearchClickCreateCommand command) {
        validateClickCommand(command);
        SearchLog searchLog = searchLogRepository.getBySearchLogId(command.getSearchLogId());
        if (searchLog == null) {
            throw new BizException(
                    "DISCOVERY-20002", "discovery.search.click.search-log-not-found", "Search log does not exist");
        }
        searchClickRepository.save(new SearchClick(
                null,
                null,
                command.getSearchLogId(),
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
                command.getRequestId(),
                command.getTraceId(),
                new Date()));
        return Boolean.TRUE;
    }

    @Override
    public PageResult<SearchLogResult> pageLogs(SearchLogPageQuery query) {
        if (query == null) {
            throw new BizException("Search log page query is required");
        }
        int pageNo = searchDomainService.normalizePageNo(query.getPageNo());
        int pageSize = searchDomainService.normalizePageSize(query.getPageSize());
        String intentType = firstOrNull(query.getIntentTypes());
        String searchStatus = firstOrNull(query.getSearchStatuses());
        PageResult<SearchLog> pageResult = searchLogRepository.page(
                query.getQueryText(), intentType, searchStatus, query.getOperatorId(), pageNo, pageSize);
        List<SearchLogResult> records = pageResult.getRecords() == null
                ? Collections.emptyList()
                : pageResult.getRecords().stream().map(this::toSearchLogResult).toList();
        return PageResult.of(pageResult.getPageNo(), pageResult.getPageSize(), pageResult.getTotalCount(), records);
    }

    @Override
    public SearchLogResult getLog(String searchLogId) {
        if (isBlank(searchLogId)) {
            throw new BizException("Search log id is required");
        }
        return toSearchLogResult(searchLogRepository.getBySearchLogId(searchLogId));
    }

    private SearchLog buildSucceededSearchLog(
            SearchQuery query,
            QueryUnderstandingResult understandingResult,
            String normalizedQueryText,
            SearchScope searchScope,
            List<SearchGroupResult> groups) {
        int totalCount = groups == null
                ? 0
                : groups.stream().mapToInt(SearchGroupResult::getCount).sum();
        return new SearchLog(
                null,
                newSearchLogId(),
                query.getQueryText(),
                normalizedQueryText,
                resolveDisplayQueryText(understandingResult, normalizedQueryText),
                resolveIntentType(understandingResult),
                searchScope,
                totalCount,
                groups == null ? 0 : groups.size(),
                "SUCCEEDED",
                null,
                null,
                query.getOperatorType(),
                query.getOperatorId(),
                query.getRequestId(),
                query.getTraceId(),
                new Date());
    }

    private SearchLog buildFailedSearchLog(
            SearchQuery query,
            QueryUnderstandingResult understandingResult,
            String normalizedQueryText,
            SearchScope searchScope,
            RuntimeException exception) {
        String failureCode = exception instanceof BizException bizException && !isBlank(bizException.getCode())
                ? bizException.getCode()
                : "DISCOVERY-20001";
        return new SearchLog(
                null,
                newSearchLogId(),
                query.getQueryText(),
                normalizedQueryText,
                resolveDisplayQueryText(understandingResult, normalizedQueryText),
                resolveIntentType(understandingResult),
                searchScope,
                0,
                0,
                "FAILED",
                failureCode,
                exception.getMessage(),
                query.getOperatorType(),
                query.getOperatorId(),
                query.getRequestId(),
                query.getTraceId(),
                new Date());
    }

    private SearchLogResult toSearchResult(SearchLog searchLog, List<SearchGroupResult> groups) {
        return new SearchLogResult(
                searchLog.getSearchLogId(),
                searchLog.getQueryText(),
                searchLog.getNormalizedQueryText(),
                searchLog.getDisplayQueryText(),
                searchLog.getIntentType() == null
                        ? null
                        : searchLog.getIntentType().value(),
                writeScope(searchLog.getSearchScope()),
                searchLog.getResultTotalCount() == null ? 0 : searchLog.getResultTotalCount(),
                searchLog.getGroupTotalCount() == null ? 0 : searchLog.getGroupTotalCount(),
                searchLog.getSearchStatus(),
                searchLog.getFailureCode(),
                searchLog.getFailureMessage(),
                searchLog.getOperatorId(),
                searchLog.getRequestId(),
                searchLog.getTraceId(),
                searchLog.getCreatedAt() == null
                        ? null
                        : searchLog.getCreatedAt().getTime(),
                groups);
    }

    private SearchScope toSearchScope(SearchQuery query) {
        return new SearchScope(
                query.getKnowledgeBases(),
                query.getCategoryCodes(),
                query.getTagNames(),
                query.getContentStatuses(),
                query.getVisibilityScopes(),
                query.getDateFrom(),
                query.getDateTo());
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

    private SearchLogResult toSearchLogResult(SearchLog entity) {
        if (entity == null) {
            return null;
        }
        return new SearchLogResult(
                entity.getSearchLogId(),
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
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().getTime(),
                Collections.emptyList());
    }

    private void validateSearchQuery(SearchQuery query) {
        if (query == null || isBlank(query.getQueryText())) {
            throw new BizException("Search query is required");
        }
    }

    private void validateClickCommand(SearchClickCreateCommand command) {
        if (command == null
                || isBlank(command.getSearchLogId())
                || isBlank(command.getContentDomain())
                || isBlank(command.getContentType())
                || isBlank(command.getContentId())
                || isBlank(command.getResultGroupKey())) {
            throw new BizException("Search click command is incomplete");
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

    private String newSearchLogId() {
        return UUID.randomUUID().toString();
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
