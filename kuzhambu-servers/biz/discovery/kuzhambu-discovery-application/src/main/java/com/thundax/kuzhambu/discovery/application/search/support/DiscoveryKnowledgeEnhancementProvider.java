package com.thundax.kuzhambu.discovery.application.search.support;

import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
import com.thundax.kuzhambu.knowledge.facade.KnowledgeFacade;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeEntityHintFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeDiscoveryTermFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeSynonymQueryFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSynonymQueryFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeTagHintFacadeResponse;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryKnowledgeEnhancementProvider {

    private final KnowledgeFacade knowledgeFacade;

    public DiscoveryKnowledgeEnhancementProvider(KnowledgeFacade knowledgeFacade) {
        this.knowledgeFacade = knowledgeFacade;
    }

    public KnowledgeEnhancementResult enhance(String term) {
        KnowledgeDiscoveryTermFacadeRequest request =
                KnowledgeDiscoveryTermFacadeRequest.builder().term(term).build();
        KnowledgeSynonymQueryFacadeRequest synonymQueryRequest = KnowledgeSynonymQueryFacadeRequest.builder()
                .term(term)
                .direction("BIDIRECTIONAL")
                .limit(50)
                .build();
        KnowledgeSynonymQueryFacadeResponse synonymQueryResult = knowledgeFacade.querySynonyms(synonymQueryRequest);
        KnowledgeTagHintFacadeResponse tagHintResult = knowledgeFacade.getTagHint(request);
        var entityHintsResponse = knowledgeFacade.listEntityHints(request);
        List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities =
                mapRecognizedEntities(entityHintsResponse == null ? null : entityHintsResponse.getEntityHints());
        List<String> expandedSynonyms = synonymQueryResult == null || synonymQueryResult.getExpandedTerms() == null
                ? Collections.emptyList()
                : synonymQueryResult.getExpandedTerms();
        return new KnowledgeEnhancementResult(expandedSynonyms, tagHintResult, recognizedEntities);
    }

    private List<QueryUnderstandingResult.RecognizedEntityResult> mapRecognizedEntities(
            List<KnowledgeEntityHintFacadeDto> entityHints) {
        if (entityHints == null || entityHints.isEmpty()) {
            return Collections.emptyList();
        }
        return entityHints.stream()
                .map(item -> new QueryUnderstandingResult.RecognizedEntityResult(
                        item.getEntityName(), item.getEntityType(), item.getTerm()))
                .toList();
    }

    public record KnowledgeEnhancementResult(
            List<String> expandedSynonyms,
            KnowledgeTagHintFacadeResponse tagHint,
            List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities) {}
}
