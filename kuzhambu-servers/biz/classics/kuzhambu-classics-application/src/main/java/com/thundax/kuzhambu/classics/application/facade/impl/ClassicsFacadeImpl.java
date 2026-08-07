package com.thundax.kuzhambu.classics.application.facade.impl;

import com.thundax.kuzhambu.classics.application.cleanup.service.ClassicsCleanupApplicationService;
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
import com.thundax.kuzhambu.classics.facade.request.ClassicsCleanupTargetsFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsCleanupExecutionFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsCleanupTargetsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassicsFacadeImpl implements ClassicsFacade {
    private static final String CLEANUP_TYPE_EXPIRED_SHARE = "EXPIRED_SHARE";
    private static final String CLEANUP_TYPE_EXPIRED_DRAFT = "EXPIRED_DRAFT";
    private static final String CLEANUP_TYPE_EXPIRED_EXPORT = "EXPIRED_EXPORT";
    private static final String CLEANUP_TARGET_TYPE_SHARE = "share";
    private static final String CLEANUP_TARGET_TYPE_DRAFT = "draft";
    private static final String CLEANUP_TARGET_TYPE_EXPORT = "export";
    private static final String UNSUPPORTED_CLEANUP_TYPE = "UNSUPPORTED_CLEANUP_TYPE";

    private final ClassicsCleanupApplicationService classicsCleanupApplicationService;
    private final ClassicsContentApplicationService classicsContentApplicationService;
    private final ClassicsReportApplicationService classicsReportApplicationService;
    private final ClassicsSearchContentApplicationService classicsSearchContentApplicationService;
    private final SancaiApplicationService sancaiApplicationService;
    private final WangqiDocumentApplicationService wangqiDocumentApplicationService;
    private final MingCustomsApplicationService mingCustomsApplicationService;
    private final ClassicsFacadeAssembler classicsFacadeAssembler;

    public ClassicsFacadeImpl(
            ClassicsCleanupApplicationService classicsCleanupApplicationService,
            ClassicsContentApplicationService classicsContentApplicationService,
            ClassicsReportApplicationService classicsReportApplicationService,
            ClassicsSearchContentApplicationService classicsSearchContentApplicationService,
            SancaiApplicationService sancaiApplicationService,
            WangqiDocumentApplicationService wangqiDocumentApplicationService,
            MingCustomsApplicationService mingCustomsApplicationService,
            ClassicsFacadeAssembler classicsFacadeAssembler) {
        this.classicsCleanupApplicationService = classicsCleanupApplicationService;
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
    public ClassicsPublicContentsFacadeResponse listWorkbenchCategoryContents() {
        return classicsFacadeAssembler.toPublicContentsFacadeResponse(
                classicsSearchContentApplicationService.listWorkbenchCategoryContents());
    }

    @Override
    @Transactional(readOnly = true)
    public ClassicsPublicContentsFacadeResponse listWorkbenchVolumeContents() {
        return classicsFacadeAssembler.toPublicContentsFacadeResponse(
                classicsSearchContentApplicationService.listWorkbenchVolumeContents());
    }

    @Override
    @Transactional(readOnly = true)
    public ClassicsPublicContentsFacadeResponse listWorkbenchContents() {
        return classicsFacadeAssembler.toPublicContentsFacadeResponse(
                classicsSearchContentApplicationService.listWorkbenchContents());
    }

    @Override
    @Transactional(readOnly = true)
    public ClassicsPublicContentsFacadeResponse listWorkbenchContents(String categoryCode, String volumeCode) {
        return classicsFacadeAssembler.toPublicContentsFacadeResponse(
                classicsSearchContentApplicationService.listWorkbenchContents(categoryCode, volumeCode));
    }

    @Override
    @Transactional(readOnly = true)
    public ClassicsPublicContentFacadeResponse getWorkbenchContent(ClassicsPublicContentFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return classicsFacadeAssembler.toPublicContentFacadeResponse(
                classicsSearchContentApplicationService.getWorkbenchContent(
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

    @Override
    @Transactional(readOnly = true)
    public ClassicsQaKnowledgeFacadeResponse getWorkbenchQaKnowledge(ClassicsQaKnowledgeFacadeRequest request) {
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
                classicsFacadeAssembler.toQaKnowledgeFacadeResponse(getSancaiWorkbenchQaKnowledge(
                        request.getContentType(), request.getContentId(), domainContentId));
            case WANGQI_DOCUMENT, MING_CUSTOMS -> getQaKnowledge(request);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public ClassicsCleanupTargetsFacadeResponse listCleanupTargets(ClassicsCleanupTargetsFacadeRequest request) {
        String cleanupType = normalizeCleanupType(request == null ? null : request.getCleanupType());
        String targetType = cleanupTargetType(cleanupType);
        if (targetType == null) {
            return ClassicsCleanupTargetsFacadeResponse.builder()
                    .cleanupType(cleanupType)
                    .supported(false)
                    .failureReason(UNSUPPORTED_CLEANUP_TYPE)
                    .targets(List.of())
                    .build();
        }
        List<ClassicsCleanupTargetsFacadeResponse.Target> targets = classicsCleanupApplicationService
                .listTargets(
                        cleanupType,
                        request == null ? null : request.getRequestedAt(),
                        request == null ? null : request.getRetentionDays(),
                        request == null ? null : request.getLimit())
                .stream()
                .map(target -> ClassicsCleanupTargetsFacadeResponse.Target.builder()
                        .targetType(target.getTargetType())
                        .targetId(target.getTargetId())
                        .build())
                .toList();
        return ClassicsCleanupTargetsFacadeResponse.builder()
                .cleanupType(cleanupType)
                .supported(true)
                .targets(targets)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsCleanupExecutionFacadeResponse executeCleanupTargets(ClassicsCleanupTargetsFacadeRequest request) {
        String cleanupType = normalizeCleanupType(request == null ? null : request.getCleanupType());
        String targetType = cleanupTargetType(cleanupType);
        if (targetType == null) {
            return ClassicsCleanupExecutionFacadeResponse.builder()
                    .cleanupType(cleanupType)
                    .supported(false)
                    .failureReason(UNSUPPORTED_CLEANUP_TYPE)
                    .itemResults(List.of())
                    .build();
        }
        List<ClassicsCleanupExecutionFacadeResponse.ItemResult> itemResults = safeTargetIds(request).stream()
                .map(targetId -> classicsCleanupApplicationService.executeTarget(cleanupType, targetId))
                .map(result -> ClassicsCleanupExecutionFacadeResponse.ItemResult.builder()
                        .targetType(result.getTargetType())
                        .targetId(result.getTargetId())
                        .success(result.isSuccess())
                        .failureReason(result.getFailureReason())
                        .build())
                .toList();
        return ClassicsCleanupExecutionFacadeResponse.builder()
                .cleanupType(cleanupType)
                .supported(true)
                .itemResults(itemResults)
                .build();
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

    private ClassicsQaKnowledgeFacadeDto getSancaiWorkbenchQaKnowledge(
            String contentType, String contentId, ClassicsContentId domainContentId) {
        var sourceContent = classicsSearchContentApplicationService.getWorkbenchContent(contentType, contentId);
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

    private static String normalizeCleanupType(String cleanupType) {
        return cleanupType == null ? null : cleanupType.trim().toUpperCase(Locale.ROOT);
    }

    private static String cleanupTargetType(String cleanupType) {
        if (cleanupType == null) {
            return null;
        }
        return switch (cleanupType) {
            case CLEANUP_TYPE_EXPIRED_SHARE -> CLEANUP_TARGET_TYPE_SHARE;
            case CLEANUP_TYPE_EXPIRED_DRAFT -> CLEANUP_TARGET_TYPE_DRAFT;
            case CLEANUP_TYPE_EXPIRED_EXPORT -> CLEANUP_TARGET_TYPE_EXPORT;
            default -> null;
        };
    }

    private static List<Long> safeTargetIds(ClassicsCleanupTargetsFacadeRequest request) {
        return request == null || request.getTargetIds() == null ? List.of() : request.getTargetIds();
    }
}
