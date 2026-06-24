package com.thundax.kuzhambu.discovery.application.search.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexApplicationService;
import com.thundax.kuzhambu.discovery.application.search.support.SearchContentProvider;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class SearchIndexApplicationServiceImpl implements SearchIndexApplicationService {

    private final SearchContentProvider searchContentProvider;
    private final SearchIndexGateway searchIndexGateway;

    public SearchIndexApplicationServiceImpl(
            SearchContentProvider searchContentProvider, SearchIndexGateway searchIndexGateway) {
        this.searchContentProvider = searchContentProvider;
        this.searchIndexGateway = searchIndexGateway;
    }

    @Override
    public Integer rebuildIndex() {
        List<com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent> sourceContents =
                searchContentProvider.listPublicContents();
        List<com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent> effectiveContents =
                sourceContents == null ? Collections.emptyList() : sourceContents;
        searchIndexGateway.rebuildIndex(effectiveContents);
        return effectiveContents.size();
    }
}
