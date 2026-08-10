package com.thundax.kuzhambu.classics.application.publication.service;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationWorkflowCommand;

public interface ClassicsPublicationCleanupApplicationService {

    boolean claimEs(ClassicsPublicationWorkflowCommand command);

    boolean claimFastGpt(ClassicsPublicationWorkflowCommand command);

    boolean qualify(ClassicsPublicationWorkflowCommand command);

    boolean complete(ClassicsPublicationWorkflowCommand command);

    boolean fail(ClassicsPublicationWorkflowCommand command);
}
