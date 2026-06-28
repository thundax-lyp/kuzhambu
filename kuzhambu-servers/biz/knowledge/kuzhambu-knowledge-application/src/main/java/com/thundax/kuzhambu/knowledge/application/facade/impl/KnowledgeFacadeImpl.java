package com.thundax.kuzhambu.knowledge.application.facade.impl;

import com.thundax.kuzhambu.knowledge.application.facade.assembler.KnowledgeFacadeAssembler;
import com.thundax.kuzhambu.knowledge.application.report.service.KnowledgeReportApplicationService;
import com.thundax.kuzhambu.knowledge.facade.KnowledgeFacade;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeSummaryFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSummaryFacadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeFacadeImpl implements KnowledgeFacade {

    private final KnowledgeReportApplicationService knowledgeReportApplicationService;
    private final KnowledgeFacadeAssembler knowledgeFacadeAssembler;

    public KnowledgeFacadeImpl(
            KnowledgeReportApplicationService knowledgeReportApplicationService,
            KnowledgeFacadeAssembler knowledgeFacadeAssembler) {
        this.knowledgeReportApplicationService = knowledgeReportApplicationService;
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
}
