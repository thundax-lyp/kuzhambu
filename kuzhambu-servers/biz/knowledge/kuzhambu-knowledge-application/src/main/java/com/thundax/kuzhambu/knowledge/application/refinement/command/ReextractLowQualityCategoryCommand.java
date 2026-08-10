package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record ReextractLowQualityCategoryCommand(
        Long reportId,
        String sourceCategoryCode,
        String taskType,
        Boolean replaceUnconfirmedOnly,
        Long modelId,
        String modelName,
        String promptMessagesJson,
        String inputPayloadJson,
        Long requestedBy) {}
