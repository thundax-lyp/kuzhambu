package com.thundax.kuzhambu.knowledge.facade;

import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeDiscoveryTermFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeSummaryFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeEntityHintsFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSummaryFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSynonymExpandFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeTagHintFacadeResponse;

public interface KnowledgeFacade {

    KnowledgeSummaryFacadeResponse summary(KnowledgeSummaryFacadeRequest request);

    KnowledgeSynonymExpandFacadeResponse expandSynonyms(KnowledgeDiscoveryTermFacadeRequest request);

    KnowledgeTagHintFacadeResponse getTagHint(KnowledgeDiscoveryTermFacadeRequest request);

    KnowledgeEntityHintsFacadeResponse listEntityHints(KnowledgeDiscoveryTermFacadeRequest request);
}
