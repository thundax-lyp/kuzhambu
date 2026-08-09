package com.thundax.kuzhambu.ai.application.invocation.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.repository.AiBusinessConfigRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiWorkerModelConfigResolver {

    private static final String DEFAULT_SERVICE_ROLE = "PRIMARY";

    private final AiBusinessConfigRepository businessConfigRepository;
    private final AiModelRepository modelRepository;
    private final ObjectMapper objectMapper;

    public AiWorkerModelConfigResolver(
            AiBusinessConfigRepository businessConfigRepository,
            AiModelRepository modelRepository,
            ObjectMapper objectMapper) {
        this.businessConfigRepository = businessConfigRepository;
        this.modelRepository = modelRepository;
        this.objectMapper = objectMapper;
    }

    public ResolvedModelConfig resolveConfig(AiInvokeCommand command) {
        if (command == null) {
            return null;
        }
        return resolveConfig(
                command.capability(),
                command.serviceId(),
                command.serviceRole(),
                command.modelId(),
                command.modelName());
    }

    public ResolvedModelConfig resolveConfig(
            AiBusinessCapability capability,
            Long serviceId,
            String serviceRole,
            AiModelId modelId,
            AiModelName requestedModelName) {
        ModelResolution resolution = resolveModel(capability, modelId);
        AiModel model = resolution.model();
        if (model.getApiSource() == null) {
            throw new IllegalArgumentException("AI model apiSource is required: " + model.getId());
        }
        if (isBlank(model.getBaseUrl())) {
            throw new IllegalArgumentException("AI model baseUrl is required: " + model.getId());
        }

        var modelName = model.getModelName();
        if (requestedModelName != null && !requestedModelName.equals(modelName)) {
            throw new IllegalArgumentException(
                    "AI model mismatch: modelId=%s, modelName=%s".formatted(model.getId(), requestedModelName));
        }

        return new ResolvedModelConfig(
                serviceId,
                resolveServiceRole(serviceRole),
                model.getId(),
                model.getApiSource() == null ? null : model.getApiSource().value(),
                model.getBaseUrl(),
                model.getEncryptedApiKey(),
                modelName,
                model.getCapabilities() == null
                        ? new ArrayList<>()
                        : model.getCapabilities().stream()
                                .map(AiModelCapability::value)
                                .toList(),
                parseParameters(model, resolution.config()),
                null);
    }

    private ModelResolution resolveModel(AiBusinessCapability capability, AiModelId modelId) {
        AiBusinessConfig config = resolveBusinessConfig(capability);
        AiModelId effectiveModelId = modelId;
        if (effectiveModelId == null) {
            if (config == null || config.getModelId() == null) {
                throw new IllegalArgumentException("AI modelId is required");
            }
            effectiveModelId = config.getModelId();
        } else if (!matchesModel(effectiveModelId, config)) {
            config = null;
        }
        AiModel model = modelRepository.get(effectiveModelId);
        if (model == null) {
            throw new IllegalArgumentException("AI model not found: " + effectiveModelId);
        }
        if (!model.isEnabled()) {
            throw new IllegalArgumentException("AI model is disabled: " + effectiveModelId);
        }
        return new ModelResolution(model, config);
    }

    private boolean matchesModel(AiModelId modelId, AiBusinessConfig config) {
        return modelId != null && config != null && modelId.equals(config.getModelId());
    }

    private AiBusinessConfig resolveBusinessConfig(AiBusinessCapability capability) {
        if (capability == null) {
            return null;
        }
        List<AiBusinessConfig> configs = businessConfigRepository.list(capability, true);
        return configs.isEmpty() ? null : configs.get(0);
    }

    private JsonNode parseParameters(AiModel model, AiBusinessConfig config) {
        ObjectNode parameters = objectMapper.createObjectNode();
        mergeParameters(parameters, model.getDefaultParamsJson(), "AI model default parameters is not valid JSON");
        if (config != null) {
            mergeParameters(
                    parameters,
                    config.getDefaultParamsJson(),
                    "AI business config default parameters is not valid JSON");
        }
        return parameters;
    }

    private void mergeParameters(ObjectNode target, String parametersJson, String errorMessage) {
        if (isBlank(parametersJson)) {
            return;
        }
        JsonNode parameters;
        try {
            parameters = objectMapper.readTree(parametersJson);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(errorMessage, ex);
        }
        if (!parameters.isObject()) {
            throw new IllegalArgumentException(errorMessage);
        }
        target.setAll((ObjectNode) parameters);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String resolveServiceRole(String serviceRole) {
        return isBlank(serviceRole) ? DEFAULT_SERVICE_ROLE : serviceRole;
    }

    public record ResolvedModelConfig(
            Long serviceId,
            String serviceRole,
            AiModelId modelId,
            String apiSource,
            String baseUrl,
            String apiKey,
            com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName modelName,
            List<String> capabilityTags,
            JsonNode parameters,
            Long timeoutMs) {}

    private record ModelResolution(AiModel model, AiBusinessConfig config) {}
}
