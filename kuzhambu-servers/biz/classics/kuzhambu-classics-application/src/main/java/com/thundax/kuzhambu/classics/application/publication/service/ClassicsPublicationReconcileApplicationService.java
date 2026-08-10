package com.thundax.kuzhambu.classics.application.publication.service;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationWorkflowCommand;

public interface ClassicsPublicationReconcileApplicationService {

    boolean succeed(ClassicsPublicationWorkflowCommand command);

    boolean reconcileFailure(ClassicsPublicationWorkflowCommand command);
}
