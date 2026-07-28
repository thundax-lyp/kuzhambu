package com.thundax.kuzhambu.classics.application.cleanup.service.impl;

import com.thundax.kuzhambu.classics.application.cleanup.result.CleanupExecutionResult;
import com.thundax.kuzhambu.classics.application.cleanup.service.ClassicsCleanupApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentExportJobIdCodec;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryDraftIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiAssetRepository;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareLinkIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.repository.ClassicsSharingRepository;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@BizExceptionBoundary
public class ClassicsCleanupApplicationServiceImpl implements ClassicsCleanupApplicationService {
    private static final String CLEANUP_TYPE_EXPIRED_SHARE = "EXPIRED_SHARE";
    private static final String CLEANUP_TYPE_EXPIRED_DRAFT = "EXPIRED_DRAFT";
    private static final String CLEANUP_TYPE_EXPIRED_EXPORT = "EXPIRED_EXPORT";
    private static final String TARGET_TYPE_SHARE = "share";
    private static final String TARGET_TYPE_DRAFT = "draft";
    private static final String TARGET_TYPE_EXPORT = "export";
    private static final String UNSUPPORTED_CLEANUP_TYPE = "UNSUPPORTED_CLEANUP_TYPE";
    private static final String TARGET_NOT_FOUND = "TARGET_NOT_FOUND";
    private static final int DEFAULT_RETENTION_DAYS = 30;
    private static final int DEFAULT_LIMIT = 200;

    private final ClassicsSharingRepository sharingRepository;
    private final SancaiAssetRepository sancaiAssetRepository;
    private final ClassicsContentRepository contentRepository;

    public ClassicsCleanupApplicationServiceImpl(
            ClassicsSharingRepository sharingRepository,
            SancaiAssetRepository sancaiAssetRepository,
            ClassicsContentRepository contentRepository) {
        this.sharingRepository = sharingRepository;
        this.sancaiAssetRepository = sancaiAssetRepository;
        this.contentRepository = contentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CleanupTarget> listTargets(String cleanupType, Date requestedAt, Integer retentionDays, Integer limit) {
        String normalizedType = normalizeCleanupType(cleanupType);
        Date effectiveNow = requestedAt == null ? new Date() : requestedAt;
        int effectiveLimit = normalizeLimit(limit);
        if (targetType(normalizedType) == null) {
            return List.of();
        }
        return switch (normalizedType) {
            case CLEANUP_TYPE_EXPIRED_SHARE ->
                sharingRepository.listExpiredShareLinkIds(effectiveNow, effectiveLimit).stream()
                        .map(id -> target(TARGET_TYPE_SHARE, id == null ? null : id.value()))
                        .toList();
            case CLEANUP_TYPE_EXPIRED_DRAFT ->
                sancaiAssetRepository
                        .listExpiredDraftIds(retentionCutoff(effectiveNow, retentionDays), effectiveLimit)
                        .stream()
                        .map(id -> target(TARGET_TYPE_DRAFT, id == null ? null : id.value()))
                        .toList();
            case CLEANUP_TYPE_EXPIRED_EXPORT ->
                contentRepository.listExpiredExportJobIds(effectiveNow, effectiveLimit).stream()
                        .map(id -> target(TARGET_TYPE_EXPORT, id == null ? null : id.value()))
                        .toList();
            default -> List.of();
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CleanupExecutionResult executeTarget(String cleanupType, Long targetId) {
        String normalizedType = normalizeCleanupType(cleanupType);
        String targetType = targetType(normalizedType);
        if (targetType == null) {
            return result("unknown", targetId, false, UNSUPPORTED_CLEANUP_TYPE);
        }
        try {
            int affectedRows =
                    switch (normalizedType) {
                        case CLEANUP_TYPE_EXPIRED_SHARE ->
                            sharingRepository.markShareLinkExpired(ClassicsShareLinkIdCodec.toDomain(targetId));
                        case CLEANUP_TYPE_EXPIRED_DRAFT ->
                            sancaiAssetRepository.deleteDraftById(SancaiEntryDraftIdCodec.toDomain(targetId));
                        case CLEANUP_TYPE_EXPIRED_EXPORT ->
                            contentRepository.markExportJobExpired(ClassicsContentExportJobIdCodec.toDomain(targetId));
                        default -> 0;
                    };
            return affectedRows > 0
                    ? result(targetType, targetId, true, null)
                    : result(targetType, targetId, false, TARGET_NOT_FOUND);
        } catch (RuntimeException exception) {
            return result(targetType, targetId, false, exception.getMessage());
        }
    }

    private static CleanupTarget target(String targetType, Long targetId) {
        return CleanupTarget.builder().targetType(targetType).targetId(targetId).build();
    }

    private static CleanupExecutionResult result(
            String targetType, Long targetId, boolean success, String failureReason) {
        return CleanupExecutionResult.builder()
                .targetType(targetType)
                .targetId(targetId)
                .success(success)
                .failureReason(failureReason)
                .build();
    }

    private static String normalizeCleanupType(String cleanupType) {
        return cleanupType == null ? null : cleanupType.trim().toUpperCase(Locale.ROOT);
    }

    private static String targetType(String cleanupType) {
        if (cleanupType == null) {
            return null;
        }
        return switch (cleanupType) {
            case CLEANUP_TYPE_EXPIRED_SHARE -> TARGET_TYPE_SHARE;
            case CLEANUP_TYPE_EXPIRED_DRAFT -> TARGET_TYPE_DRAFT;
            case CLEANUP_TYPE_EXPIRED_EXPORT -> TARGET_TYPE_EXPORT;
            default -> null;
        };
    }

    private static int normalizeLimit(Integer limit) {
        return limit == null || limit <= 0 ? DEFAULT_LIMIT : limit;
    }

    private static Date retentionCutoff(Date now, Integer retentionDays) {
        int effectiveRetentionDays =
                retentionDays == null || retentionDays <= 0 ? DEFAULT_RETENTION_DAYS : retentionDays;
        return new Date(now.getTime() - effectiveRetentionDays * 24L * 60L * 60L * 1000L);
    }
}
