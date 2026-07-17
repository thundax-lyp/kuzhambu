package com.thundax.kuzhambu.ai.interfaces.admin.config.assembler;

import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request.AiConfigRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public final class AiConfigInterfaceAssembler {

    private AiConfigInterfaceAssembler() {}

    public static AiModel toModel(AiConfigRequests.ModelSaveRequest request) {
        AiModel model = new AiModel();
        model.setId(AiModelIdCodec.toDomain(request.getId()));
        model.setApiSource(AiApiSource.from(request.getApiSource()));
        model.setBaseUrl(request.getBaseUrl());
        model.setEncryptedApiKey(request.getApiKey());
        model.setModelName(request.getModelName());
        model.setDisplayName(request.getDisplayName());
        model.setCapabilities(toModelCapabilities(request.getCapabilities()));
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
                .id(AiModelIdCodec.toValue(model.getId()))
                .apiSource(
                        model.getApiSource() == null
                                ? null
                                : model.getApiSource().value())
                .baseUrl(model.getBaseUrl())
                .apiKeyConfigured(!isBlank(model.getEncryptedApiKey()))
                .modelName(model.getModelName())
                .displayName(model.getDisplayName())
                .capabilities(toModelCapabilityValues(model.getCapabilities()))
                .defaultParamsJson(model.getDefaultParamsJson())
                .description(model.getDescription())
                .enabled(model.isEnabled())
                .registeredAt(model.getRegisteredAt())
                .build();
    }

    public static AiConfigResponses.CapabilityResponse toResponse(AiBusinessCapability capability) {
        if (capability == null) {
            return AiConfigResponses.CapabilityResponse.builder().build();
        }
        return AiConfigResponses.CapabilityResponse.builder()
                .capability(capability.value())
                .name(capability.displayName())
                .requiredTags(Collections.emptyList())
                .enabled(true)
                .priority(capability.ordinal() + 1)
                .build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static List<AiModelCapability> toModelCapabilities(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream().map(AiModelCapability::from).toList();
    }

    private static List<String> toModelCapabilityValues(List<AiModelCapability> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream().map(AiModelCapability::value).toList();
    }
}
