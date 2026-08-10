package com.thundax.kuzhambu.classics.application.publication.service;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationWorkflowCommand;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;

public interface ClassicsPublicationExecutionApplicationService {

    boolean claim(ClassicsPublicationWorkflowCommand command);

    ClassicsPublicationJob start(ClassicsPublicationWorkflowCommand command);

    boolean releaseClaim(ClassicsPublicationWorkflowCommand command);

    boolean retry(ClassicsPublicationWorkflowCommand command);

    boolean fail(ClassicsPublicationWorkflowCommand command);
}
