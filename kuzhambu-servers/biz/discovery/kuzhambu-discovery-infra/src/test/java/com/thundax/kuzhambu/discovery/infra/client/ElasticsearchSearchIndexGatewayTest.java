package com.thundax.kuzhambu.discovery.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchKeyword;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

class ElasticsearchSearchIndexGatewayTest {

    @Test
    void searchShouldThrowUnsupportedOperationExceptionWithIndexName() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        properties.setIndexName("discovery-search-test");
        ElasticsearchSearchIndexGateway gateway = new ElasticsearchSearchIndexGateway(properties);

        UnsupportedOperationException exception =
                assertThrows(UnsupportedOperationException.class, () -> gateway.search(null, null, 1, 20));

        assertTrue(exception.getMessage().contains("discovery-search-test"));
    }

    @Test
    void rebuildAndUpsertShouldThrowUnsupportedOperationExceptionWithIndexName() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        properties.setIndexName("discovery-search-test");
        ElasticsearchSearchIndexGateway gateway = new ElasticsearchSearchIndexGateway(properties);

        UnsupportedOperationException rebuildException =
                assertThrows(UnsupportedOperationException.class, () -> gateway.rebuildIndex(Collections.emptyList()));
        UnsupportedOperationException upsertException = assertThrows(
                UnsupportedOperationException.class,
                () -> gateway.upsertDocuments(Collections.<SearchSourceContent>emptyList()));

        assertTrue(rebuildException.getMessage().contains("discovery-search-test"));
        assertTrue(upsertException.getMessage().contains("discovery-search-test"));
    }

    @Test
    void searchShouldMapGroupedResultsWhenOperationsReturnHits() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        @SuppressWarnings("unchecked")
        SearchHits<DiscoverySearchDocument> searchHits = mock(SearchHits.class);
        @SuppressWarnings("unchecked")
        SearchHit<DiscoverySearchDocument> searchHit = mock(SearchHit.class);
        DiscoverySearchDocument document = new DiscoverySearchDocument(
                "SANCAI_ENTRY:1001",
                "CLASSICS",
                "SANCAI_ENTRY",
                "1001",
                "SANCAI_ENTRY",
                "11",
                "天文",
                "黄帝",
                "摘要",
                "正文",
                List.of("上古"),
                "PUBLISHED",
                "PUBLIC",
                3,
                null,
                Instant.parse("2026-01-02T00:00:00Z"),
                false,
                null,
                "/classics/sancai/1001");
        when(searchHit.getContent()).thenReturn(document);
        when(searchHits.getTotalHits()).thenReturn(21L);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(operations.search(
                        any(org.springframework.data.elasticsearch.core.query.CriteriaQuery.class),
                        eq(DiscoverySearchDocument.class),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(searchHits);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        var page = gateway.search(new SearchKeyword("黄帝", "黄帝", "黄帝"), new SearchScope(), 1, 20);

        assertEquals(21, page.getTotalCount());
        assertTrue(page.getGroups().size() == 1);
        assertTrue(page.getGroups().get(0).getItems().size() == 1);
        assertTrue(page.getGroups().get(0).getItems().get(0).getTargetPath().contains("/classics/sancai/1001"));
        assertTrue(page.getGroups().get(0).getItems().get(0).getHighlightText().contains("<mark>黄帝</mark>"));
        assertTrue(page.getGroups().get(0).getItems().get(0).getGroupRank() == 1);
        assertEquals("SANCAI_ENTRY", page.getGroups().get(0).getItems().get(0).getKnowledgeBase());
        assertEquals("11", page.getGroups().get(0).getItems().get(0).getCategoryCode());
        assertEquals(List.of("上古"), page.getGroups().get(0).getItems().get(0).getTagNames());
        assertEquals("PUBLISHED", page.getGroups().get(0).getItems().get(0).getContentStatus());
        assertEquals("PUBLIC", page.getGroups().get(0).getItems().get(0).getVisibility());
        assertEquals(
                1_767_312_000_000L, page.getGroups().get(0).getItems().get(0).getUpdatedAt());
    }

    @Test
    void searchShouldHighlightSummaryBodyAndFallbackText() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        @SuppressWarnings("unchecked")
        SearchHits<DiscoverySearchDocument> searchHits = mock(SearchHits.class);
        SearchHit<DiscoverySearchDocument> summaryHit = searchHit(document("1001", "标题", "包含天地的摘要", "正文", 1));
        SearchHit<DiscoverySearchDocument> bodyHit = searchHit(document("1002", "标题", "摘要", "正文包含天地", 2));
        SearchHit<DiscoverySearchDocument> fallbackHit = searchHit(document("1003", "标题", "没有命中", "正文", 3));
        when(searchHits.getSearchHits()).thenReturn(List.of(summaryHit, bodyHit, fallbackHit));
        when(operations.search(
                        any(org.springframework.data.elasticsearch.core.query.CriteriaQuery.class),
                        eq(DiscoverySearchDocument.class),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(searchHits);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        var page = gateway.search(new SearchKeyword("天地", "天地", "天地"), new SearchScope(), 1, 20);

        var items = page.getGroups().get(0).getItems();
        assertTrue(items.get(0).getHighlightText().contains("<mark>天地</mark>"));
        assertTrue(items.get(1).getHighlightText().contains("<mark>天地</mark>"));
        assertTrue(items.get(2).getHighlightText().equals("没有命中"));
        assertTrue(items.get(0).getGroupRank() == 1);
        assertTrue(items.get(1).getGroupRank() == 2);
        assertTrue(items.get(2).getGroupRank() == 3);
    }

    @Test
    void searchShouldFallbackWhenKeywordIsBlank() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        @SuppressWarnings("unchecked")
        SearchHits<DiscoverySearchDocument> searchHits = mock(SearchHits.class);
        SearchHit<DiscoverySearchDocument> searchHit = searchHit(document("1001", "标题", "", "正文", 1));
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(operations.search(
                        any(org.springframework.data.elasticsearch.core.query.CriteriaQuery.class),
                        eq(DiscoverySearchDocument.class),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(searchHits);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        var page = gateway.search(new SearchKeyword("", "", ""), new SearchScope(), 1, 20);

        assertTrue(page.getGroups().get(0).getItems().get(0).getHighlightText().equals("标题"));
    }

    @Test
    void searchShouldApplyAdvancedScopeCriteria() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        @SuppressWarnings("unchecked")
        SearchHits<DiscoverySearchDocument> searchHits = mock(SearchHits.class);
        ArgumentCaptor<CriteriaQuery> queryCaptor = ArgumentCaptor.forClass(CriteriaQuery.class);
        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(operations.search(
                        queryCaptor.capture(),
                        eq(DiscoverySearchDocument.class),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(searchHits);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        gateway.search(
                new SearchKeyword("黄帝", "黄帝", "黄帝"),
                new SearchScope(
                        List.of("SANCAI_ENTRY"),
                        List.of("11"),
                        List.of("上古"),
                        List.of("PUBLISHED"),
                        List.of("PUBLIC"),
                        List.of(),
                        new Date(1_718_000_000_000L),
                        new Date(1_720_419_200_000L)),
                1,
                20);

        Set<String> fieldNames = flattenCriteria(queryCaptor.getValue().getCriteria()).stream()
                .map(Criteria::getField)
                .filter(field -> field != null && field.getName() != null)
                .map(field -> field.getName())
                .collect(Collectors.toSet());
        assertTrue(fieldNames.contains("knowledgeBase"));
        assertTrue(fieldNames.contains("categoryCode"));
        assertTrue(fieldNames.contains("tagNames"));
        assertTrue(fieldNames.contains("status"));
        assertTrue(fieldNames.contains("visibility"));
        assertTrue(fieldNames.contains("updatedAt"));
        assertTrue(fieldNames.contains("deleted"));
    }

    @Test
    void searchShouldApplyExplicitVisibilityCriteriaWithoutPrivateKnowledgeBasePermissionCriteria() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        @SuppressWarnings("unchecked")
        SearchHits<DiscoverySearchDocument> searchHits = mock(SearchHits.class);
        ArgumentCaptor<CriteriaQuery> queryCaptor = ArgumentCaptor.forClass(CriteriaQuery.class);
        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(operations.search(
                        queryCaptor.capture(),
                        eq(DiscoverySearchDocument.class),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(searchHits);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        gateway.search(
                new SearchKeyword("黄帝", "黄帝", "黄帝"),
                new SearchScope(
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of("PUBLIC", "PRIVATE"),
                        List.of("SANCAI_ENTRY"),
                        null,
                        null),
                1,
                20);

        Set<String> fieldNames = flattenCriteria(queryCaptor.getValue().getCriteria()).stream()
                .map(Criteria::getField)
                .filter(field -> field != null && field.getName() != null)
                .map(field -> field.getName())
                .collect(Collectors.toSet());
        assertTrue(fieldNames.contains("visibility"));
        assertFalse(fieldNames.contains("knowledgeBase"));
    }

    @Test
    void previewShouldApplyPublicVisibilityCriteria() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        @SuppressWarnings("unchecked")
        SearchHits<DiscoverySearchDocument> searchHits = mock(SearchHits.class);
        ArgumentCaptor<CriteriaQuery> queryCaptor = ArgumentCaptor.forClass(CriteriaQuery.class);
        SearchHit<DiscoverySearchDocument> searchHit = searchHit(document("1001", "标题", "摘要", "正文", 1));
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(operations.search(
                        queryCaptor.capture(),
                        eq(DiscoverySearchDocument.class),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(searchHits);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        var preview = gateway.getPreview("SANCAI_ENTRY", "1001");

        Set<String> fieldNames = flattenCriteria(queryCaptor.getValue().getCriteria()).stream()
                .map(Criteria::getField)
                .filter(field -> field != null && field.getName() != null)
                .map(field -> field.getName())
                .collect(Collectors.toSet());
        assertEquals("1001", preview.getContentId());
        assertEquals("正文", preview.getBodyText());
        assertTrue(fieldNames.contains("contentType"));
        assertTrue(fieldNames.contains("contentId"));
        assertTrue(fieldNames.contains("visibility"));
        assertFalse(fieldNames.contains("knowledgeBase"));
        assertTrue(fieldNames.contains("deleted"));
    }

    @Test
    void previewShouldReturnNullWhenNoHit() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        @SuppressWarnings("unchecked")
        SearchHits<DiscoverySearchDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(operations.search(
                        any(CriteriaQuery.class),
                        eq(DiscoverySearchDocument.class),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(searchHits);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        var preview = gateway.getPreview("SANCAI_ENTRY", "1001");

        assertEquals(null, preview);
    }

    @Test
    void searchShouldUseMatchCriteriaForChineseKeyword() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        @SuppressWarnings("unchecked")
        SearchHits<DiscoverySearchDocument> searchHits = mock(SearchHits.class);
        ArgumentCaptor<CriteriaQuery> queryCaptor = ArgumentCaptor.forClass(CriteriaQuery.class);
        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(operations.search(
                        queryCaptor.capture(),
                        eq(DiscoverySearchDocument.class),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(searchHits);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        gateway.search(new SearchKeyword("三才", "三才", "三才"), new SearchScope(), 1, 20);

        Set<Criteria.OperationKey> operationKeys = flattenCriteria(
                        queryCaptor.getValue().getCriteria())
                .stream()
                .flatMap(criteria -> criteria.getQueryCriteriaEntries().stream())
                .map(Criteria.CriteriaEntry::getKey)
                .collect(Collectors.toSet());
        assertTrue(operationKeys.contains(Criteria.OperationKey.MATCHES));
        assertTrue(!operationKeys.contains(Criteria.OperationKey.CONTAINS));
    }

    @Test
    void rebuildIndexShouldSaveAssembledDocuments() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        IndexOperations indexOperations = mock(IndexOperations.class);
        when(operations.indexOps(any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(false);
        when(indexOperations.create()).thenReturn(true);
        when(indexOperations.createMapping(DiscoverySearchDocument.class)).thenReturn(Document.create());
        when(indexOperations.putMapping(any(Document.class))).thenReturn(true);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        gateway.rebuildIndex(List.of(new SearchSourceContent(
                "CLASSICS",
                "SANCAI_ENTRY",
                "1001",
                "SANCAI_ENTRY",
                "11",
                "天文",
                "黄帝",
                "摘要",
                List.of("正文"),
                List.of(),
                "PUBLISHED",
                "PUBLIC",
                3,
                null,
                null)));

        verify(operations)
                .save(any(List.class), any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class));
    }

    @Test
    void rebuildIndexShouldOnlySavePublicDocuments() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        IndexOperations indexOperations = mock(IndexOperations.class);
        ArgumentCaptor<List<DiscoverySearchDocument>> documentsCaptor = ArgumentCaptor.forClass(List.class);
        when(operations.indexOps(any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(false);
        when(indexOperations.create()).thenReturn(true);
        when(indexOperations.createMapping(DiscoverySearchDocument.class)).thenReturn(Document.create());
        when(indexOperations.putMapping(any(Document.class))).thenReturn(true);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        gateway.rebuildIndex(List.of(sourceContent("1001", "PUBLIC"), sourceContent("1002", "PRIVATE")));

        verify(operations)
                .save(
                        documentsCaptor.capture(),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class));
        assertEquals(1, documentsCaptor.getValue().size());
        assertEquals("1001", documentsCaptor.getValue().get(0).getContentId());
        assertEquals("PUBLIC", documentsCaptor.getValue().get(0).getVisibility());
    }

    @Test
    void upsertDocumentsShouldDeleteNonPublicDocumentsAndSavePublicDocumentsOnly() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        ArgumentCaptor<List<DiscoverySearchDocument>> documentsCaptor = ArgumentCaptor.forClass(List.class);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        gateway.upsertDocuments(List.of(sourceContent("1001", "PUBLIC"), sourceContent("1002", "PRIVATE")));

        verify(operations)
                .delete(
                        eq("SANCAI_ENTRY:1002"),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class));
        verify(operations)
                .save(
                        documentsCaptor.capture(),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class));
        assertEquals(1, documentsCaptor.getValue().size());
        assertEquals("1001", documentsCaptor.getValue().get(0).getContentId());
    }

    @Test
    void markDocumentDeletedShouldSkipWhenExistingVersionIsNewer() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        DiscoverySearchDocument existing = new DiscoverySearchDocument();
        existing.setDocumentId("SANCAI_ENTRY:1001");
        existing.setSourceVersionNo(5);
        when(operations.get(eq("SANCAI_ENTRY:1001"), eq(DiscoverySearchDocument.class), any()))
                .thenReturn(existing);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        gateway.markDocumentDeleted("SANCAI_ENTRY", "1001", 4, new Date());

        verify(operations, never()).save(any(DiscoverySearchDocument.class), any());
    }

    @Test
    void cleanupDeletedDocumentsOlderThanShouldDeleteOnlyMatchedDocuments() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        @SuppressWarnings("unchecked")
        SearchHits<DiscoverySearchDocument> searchHits = mock(SearchHits.class);
        @SuppressWarnings("unchecked")
        SearchHit<DiscoverySearchDocument> searchHit = mock(SearchHit.class);
        DiscoverySearchDocument deletedDocument = new DiscoverySearchDocument();
        deletedDocument.setDocumentId("SANCAI_ENTRY:1001");
        when(searchHit.getContent()).thenReturn(deletedDocument);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(operations.search(
                        any(org.springframework.data.elasticsearch.core.query.CriteriaQuery.class),
                        eq(DiscoverySearchDocument.class),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(searchHits);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        int deletedCount = gateway.cleanupDeletedDocumentsOlderThan(Instant.parse("2026-06-24T00:00:00Z"));

        assertTrue(deletedCount == 1);
        verify(operations)
                .delete(
                        eq("SANCAI_ENTRY:1001"),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class));
    }

    private SearchHit<DiscoverySearchDocument> searchHit(DiscoverySearchDocument document) {
        @SuppressWarnings("unchecked")
        SearchHit<DiscoverySearchDocument> searchHit = mock(SearchHit.class);
        when(searchHit.getContent()).thenReturn(document);
        return searchHit;
    }

    private List<Criteria> flattenCriteria(Criteria criteria) {
        List<Criteria> result = new ArrayList<>();
        if (criteria == null) {
            return result;
        }
        result.add(criteria);
        for (Criteria chainedCriteria : criteria.getCriteriaChain()) {
            if (chainedCriteria != criteria) {
                result.addAll(flattenCriteria(chainedCriteria));
            }
        }
        for (Criteria subCriteria : criteria.getSubCriteria()) {
            result.addAll(flattenCriteria(subCriteria));
        }
        return result;
    }

    private DiscoverySearchDocument document(
            String contentId, String title, String summary, String bodyText, int rank) {
        return new DiscoverySearchDocument(
                "SANCAI_ENTRY:" + contentId,
                "CLASSICS",
                "SANCAI_ENTRY",
                contentId,
                "SANCAI_ENTRY",
                "11",
                "天文",
                title,
                summary,
                bodyText,
                List.of(),
                "PUBLISHED",
                "PUBLIC",
                rank,
                null,
                null,
                false,
                null,
                "/classics/sancai/" + contentId);
    }

    private SearchSourceContent sourceContent(String contentId, String visibility) {
        return new SearchSourceContent(
                "CLASSICS",
                "SANCAI_ENTRY",
                contentId,
                "SANCAI_ENTRY",
                "11",
                "天文",
                "黄帝",
                "摘要",
                List.of("正文"),
                List.of(),
                "PUBLISHED",
                visibility,
                3,
                null,
                null);
    }
}
