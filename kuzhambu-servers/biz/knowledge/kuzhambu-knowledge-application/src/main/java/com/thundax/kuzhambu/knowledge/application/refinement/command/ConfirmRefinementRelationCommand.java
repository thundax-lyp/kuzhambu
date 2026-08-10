package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record ConfirmRefinementRelationCommand(Long refinementTaskId, String relationKey, Long operatorId) {}
