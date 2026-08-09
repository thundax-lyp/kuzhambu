package com.thundax.kuzhambu.discovery.domain.search.support;

import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchKeyword;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import java.util.Collections;

public class SearchQueryNormalizer {

    public SearchKeyword normalizeKeyword(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return new SearchKeyword(queryText, "", "");
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
                    null,
                    null);
        }
        return searchScope;
    }
}
