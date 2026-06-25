package com.thundax.kuzhambu.discovery.application.search.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiRequest;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiResult;
import com.thundax.kuzhambu.ai.domain.discovery.service.DiscoveryAiDomainService;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
import com.thundax.kuzhambu.discovery.application.search.service.QueryUnderstandingApplicationService;
import com.thundax.kuzhambu.discovery.application.search.support.DiscoveryKnowledgeEnhancementProvider;
import com.thundax.kuzhambu.discovery.application.search.support.QueryUnderstandingPayloadBuilder;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.QueryUnderstanding;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.domain.search.repository.QueryUnderstandingRepository;
import com.thundax.kuzhambu.discovery.domain.service.SearchDomainService;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class QueryUnderstandingApplicationServiceImpl implements QueryUnderstandingApplicationService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Long DEFAULT_SERVICE_ID = 0L;
    private static final String DEFAULT_SERVICE_ROLE = "discovery-query-understanding";
    private static final Long DEFAULT_MODEL_ID = 0L;
    private static final String DEFAULT_MODEL_NAME = "discovery-default";
    private static final Long DEFAULT_PROMPT_VERSION_ID = 0L;
    private static final String DEFAULT_LOCALE = "zh-CN";

    private final SearchDomainService searchDomainService = new SearchDomainService();
    private final QueryUnderstandingRepository queryUnderstandingRepository;
    private final DiscoveryKnowledgeEnhancementProvider knowledgeEnhancementProvider;
    private final QueryUnderstandingPayloadBuilder payloadBuilder;
    private final DiscoveryAiDomainService discoveryAiDomainService;

    public QueryUnderstandingApplicationServiceImpl(
            QueryUnderstandingRepository queryUnderstandingRepository,
            DiscoveryKnowledgeEnhancementProvider knowledgeEnhancementProvider,
            QueryUnderstandingPayloadBuilder payloadBuilder,
            DiscoveryAiDomainService discoveryAiDomainService) {
        this.queryUnderstandingRepository = queryUnderstandingRepository;
        this.knowledgeEnhancementProvider = knowledgeEnhancementProvider;
        this.payloadBuilder = payloadBuilder;
        this.discoveryAiDomainService = discoveryAiDomainService;
    }

    @Override
    public QueryUnderstandingResult understand(SearchQuery query) {
        if (query == null
                || query.getQueryText() == null
                || query.getQueryText().isBlank()) {
            throw new BizException("Search query is required");
        }
        String normalizedQueryText =
                searchDomainService.normalizeKeyword(query.getQueryText()).getNormalizedText();
        var enhancement = knowledgeEnhancementProvider.enhance(normalizedQueryText);
        try {
            DiscoveryAiRequest aiRequest = new DiscoveryAiRequest(
                    DEFAULT_SERVICE_ID,
                    DEFAULT_SERVICE_ROLE,
                    DEFAULT_MODEL_ID,
                    DEFAULT_MODEL_NAME,
                    DEFAULT_PROMPT_VERSION_ID,
                    query.getRequestId(),
                    query.getTraceId(),
                    payloadBuilder.buildPromptMessagesJson(query, normalizedQueryText, enhancement),
                    null,
                    null,
                    payloadBuilder.buildInputPayloadJson(query, normalizedQueryText, enhancement),
                    payloadBuilder.buildOutputSchemaJson(),
                    false,
                    true,
                    DEFAULT_LOCALE);
            DiscoveryAiResult aiResult = discoveryAiDomainService.understandQuery(aiRequest);
            QueryUnderstandingResult result = toResult(
                    normalizedQueryText,
                    enhancement.expandedSynonyms(),
                    enhancement.recognizedEntities(),
                    query,
                    aiResult);
            queryUnderstandingRepository.save(toSucceededEntity(query, result));
            return result;
        } catch (RuntimeException exception) {
            queryUnderstandingRepository.save(toFailedEntity(query, normalizedQueryText, enhancement, exception));
            throw exception;
        }
    }

    private QueryUnderstandingResult toResult(
            String normalizedQueryText,
            List<String> expandedSynonyms,
            List<QueryUnderstandingResult.RecognizedEntityResult> defaultRecognizedEntities,
            SearchQuery query,
            DiscoveryAiResult aiResult) {
        if (aiResult == null
                || aiResult.getResultPayload() == null
                || aiResult.getResultPayload().isBlank()) {
            return new QueryUnderstandingResult(
                    normalizedQueryText,
                    normalizedQueryText,
                    SearchIntentType.KEYWORD_SEARCH.value(),
                    safeList(expandedSynonyms),
                    safeList(defaultRecognizedEntities),
                    query.getRequestId(),
                    query.getTraceId());
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(aiResult.getResultPayload());
            String rewrittenQueryText =
                    textOrDefault(root, List.of("rewrittenQueryText", "rewrittenQuery"), normalizedQueryText);
            String intent = textOrDefault(root, List.of("intent"), SearchIntentType.KEYWORD_SEARCH.value());
            List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities =
                    parseRecognizedEntities(root.get("recognizedEntities"));
            if (recognizedEntities.isEmpty()) {
                recognizedEntities = safeList(defaultRecognizedEntities);
            }
            return new QueryUnderstandingResult(
                    normalizedQueryText,
                    rewrittenQueryText,
                    intent,
                    safeList(expandedSynonyms),
                    recognizedEntities,
                    query.getRequestId(),
                    query.getTraceId());
        } catch (JsonProcessingException exception) {
            throw new BizException(
                    "DISCOVERY-20004",
                    "discovery.search.query-understanding.result-parse-failed",
                    "Query understanding result parse failed",
                    exception);
        }
    }

    private QueryUnderstanding toSucceededEntity(SearchQuery query, QueryUnderstandingResult result) {
        return new QueryUnderstanding(
                null,
                null,
                null,
                query.getQueryText(),
                result.getNormalizedQueryText(),
                result.getRewrittenQueryText(),
                parseIntent(result.getIntent()),
                writeJson(result.getRecognizedEntities()),
                writeJson(result.getExpandedSynonyms()),
                "SUCCEEDED",
                null,
                null,
                query.getRequestId(),
                query.getTraceId(),
                new Date());
    }

    private QueryUnderstanding toFailedEntity(
            SearchQuery query,
            String normalizedQueryText,
            DiscoveryKnowledgeEnhancementProvider.KnowledgeEnhancementResult enhancement,
            RuntimeException exception) {
        String failureCode = exception instanceof BizException bizException && bizException.getCode() != null
                ? bizException.getCode()
                : "DISCOVERY-20002";
        return new QueryUnderstanding(
                null,
                null,
                null,
                query.getQueryText(),
                normalizedQueryText,
                normalizedQueryText,
                SearchIntentType.UNKNOWN,
                writeJson(enhancement.recognizedEntities()),
                writeJson(enhancement.expandedSynonyms()),
                "FAILED",
                failureCode,
                exception.getMessage(),
                query.getRequestId(),
                query.getTraceId(),
                new Date());
    }

    private List<QueryUnderstandingResult.RecognizedEntityResult> parseRecognizedEntities(JsonNode node)
            throws JsonProcessingException {
        if (node == null || node.isNull() || !node.isArray()) {
            return Collections.emptyList();
        }
        return OBJECT_MAPPER.readValue(
                OBJECT_MAPPER.writeValueAsString(node),
                new TypeReference<List<QueryUnderstandingResult.RecognizedEntityResult>>() {});
    }

    private String textOrDefault(JsonNode root, List<String> fieldNames, String defaultValue) {
        for (String fieldName : fieldNames) {
            JsonNode node = root.get(fieldName);
            if (node != null && !node.isNull() && !node.asText().isBlank()) {
                return node.asText();
            }
        }
        return defaultValue;
    }

    private SearchIntentType parseIntent(String value) {
        try {
            return value == null ? SearchIntentType.UNKNOWN : SearchIntentType.from(value);
        } catch (RuntimeException exception) {
            return SearchIntentType.UNKNOWN;
        }
    }

    private String writeJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(
                    "DISCOVERY-20005",
                    "discovery.search.query-understanding.persist-json-failed",
                    "Query understanding persistence json build failed",
                    exception);
        }
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }
}
