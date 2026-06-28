package com.thundax.kuzhambu.knowledge.facade;

import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeContentTagRefFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeDiscoveryTermFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeRemoveContentTagRefFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeResolveTagFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeSummaryFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeEntityHintsFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSummaryFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSynonymExpandFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeTagFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeTagHintFacadeResponse;

public interface KnowledgeFacade {

    KnowledgeSummaryFacadeResponse summary(KnowledgeSummaryFacadeRequest request);

    KnowledgeSynonymExpandFacadeResponse expandSynonyms(KnowledgeDiscoveryTermFacadeRequest request);

    KnowledgeTagHintFacadeResponse getTagHint(KnowledgeDiscoveryTermFacadeRequest request);

    KnowledgeEntityHintsFacadeResponse listEntityHints(KnowledgeDiscoveryTermFacadeRequest request);

    KnowledgeTagFacadeResponse resolveOrCreateManualTag(KnowledgeResolveTagFacadeRequest request);

    KnowledgeTagFacadeResponse resolveOrCreateAiTag(KnowledgeResolveTagFacadeRequest request);

    void syncContentTagRef(KnowledgeContentTagRefFacadeRequest request);

    void removeContentTagRef(KnowledgeRemoveContentTagRefFacadeRequest request);
}
