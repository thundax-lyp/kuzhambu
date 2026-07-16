package com.thundax.kuzhambu.ai.application.invocation.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.application.config.service.AiServiceConfigApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiServiceConfig;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiWorkerModelConfigResolver {

    private final AiServiceConfigApplicationService serviceConfigService;
    private final AiModelApplicationService modelService;
    private final AiCapabilityApplicationService capabilityService;
    private final ObjectMapper objectMapper;

    public AiWorkerModelConfigResolver(
            AiServiceConfigApplicationService serviceConfigService,
            AiModelApplicationService modelService,
            AiCapabilityApplicationService capabilityService,
            ObjectMapper objectMapper) {
        this.serviceConfigService = serviceConfigService;
        this.modelService = modelService;
        this.capabilityService = capabilityService;
        this.objectMapper = objectMapper;
    }

    public ResolvedModelConfig resolve(AiInvokeCommand command) {
        if (command == null) {
            return null;
        }

        AiModel model = resolveModel(command);
        AiServiceConfig serviceConfig = resolveServiceConfig(command, model);

        if (serviceConfig == null) {
            throw new IllegalArgumentException("AI service config not found: serviceRole=%s, serviceId=%s"
                    .formatted(command.getServiceRole(), command.getServiceId()));
        }
        if (!serviceConfig.isAvailable()) {
            throw new IllegalArgumentException("AI service is unavailable: serviceRole=%s, serviceId=%s"
                    .formatted(serviceConfig.getServiceRole(), serviceConfig.getServiceId()));
        }

        if (model.getServiceId() != null
                && serviceConfig.getServiceId() != null
                && !model.getServiceId().equals(serviceConfig.getServiceId())) {
            throw new IllegalArgumentException("AI service mismatch: serviceRole=%s, modelId=%s"
                    .formatted(serviceConfig.getServiceRole(), model.getModelId()));
        }

        if (!isBlank(command.getModelName()) && !command.getModelName().equals(model.getModelName())) {
            throw new IllegalArgumentException("AI model mismatch: modelId=%s, modelName=%s"
                    .formatted(command.getModelId(), command.getModelName()));
        }

        return new ResolvedModelConfig(
                serviceConfig.getServiceId(),
                serviceConfig.getServiceRole(),
                model.getModelId(),
                serviceConfig.getApiSource(),
                serviceConfig.getBaseUrl(),
                serviceConfig.getEncryptedApiKey(),
                model.getModelName(),
                model.getCapabilityTags() == null ? new ArrayList<>() : new ArrayList<>(model.getCapabilityTags()),
                parseParameters(model),
                null);
    }

    private AiServiceConfig resolveServiceConfig(AiInvokeCommand command, AiModel model) {
        if (!isBlank(command.getServiceRole())) {
            AiServiceConfig byRole = serviceConfigService.getByRole(command.getServiceRole());
            if (byRole == null && command.getServiceId() != null) {
                return serviceConfigService.getByServiceId(command.getServiceId());
            }
            return byRole;
        }
        if (command.getServiceId() != null) {
            return serviceConfigService.getByServiceId(command.getServiceId());
        }
        if (model != null && model.getServiceId() != null) {
            return serviceConfigService.getByServiceId(model.getServiceId());
        }
        return null;
    }

    private AiModel resolveModel(AiInvokeCommand command) {
        if (command.getModelId() == null) {
            AiCapabilityMapping mapping = resolveCapabilityMapping(command);
            if (mapping == null || mapping.getModelId() == null) {
                throw new IllegalArgumentException("AI modelId is required");
            }
            command.setModelId(mapping.getModelId());
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

    private AiCapabilityMapping resolveCapabilityMapping(AiInvokeCommand command) {
        if (capabilityService == null || isBlank(command.getScope()) || isBlank(command.getCapability())) {
            return null;
        }
        AiCapabilityMapping mapping = capabilityService.getMapping(command.getScope(), command.getCapability());
        if (mapping == null) {
            return null;
        }
        if (!mapping.isEnabled()) {
            throw new IllegalArgumentException("AI capability mapping is disabled: scope=%s, capability=%s"
                    .formatted(command.getScope(), command.getCapability()));
        }
        return mapping;
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
