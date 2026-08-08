package com.thundax.kuzhambu.classics.application.publication.service;

import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import java.time.Instant;

public interface ClassicsPublicationReconcileApplicationService {

    boolean succeed(ClassicsPublicationJob job, Instant finishedAt);

    boolean reconcileFailure(ClassicsPublicationJob job);
}
