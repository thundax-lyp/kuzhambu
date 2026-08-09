package com.thundax.kuzhambu.discovery.application.search.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
import com.thundax.kuzhambu.discovery.application.search.service.QueryUnderstandingApplicationService;
import com.thundax.kuzhambu.discovery.application.search.support.DiscoveryKnowledgeEnhancementProvider;
import com.thundax.kuzhambu.discovery.application.search.support.QueryUnderstandingPayloadBuilder;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.QueryUnderstanding;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.domain.search.repository.QueryUnderstandingRepository;
import com.thundax.kuzhambu.discovery.domain.search.support.SearchQueryNormalizer;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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

    private final SearchQueryNormalizer searchQueryNormalizer = new SearchQueryNormalizer();
    private final QueryUnderstandingRepository queryUnderstandingRepository;
    private final DiscoveryKnowledgeEnhancementProvider knowledgeEnhancementProvider;
    private final QueryUnderstandingPayloadBuilder payloadBuilder;
    private final AiFacade aiFacade;

    public QueryUnderstandingApplicationServiceImpl(
            QueryUnderstandingRepository queryUnderstandingRepository,
            DiscoveryKnowledgeEnhancementProvider knowledgeEnhancementProvider,
            QueryUnderstandingPayloadBuilder payloadBuilder,
            AiFacade aiFacade) {
        this.queryUnderstandingRepository = queryUnderstandingRepository;
        this.knowledgeEnhancementProvider = knowledgeEnhancementProvider;
        this.payloadBuilder = payloadBuilder;
        this.aiFacade = aiFacade;
    }

    @Override
    public QueryUnderstandingResult understand(SearchQuery query) {
        if (query == null) {
            throw new BizException("Search query is required");
        }
        if (StringUtils.isBlank(query.getQueryText())) {
            return new QueryUnderstandingResult(
                    "",
                    "",
                    SearchIntentType.KEYWORD_SEARCH.value(),
                    List.of(),
                    RequestIdCodec.toValue(query.getRequestId()),
                    TraceIdCodec.toValue(query.getTraceId()));
        }
        String normalizedQueryText =
                searchQueryNormalizer.normalizeKeyword(query.getQueryText()).getNormalizedText();
        var enhancement = knowledgeEnhancementProvider.enhance(normalizedQueryText);
        try {
            DiscoveryAiFacadeRequest aiRequest = DiscoveryAiFacadeRequest.builder()
                    .serviceId(DEFAULT_SERVICE_ID)
                    .serviceRole(DEFAULT_SERVICE_ROLE)
                    .modelId(DEFAULT_MODEL_ID)
                    .modelName(DEFAULT_MODEL_NAME)
                    .promptVersionId(DEFAULT_PROMPT_VERSION_ID)
                    .requestId(RequestIdCodec.toValue(query.getRequestId()))
                    .traceId(TraceIdCodec.toValue(query.getTraceId()))
                    .promptMessagesJson(payloadBuilder.buildPromptMessagesJson(query, normalizedQueryText, enhancement))
                    .inputPayloadJson(payloadBuilder.buildInputPayloadJson(query, normalizedQueryText, enhancement))
                    .outputSchemaJson(payloadBuilder.buildOutputSchemaJson())
                    .stream(false)
                    .forceJson(true)
                    .locale(DEFAULT_LOCALE)
                    .build();
            DiscoveryAiFacadeResponse aiResult = aiFacade.understandDiscoveryQuery(aiRequest);
            ensureAiSucceeded(aiResult, "DISCOVERY-20003", "discovery.search.query-understanding.ai-failed");
            QueryUnderstandingResult result =
                    toResult(normalizedQueryText, enhancement.recognizedEntities(), query, aiResult);
            queryUnderstandingRepository.save(toSucceededEntity(query, result));
            return result;
        } catch (RuntimeException exception) {
            queryUnderstandingRepository.save(toFailedEntity(query, normalizedQueryText, enhancement, exception));
            return toDefaultResult(normalizedQueryText, enhancement, query);
        }
    }

    private QueryUnderstandingResult toDefaultResult(
            String normalizedQueryText,
            com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult enhancement,
            SearchQuery query) {
        return new QueryUnderstandingResult(
                normalizedQueryText,
                normalizedQueryText,
                SearchIntentType.KEYWORD_SEARCH.value(),
                safeList(enhancement.recognizedEntities()),
                RequestIdCodec.toValue(query.getRequestId()),
                TraceIdCodec.toValue(query.getTraceId()));
    }

    private QueryUnderstandingResult toResult(
            String normalizedQueryText,
            List<QueryUnderstandingResult.RecognizedEntityResult> defaultRecognizedEntities,
            SearchQuery query,
            DiscoveryAiFacadeResponse aiResult) {
        if (aiResult == null
                || aiResult.getResultPayload() == null
                || aiResult.getResultPayload().isBlank()) {
            return new QueryUnderstandingResult(
                    normalizedQueryText,
                    normalizedQueryText,
                    SearchIntentType.KEYWORD_SEARCH.value(),
                    safeList(defaultRecognizedEntities),
                    RequestIdCodec.toValue(query.getRequestId()),
                    TraceIdCodec.toValue(query.getTraceId()));
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(aiResult.getResultPayload());
            String rewrittenQueryText =
                    textOrDefault(root, List.of("rewrittenQueryText", "rewrittenQuery"), normalizedQueryText);
            String intent =
                    textOrDefault(root, List.of("intentType", "intent"), SearchIntentType.KEYWORD_SEARCH.value());
            List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities =
                    parseRecognizedEntities(root.get("recognizedEntities"));
            if (recognizedEntities.isEmpty()) {
                recognizedEntities = safeList(defaultRecognizedEntities);
            }
            return new QueryUnderstandingResult(
                    normalizedQueryText,
                    rewrittenQueryText,
                    intent,
                    recognizedEntities,
                    RequestIdCodec.toValue(query.getRequestId()),
                    TraceIdCodec.toValue(query.getTraceId()));
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
                "SUCCEEDED",
                null,
                null,
                RequestIdCodec.toValue(query.getRequestId()),
                TraceIdCodec.toValue(query.getTraceId()),
                Instant.now());
    }

    private QueryUnderstanding toFailedEntity(
            SearchQuery query,
            String normalizedQueryText,
            com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult enhancement,
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
                "FAILED",
                failureCode,
                exception.getMessage(),
                RequestIdCodec.toValue(query.getRequestId()),
                TraceIdCodec.toValue(query.getTraceId()),
                Instant.now());
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

    private void ensureAiSucceeded(DiscoveryAiFacadeResponse aiResult, String defaultCode, String defaultMessageKey) {
        if (aiResult == null) {
            throw new BizException(defaultCode, defaultMessageKey, "Discovery AI result is missing");
        }
        if (!"SUCCEEDED".equalsIgnoreCase(aiResult.getStatus())) {
            String code = StringUtils.defaultIfBlank(aiResult.getErrorType(), defaultCode);
            String message =
                    StringUtils.defaultIfBlank(aiResult.getErrorMessage(), "Discovery AI invocation did not succeed");
            throw new BizException(code, defaultMessageKey, message);
        }
    }
}
