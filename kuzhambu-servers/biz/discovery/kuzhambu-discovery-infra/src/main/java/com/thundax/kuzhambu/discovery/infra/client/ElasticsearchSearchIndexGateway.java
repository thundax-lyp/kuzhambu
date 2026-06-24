package com.thundax.kuzhambu.discovery.infra.client;

import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchKeyword;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchSearchIndexGateway implements SearchIndexGateway {

    private final DiscoverySearchIndexProperties properties;
    private final ElasticsearchOperations elasticsearchOperations;
    private final DiscoverySearchDocumentAssembler documentAssembler;

    public ElasticsearchSearchIndexGateway() {
        this(new DiscoverySearchIndexProperties(), null, new DiscoverySearchDocumentAssembler());
    }

    public ElasticsearchSearchIndexGateway(DiscoverySearchIndexProperties properties) {
        this(properties, null, new DiscoverySearchDocumentAssembler());
    }

    @Autowired
    public ElasticsearchSearchIndexGateway(
            DiscoverySearchIndexProperties properties,
            ElasticsearchOperations elasticsearchOperations,
            DiscoverySearchDocumentAssembler documentAssembler) {
        this.properties = properties;
        this.elasticsearchOperations = elasticsearchOperations;
        this.documentAssembler = documentAssembler;
    }

    @Override
    public List<SearchGroupResult> search(SearchKeyword keyword, SearchScope searchScope, int pageNo, int pageSize) {
        ElasticsearchOperations operations = requireOperations("search");
        CriteriaQuery query =
                new CriteriaQuery(buildCriteria(keyword == null ? null : keyword.getNormalizedText(), searchScope));
        query.setPageable(PageRequest.of(Math.max(pageNo - 1, 0), pageSize));
        List<SearchHit<DiscoverySearchDocument>> hits = operations
                .search(query, DiscoverySearchDocument.class, indexCoordinates())
                .getSearchHits();
        return toGroupedResults(hits);
    }

    @Override
    public void rebuildIndex(List<SearchSourceContent> sourceContents) {
        ElasticsearchOperations operations = requireOperations("rebuild");
        var indexOperations = operations.indexOps(indexCoordinates());
        if (indexOperations.exists()) {
            indexOperations.delete();
        }
        indexOperations.create();
        indexOperations.putMapping(indexOperations.createMapping(DiscoverySearchDocument.class));
        saveDocuments(operations, toDocuments(sourceContents));
    }

    @Override
    public void upsertDocuments(List<SearchSourceContent> sourceContents) {
        ElasticsearchOperations operations = requireOperations("upsert");
        saveDocuments(operations, toDocuments(sourceContents));
    }

    @Override
    public Integer getSourceVersionNo(String documentId) {
        ElasticsearchOperations operations = requireOperations("get-version");
        DiscoverySearchDocument document =
                operations.get(documentId, DiscoverySearchDocument.class, indexCoordinates());
        return document == null ? null : document.getSourceVersionNo();
    }

    @Override
    public void markDocumentDeleted(
            String contentType, String contentId, Integer currentVersionNo, java.util.Date occurredAt) {
        ElasticsearchOperations operations = requireOperations("mark-deleted");
        String documentId = contentType + ":" + contentId;
        DiscoverySearchDocument existing =
                operations.get(documentId, DiscoverySearchDocument.class, indexCoordinates());
        if (existing != null
                && existing.getSourceVersionNo() != null
                && currentVersionNo != null
                && currentVersionNo < existing.getSourceVersionNo()) {
            return;
        }
        DiscoverySearchDocument deletedDocument = existing == null ? new DiscoverySearchDocument() : existing;
        deletedDocument.setDocumentId(documentId);
        deletedDocument.setContentType(contentType);
        deletedDocument.setContentId(contentId);
        deletedDocument.setSourceVersionNo(currentVersionNo);
        deletedDocument.setDeleted(Boolean.TRUE);
        deletedDocument.setDeletedAt(occurredAt == null ? null : occurredAt.toInstant());
        operations.save(deletedDocument, indexCoordinates());
    }

    public Integer cleanupDeletedDocumentsOlderThan(Instant threshold) {
        ElasticsearchOperations operations = requireOperations("cleanup-deleted");
        if (threshold == null) {
            return 0;
        }
        Criteria criteria = new Criteria("deleted").is(true).and(new Criteria("deletedAt").lessThan(threshold));
        CriteriaQuery query = new CriteriaQuery(criteria);
        List<SearchHit<DiscoverySearchDocument>> hits = operations
                .search(query, DiscoverySearchDocument.class, indexCoordinates())
                .getSearchHits();
        if (hits == null || hits.isEmpty()) {
            return 0;
        }
        int deletedCount = 0;
        for (SearchHit<DiscoverySearchDocument> hit : hits) {
            DiscoverySearchDocument document = hit == null ? null : hit.getContent();
            if (document == null || document.getDocumentId() == null) {
                continue;
            }
            operations.delete(document.getDocumentId(), indexCoordinates());
            deletedCount++;
        }
        return deletedCount;
    }

    private ElasticsearchOperations requireOperations(String operation) {
        if (elasticsearchOperations == null) {
            throw new UnsupportedOperationException("Discovery search backend is not implemented for "
                    + operation
                    + " on index "
                    + properties.getIndexName());
        }
        return elasticsearchOperations;
    }

    private List<DiscoverySearchDocument> toDocuments(List<SearchSourceContent> sourceContents) {
        if (sourceContents == null || sourceContents.isEmpty()) {
            return Collections.emptyList();
        }
        List<DiscoverySearchDocument> documents = new ArrayList<>(sourceContents.size());
        for (SearchSourceContent sourceContent : sourceContents) {
            DiscoverySearchDocument document = documentAssembler.toDocument(sourceContent);
            if (document != null) {
                documents.add(document);
            }
        }
        return documents;
    }

    private void saveDocuments(ElasticsearchOperations operations, List<DiscoverySearchDocument> documents) {
        if (documents.isEmpty()) {
            return;
        }
        int batchSize = properties.getBatchSize() <= 0 ? 200 : properties.getBatchSize();
        for (int start = 0; start < documents.size(); start += batchSize) {
            int end = Math.min(start + batchSize, documents.size());
            operations.save(documents.subList(start, end), indexCoordinates());
        }
    }

    private Criteria buildCriteria(String keyword, SearchScope searchScope) {
        Criteria criteria = baseKeywordCriteria(keyword);
        if (searchScope == null) {
            return criteria.and(new Criteria("deleted").is(false));
        }
        criteria = appendInFilter(criteria, "knowledgeBase", searchScope.getKnowledgeBases());
        criteria = appendInFilter(criteria, "categoryCode", searchScope.getCategoryCodes());
        criteria = appendInFilter(criteria, "tagNames", searchScope.getTagNames());
        criteria = appendInFilter(criteria, "status", searchScope.getContentStatuses());
        criteria = appendInFilter(criteria, "visibility", searchScope.getVisibilityScopes());
        criteria = criteria.and(new Criteria("deleted").is(false));
        if (searchScope.getDateFrom() != null) {
            criteria = criteria.and(new Criteria("updatedAt")
                    .greaterThanEqual(searchScope.getDateFrom().toInstant()));
        }
        if (searchScope.getDateTo() != null) {
            criteria = criteria.and(new Criteria("updatedAt")
                    .lessThanEqual(searchScope.getDateTo().toInstant()));
        }
        return criteria;
    }

    private Criteria baseKeywordCriteria(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new Criteria();
        }
        return new Criteria("title")
                .contains(keyword)
                .or(new Criteria("summary").contains(keyword))
                .or(new Criteria("bodyText").contains(keyword));
    }

    private Criteria appendInFilter(Criteria criteria, String fieldName, List<String> values) {
        if (values == null || values.isEmpty()) {
            return criteria;
        }
        List<String> filteredValues = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
        if (filteredValues.isEmpty()) {
            return criteria;
        }
        return criteria.and(new Criteria(fieldName).in(filteredValues.toArray()));
    }

    private List<SearchGroupResult> toGroupedResults(List<SearchHit<DiscoverySearchDocument>> hits) {
        if (hits == null || hits.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, List<SearchResult>> itemsByGroup = new LinkedHashMap<>();
        int resultRank = 1;
        for (SearchHit<DiscoverySearchDocument> hit : hits) {
            DiscoverySearchDocument document = hit == null ? null : hit.getContent();
            if (document == null) {
                continue;
            }
            String groupKey = document.getContentType();
            List<SearchResult> items = itemsByGroup.computeIfAbsent(groupKey, key -> new ArrayList<>());
            items.add(new SearchResult(
                    document.getContentDomain(),
                    document.getContentType(),
                    document.getContentId(),
                    document.getTitle(),
                    document.getSummary(),
                    null,
                    resultRank++,
                    items.size() + 1,
                    document.getSourcePath()));
        }
        return itemsByGroup.entrySet().stream()
                .map(entry -> new SearchGroupResult(
                        entry.getKey(),
                        groupTitle(entry.getKey()),
                        entry.getValue().size(),
                        entry.getValue()))
                .toList();
    }

    private String groupTitle(String contentType) {
        return switch (contentType) {
            case "SANCAI_ENTRY" -> "三才图会";
            case "WANGQI_DOCUMENT" -> "王圻文档";
            case "MING_CUSTOMS" -> "明代习俗";
            default -> contentType;
        };
    }

    private IndexCoordinates indexCoordinates() {
        return IndexCoordinates.of(properties.getIndexName());
    }
}
