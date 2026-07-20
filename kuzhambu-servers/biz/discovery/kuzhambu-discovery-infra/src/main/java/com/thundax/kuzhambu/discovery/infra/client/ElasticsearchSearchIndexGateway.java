package com.thundax.kuzhambu.discovery.infra.client;

import com.thundax.kuzhambu.common.elasticsearch.support.ElasticsearchOperationsSupport;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPageResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPreviewResult;
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
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchSearchIndexGateway implements SearchIndexGateway {

    private static final int HIGHLIGHT_CONTEXT_LENGTH = 60;
    private static final int FALLBACK_LENGTH = 160;
    private static final String PUBLIC_VISIBILITY = "PUBLIC";

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
    public SearchPageResult search(SearchKeyword keyword, SearchScope searchScope, int pageNo, int pageSize) {
        ElasticsearchOperations operations = requireOperations("search");
        CriteriaQuery query =
                new CriteriaQuery(buildCriteria(keyword == null ? null : keyword.getNormalizedText(), searchScope));
        query.setPageable(PageRequest.of(Math.max(pageNo - 1, 0), pageSize));
        SearchHits<DiscoverySearchDocument> searchHits =
                operations.search(query, DiscoverySearchDocument.class, indexCoordinates());
        return new SearchPageResult(
                ElasticsearchOperationsSupport.toIntTotalHits(searchHits.getTotalHits()),
                toGroupedResults(searchHits.getSearchHits(), keyword == null ? null : keyword.getNormalizedText()));
    }

    @Override
    public SearchPreviewResult getPreview(String contentType, String contentId) {
        ElasticsearchOperations operations = requireOperations("preview");
        Criteria criteria = new Criteria("contentType").is(contentType).and(new Criteria("contentId").is(contentId));
        criteria = appendPublicVisibilityFilter(criteria);
        criteria = criteria.and(new Criteria("deleted").is(false));
        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(0, 1));
        List<SearchHit<DiscoverySearchDocument>> hits = operations
                .search(query, DiscoverySearchDocument.class, indexCoordinates())
                .getSearchHits();
        if (hits == null || hits.isEmpty()) {
            return null;
        }
        DiscoverySearchDocument document = ElasticsearchOperationsSupport.firstContent(hits);
        return toPreviewResult(document);
    }

    @Override
    public void rebuildIndex(List<SearchSourceContent> sourceContents) {
        ElasticsearchOperations operations = requireOperations("rebuild");
        ElasticsearchOperationsSupport.recreateIndex(operations, indexCoordinates(), DiscoverySearchDocument.class);
        saveDocuments(operations, toPublicDocuments(sourceContents));
    }

    @Override
    public void upsertDocuments(List<SearchSourceContent> sourceContents) {
        ElasticsearchOperations operations = requireOperations("upsert");
        List<DiscoverySearchDocument> documents = toDocuments(sourceContents);
        deleteNonPublicDocuments(operations, documents);
        saveDocuments(operations, publicDocuments(documents));
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
        return ElasticsearchOperationsSupport.requireOperations(
                elasticsearchOperations, "Discovery search", operation, properties.getIndexName());
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

    private List<DiscoverySearchDocument> toPublicDocuments(List<SearchSourceContent> sourceContents) {
        return publicDocuments(toDocuments(sourceContents));
    }

    private List<DiscoverySearchDocument> publicDocuments(List<DiscoverySearchDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }
        return documents.stream().filter(this::isPublicDocument).toList();
    }

    private void deleteNonPublicDocuments(ElasticsearchOperations operations, List<DiscoverySearchDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        for (DiscoverySearchDocument document : documents) {
            if (isPublicDocument(document) || document.getDocumentId() == null) {
                continue;
            }
            operations.delete(document.getDocumentId(), indexCoordinates());
        }
    }

    private boolean isPublicDocument(DiscoverySearchDocument document) {
        return document != null && PUBLIC_VISIBILITY.equalsIgnoreCase(document.getVisibility());
    }

    private void saveDocuments(ElasticsearchOperations operations, List<DiscoverySearchDocument> documents) {
        if (documents.isEmpty()) {
            return;
        }
        ElasticsearchOperationsSupport.saveInBatches(
                operations, documents, indexCoordinates(), properties.getBatchSize());
    }

    private Criteria buildCriteria(String keyword, SearchScope searchScope) {
        Criteria criteria = baseKeywordCriteria(keyword);
        if (searchScope == null) {
            criteria = appendPublicVisibilityFilter(criteria);
            return criteria.and(new Criteria("deleted").is(false));
        }
        criteria = appendInFilter(criteria, "knowledgeBase", searchScope.getKnowledgeBases());
        criteria = appendInFilter(criteria, "categoryCode", searchScope.getCategoryCodes());
        criteria = appendInFilter(criteria, "tagNames", searchScope.getTagNames());
        criteria = appendInFilter(criteria, "status", searchScope.getContentStatuses());
        criteria = appendPublicVisibilityFilter(criteria);
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
                .matches(keyword)
                .or(new Criteria("summary").matches(keyword))
                .or(new Criteria("bodyText").matches(keyword));
    }

    private Criteria appendInFilter(Criteria criteria, String fieldName, List<String> values) {
        if (values == null || values.isEmpty()) {
            return criteria;
        }
        List<String> filteredValues = ElasticsearchOperationsSupport.normalizedValues(values);
        if (filteredValues.isEmpty()) {
            return criteria;
        }
        return criteria.and(new Criteria(fieldName).in(filteredValues.toArray()));
    }

    private Criteria appendPublicVisibilityFilter(Criteria criteria) {
        return criteria.and(new Criteria("visibility").is(PUBLIC_VISIBILITY));
    }

    private List<SearchGroupResult> toGroupedResults(List<SearchHit<DiscoverySearchDocument>> hits, String keyword) {
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
                    document.getKnowledgeBase(),
                    document.getCategoryCode(),
                    document.getTitle(),
                    document.getSummary(),
                    buildHighlightText(document, keyword),
                    document.getTagNames(),
                    document.getStatus(),
                    document.getVisibility(),
                    document.getUpdatedAt() == null
                            ? null
                            : document.getUpdatedAt().toEpochMilli(),
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

    private SearchPreviewResult toPreviewResult(DiscoverySearchDocument document) {
        if (document == null) {
            return null;
        }
        return new SearchPreviewResult(
                document.getContentDomain(),
                document.getContentType(),
                document.getContentId(),
                document.getKnowledgeBase(),
                document.getCategoryCode(),
                document.getCategoryName(),
                document.getTitle(),
                document.getSummary(),
                document.getBodyText(),
                document.getTagNames(),
                document.getStatus(),
                document.getVisibility(),
                document.getSourceVersionNo(),
                document.getPublishedAt() == null
                        ? null
                        : document.getPublishedAt().toEpochMilli(),
                document.getUpdatedAt() == null ? null : document.getUpdatedAt().toEpochMilli(),
                document.getSourcePath());
    }

    private String groupTitle(String contentType) {
        return switch (contentType) {
            case "SANCAI_ENTRY" -> "三才图会";
            case "WANGQI_DOCUMENT" -> "王圻文档";
            case "MING_CUSTOMS" -> "明代习俗";
            default -> contentType;
        };
    }

    private String buildHighlightText(DiscoverySearchDocument document, String keyword) {
        if (document == null) {
            return "";
        }
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        if (normalizedKeyword != null && !normalizedKeyword.isBlank()) {
            String highlighted = highlightFirstMatch(document.getTitle(), normalizedKeyword);
            if (highlighted != null) {
                return highlighted;
            }
            highlighted = highlightFirstMatch(document.getSummary(), normalizedKeyword);
            if (highlighted != null) {
                return highlighted;
            }
            highlighted = highlightFirstMatch(document.getBodyText(), normalizedKeyword);
            if (highlighted != null) {
                return highlighted;
            }
        }
        return fallbackHighlightText(document);
    }

    private String highlightFirstMatch(String text, String keyword) {
        if (text == null || text.isBlank() || keyword == null || keyword.isBlank()) {
            return null;
        }
        int matchIndex = text.indexOf(keyword);
        if (matchIndex < 0) {
            return null;
        }
        int start = Math.max(0, matchIndex - HIGHLIGHT_CONTEXT_LENGTH);
        int end = Math.min(text.length(), matchIndex + keyword.length() + HIGHLIGHT_CONTEXT_LENGTH);
        return text.substring(start, matchIndex)
                + "<mark>"
                + text.substring(matchIndex, matchIndex + keyword.length())
                + "</mark>"
                + text.substring(matchIndex + keyword.length(), end);
    }

    private String fallbackHighlightText(DiscoverySearchDocument document) {
        String fallback = document.getSummary();
        if (fallback == null || fallback.isBlank()) {
            fallback = document.getTitle();
        }
        if (fallback == null) {
            return "";
        }
        return fallback.length() <= FALLBACK_LENGTH ? fallback : fallback.substring(0, FALLBACK_LENGTH);
    }

    private IndexCoordinates indexCoordinates() {
        return ElasticsearchOperationsSupport.indexCoordinates(properties.getIndexName());
    }
}
