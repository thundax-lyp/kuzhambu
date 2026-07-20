package com.thundax.kuzhambu.common.elasticsearch.support;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

public final class ElasticsearchOperationsSupport {

    public static final int DEFAULT_BATCH_SIZE = 200;

    private ElasticsearchOperationsSupport() {}

    public static ElasticsearchOperations requireOperations(
            ElasticsearchOperations operations, String backendName, String operation, String indexName) {
        if (operations == null) {
            throw new UnsupportedOperationException(
                    backendName + " backend is not implemented for " + operation + " on index " + indexName);
        }
        return operations;
    }

    public static IndexCoordinates indexCoordinates(String indexName) {
        return IndexCoordinates.of(indexName);
    }

    public static <T> void recreateIndex(
            ElasticsearchOperations operations, IndexCoordinates indexCoordinates, Class<T> documentClass) {
        var indexOperations = operations.indexOps(indexCoordinates);
        if (indexOperations.exists()) {
            indexOperations.delete();
        }
        indexOperations.create();
        indexOperations.putMapping(indexOperations.createMapping(documentClass));
    }

    public static <T> void saveInBatches(
            ElasticsearchOperations operations,
            Collection<T> documents,
            IndexCoordinates indexCoordinates,
            int configuredBatchSize) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        List<T> documentList = List.copyOf(documents);
        int batchSize = configuredBatchSize <= 0 ? DEFAULT_BATCH_SIZE : configuredBatchSize;
        for (int start = 0; start < documentList.size(); start += batchSize) {
            int end = Math.min(start + batchSize, documentList.size());
            operations.save(documentList.subList(start, end), indexCoordinates);
        }
    }

    public static <T> T firstContent(List<SearchHit<T>> hits) {
        if (hits == null || hits.isEmpty() || hits.get(0) == null) {
            return null;
        }
        return hits.get(0).getContent();
    }

    public static <T> List<T> contents(List<SearchHit<T>> hits) {
        if (hits == null || hits.isEmpty()) {
            return Collections.emptyList();
        }
        return hits.stream()
                .filter(hit -> hit != null && hit.getContent() != null)
                .map(SearchHit::getContent)
                .toList();
    }

    public static int toIntTotalHits(long totalHits) {
        return totalHits > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) totalHits;
    }

    public static List<String> normalizedValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
    }
}
