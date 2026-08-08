package com.thundax.kuzhambu.classics.application.publication.service;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationCreateCommand;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationCreateResult;

public interface ClassicsPublicationCreationApplicationService {

    ClassicsPublicationCreateResult create(ClassicsPublicationCreateCommand command);
}
