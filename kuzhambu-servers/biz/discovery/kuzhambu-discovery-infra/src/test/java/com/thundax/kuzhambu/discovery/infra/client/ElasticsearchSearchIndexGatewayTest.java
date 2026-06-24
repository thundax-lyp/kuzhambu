package com.thundax.kuzhambu.discovery.infra.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchKeyword;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;

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
                List.of(),
                "PUBLISHED",
                "PUBLIC",
                null,
                null,
                "/classics/sancai/1001");
        when(searchHit.getContent()).thenReturn(document);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(operations.search(
                        any(org.springframework.data.elasticsearch.core.query.CriteriaQuery.class),
                        eq(DiscoverySearchDocument.class),
                        any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class)))
                .thenReturn(searchHits);
        ElasticsearchSearchIndexGateway gateway =
                new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

        var groups = gateway.search(new SearchKeyword("黄帝", "黄帝", "黄帝"), new SearchScope(), 1, 20);

        assertTrue(groups.size() == 1);
        assertTrue(groups.get(0).getItems().size() == 1);
        assertTrue(groups.get(0).getItems().get(0).getTargetPath().contains("/classics/sancai/1001"));
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
                null,
                null)));

        verify(operations)
                .save(any(List.class), any(org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.class));
    }
}
