package com.thundax.kuzhambu.classics.application.facade.impl;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.facade.assembler.ClassicsFacadeAssembler;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.application.report.service.ClassicsReportApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.application.search.service.ClassicsSearchContentApplicationService;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassicsFacadeImpl implements ClassicsFacade {

    private final ClassicsContentApplicationService classicsContentApplicationService;
    private final ClassicsReportApplicationService classicsReportApplicationService;
    private final ClassicsSearchContentApplicationService classicsSearchContentApplicationService;
    private final SancaiApplicationService sancaiApplicationService;
    private final WangqiDocumentApplicationService wangqiDocumentApplicationService;
    private final MingCustomsApplicationService mingCustomsApplicationService;
    private final ClassicsFacadeAssembler classicsFacadeAssembler;

    public ClassicsFacadeImpl(
            ClassicsContentApplicationService classicsContentApplicationService,
            ClassicsReportApplicationService classicsReportApplicationService,
            ClassicsSearchContentApplicationService classicsSearchContentApplicationService,
            SancaiApplicationService sancaiApplicationService,
            WangqiDocumentApplicationService wangqiDocumentApplicationService,
            MingCustomsApplicationService mingCustomsApplicationService,
            ClassicsFacadeAssembler classicsFacadeAssembler) {
        this.classicsContentApplicationService = classicsContentApplicationService;
        this.classicsReportApplicationService = classicsReportApplicationService;
        this.classicsSearchContentApplicationService = classicsSearchContentApplicationService;
        this.sancaiApplicationService = sancaiApplicationService;
        this.wangqiDocumentApplicationService = wangqiDocumentApplicationService;
        this.mingCustomsApplicationService = mingCustomsApplicationService;
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
        if (request == null
                || request.getContentType() == null
                || request.getContentType().isBlank()
                || request.getContentId() == null
                || request.getContentId().isBlank()) {
            return null;
        }
        Long contentId = parseContentId(request.getContentId());
        if (contentId == null) {
            return null;
        }
        ClassicsContentType contentType = ClassicsContentType.from(request.getContentType());
        ClassicsContentId domainContentId = ClassicsContentIdCodec.toDomain(contentId);
        return switch (contentType) {
            case SANCAI_ENTRY ->
                classicsFacadeAssembler.toQaKnowledgeFacadeResponse(
                        getSancaiQaKnowledge(request.getContentType(), request.getContentId(), domainContentId));
            case WANGQI_DOCUMENT ->
                classicsFacadeAssembler.toQaKnowledgeFacadeResponse(
                        getWangqiQaKnowledge(request.getContentType(), request.getContentId(), domainContentId));
            case MING_CUSTOMS ->
                classicsFacadeAssembler.toQaKnowledgeFacadeResponse(
                        getMingCustomsQaKnowledge(request.getContentType(), request.getContentId(), domainContentId));
        };
    }

    private ClassicsQaKnowledgeFacadeDto getSancaiQaKnowledge(
            String contentType, String contentId, ClassicsContentId domainContentId) {
        var sourceContent = classicsSearchContentApplicationService.getPublicContent(contentType, contentId);
        if (sourceContent == null) {
            return null;
        }
        SancaiEntry entry = sancaiApplicationService.getEntry(SancaiEntryIdCodec.toDomain(domainContentId.value()));
        if (entry == null) {
            return null;
        }
        List<ClassicsContentTag> tags = classicsContentApplicationService.listTags(contentType, domainContentId);
        List<ClassicsContentQaPair> qaPairs =
                classicsContentApplicationService.listQaPairs(contentType, domainContentId);
        return classicsFacadeAssembler.toQaKnowledgeFacadeDto(
                sourceContent, entry.getOriginalText(), entry.getTranslationText(), null, null, tags, qaPairs);
    }

    private ClassicsQaKnowledgeFacadeDto getWangqiQaKnowledge(
            String contentType, String contentId, ClassicsContentId domainContentId) {
        var sourceContent = classicsSearchContentApplicationService.getPublicContent(contentType, contentId);
        if (sourceContent == null) {
            return null;
        }
        WangqiDocument document =
                wangqiDocumentApplicationService.get(WangqiDocumentIdCodec.toDomain(domainContentId.value()));
        if (document == null) {
            return null;
        }
        List<ClassicsContentTag> tags = classicsContentApplicationService.listTags(contentType, domainContentId);
        List<ClassicsContentQaPair> qaPairs =
                classicsContentApplicationService.listQaPairs(contentType, domainContentId);
        return classicsFacadeAssembler.toQaKnowledgeFacadeDto(
                sourceContent, null, null, document.getContent(), null, tags, qaPairs);
    }

    private ClassicsQaKnowledgeFacadeDto getMingCustomsQaKnowledge(
            String contentType, String contentId, ClassicsContentId domainContentId) {
        var sourceContent = classicsSearchContentApplicationService.getPublicContent(contentType, contentId);
        if (sourceContent == null) {
            return null;
        }
        MingCustomsEntry entry =
                mingCustomsApplicationService.get(MingCustomsEntryIdCodec.toDomain(domainContentId.value()));
        if (entry == null) {
            return null;
        }
        List<ClassicsContentTag> tags = classicsContentApplicationService.listTags(contentType, domainContentId);
        List<ClassicsContentQaPair> qaPairs =
                classicsContentApplicationService.listQaPairs(contentType, domainContentId);
        return classicsFacadeAssembler.toQaKnowledgeFacadeDto(
                sourceContent, null, null, entry.getContent(), entry.getOriginalExcerpts(), tags, qaPairs);
    }

    private Long parseContentId(String contentId) {
        try {
            return Long.valueOf(contentId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
