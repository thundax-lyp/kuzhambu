package com.thundax.kuzhambu.ai.application.invocation.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.config.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiWorkerModelConfigResolver {

    private final AiModelApplicationService modelService;
    private final ObjectMapper objectMapper;

    public AiWorkerModelConfigResolver(AiModelApplicationService modelService, ObjectMapper objectMapper) {
        this.modelService = modelService;
        this.objectMapper = objectMapper;
    }

    public ResolvedModelConfig resolve(AiInvokeCommand command) {
        if (command == null) {
            return null;
        }

        AiModel model = resolveModel(command);
        if (model.getApiSource() == null) {
            throw new IllegalArgumentException("AI model apiSource is required: " + command.getModelId());
        }
        if (isBlank(model.getBaseUrl())) {
            throw new IllegalArgumentException("AI model baseUrl is required: " + command.getModelId());
        }

        if (!isBlank(command.getModelName()) && !command.getModelName().equals(model.getModelName())) {
            throw new IllegalArgumentException("AI model mismatch: modelId=%s, modelName=%s"
                    .formatted(command.getModelId(), command.getModelName()));
        }

        return new ResolvedModelConfig(
                command.getServiceId(),
                command.getServiceRole(),
                value(model.getId()),
                model.getApiSource() == null ? null : model.getApiSource().value(),
                model.getBaseUrl(),
                model.getEncryptedApiKey(),
                model.getModelName(),
                model.getCapabilities() == null
                        ? new ArrayList<>()
                        : model.getCapabilities().stream()
                                .map(AiModelCapability::value)
                                .toList(),
                parseParameters(model),
                null);
    }

    private AiModel resolveModel(AiInvokeCommand command) {
        if (command.getModelId() == null) {
            throw new IllegalArgumentException("AI modelId is required");
        }
        AiModel model = modelService.get(command.getModelId());
        if (model == null) {
            throw new IllegalArgumentException("AI model not found: " + command.getModelId());
        }
        if (!model.isEnabled()) {
            throw new IllegalArgumentException("AI model is disabled: " + command.getModelId());
        }
        return model;
    }

    private JsonNode parseParameters(AiModel model) {
        if (isBlank(model.getDefaultParamsJson())) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(model.getDefaultParamsJson());
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("AI model default parameters is not valid JSON", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Long value(AiModelId id) {
        return id == null ? null : id.value();
    }

    public record ResolvedModelConfig(
            Long serviceId,
            String serviceRole,
            Long modelId,
            String apiSource,
            String baseUrl,
            String apiKey,
            String modelName,
            List<String> capabilityTags,
            JsonNode parameters,
            Long timeoutMs) {}
}
