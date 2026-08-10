package com.thundax.kuzhambu.classics.application.publication.service;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationWorkflowCommand;

public interface ClassicsPublicationContentCommitApplicationService {

    boolean commit(ClassicsPublicationWorkflowCommand command);
}
