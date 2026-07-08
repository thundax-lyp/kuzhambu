package com.thundax.kuzhambu.discovery.application.search.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.facade.KnowledgeFacade;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeDiscoveryTermFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeSynonymQueryFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeEntityHintsFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSynonymQueryFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeTagHintFacadeResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DiscoveryKnowledgeEnhancementProviderTest {

    @Test
    void enhanceShouldQueryBidirectionalSynonyms() {
        KnowledgeFacade knowledgeFacade = mock(KnowledgeFacade.class);
        when(knowledgeFacade.querySynonyms(argThat(request -> "礼制".equals(request.getTerm())
                        && "BIDIRECTIONAL".equals(request.getDirection())
                        && Integer.valueOf(50).equals(request.getLimit()))))
                .thenReturn(KnowledgeSynonymQueryFacadeResponse.builder()
                        .term("礼制")
                        .normalizedTerm("礼制")
                        .direction("BIDIRECTIONAL")
                        .limit(50)
                        .matches(List.of())
                        .expandedTerms(List.of("礼学", "典礼"))
                        .build());
        when(knowledgeFacade.getTagHint(
                        KnowledgeDiscoveryTermFacadeRequest.builder().term("礼制").build()))
                .thenReturn(mock(KnowledgeTagHintFacadeResponse.class));
        when(knowledgeFacade.listEntityHints(
                        KnowledgeDiscoveryTermFacadeRequest.builder().term("礼制").build()))
                .thenReturn(KnowledgeEntityHintsFacadeResponse.builder()
                        .entityHints(List.of())
                        .build());

        DiscoveryKnowledgeEnhancementProvider provider = new DiscoveryKnowledgeEnhancementProvider(knowledgeFacade);
        DiscoveryKnowledgeEnhancementProvider.KnowledgeEnhancementResult enhancement = provider.enhance("礼制");

        assertEquals(List.of("礼学", "典礼"), enhancement.expandedSynonyms());
        assertEquals(0, enhancement.recognizedEntities().size());

        ArgumentCaptor<KnowledgeSynonymQueryFacadeRequest> requestCaptor =
                ArgumentCaptor.forClass(KnowledgeSynonymQueryFacadeRequest.class);
        verify(knowledgeFacade).querySynonyms(requestCaptor.capture());
        assertEquals("礼制", requestCaptor.getValue().getTerm());
        assertEquals("BIDIRECTIONAL", requestCaptor.getValue().getDirection());
        assertEquals(Integer.valueOf(50), requestCaptor.getValue().getLimit());
    }
}
