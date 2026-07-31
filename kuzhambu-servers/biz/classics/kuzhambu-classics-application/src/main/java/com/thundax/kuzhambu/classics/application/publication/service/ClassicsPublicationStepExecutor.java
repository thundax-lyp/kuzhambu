package com.thundax.kuzhambu.classics.application.publication.service;

import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;

public interface ClassicsPublicationStepExecutor {
    boolean execute(ClassicsPublicationJobId jobId, ClassicsPublicationExecutionToken executionToken);
}
