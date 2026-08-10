package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record ConfirmRefinementLineageNodeCommand(Long refinementTaskId, String nodeKey, Long operatorId) {}
