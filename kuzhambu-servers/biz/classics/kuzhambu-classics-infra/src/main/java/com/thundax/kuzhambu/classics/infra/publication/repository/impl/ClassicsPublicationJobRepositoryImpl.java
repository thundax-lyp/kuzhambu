package com.thundax.kuzhambu.classics.infra.publication.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.codec.ClassicsPublicationExecutionTokenCodec;
import com.thundax.kuzhambu.classics.domain.publication.codec.ClassicsPublicationJobIdCodec;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.classics.infra.publication.persistence.assembler.ClassicsPublicationPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.publication.persistence.dataobject.ClassicsPublicationJobDO;
import com.thundax.kuzhambu.classics.infra.publication.persistence.mapper.ClassicsPublicationJobMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class ClassicsPublicationJobRepositoryImpl implements ClassicsPublicationJobRepository {
    private final ClassicsPublicationJobMapper mapper;

    public ClassicsPublicationJobRepositoryImpl(ClassicsPublicationJobMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ClassicsPublicationJobId insert(ClassicsPublicationJob job) {
        ClassicsPublicationJobDO data = ClassicsPublicationPersistenceAssembler.toObject(job);
        mapper.insert(data);
        return ClassicsPublicationJobIdCodec.toDomain(data.getId());
    }

    @Override
    public ClassicsPublicationJob getById(ClassicsPublicationJobId id) {
        return ClassicsPublicationPersistenceAssembler.toDomain(
                mapper.selectById(ClassicsPublicationJobIdCodec.toValue(id)));
    }

    @Override
    public PageResult<ClassicsPublicationJob> page(
            ClassicsPublicationJobType jobType,
            ClassicsPublicationJobResultStatus jobResultStatus,
            ClassicsPublicationJobStatus jobStatus,
            ClassicsContentType contentType,
            String keyword,
            int pageNo,
            int pageSize) {
        LambdaQueryWrapper<ClassicsPublicationJobDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(jobType != null, ClassicsPublicationJobDO::getJobType, jobType == null ? null : jobType.name())
                .eq(
                        jobResultStatus != null,
                        ClassicsPublicationJobDO::getJobResultStatus,
                        jobResultStatus == null ? null : jobResultStatus.name())
                .eq(
                        jobStatus != null,
                        ClassicsPublicationJobDO::getJobStatus,
                        jobStatus == null ? null : jobStatus.name())
                .eq(
                        contentType != null,
                        ClassicsPublicationJobDO::getContentType,
                        contentType == null ? null : contentType.name())
                .and(StringUtils.isNotBlank(keyword), query -> query.like(
                                ClassicsPublicationJobDO::getContentTitleSnapshot, keyword.trim())
                        .or()
                        .like(ClassicsPublicationJobDO::getContentId, keyword.trim()));
        wrapper.orderByDesc(ClassicsPublicationJobDO::getRequestedAt).orderByDesc(ClassicsPublicationJobDO::getId);
        Page<ClassicsPublicationJobDO> dataPage = mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<ClassicsPublicationJob> records = dataPage.getRecords().stream()
                .map(ClassicsPublicationPersistenceAssembler::toDomain)
                .toList();
        return PageResult.of((int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), records);
    }

    @Override
    public ClassicsPublicationJob lockByContent(ClassicsContentType contentType, Long contentId) {
        return ClassicsPublicationPersistenceAssembler.toDomain(
                mapper.selectByContentForUpdate(contentType.name(), contentId));
    }

    @Override
    public int deleteById(ClassicsPublicationJobId id) {
        return mapper.deleteById(ClassicsPublicationJobIdCodec.toValue(id));
    }

    @Override
    public int claimExecution(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            Instant now,
            Instant dispatchExpiresAt) {
        return mapper.claimExecution(
                ClassicsPublicationJobIdCodec.toValue(id),
                ClassicsPublicationExecutionTokenCodec.toValue(token),
                now,
                dispatchExpiresAt);
    }

    @Override
    public int markThreadStarted(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            Instant startedAt,
            Instant sliceExpiresAt) {
        return mapper.markThreadStarted(
                ClassicsPublicationJobIdCodec.toValue(id),
                ClassicsPublicationExecutionTokenCodec.toValue(token),
                startedAt,
                sliceExpiresAt);
    }

    @Override
    public int advanceMilestone(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            ClassicsPublicationJobStatus expectedStatus,
            ClassicsPublicationJobStatus nextStatus,
            Long contentVersionId,
            Integer contentVersionNo,
            String esDocumentId,
            String fastGptCollectionId,
            String fastGptDataIdsJson,
            String detailJson) {
        return mapper.advanceMilestone(
                ClassicsPublicationJobIdCodec.toValue(id),
                ClassicsPublicationExecutionTokenCodec.toValue(token),
                expectedStatus.name(),
                nextStatus.name(),
                contentVersionId,
                contentVersionNo,
                esDocumentId,
                fastGptCollectionId,
                fastGptDataIdsJson,
                detailJson);
    }

    @Override
    public int bindFastGptCollection(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            ClassicsPublicationJobStatus expectedStatus,
            String fastGptCollectionId) {
        return mapper.bindFastGptCollection(
                ClassicsPublicationJobIdCodec.toValue(id),
                ClassicsPublicationExecutionTokenCodec.toValue(token),
                expectedStatus.name(),
                fastGptCollectionId);
    }

    @Override
    public int releaseForRetry(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            Instant nextRetryAt,
            String failureReason,
            String detailJson) {
        return mapper.releaseForRetry(
                ClassicsPublicationJobIdCodec.toValue(id),
                ClassicsPublicationExecutionTokenCodec.toValue(token),
                nextRetryAt,
                failureReason,
                detailJson);
    }

    @Override
    public int markTerminalFailure(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            Instant finishedAt,
            String failureReason,
            String detailJson) {
        return mapper.markTerminalFailure(
                ClassicsPublicationJobIdCodec.toValue(id),
                ClassicsPublicationExecutionTokenCodec.toValue(token),
                finishedAt,
                failureReason,
                detailJson);
    }

    @Override
    public int claimEsCleanup(ClassicsPublicationJobId id, String token, Instant now, Instant expiresAt) {
        return mapper.claimEsCleanup(ClassicsPublicationJobIdCodec.toValue(id), token, now, expiresAt);
    }

    @Override
    public int completeEsCleanup(ClassicsPublicationJobId id, String token) {
        return mapper.completeEsCleanup(ClassicsPublicationJobIdCodec.toValue(id), token);
    }

    @Override
    public int failEsCleanup(ClassicsPublicationJobId id, String token, String detailJson) {
        return mapper.failEsCleanup(ClassicsPublicationJobIdCodec.toValue(id), token, detailJson);
    }

    @Override
    public int claimFastGptCleanup(ClassicsPublicationJobId id, String token, Instant now, Instant expiresAt) {
        return mapper.claimFastGptCleanup(ClassicsPublicationJobIdCodec.toValue(id), token, now, expiresAt);
    }

    @Override
    public int completeFastGptCleanup(ClassicsPublicationJobId id, String token) {
        return mapper.completeFastGptCleanup(ClassicsPublicationJobIdCodec.toValue(id), token);
    }

    @Override
    public int failFastGptCleanup(ClassicsPublicationJobId id, String token, String detailJson) {
        return mapper.failFastGptCleanup(ClassicsPublicationJobIdCodec.toValue(id), token, detailJson);
    }
}
