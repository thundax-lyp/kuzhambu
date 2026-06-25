package com.thundax.kuzhambu.discovery.application.search.support;

import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryEntityHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryTagHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.KnowledgeTaxonomyReadApplicationService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryKnowledgeEnhancementProvider {

    private final KnowledgeTaxonomyReadApplicationService knowledgeTaxonomyReadApplicationService;

    public DiscoveryKnowledgeEnhancementProvider(
            KnowledgeTaxonomyReadApplicationService knowledgeTaxonomyReadApplicationService) {
        this.knowledgeTaxonomyReadApplicationService = knowledgeTaxonomyReadApplicationService;
    }

    public KnowledgeEnhancementResult enhance(String term) {
        var synonymExpandResult = knowledgeTaxonomyReadApplicationService.expandSynonyms(term);
        DiscoveryTagHintResult tagHintResult = knowledgeTaxonomyReadApplicationService.getTagHint(term);
        List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities =
                mapRecognizedEntities(knowledgeTaxonomyReadApplicationService.listEntityHints(term));
        List<String> expandedSynonyms = synonymExpandResult == null || synonymExpandResult.getExpandedTerms() == null
                ? Collections.emptyList()
                : synonymExpandResult.getExpandedTerms();
        return new KnowledgeEnhancementResult(expandedSynonyms, tagHintResult, recognizedEntities);
    }

    private List<QueryUnderstandingResult.RecognizedEntityResult> mapRecognizedEntities(
            List<DiscoveryEntityHintResult> entityHints) {
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
            DiscoveryTagHintResult tagHint,
            List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities) {}
}
