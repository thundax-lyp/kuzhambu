package com.thundax.kuzhambu.discovery.application.search.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
import com.thundax.kuzhambu.discovery.application.search.support.DiscoveryKnowledgeEnhancementProvider;
import com.thundax.kuzhambu.discovery.application.search.support.QueryUnderstandingPayloadBuilder;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.QueryUnderstanding;
import com.thundax.kuzhambu.discovery.domain.search.repository.QueryUnderstandingRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class QueryUnderstandingApplicationServiceImplTest {

    @Test
    void understandShouldRejectNullQuery() {
        QueryUnderstandingApplicationServiceImpl service = new QueryUnderstandingApplicationServiceImpl(
                mock(QueryUnderstandingRepository.class),
                mock(DiscoveryKnowledgeEnhancementProvider.class),
                mock(QueryUnderstandingPayloadBuilder.class),
                mock(AiFacade.class));

        BizException exception = assertThrows(BizException.class, () -> service.understand(null));
        assertEquals("Search query is required", exception.getMessage());
    }

    @Test
    void understandShouldReturnDefaultResultAndSkipAiForBlankQuery() {
        QueryUnderstandingRepository repository = mock(QueryUnderstandingRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        QueryUnderstandingPayloadBuilder payloadBuilder = mock(QueryUnderstandingPayloadBuilder.class);
        AiFacade aiFacade = mock(AiFacade.class);
        QueryUnderstandingApplicationServiceImpl service =
                new QueryUnderstandingApplicationServiceImpl(repository, enhancementProvider, payloadBuilder, aiFacade);
        SearchQuery query = new SearchQuery(
                " ",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                1,
                20,
                "ANONYMOUS",
                null,
                "req-blank",
                "trace-blank");

        QueryUnderstandingResult result = service.understand(query);

        assertEquals("", result.getNormalizedQueryText());
        assertEquals("", result.getRewrittenQueryText());
        assertEquals("KEYWORD_SEARCH", result.getIntent());
        assertEquals("req-blank", result.getRequestId());
        assertEquals("trace-blank", result.getTraceId());
        verifyNoInteractions(repository, enhancementProvider, payloadBuilder, aiFacade);
    }

    @Test
    void understandShouldReturnStructuredResultAndPersistSuccess() {
        QueryUnderstandingRepository repository = mock(QueryUnderstandingRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        QueryUnderstandingPayloadBuilder payloadBuilder = mock(QueryUnderstandingPayloadBuilder.class);
        AiFacade aiFacade = mock(AiFacade.class);
        when(enhancementProvider.enhance("礼制"))
                .thenReturn(new com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult(
                        List.of("礼学", "典礼"),
                        null,
                        List.of(new QueryUnderstandingResult.RecognizedEntityResult("礼制", "TAG", "礼制"))));
        when(payloadBuilder.buildPromptMessagesJson(any(), any(), any())).thenReturn("[]");
        when(payloadBuilder.buildInputPayloadJson(any(), any(), any())).thenReturn("{\"query\":\"礼制\"}");
        when(payloadBuilder.buildOutputSchemaJson()).thenReturn("{\"type\":\"object\"}");
        when(aiFacade.understandDiscoveryQuery(any()))
                .thenReturn(DiscoveryAiFacadeResponse.builder()
                        .callId(101L)
                        .status("SUCCEEDED")
                        .capability("query_understanding")
                        .resultFormat("STRUCTURED")
                        .resultPayload("{\"intent\":\"NATURAL_LANGUAGE_SEARCH\",\"rewrittenQueryText\":\"礼制 礼学\"}")
                        .build());
        when(repository.save(any())).thenReturn(1L);

        QueryUnderstandingApplicationServiceImpl service =
                new QueryUnderstandingApplicationServiceImpl(repository, enhancementProvider, payloadBuilder, aiFacade);
        SearchQuery query = new SearchQuery(
                " 礼制 ",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                1,
                20,
                "ANONYMOUS",
                null,
                "req-1",
                "trace-1");

        QueryUnderstandingResult result = service.understand(query);
        ArgumentCaptor<QueryUnderstanding> captor = ArgumentCaptor.forClass(QueryUnderstanding.class);

        verify(repository).save(captor.capture());
        assertEquals("礼制", result.getNormalizedQueryText());
        assertEquals("礼制 礼学", result.getRewrittenQueryText());
        assertEquals("NATURAL_LANGUAGE_SEARCH", result.getIntent());
        assertEquals(List.of("礼学", "典礼"), result.getExpandedSynonyms());
        assertEquals("SUCCEEDED", captor.getValue().getUnderstandingStatus());
        assertEquals("礼制", captor.getValue().getNormalizedQueryText());
    }

    @Test
    void understandShouldPersistFailureAndReturnDefaultResultWhenAiFails() {
        QueryUnderstandingRepository repository = mock(QueryUnderstandingRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        QueryUnderstandingPayloadBuilder payloadBuilder = mock(QueryUnderstandingPayloadBuilder.class);
        AiFacade aiFacade = mock(AiFacade.class);
        when(enhancementProvider.enhance("礼制"))
                .thenReturn(new com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult(
                        List.of("礼学"), null, List.of()));
        when(payloadBuilder.buildPromptMessagesJson(any(), any(), any())).thenReturn("[]");
        when(payloadBuilder.buildInputPayloadJson(any(), any(), any())).thenReturn("{\"query\":\"礼制\"}");
        when(payloadBuilder.buildOutputSchemaJson()).thenReturn("{\"type\":\"object\"}");
        when(aiFacade.understandDiscoveryQuery(any()))
                .thenThrow(new BizException(
                        "DISCOVERY-29999", "discovery.search.query-understanding.ai-failed", "AI failed"));
        when(repository.save(any())).thenReturn(1L);

        QueryUnderstandingApplicationServiceImpl service =
                new QueryUnderstandingApplicationServiceImpl(repository, enhancementProvider, payloadBuilder, aiFacade);
        SearchQuery query = new SearchQuery(
                "礼制",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                1,
                20,
                "ANONYMOUS",
                null,
                "req-1",
                "trace-1");

        QueryUnderstandingResult result = service.understand(query);
        ArgumentCaptor<QueryUnderstanding> captor = ArgumentCaptor.forClass(QueryUnderstanding.class);

        verify(repository).save(captor.capture());
        assertEquals("礼制", result.getNormalizedQueryText());
        assertEquals("礼制", result.getRewrittenQueryText());
        assertEquals("KEYWORD_SEARCH", result.getIntent());
        assertEquals(List.of("礼学"), result.getExpandedSynonyms());
        assertEquals("FAILED", captor.getValue().getUnderstandingStatus());
        assertEquals("DISCOVERY-29999", captor.getValue().getFailureCode());
    }
}
