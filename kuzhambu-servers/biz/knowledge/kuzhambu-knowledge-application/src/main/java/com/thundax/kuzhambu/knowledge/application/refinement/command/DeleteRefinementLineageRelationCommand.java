package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record DeleteRefinementLineageRelationCommand(Long refinementTaskId, String relationKey, Long operatorId) {}
