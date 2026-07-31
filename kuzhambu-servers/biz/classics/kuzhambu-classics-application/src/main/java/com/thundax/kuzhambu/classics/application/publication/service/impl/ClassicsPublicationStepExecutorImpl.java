package com.thundax.kuzhambu.classics.application.publication.service.impl;

import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationPayload;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationStepExecutor;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationFastGptGateway;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationPayloadAssembler;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.discovery.facade.DiscoverySearchPublicationFacade;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationReferenceFacadeRequest;
import java.time.Instant;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class ClassicsPublicationStepExecutorImpl implements ClassicsPublicationStepExecutor {
    private final ClassicsPublicationJobRepository jobRepository;
    private final ClassicsContentRepository contentRepository;
    private final DiscoverySearchPublicationFacade searchFacade;
    private final ClassicsPublicationFastGptGateway fastGptGateway;
    private final ClassicsPublicationPayloadAssembler payloadAssembler;
    private final ClassicsPublicationSnapshotBindApplicationServiceImpl snapshotBindService;
    private final ClassicsPublicationContentCommitApplicationServiceImpl contentCommitService;

    public ClassicsPublicationStepExecutorImpl(
            ClassicsPublicationJobRepository jobRepository,
            ClassicsContentRepository contentRepository,
            DiscoverySearchPublicationFacade searchFacade,
            ClassicsPublicationFastGptGateway fastGptGateway,
            ClassicsPublicationPayloadAssembler payloadAssembler,
            ClassicsPublicationSnapshotBindApplicationServiceImpl snapshotBindService,
            ClassicsPublicationContentCommitApplicationServiceImpl contentCommitService) {
        this.jobRepository = jobRepository;
        this.contentRepository = contentRepository;
        this.searchFacade = searchFacade;
        this.fastGptGateway = fastGptGateway;
        this.payloadAssembler = payloadAssembler;
        this.snapshotBindService = snapshotBindService;
        this.contentCommitService = contentCommitService;
    }

    @Override
    public boolean execute(ClassicsPublicationJobId jobId, ClassicsPublicationExecutionToken executionToken) {
        ClassicsPublicationJob job = jobRepository.getById(jobId);
        if (!owned(job, executionToken)) {
            return false;
        }
        ClassicsPublicationJobStatus next =
                ClassicsPublicationStateMachine.nextStep(job.getJobType(), job.getJobStatus());
        if (next == null) {
            return false;
        }
        return switch (next) {
            case SNAPSHOT_READY -> snapshotBindService.bind(job, executionToken);
            case ES_PREPARED -> prepareSearch(job, executionToken);
            case FASTGPT_PREPARED -> prepareFastGpt(job, executionToken);
            case ES_READY -> readySearch(job, executionToken);
            case FASTGPT_READY -> enableFastGpt(job, executionToken);
            case ES_DISABLED -> offlineSearch(job, executionToken);
            case FASTGPT_DISABLED -> disableFastGpt(job, executionToken);
            case CONTENT_COMMITTED -> contentCommitService.commit(job, executionToken);
            case QUEUED -> false;
        };
    }

    private boolean prepareSearch(ClassicsPublicationJob job, ClassicsPublicationExecutionToken executionToken) {
        ClassicsPublicationPayload payload = payload(job);
        searchFacade.prepare(payload.searchDocument());
        return advance(
                job,
                executionToken,
                ClassicsPublicationJobStatus.ES_PREPARED,
                null,
                null,
                payload.searchDocument().getSourceId(),
                null);
    }

    private boolean prepareFastGpt(ClassicsPublicationJob job, ClassicsPublicationExecutionToken executionToken) {
        ClassicsPublicationPayload payload = payload(job);
        String collectionId = job.getFastGptCollectionId();
        if (collectionId == null) {
            collectionId = fastGptGateway.createCollection(payload.fastGptCollectionName());
            if (jobRepository.bindFastGptCollection(job.getId(), executionToken, job.getJobStatus(), collectionId)
                    != 1) {
                return false;
            }
            job.setFastGptCollectionId(collectionId);
        }
        fastGptGateway.fullReplace(collectionId, payload.fastGptFragments());
        return advance(
                job, executionToken, ClassicsPublicationJobStatus.FASTGPT_PREPARED, null, null, null, collectionId);
    }

    private boolean readySearch(ClassicsPublicationJob job, ClassicsPublicationExecutionToken executionToken) {
        searchFacade.markReady(reference(job));
        return advance(job, executionToken, ClassicsPublicationJobStatus.ES_READY, null, null, null, null);
    }

    private boolean enableFastGpt(ClassicsPublicationJob job, ClassicsPublicationExecutionToken executionToken) {
        if (job.getFastGptCollectionId() == null) {
            throw new IllegalStateException("FastGPT collection reference is missing");
        }
        fastGptGateway.enable(job.getFastGptCollectionId());
        return advance(job, executionToken, ClassicsPublicationJobStatus.FASTGPT_READY, null, null, null, null);
    }

    private boolean offlineSearch(ClassicsPublicationJob job, ClassicsPublicationExecutionToken executionToken) {
        String documentId = documentId(job);
        searchFacade.markOffline(DiscoverySearchPublicationReferenceFacadeRequest.builder()
                .documentId(documentId)
                .occurredAt(Instant.now())
                .build());
        return advance(job, executionToken, ClassicsPublicationJobStatus.ES_DISABLED, null, null, documentId, null);
    }

    private boolean disableFastGpt(ClassicsPublicationJob job, ClassicsPublicationExecutionToken executionToken) {
        fastGptGateway.disable(job.getFastGptCollectionId());
        return advance(job, executionToken, ClassicsPublicationJobStatus.FASTGPT_DISABLED, null, null, null, null);
    }

    private ClassicsPublicationPayload payload(ClassicsPublicationJob job) {
        if (job.getContentVersionId() == null) {
            throw new IllegalStateException("Bound formal version is missing");
        }
        ClassicsContentVersion version =
                contentRepository.getVersionById(new ClassicsContentVersionId(job.getContentVersionId()));
        return payloadAssembler.assemble(job, version);
    }

    private boolean advance(
            ClassicsPublicationJob job,
            ClassicsPublicationExecutionToken executionToken,
            ClassicsPublicationJobStatus nextStatus,
            Long contentVersionId,
            Integer contentVersionNo,
            String esDocumentId,
            String fastGptCollectionId) {
        return jobRepository.advanceMilestone(
                        job.getId(),
                        executionToken,
                        job.getJobStatus(),
                        nextStatus,
                        contentVersionId,
                        contentVersionNo,
                        esDocumentId,
                        fastGptCollectionId,
                        null,
                        null)
                == 1;
    }

    private static DiscoverySearchPublicationReferenceFacadeRequest reference(ClassicsPublicationJob job) {
        return DiscoverySearchPublicationReferenceFacadeRequest.builder()
                .documentId(documentId(job))
                .contentVersionId(String.valueOf(job.getContentVersionId()))
                .contentVersionNo(job.getContentVersionNo())
                .occurredAt(Instant.now())
                .build();
    }

    private static String documentId(ClassicsPublicationJob job) {
        return job.getEsDocumentId() == null
                ? job.getContentType().name() + ":" + job.getContentId()
                : job.getEsDocumentId();
    }

    private static boolean owned(ClassicsPublicationJob job, ClassicsPublicationExecutionToken executionToken) {
        return job != null
                && executionToken != null
                && job.getJobResultStatus() == ClassicsPublicationJobResultStatus.RUNNING
                && Objects.equals(job.getExecutionToken(), executionToken);
    }
}
