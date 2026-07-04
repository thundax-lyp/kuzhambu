package com.thundax.kuzhambu.classics.application.facade.impl;

import com.thundax.kuzhambu.classics.application.facade.assembler.ClassicsFacadeAssembler;
import com.thundax.kuzhambu.classics.application.report.service.ClassicsReportApplicationService;
import com.thundax.kuzhambu.classics.application.search.service.ClassicsSearchContentApplicationService;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassicsFacadeImpl implements ClassicsFacade {

    private final ClassicsReportApplicationService classicsReportApplicationService;
    private final ClassicsSearchContentApplicationService classicsSearchContentApplicationService;
    private final ClassicsFacadeAssembler classicsFacadeAssembler;

    public ClassicsFacadeImpl(
            ClassicsReportApplicationService classicsReportApplicationService,
            ClassicsSearchContentApplicationService classicsSearchContentApplicationService,
            ClassicsFacadeAssembler classicsFacadeAssembler) {
        this.classicsReportApplicationService = classicsReportApplicationService;
        this.classicsSearchContentApplicationService = classicsSearchContentApplicationService;
        this.classicsFacadeAssembler = classicsFacadeAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public ClassicsSummaryFacadeResponse summary(ClassicsSummaryFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return classicsFacadeAssembler.toFacadeResponse(classicsReportApplicationService.summary(
                request.getPeriodStart(), request.getPeriodEnd(), request.getBucketType()));
    }

    @Override
    @Transactional(readOnly = true)
    public ClassicsPublicContentsFacadeResponse listPublicContents() {
        return classicsFacadeAssembler.toPublicContentsFacadeResponse(
                classicsSearchContentApplicationService.listPublicContents());
    }

    @Override
    @Transactional(readOnly = true)
    public ClassicsPublicContentFacadeResponse getPublicContent(ClassicsPublicContentFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return classicsFacadeAssembler.toPublicContentFacadeResponse(
                classicsSearchContentApplicationService.getPublicContent(
                        request.getContentType(), request.getContentId()));
    }

    @Override
    @Transactional(readOnly = true)
    public ClassicsQaKnowledgeFacadeResponse getQaKnowledge(ClassicsQaKnowledgeFacadeRequest request) {
        return null;
    }
}
