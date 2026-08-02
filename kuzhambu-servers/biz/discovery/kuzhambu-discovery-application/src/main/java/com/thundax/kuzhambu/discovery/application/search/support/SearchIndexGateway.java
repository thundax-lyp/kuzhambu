package com.thundax.kuzhambu.discovery.application.search.support;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPageResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPreviewResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationCandidateResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationDocument;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationProbeResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchKeyword;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import java.time.Instant;
import java.util.List;

public interface SearchIndexGateway {

    SearchPageResult search(SearchKeyword keyword, SearchScope searchScope, int pageNo, int pageSize);

    SearchPreviewResult getPreview(String contentType, String contentId);

    void rebuildIndex(List<SearchSourceContent> sourceContents);

    void upsertDocuments(List<SearchSourceContent> sourceContents);

    Integer getSourceVersionNo(String documentId);

    void markDocumentDeleted(String contentType, String contentId, Integer currentVersionNo, Instant occurredAt);

    Integer cleanupDeletedDocumentsOlderThan(Instant threshold);

    void preparePublication(SearchPublicationDocument document);

    SearchPublicationProbeResult probePublication(String documentId);

    boolean markPublicationReady(String documentId, String contentVersionId, Integer contentVersionNo);

    boolean markPublicationOffline(String documentId, Instant occurredAt);

    void deletePublication(String documentId);

    PageResult<SearchPublicationCandidateResult> pageReadyPublicationCandidates(
            String contentType, String categoryId, String volumeId, String keyword, int pageNo, int pageSize);
}
