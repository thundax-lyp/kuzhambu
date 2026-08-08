package com.thundax.kuzhambu.classics.application.publication.service;

import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;

public interface ClassicsPublicationSnapshotBindApplicationService {

    boolean bind(ClassicsPublicationJob job, ClassicsPublicationExecutionToken executionToken);
}
