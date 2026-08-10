package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record DeleteRefinementRelationCommand(Long refinementTaskId, String relationKey, Long operatorId) {}
