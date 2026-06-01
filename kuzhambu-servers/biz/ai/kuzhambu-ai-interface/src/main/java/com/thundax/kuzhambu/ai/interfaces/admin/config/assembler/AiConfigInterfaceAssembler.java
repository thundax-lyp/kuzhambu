package com.thundax.kuzhambu.ai.interfaces.admin.config.assembler;

import com.thundax.kuzhambu.ai.application.capability.command.AiCapabilityMappingSaveCommand;
import com.thundax.kuzhambu.ai.application.capability.result.AiActionStatusResult;
import com.thundax.kuzhambu.ai.application.model.command.AiModelCheckCommand;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapability;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiServiceConfig;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModelCheckRecord;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request.AiConfigRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses;
import java.time.Instant;
import java.util.Collections;

public final class AiConfigInterfaceAssembler {

    private AiConfigInterfaceAssembler() {}

    public static AiServiceConfig toServiceConfig(AiConfigRequests.ServiceConfigSaveRequest request) {
        AiServiceConfig config = new AiServiceConfig();
        config.setServiceId(request.getServiceId());
        config.setServiceRole(request.getServiceRole());
        config.setApiSource(request.getApiSource());
        config.setBaseUrl(request.getBaseUrl());
        config.setEncryptedApiKey(request.getEncryptedApiKey());
        config.setEnabled(request.getEnabled() == null || request.getEnabled());
        config.setStatus(defaultString(request.getStatus(), "UNAVAILABLE"));
        config.setConfiguredAt(Instant.now());
        return config;
    }

    public static AiConfigResponses.ServiceConfigResponse toResponse(AiServiceConfig config) {
        if (config == null) {
            return AiConfigResponses.ServiceConfigResponse.builder().build();
        }
        return AiConfigResponses.ServiceConfigResponse.builder()
                .serviceId(config.getServiceId())
                .serviceRole(config.getServiceRole())
                .apiSource(config.getApiSource())
                .baseUrl(config.getBaseUrl())
                .apiKeyConfigured(!isBlank(config.getEncryptedApiKey()))
                .enabled(config.isEnabled())
                .status(config.getStatus())
                .lastCheckedAt(config.getLastCheckedAt())
                .configuredAt(config.getConfiguredAt())
                .build();
    }

    public static AiModel toModel(AiConfigRequests.ModelSaveRequest request) {
        AiModel model = new AiModel();
        model.setModelId(request.getModelId());
        model.setServiceId(request.getServiceId());
        model.setModelName(request.getModelName());
        model.setDisplayName(request.getDisplayName());
        model.setCapabilityTags(
                request.getCapabilityTags() == null ? Collections.emptyList() : request.getCapabilityTags());
        model.setDefaultParamsJson(request.getDefaultParamsJson());
        model.setDescription(request.getDescription());
        model.setEnabled(request.getEnabled() == null || request.getEnabled());
        model.setRegisteredAt(Instant.now());
        return model;
    }

    public static AiConfigResponses.ModelResponse toResponse(AiModel model) {
        if (model == null) {
            return AiConfigResponses.ModelResponse.builder().build();
        }
        return AiConfigResponses.ModelResponse.builder()
                .modelId(model.getModelId())
                .serviceId(model.getServiceId())
                .modelName(model.getModelName())
                .displayName(model.getDisplayName())
                .capabilityTags(model.getCapabilityTags())
                .defaultParamsJson(model.getDefaultParamsJson())
                .description(model.getDescription())
                .enabled(model.isEnabled())
                .registeredAt(model.getRegisteredAt())
                .build();
    }

    public static AiModelCheckCommand toCheckCommand(AiConfigRequests.ModelCheckRecordRequest request) {
        AiModelCheckCommand command = new AiModelCheckCommand();
        command.setCheckId(request.getCheckId());
        command.setModelId(request.getModelId());
        command.setServiceId(request.getServiceId());
        command.setModelName(request.getModelName());
        command.setStatus(request.getStatus());
        command.setLatencyMs(request.getLatencyMs());
        command.setErrorType(request.getErrorType());
        command.setErrorMessage(request.getErrorMessage());
        command.setCheckedAt(request.getCheckedAt());
        return command;
    }

    public static AiConfigResponses.ModelCheckRecordResponse toResponse(AiModelCheckRecord record) {
        if (record == null) {
            return AiConfigResponses.ModelCheckRecordResponse.builder().build();
        }
        return AiConfigResponses.ModelCheckRecordResponse.builder()
                .checkId(record.getCheckId())
                .modelId(record.getModelId())
                .serviceId(record.getServiceId())
                .modelName(record.getModelName())
                .status(record.getStatus())
                .latencyMs(record.getLatencyMs())
                .errorType(record.getErrorType())
                .errorMessage(record.getErrorMessage())
                .checkedAt(record.getCheckedAt())
                .build();
    }

    public static AiConfigResponses.CapabilityResponse toResponse(AiCapability capability) {
        if (capability == null) {
            return AiConfigResponses.CapabilityResponse.builder().build();
        }
        return AiConfigResponses.CapabilityResponse.builder()
                .capability(capability.getCapability())
                .name(capability.getName())
                .requiredTags(capability.getRequiredTags())
                .outputMode(capability.getOutputMode())
                .enabled(capability.isEnabled())
                .priority(capability.getPriority())
                .build();
    }

    public static AiCapabilityMappingSaveCommand toMappingCommand(
            AiConfigRequests.CapabilityMappingSaveRequest request) {
        AiCapabilityMappingSaveCommand command = new AiCapabilityMappingSaveCommand();
        command.setMappingId(request.getMappingId());
        command.setScope(request.getScope());
        command.setCapability(request.getCapability());
        command.setModelId(request.getModelId());
        command.setEnabled(request.getEnabled() == null || request.getEnabled());
        command.setConfiguredAt(Instant.now());
        return command;
    }

    public static AiConfigResponses.CapabilityMappingResponse toResponse(AiCapabilityMapping mapping) {
        if (mapping == null) {
            return AiConfigResponses.CapabilityMappingResponse.builder().build();
        }
        return AiConfigResponses.CapabilityMappingResponse.builder()
                .mappingId(mapping.getMappingId())
                .scope(mapping.getScope())
                .capability(mapping.getCapability())
                .modelId(mapping.getModelId())
                .enabled(mapping.isEnabled())
                .configuredAt(mapping.getConfiguredAt())
                .build();
    }

    public static AiConfigResponses.ActionStatusResponse toResponse(AiActionStatusResult result) {
        if (result == null) {
            return AiConfigResponses.ActionStatusResponse.builder().build();
        }
        return AiConfigResponses.ActionStatusResponse.builder()
                .scope(result.getScope())
                .capability(result.getCapability())
                .available(result.isAvailable())
                .unavailableReason(result.getUnavailableReason())
                .checkedAt(result.getCheckedAt())
                .build();
    }

    private static String defaultString(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
