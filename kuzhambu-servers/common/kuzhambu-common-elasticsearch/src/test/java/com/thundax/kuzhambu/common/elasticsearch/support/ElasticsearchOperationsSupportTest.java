package com.thundax.kuzhambu.common.elasticsearch.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

class ElasticsearchOperationsSupportTest {

    @Test
    void shouldRejectMissingOperationsWithBackendContext() {
        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> ElasticsearchOperationsSupport.requireOperations(null, "Search", "query", "idx"));

        assertEquals("Search backend is not implemented for query on index idx", exception.getMessage());
    }

    @Test
    void shouldRecreateIndexWithMapping() {
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        IndexOperations indexOperations = mock(IndexOperations.class);
        IndexCoordinates indexCoordinates = IndexCoordinates.of("idx");
        org.springframework.data.elasticsearch.core.document.Document mapping =
                org.springframework.data.elasticsearch.core.document.Document.create();

        when(operations.indexOps(indexCoordinates)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(true);
        when(indexOperations.createMapping(Document.class)).thenReturn(mapping);

        ElasticsearchOperationsSupport.recreateIndex(operations, indexCoordinates, Document.class);

        verify(indexOperations).delete();
        verify(indexOperations).create();
        verify(indexOperations).putMapping(mapping);
    }

    @Test
    void shouldSaveDocumentsInConfiguredBatches() {
        ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
        IndexCoordinates indexCoordinates = IndexCoordinates.of("idx");

        ElasticsearchOperationsSupport.saveInBatches(operations, List.of("a", "b", "c", "d", "e"), indexCoordinates, 2);

        verify(operations).save(List.of("a", "b"), indexCoordinates);
        verify(operations).save(List.of("c", "d"), indexCoordinates);
        verify(operations).save(List.of("e"), indexCoordinates);
    }

    @Test
    void shouldExtractFirstContentAndIgnoreMissingHit() {
        SearchHit<String> hit = mock(SearchHit.class);
        when(hit.getContent()).thenReturn("content");

        assertNull(ElasticsearchOperationsSupport.firstContent(List.of()));
        assertEquals("content", ElasticsearchOperationsSupport.firstContent(List.of(hit)));
    }

    @Test
    void shouldNormalizeStringValues() {
        assertEquals(List.of("a", "b"), ElasticsearchOperationsSupport.normalizedValues(List.of(" a ", "", "b")));
    }

    @Test
    void shouldCapTotalHitsAtIntegerMaxValue() {
        assertEquals(Integer.MAX_VALUE, ElasticsearchOperationsSupport.toIntTotalHits(Long.MAX_VALUE));
        assertEquals(42, ElasticsearchOperationsSupport.toIntTotalHits(42L));
    }

    static class Document {}
}
