package com.thundax.kuzhambu.discovery.application.search.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.search.command.SearchClickCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchLogPageQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchLogResult;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import com.thundax.kuzhambu.discovery.application.search.support.SearchPermissionFilter;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClick;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchLog;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchClickRepository;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchLogRepository;
import com.thundax.kuzhambu.discovery.domain.service.SearchDomainService;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class SearchApplicationServiceImpl implements SearchApplicationService {

    private final SearchLogRepository searchLogRepository;
    private final SearchClickRepository searchClickRepository;
    private final SearchDomainService searchDomainService;
    private final SearchIndexGateway searchIndexGateway;
    private final SearchPermissionFilter searchPermissionFilter;

    public SearchApplicationServiceImpl(
            SearchLogRepository searchLogRepository,
            SearchClickRepository searchClickRepository,
            SearchDomainService searchDomainService,
            SearchIndexGateway searchIndexGateway,
            SearchPermissionFilter searchPermissionFilter) {
        this.searchLogRepository = searchLogRepository;
        this.searchClickRepository = searchClickRepository;
        this.searchDomainService = searchDomainService;
        this.searchIndexGateway = searchIndexGateway;
        this.searchPermissionFilter = searchPermissionFilter;
    }

    @Override
    public SearchLogResult search(SearchQuery query) {
        validateSearchQuery(query);
        var keyword = searchDomainService.normalizeKeyword(query.getQueryText());
        var scope = searchDomainService.normalizeScope(toSearchScope(query));
        int pageNo = searchDomainService.normalizePageNo(query.getPageNo());
        int pageSize = searchDomainService.normalizePageSize(query.getPageSize());
        try {
            List<SearchGroupResult> groups = searchIndexGateway.search(keyword, scope, pageNo, pageSize);
            List<SearchGroupResult> filteredGroups = searchPermissionFilter.filter(query, groups);
            return buildSearchResult(query, filteredGroups);
        } catch (UnsupportedOperationException exception) {
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

    private SearchLogResult buildSearchResult(SearchQuery query, List<SearchGroupResult> groups) {
        int totalCount = groups == null
                ? 0
                : groups.stream().mapToInt(SearchGroupResult::getCount).sum();
        return new SearchLogResult(
                query.getRequestId(),
                query.getQueryText(),
                query.getQueryText() == null ? null : query.getQueryText().trim(),
                query.getQueryText() == null ? null : query.getQueryText().trim(),
                null,
                null,
                totalCount,
                groups == null ? 0 : groups.size(),
                "SUCCEEDED",
                null,
                null,
                query.getOperatorId(),
                query.getRequestId(),
                query.getTraceId(),
                System.currentTimeMillis(),
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
                null,
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
}
