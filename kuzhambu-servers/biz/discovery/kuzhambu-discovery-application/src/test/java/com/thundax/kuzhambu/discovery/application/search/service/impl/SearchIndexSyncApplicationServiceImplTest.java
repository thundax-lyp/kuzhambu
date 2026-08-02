package com.thundax.kuzhambu.discovery.application.search.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import com.thundax.kuzhambu.discovery.application.search.support.SearchContentProvider;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class SearchIndexSyncApplicationServiceImplTest {

    @Test
    void syncUpsertShouldSkipWhenMessageVersionIsOlderThanCurrentContent() {
        SearchContentProvider searchContentProvider = mock(SearchContentProvider.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        SearchIndexSyncApplicationServiceImpl service =
                new SearchIndexSyncApplicationServiceImpl(searchContentProvider, searchIndexGateway);
        when(searchContentProvider.getPublicContent("SANCAI_ENTRY", "1001")).thenReturn(sourceContent(3));

        Boolean synced = service.syncUpsert("SANCAI_ENTRY", "1001", 2);

        assertFalse(synced);
        verify(searchIndexGateway, never()).upsertDocuments(List.of(sourceContent(3)));
    }

    @Test
    void syncUpsertShouldUpsertWhenVersionMatchesAndIndexVersionMatches() {
        SearchContentProvider searchContentProvider = mock(SearchContentProvider.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        SearchIndexSyncApplicationServiceImpl service =
                new SearchIndexSyncApplicationServiceImpl(searchContentProvider, searchIndexGateway);
        SearchSourceContent currentContent = sourceContent(3);
        when(searchContentProvider.getPublicContent("SANCAI_ENTRY", "1001")).thenReturn(currentContent);
        when(searchIndexGateway.getSourceVersionNo("SANCAI_ENTRY:1001")).thenReturn(3);

        Boolean synced = service.syncUpsert("SANCAI_ENTRY", "1001", 3);

        assertTrue(synced);
        verify(searchIndexGateway).upsertDocuments(List.of(currentContent));
    }

    @Test
    void syncUpsertShouldUpsertWhenContentVersionIsNewerThanIndexedVersion() {
        SearchContentProvider searchContentProvider = mock(SearchContentProvider.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        SearchIndexSyncApplicationServiceImpl service =
                new SearchIndexSyncApplicationServiceImpl(searchContentProvider, searchIndexGateway);
        SearchSourceContent currentContent = sourceContent(4);
        when(searchContentProvider.getPublicContent("SANCAI_ENTRY", "1001")).thenReturn(currentContent);
        when(searchIndexGateway.getSourceVersionNo("SANCAI_ENTRY:1001")).thenReturn(3);

        Boolean synced = service.syncUpsert("SANCAI_ENTRY", "1001", 4);

        assertTrue(synced);
        verify(searchIndexGateway).upsertDocuments(List.of(currentContent));
    }

    @Test
    void syncUpsertShouldMarkDeletedWhenCurrentPublicContentDoesNotExist() {
        SearchContentProvider searchContentProvider = mock(SearchContentProvider.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        SearchIndexSyncApplicationServiceImpl service =
                new SearchIndexSyncApplicationServiceImpl(searchContentProvider, searchIndexGateway);
        when(searchContentProvider.getPublicContent("SANCAI_ENTRY", "1001")).thenReturn(null);

        Boolean synced = service.syncUpsert("SANCAI_ENTRY", "1001", 4);

        assertTrue(synced);
        verify(searchIndexGateway).markDocumentDeleted("SANCAI_ENTRY", "1001", 4, null);
        verify(searchIndexGateway, never()).upsertDocuments(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void syncDeleteShouldDelegateDeleteMarkerUpdate() {
        SearchContentProvider searchContentProvider = mock(SearchContentProvider.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        SearchIndexSyncApplicationServiceImpl service =
                new SearchIndexSyncApplicationServiceImpl(searchContentProvider, searchIndexGateway);
        Instant occurredAt = Instant.now();

        Boolean synced = service.syncDelete("SANCAI_ENTRY", "1001", 5, occurredAt);

        assertTrue(synced);
        verify(searchIndexGateway).markDocumentDeleted("SANCAI_ENTRY", "1001", 5, occurredAt);
    }

    private SearchSourceContent sourceContent(int currentVersionNo) {
        return new SearchSourceContent(
                "CLASSICS",
                "SANCAI_ENTRY",
                "1001",
                "SANCAI_ENTRY",
                "11",
                "天文",
                "黄帝",
                "摘要",
                List.of("原文"),
                List.of(),
                currentVersionNo,
                null,
                null);
    }
}
