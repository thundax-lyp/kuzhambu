package com.thundax.kuzhambu.classics.application.publication.service.impl;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationPayloadAssembler;
import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationContent;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassicsPublicationSnapshotBindApplicationServiceImpl {
    private final ClassicsContentRepository contentRepository;
    private final ClassicsContentApplicationService contentApplicationService;
    private final ClassicsPublicationJobRepository jobRepository;
    private final ClassicsPublicationPayloadAssembler payloadAssembler;

    public ClassicsPublicationSnapshotBindApplicationServiceImpl(
            ClassicsContentRepository contentRepository,
            ClassicsContentApplicationService contentApplicationService,
            ClassicsPublicationJobRepository jobRepository,
            ClassicsPublicationPayloadAssembler payloadAssembler) {
        this.contentRepository = contentRepository;
        this.contentApplicationService = contentApplicationService;
        this.jobRepository = jobRepository;
        this.payloadAssembler = payloadAssembler;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean bind(ClassicsPublicationJob job, ClassicsPublicationExecutionToken executionToken) {
        ClassicsContentId contentId = new ClassicsContentId(job.getContentId());
        ClassicsPublicationContent state = contentRepository.lockPublicationContent(job.getContentType(), contentId);
        if (state == null
                || !job.getId().equals(state.getCurrentJobId())
                || state.getTransitionStatus() != ClassicsPublicationTransitionStatus.PUBLISHING) {
            return false;
        }

        Versionable content = currentContent(job.getContentType(), contentId);
        ClassicsContentVersion version =
                contentApplicationService.ensureVersioned(content, ClassicsContentChangeType.MANUAL_SAVE, "发布正式版本");
        if (version == null || version.getId() == null) {
            throw new IllegalStateException("FORMAL_VERSION_MISSING");
        }
        persistVersionMarkers(content);

        job.setContentVersionId(version.getId().value());
        job.setContentVersionNo(version.getVersionNo());
        payloadAssembler.assemble(job, version);
        int advanced = jobRepository.advanceMilestone(
                job.getId(),
                executionToken,
                ClassicsPublicationJobStatus.QUEUED,
                ClassicsPublicationJobStatus.SNAPSHOT_READY,
                job.getContentVersionId(),
                job.getContentVersionNo(),
                null,
                null,
                null,
                null);
        if (advanced != 1) {
            throw new IllegalStateException("Publication execution token expired during snapshot bind");
        }
        return true;
    }

    private Versionable currentContent(ClassicsContentType contentType, ClassicsContentId contentId) {
        return switch (contentType) {
            case SANCAI_ENTRY -> contentRepository.getSancaiEntryForAiApply(contentId);
            case WANGQI_DOCUMENT -> contentRepository.getWangqiDocumentForAiApply(contentId);
            case MING_CUSTOMS -> contentRepository.getMingCustomsEntryForAiApply(contentId);
        };
    }

    private void persistVersionMarkers(Versionable content) {
        int updated;
        if (content instanceof SancaiEntry entry) {
            updated = contentRepository.updateSancaiEntryVersionMarkers(entry);
        } else if (content instanceof WangqiDocument document) {
            updated = contentRepository.updateWangqiDocumentVersionMarkers(document);
        } else if (content instanceof MingCustomsEntry entry) {
            updated = contentRepository.updateMingCustomsEntryVersionMarkers(entry);
        } else {
            updated = 0;
        }
        if (updated != 1) {
            throw new IllegalStateException("FORMAL_VERSION_MARKER_UPDATE_FAILED");
        }
    }
}
