package com.thundax.kuzhambu.ai.application.invocation.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.ai.application.config.business.service.AiBusinessConfigApplicationService;
import com.thundax.kuzhambu.ai.application.config.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiWorkerModelConfigResolver {

    private final AiBusinessConfigApplicationService businessConfigService;
    private final AiModelApplicationService modelService;
    private final ObjectMapper objectMapper;

    public AiWorkerModelConfigResolver(
            AiBusinessConfigApplicationService businessConfigService,
            AiModelApplicationService modelService,
            ObjectMapper objectMapper) {
        this.businessConfigService = businessConfigService;
        this.modelService = modelService;
        this.objectMapper = objectMapper;
    }

    public ResolvedModelConfig resolve(AiInvokeCommand command) {
        if (command == null) {
            return null;
        }

        ModelResolution resolution = resolveModel(command);
        AiModel model = resolution.model();
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
                parseParameters(model, resolution.config()),
                null);
    }

    private ModelResolution resolveModel(AiInvokeCommand command) {
        AiBusinessConfig config = resolveBusinessConfig(command);
        if (command.getModelId() == null) {
            if (config == null || config.getModelId() == null) {
                throw new IllegalArgumentException("AI modelId is required");
            }
            command.setModelId(value(config.getModelId()));
        } else if (!matchesModel(command.getModelId(), config)) {
            config = null;
        }
        AiModel model = modelService.get(command.getModelId());
        if (model == null) {
            throw new IllegalArgumentException("AI model not found: " + command.getModelId());
        }
        if (!model.isEnabled()) {
            throw new IllegalArgumentException("AI model is disabled: " + command.getModelId());
        }
        return new ModelResolution(model, config);
    }

    private boolean matchesModel(Long modelId, AiBusinessConfig config) {
        return modelId != null && config != null && modelId.equals(value(config.getModelId()));
    }

    private AiBusinessConfig resolveBusinessConfig(AiInvokeCommand command) {
        if (isBlank(command.getCapability())) {
            return null;
        }
        List<AiBusinessConfig> configs = businessConfigService.list(command.getCapability(), true);
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

    private record ModelResolution(AiModel model, AiBusinessConfig config) {}
}
