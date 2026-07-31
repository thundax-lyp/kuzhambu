package com.thundax.kuzhambu.classics.infra.publication.persistence.assembler;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.codec.ClassicsPublicationExecutionTokenCodec;
import com.thundax.kuzhambu.classics.domain.publication.codec.ClassicsPublicationJobIdCodec;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationCleanupStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.infra.publication.persistence.dataobject.ClassicsPublicationJobDO;

public final class ClassicsPublicationPersistenceAssembler {
    private ClassicsPublicationPersistenceAssembler() {}

    public static ClassicsPublicationJobDO toObject(ClassicsPublicationJob job) {
        if (job == null) {
            return null;
        }
        ClassicsPublicationJobDO data = new ClassicsPublicationJobDO();
        data.setId(ClassicsPublicationJobIdCodec.toValue(job.getId()));
        data.setJobType(name(job.getJobType()));
        data.setContentType(name(job.getContentType()));
        data.setContentId(job.getContentId());
        data.setContentTitleSnapshot(job.getContentTitleSnapshot());
        data.setContentDeletedAt(job.getContentDeletedAt());
        data.setSourceLifecycleStatus(name(job.getSourceLifecycleStatus()));
        data.setTargetLifecycleStatus(name(job.getTargetLifecycleStatus()));
        data.setContentVersionId(job.getContentVersionId());
        data.setContentVersionNo(job.getContentVersionNo());
        data.setJobStatus(name(job.getJobStatus()));
        data.setJobResultStatus(name(job.getJobResultStatus()));
        data.setExecutionToken(ClassicsPublicationExecutionTokenCodec.toValue(job.getExecutionToken()));
        data.setExpiresAt(job.getExpiresAt());
        data.setNextRetryAt(job.getNextRetryAt());
        data.setAttemptCount(job.getAttemptCount());
        data.setMaxAttempts(job.getMaxAttempts());
        data.setEsDocumentId(job.getEsDocumentId());
        data.setFastGptCollectionId(job.getFastGptCollectionId());
        data.setFastGptDataIdsJson(job.getFastGptDataIdsJson());
        data.setEsCleanupStatus(name(job.getEsCleanupStatus()));
        data.setEsCleanupToken(job.getEsCleanupToken());
        data.setEsCleanupExpiresAt(job.getEsCleanupExpiresAt());
        data.setFastGptCleanupStatus(name(job.getFastGptCleanupStatus()));
        data.setFastGptCleanupToken(job.getFastGptCleanupToken());
        data.setFastGptCleanupExpiresAt(job.getFastGptCleanupExpiresAt());
        data.setDetailJson(job.getDetailJson());
        data.setRequestedAt(job.getRequestedAt());
        data.setStartedAt(job.getStartedAt());
        data.setFinishedAt(job.getFinishedAt());
        data.setFailureReason(job.getFailureReason());
        return data;
    }

    public static ClassicsPublicationJob toDomain(ClassicsPublicationJobDO data) {
        if (data == null) {
            return null;
        }
        return new ClassicsPublicationJob(
                ClassicsPublicationJobIdCodec.toDomain(data.getId()),
                valueOf(ClassicsPublicationJobType.class, data.getJobType()),
                valueOf(ClassicsContentType.class, data.getContentType()),
                data.getContentId(),
                data.getContentTitleSnapshot(),
                data.getContentDeletedAt(),
                valueOf(ClassicsPublicationLifecycleStatus.class, data.getSourceLifecycleStatus()),
                valueOf(ClassicsPublicationLifecycleStatus.class, data.getTargetLifecycleStatus()),
                data.getContentVersionId(),
                data.getContentVersionNo(),
                valueOf(ClassicsPublicationJobStatus.class, data.getJobStatus()),
                valueOf(ClassicsPublicationJobResultStatus.class, data.getJobResultStatus()),
                ClassicsPublicationExecutionTokenCodec.toDomain(data.getExecutionToken()),
                data.getExpiresAt(),
                data.getNextRetryAt(),
                data.getAttemptCount(),
                data.getMaxAttempts(),
                data.getEsDocumentId(),
                data.getFastGptCollectionId(),
                data.getFastGptDataIdsJson(),
                valueOf(ClassicsPublicationCleanupStatus.class, data.getEsCleanupStatus()),
                data.getEsCleanupToken(),
                data.getEsCleanupExpiresAt(),
                valueOf(ClassicsPublicationCleanupStatus.class, data.getFastGptCleanupStatus()),
                data.getFastGptCleanupToken(),
                data.getFastGptCleanupExpiresAt(),
                data.getDetailJson(),
                data.getRequestedAt(),
                data.getStartedAt(),
                data.getFinishedAt(),
                data.getFailureReason());
    }

    private static String name(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static <T extends Enum<T>> T valueOf(Class<T> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }
}
