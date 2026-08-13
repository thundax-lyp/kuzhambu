package com.thundax.kuzhambu.knowledge.application.graph.command;

import java.util.List;

public record GraphBatchPublicationCommand(List<GraphPublicationCommand> materials) {}
