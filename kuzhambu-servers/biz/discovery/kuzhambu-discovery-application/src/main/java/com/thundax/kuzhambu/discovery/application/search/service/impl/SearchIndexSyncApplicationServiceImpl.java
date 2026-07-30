package com.thundax.kuzhambu.discovery.application.search.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexSyncApplicationService;
import com.thundax.kuzhambu.discovery.application.search.support.SearchContentProvider;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class SearchIndexSyncApplicationServiceImpl implements SearchIndexSyncApplicationService {

    private final SearchContentProvider searchContentProvider;
    private final SearchIndexGateway searchIndexGateway;

    public SearchIndexSyncApplicationServiceImpl(
            SearchContentProvider searchContentProvider, SearchIndexGateway searchIndexGateway) {
        this.searchContentProvider = searchContentProvider;
        this.searchIndexGateway = searchIndexGateway;
    }

    @Override
    public Boolean syncUpsert(String contentType, String contentId, Integer currentVersionNo) {
        SearchSourceContent currentContent = searchContentProvider.getPublicContent(contentType, contentId);
        if (currentContent == null) {
            if (currentVersionNo == null) {
                return Boolean.FALSE;
            }
            searchIndexGateway.markDocumentDeleted(contentType, contentId, currentVersionNo, null);
            return Boolean.TRUE;
        }
        if (currentContent.getCurrentVersionNo() == null || currentVersionNo == null) {
            return Boolean.FALSE;
        }
        if (!currentVersionNo.equals(currentContent.getCurrentVersionNo())) {
            return Boolean.FALSE;
        }
        Integer indexedVersionNo = searchIndexGateway.getSourceVersionNo(documentId(contentType, contentId));
        if (indexedVersionNo != null && currentContent.getCurrentVersionNo() < indexedVersionNo) {
            return Boolean.FALSE;
        }
        searchIndexGateway.upsertDocuments(List.of(currentContent));
        return Boolean.TRUE;
    }

    @Override
    public Boolean syncDelete(String contentType, String contentId, Integer currentVersionNo, Instant occurredAt) {
        searchIndexGateway.markDocumentDeleted(contentType, contentId, currentVersionNo, occurredAt);
        return Boolean.TRUE;
    }

    private String documentId(String contentType, String contentId) {
        return contentType + ":" + contentId;
    }
}
