package com.thundax.kuzhambu.discovery.domain.service;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchKeyword;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import java.util.Collections;

public class SearchDomainService {

    private static final int FIRST_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;

    public SearchKeyword normalizeKeyword(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            throw new DomainException("搜索词不能为空");
        }
        String normalizedText = queryText.trim();
        return new SearchKeyword(queryText, normalizedText, normalizedText);
    }

    public SearchScope normalizeScope(SearchScope searchScope) {
        if (searchScope == null) {
            return new SearchScope(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    null,
                    null);
        }
        return searchScope;
    }

    public int normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo < FIRST_PAGE_NO) {
            return FIRST_PAGE_NO;
        }
        return pageNo;
    }

    public int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public void expandSynonyms(SearchKeyword keyword) {
        throw new DomainException("DISCOVERY-10002", "discovery.search.synonyms.not-implemented", "同义词扩展尚未实现");
    }

    public void linkEntities(SearchKeyword keyword) {
        throw new DomainException("DISCOVERY-10003", "discovery.search.entities.not-implemented", "实体识别增强尚未实现");
    }
}
