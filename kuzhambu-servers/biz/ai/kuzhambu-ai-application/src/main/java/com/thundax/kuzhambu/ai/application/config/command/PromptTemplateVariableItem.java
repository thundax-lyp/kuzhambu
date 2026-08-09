package com.thundax.kuzhambu.ai.application.config.command;

public record PromptTemplateVariableItem(String variableName, boolean required, String description, Integer priority) {}
