package com.thundax.kuzhambu.discovery.application.search.result;

import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeTagHintFacadeResponse;
import java.util.List;

public record KnowledgeEnhancementResult(
        List<String> expandedSynonyms,
        KnowledgeTagHintFacadeResponse tagHint,
        List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities) {}
