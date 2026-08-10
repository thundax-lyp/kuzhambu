package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record ConfirmRefinementLineageRelationCommand(Long refinementTaskId, String relationKey, Long operatorId) {}
