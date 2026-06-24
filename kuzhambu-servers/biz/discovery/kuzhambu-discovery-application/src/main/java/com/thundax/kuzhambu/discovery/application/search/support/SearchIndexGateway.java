package com.thundax.kuzhambu.discovery.application.search.support;

import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchKeyword;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import java.util.List;

public interface SearchIndexGateway {

    List<SearchGroupResult> search(SearchKeyword keyword, SearchScope searchScope, int pageNo, int pageSize);

    void rebuildIndex(List<SearchSourceContent> sourceContents);

    void upsertDocuments(List<SearchSourceContent> sourceContents);
}
