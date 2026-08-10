package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record DeleteRefinementLineageNodeCommand(Long refinementTaskId, String nodeKey, Long operatorId) {}
