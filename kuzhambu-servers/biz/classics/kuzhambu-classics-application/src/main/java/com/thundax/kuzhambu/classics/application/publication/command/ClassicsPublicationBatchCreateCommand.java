package com.thundax.kuzhambu.classics.application.publication.command;

import java.util.List;

public record ClassicsPublicationBatchCreateCommand(List<ClassicsPublicationCreateCommand> commands) {}
