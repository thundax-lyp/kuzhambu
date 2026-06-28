package com.thundax.kuzhambu.knowledge.application.facade.impl;

import com.thundax.kuzhambu.knowledge.application.facade.assembler.KnowledgeFacadeAssembler;
import com.thundax.kuzhambu.knowledge.application.report.service.KnowledgeReportApplicationService;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.KnowledgeTaxonomyReadApplicationService;
import com.thundax.kuzhambu.knowledge.domain.service.KnowledgeTagBindingDomainService;
import com.thundax.kuzhambu.knowledge.facade.KnowledgeFacade;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeFacadeImpl implements KnowledgeFacade {

    private final KnowledgeReportApplicationService knowledgeReportApplicationService;
    private final KnowledgeTaxonomyReadApplicationService knowledgeTaxonomyReadApplicationService;
    private final KnowledgeTagBindingDomainService knowledgeTagBindingDomainService;
    private final KnowledgeFacadeAssembler knowledgeFacadeAssembler;

    public KnowledgeFacadeImpl(
            KnowledgeReportApplicationService knowledgeReportApplicationService,
            KnowledgeTaxonomyReadApplicationService knowledgeTaxonomyReadApplicationService,
            KnowledgeTagBindingDomainService knowledgeTagBindingDomainService,
            KnowledgeFacadeAssembler knowledgeFacadeAssembler) {
        this.knowledgeReportApplicationService = knowledgeReportApplicationService;
        this.knowledgeTaxonomyReadApplicationService = knowledgeTaxonomyReadApplicationService;
        this.knowledgeTagBindingDomainService = knowledgeTagBindingDomainService;
        this.knowledgeFacadeAssembler = knowledgeFacadeAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeSummaryFacadeResponse summary(KnowledgeSummaryFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return knowledgeFacadeAssembler.toSummaryResponse(knowledgeReportApplicationService.summary(
                request.getPeriodStart(), request.getPeriodEnd(), request.getBucketType()));
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeSynonymExpandFacadeResponse expandSynonyms(KnowledgeDiscoveryTermFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return knowledgeFacadeAssembler.toSynonymExpandResponse(
                knowledgeTaxonomyReadApplicationService.expandSynonyms(request.getTerm()));
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeTagHintFacadeResponse getTagHint(KnowledgeDiscoveryTermFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return knowledgeFacadeAssembler.toTagHintResponse(
                knowledgeTaxonomyReadApplicationService.getTagHint(request.getTerm()));
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeEntityHintsFacadeResponse listEntityHints(KnowledgeDiscoveryTermFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return knowledgeFacadeAssembler.toEntityHintsResponse(
                knowledgeTaxonomyReadApplicationService.listEntityHints(request.getTerm()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeTagFacadeResponse resolveOrCreateManualTag(KnowledgeResolveTagFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return knowledgeFacadeAssembler.toTagResponse(
                knowledgeTagBindingDomainService.resolveOrCreateManualTag(request.getTagName()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeTagFacadeResponse resolveOrCreateAiTag(KnowledgeResolveTagFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return knowledgeFacadeAssembler.toTagResponse(
                knowledgeTagBindingDomainService.resolveOrCreateAiTag(request.getTagName()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncContentTagRef(KnowledgeContentTagRefFacadeRequest request) {
        if (request == null) {
            return;
        }
        knowledgeTagBindingDomainService.syncContentTagRef(
                knowledgeFacadeAssembler.toTagId(request),
                knowledgeFacadeAssembler.toContentType(request),
                request.getContentId(),
                request.getContentTitle(),
                knowledgeFacadeAssembler.toTagSource(request));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeContentTagRef(KnowledgeRemoveContentTagRefFacadeRequest request) {
        if (request == null) {
            return;
        }
        knowledgeTagBindingDomainService.removeContentTagRef(
                knowledgeFacadeAssembler.toTagId(request),
                knowledgeFacadeAssembler.toContentType(request),
                request.getContentId());
    }
}
