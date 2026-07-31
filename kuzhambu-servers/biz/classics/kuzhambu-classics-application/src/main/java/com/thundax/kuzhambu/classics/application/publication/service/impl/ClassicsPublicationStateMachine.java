package com.thundax.kuzhambu.classics.application.publication.service.impl;

import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import java.util.Map;

public final class ClassicsPublicationStateMachine {
    private static final Map<ClassicsPublicationJobStatus, ClassicsPublicationJobStatus> PUBLISH_STEPS = Map.of(
            ClassicsPublicationJobStatus.QUEUED, ClassicsPublicationJobStatus.SNAPSHOT_READY,
            ClassicsPublicationJobStatus.SNAPSHOT_READY, ClassicsPublicationJobStatus.ES_PREPARED,
            ClassicsPublicationJobStatus.ES_PREPARED, ClassicsPublicationJobStatus.FASTGPT_PREPARED,
            ClassicsPublicationJobStatus.FASTGPT_PREPARED, ClassicsPublicationJobStatus.ES_READY,
            ClassicsPublicationJobStatus.ES_READY, ClassicsPublicationJobStatus.FASTGPT_READY,
            ClassicsPublicationJobStatus.FASTGPT_READY, ClassicsPublicationJobStatus.CONTENT_COMMITTED);
    private static final Map<ClassicsPublicationJobStatus, ClassicsPublicationJobStatus> OFFLINE_STEPS = Map.of(
            ClassicsPublicationJobStatus.QUEUED, ClassicsPublicationJobStatus.ES_DISABLED,
            ClassicsPublicationJobStatus.ES_DISABLED, ClassicsPublicationJobStatus.FASTGPT_DISABLED,
            ClassicsPublicationJobStatus.FASTGPT_DISABLED, ClassicsPublicationJobStatus.CONTENT_COMMITTED);

    private ClassicsPublicationStateMachine() {}

    public static ClassicsPublicationJobStatus nextStep(
            ClassicsPublicationJobType jobType, ClassicsPublicationJobStatus jobStatus) {
        if (jobType == null || jobStatus == null) {
            return null;
        }
        return (jobType == ClassicsPublicationJobType.PUBLISH ? PUBLISH_STEPS : OFFLINE_STEPS).get(jobStatus);
    }
}
