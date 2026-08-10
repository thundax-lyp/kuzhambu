package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record ConfirmRefinementEntityCommand(Long refinementTaskId, String entityKey, Long operatorId) {}
