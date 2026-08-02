package com.thundax.kuzhambu.discovery.infra.client;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.elasticsearch.support.ElasticsearchOperationsSupport;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPageResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPreviewResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationCandidateResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationCategoryAggregationResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationDocument;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationProbeResult;
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
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
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
    private static final int CATEGORY_AGGREGATION_BUCKET_SIZE = 1000;
    private static final String READY_CATEGORY_AGGREGATION = "ready_categories";
    private static final String REPRESENTATIVE_HIT_AGGREGATION = "representative_hit";
    private static final String PUBLICATION_PREPARING = "PREPARING";
    private static final String PUBLICATION_READY = "READY";
    private static final String PUBLICATION_OFFLINE = "OFFLINE";

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
        criteria = appendReadyPublicationFilter(criteria);
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
        saveDocuments(operations, toDocuments(sourceContents));
    }

    @Override
    public void upsertDocuments(List<SearchSourceContent> sourceContents) {
        ElasticsearchOperations operations = requireOperations("upsert");
        List<DiscoverySearchDocument> documents = toDocuments(sourceContents);
        saveDocuments(operations, documents);
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
            String contentType, String contentId, Integer currentVersionNo, Instant occurredAt) {
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
        deletedDocument.setDeletedAt(occurredAt);
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

    @Override
    public void preparePublication(SearchPublicationDocument document) {
        ElasticsearchOperations operations = requireOperations("publication-prepare");
        DiscoverySearchDocument target = new DiscoverySearchDocument();
        target.setDocumentId(document.getSourceId());
        target.setContentDomain("CLASSICS");
        target.setContentType(document.getContentType());
        target.setContentId(document.getContentId());
        target.setContentVersionId(document.getContentVersionId());
        target.setContentVersionNo(document.getContentVersionNo());
        target.setSourceVersionNo(document.getContentVersionNo());
        target.setKnowledgeBase(document.getContentType());
        target.setCategoryCode(document.getCategoryId());
        target.setCategoryName(document.getCategoryName());
        target.setVolumeId(document.getVolumeId());
        target.setVolumeTitle(document.getVolumeTitle());
        target.setTitle(document.getTitle());
        target.setSummary(document.getSummary());
        target.setTextSegments(document.getTextSegments());
        target.setBodyText(joinTextSegments(document.getTextSegments()));
        target.setTagNames(document.getTagNames());
        target.setPublicationStatus(PUBLICATION_PREPARING);
        target.setDeleted(Boolean.FALSE);
        target.setDeletedAt(null);
        target.setUpdatedAt(document.getContentUpdatedAt());
        target.setSourcePath(buildSourcePath(document.getContentType(), document.getContentId()));
        operations.save(target, indexCoordinates());
    }

    @Override
    public SearchPublicationProbeResult probePublication(String documentId) {
        ElasticsearchOperations operations = requireOperations("publication-probe");
        DiscoverySearchDocument document =
                operations.get(documentId, DiscoverySearchDocument.class, indexCoordinates());
        if (document == null) {
            return SearchPublicationProbeResult.missing();
        }
        return new SearchPublicationProbeResult(
                true,
                document.getPublicationStatus(),
                document.getDeleted(),
                document.getContentVersionId(),
                document.getContentVersionNo());
    }

    @Override
    public boolean markPublicationReady(String documentId, String contentVersionId, Integer contentVersionNo) {
        ElasticsearchOperations operations = requireOperations("publication-ready");
        DiscoverySearchDocument document =
                operations.get(documentId, DiscoverySearchDocument.class, indexCoordinates());
        if (!matchesVersion(document, contentVersionId, contentVersionNo)) {
            return false;
        }
        document.setPublicationStatus(PUBLICATION_READY);
        document.setDeleted(Boolean.FALSE);
        document.setDeletedAt(null);
        operations.save(document, indexCoordinates());
        return true;
    }

    @Override
    public boolean markPublicationOffline(String documentId, Instant occurredAt) {
        ElasticsearchOperations operations = requireOperations("publication-offline");
        DiscoverySearchDocument document =
                operations.get(documentId, DiscoverySearchDocument.class, indexCoordinates());
        if (document == null) {
            return true;
        }
        document.setPublicationStatus(PUBLICATION_OFFLINE);
        document.setDeleted(Boolean.TRUE);
        document.setDeletedAt(occurredAt);
        operations.save(document, indexCoordinates());
        return true;
    }

    @Override
    public void deletePublication(String documentId) {
        requireOperations("publication-delete").delete(documentId, indexCoordinates());
    }

    @Override
    public PageResult<SearchPublicationCandidateResult> pageReadyPublicationCandidates(
            String contentType, String categoryId, String volumeId, String keyword, int pageNo, int pageSize) {
        ElasticsearchOperations operations = requireOperations("publication-candidates");
        Criteria criteria = buildPublicationCandidateCriteria(contentType, categoryId, volumeId, keyword);
        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(Math.max(pageNo - 1, 0), pageSize));
        SearchHits<DiscoverySearchDocument> searchHits =
                operations.search(query, DiscoverySearchDocument.class, indexCoordinates());
        List<SearchPublicationCandidateResult> records = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .filter(document -> document != null && document.getContentId() != null)
                .map(document -> new SearchPublicationCandidateResult(
                        document.getContentType(),
                        document.getContentId(),
                        document.getCategoryCode(),
                        document.getVolumeId()))
                .toList();
        return PageResult.of(
                pageNo, pageSize, ElasticsearchOperationsSupport.toIntTotalHits(searchHits.getTotalHits()), records);
    }

    @Override
    public List<SearchPublicationCategoryAggregationResult> listReadyPublicationCategoryAggregations(
            String contentType) {
        ElasticsearchOperations operations = requireOperations("publication-category-aggregations");
        NativeQuery query = NativeQuery.builder()
                .withQuery(buildReadyPublicationNativeQuery(contentType))
                .withAggregation(READY_CATEGORY_AGGREGATION, Aggregation.of(aggregation -> aggregation
                        .terms(terms -> terms.field("categoryCode").size(CATEGORY_AGGREGATION_BUCKET_SIZE))
                        .aggregations(REPRESENTATIVE_HIT_AGGREGATION, representativeHitAggregation())))
                .withMaxResults(0)
                .build();
        SearchHits<DiscoverySearchDocument> searchHits =
                operations.search(query, DiscoverySearchDocument.class, indexCoordinates());
        return toCategoryAggregationResults(searchHits);
    }

    private ElasticsearchOperations requireOperations(String operation) {
        return ElasticsearchOperationsSupport.requireOperations(
                elasticsearchOperations, "Discovery search", operation, properties.getIndexName());
    }

    private boolean matchesVersion(
            DiscoverySearchDocument document, String contentVersionId, Integer contentVersionNo) {
        return document != null
                && java.util.Objects.equals(document.getContentVersionId(), contentVersionId)
                && java.util.Objects.equals(document.getContentVersionNo(), contentVersionNo);
    }

    private Query buildReadyPublicationNativeQuery(String contentType) {
        return Query.of(query -> query.bool(bool -> {
            bool.filter(termQuery("publicationStatus", PUBLICATION_READY));
            bool.filter(termQuery("deleted", false));
            if (contentType != null && !contentType.isBlank()) {
                bool.filter(termQuery("contentType", contentType));
            }
            return bool;
        }));
    }

    private Query termQuery(String fieldName, String value) {
        return Query.of(query -> query.term(term -> term.field(fieldName).value(value)));
    }

    private Query termQuery(String fieldName, boolean value) {
        return Query.of(query -> query.term(term -> term.field(fieldName).value(value)));
    }

    private Aggregation representativeHitAggregation() {
        return Aggregation.of(aggregation -> aggregation.topHits(
                topHits -> topHits.size(1).source(source -> source.filter(filter -> filter.includes("contentId")))));
    }

    private List<SearchPublicationCategoryAggregationResult> toCategoryAggregationResults(
            SearchHits<DiscoverySearchDocument> searchHits) {
        if (searchHits == null || !(searchHits.getAggregations() instanceof ElasticsearchAggregations aggregations)) {
            return List.of();
        }
        var categoryAggregation = aggregations.get(READY_CATEGORY_AGGREGATION);
        Aggregate aggregate = categoryAggregation == null
                ? null
                : categoryAggregation.aggregation().getAggregate();
        if (aggregate == null || !aggregate.isSterms()) {
            return List.of();
        }
        return aggregate.sterms().buckets().array().stream()
                .filter(Objects::nonNull)
                .map(this::toCategoryAggregationResult)
                .filter(Objects::nonNull)
                .toList();
    }

    private SearchPublicationCategoryAggregationResult toCategoryAggregationResult(StringTermsBucket bucket) {
        if (bucket.key() == null || !bucket.key().isString()) {
            return null;
        }
        return new SearchPublicationCategoryAggregationResult(
                bucket.key().stringValue(), bucket.docCount(), representativeContentId(bucket));
    }

    private String representativeContentId(StringTermsBucket bucket) {
        Aggregate aggregate = bucket.aggregations().get(REPRESENTATIVE_HIT_AGGREGATION);
        if (aggregate == null || !aggregate.isTopHits()) {
            return null;
        }
        var hits = aggregate.topHits().hits();
        if (hits == null || hits.hits() == null || hits.hits().isEmpty()) {
            return null;
        }
        JsonData source = hits.hits().get(0).source();
        if (source == null) {
            return null;
        }
        Object contentId = source.to(Map.class).get("contentId");
        return contentId == null ? null : String.valueOf(contentId);
    }

    private String joinTextSegments(List<String> textSegments) {
        if (textSegments == null || textSegments.isEmpty()) {
            return null;
        }
        return textSegments.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .reduce((left, right) -> left + "\n" + right)
                .orElse(null);
    }

    private String buildSourcePath(String contentType, String contentId) {
        return switch (contentType) {
            case "SANCAI_ENTRY" -> "/classics/sancai/" + contentId;
            case "WANGQI_DOCUMENT" -> "/classics/wangqi/" + contentId;
            case "MING_CUSTOMS" -> "/classics/ming-customs/" + contentId;
            default ->
                throw new UnsupportedOperationException("Unknown discovery search source content type: " + contentType);
        };
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
        ElasticsearchOperationsSupport.saveInBatches(
                operations, documents, indexCoordinates(), properties.getBatchSize());
    }

    private Criteria buildCriteria(String keyword, SearchScope searchScope) {
        Criteria criteria = baseKeywordCriteria(keyword);
        if (searchScope == null) {
            return appendReadyPublicationFilter(criteria);
        }
        criteria = appendInFilter(criteria, "knowledgeBase", searchScope.getKnowledgeBases());
        criteria = appendInFilter(criteria, "categoryCode", searchScope.getCategoryCodes());
        criteria = appendInFilter(criteria, "tagNames", searchScope.getTagNames());
        criteria = appendReadyPublicationFilter(criteria);
        if (searchScope.getDateFrom() != null) {
            criteria = criteria.and(new Criteria("updatedAt").greaterThanEqual(searchScope.getDateFrom()));
        }
        if (searchScope.getDateTo() != null) {
            criteria = criteria.and(new Criteria("updatedAt").lessThanEqual(searchScope.getDateTo()));
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

    private Criteria appendReadyPublicationFilter(Criteria criteria) {
        return criteria.and(new Criteria("publicationStatus").is(PUBLICATION_READY))
                .and(new Criteria("deleted").is(false));
    }

    private Criteria buildPublicationCandidateCriteria(
            String contentType, String categoryId, String volumeId, String keyword) {
        Criteria criteria = baseKeywordCriteria(keyword);
        criteria = appendReadyPublicationFilter(criteria);
        if (contentType != null && !contentType.isBlank()) {
            criteria = criteria.and(new Criteria("contentType").is(contentType));
        }
        if (categoryId != null && !categoryId.isBlank()) {
            criteria = criteria.and(new Criteria("categoryCode").is(categoryId));
        }
        if (volumeId != null && !volumeId.isBlank()) {
            criteria = criteria.and(new Criteria("volumeId").is(volumeId));
        }
        return criteria;
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
