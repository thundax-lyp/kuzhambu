package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record DeleteRefinementEntityCommand(Long refinementTaskId, String entityKey, Long operatorId) {}
