package com.thundax.kuzhambu.ai.interfaces.admin.config.assembler;

import com.thundax.kuzhambu.ai.application.config.command.CreateAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.CreateAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeleteAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeleteAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.command.UpdateAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.UpdateAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.query.GetAiBusinessConfigByCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetAiBusinessConfigQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetAiModelQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiBusinessConfigsQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiModelsQuery;
import com.thundax.kuzhambu.ai.domain.config.codec.AiBusinessConfigIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request.AiConfigRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response.AiConfigResponses;
import java.util.Collections;
import java.util.List;

public final class AiConfigInterfaceAssembler {

    private AiConfigInterfaceAssembler() {}

    public static GetAiModelQuery toGetModelQuery(Long value) {
        return new GetAiModelQuery(AiModelIdCodec.toDomain(value));
    }

    public static GetAiModelQuery toGetModelQuery(AiModelId value) {
        return new GetAiModelQuery(value);
    }

    public static ListAiModelsQuery toListModelsQuery(AiConfigRequests.ModelListRequest request) {
        return new ListAiModelsQuery(
                toApiSource(request == null ? null : request.getApiSource()),
                request == null ? null : request.getEnabled());
    }

    public static CreateAiModelCommand toCreateModelCommand(AiConfigRequests.ModelSaveRequest request) {
        return new CreateAiModelCommand(
                AiModelIdCodec.toDomain(request.getId()),
                AiApiSource.from(request.getApiSource()),
                request.getBaseUrl(),
                request.getApiKey(),
                AiModelNameCodec.toDomain(request.getModelName()),
                request.getDisplayName(),
                toModelCapabilities(request.getCapabilities()),
                request.getDefaultParamsJson(),
                request.getDescription(),
                request.getEnabled());
    }

    public static UpdateAiModelCommand toUpdateModelCommand(AiConfigRequests.ModelSaveRequest request) {
        return new UpdateAiModelCommand(
                AiModelIdCodec.toDomain(request.getId()),
                AiApiSource.from(request.getApiSource()),
                request.getBaseUrl(),
                request.getApiKey(),
                AiModelNameCodec.toDomain(request.getModelName()),
                request.getDisplayName(),
                toModelCapabilities(request.getCapabilities()),
                request.getDefaultParamsJson(),
                request.getDescription(),
                request.getEnabled());
    }

    public static DeleteAiModelCommand toDeleteModelCommand(Long value) {
        return new DeleteAiModelCommand(AiModelIdCodec.toDomain(value));
    }

    public static GetAiBusinessConfigQuery toGetBusinessConfigQuery(Long value) {
        return new GetAiBusinessConfigQuery(AiBusinessConfigIdCodec.toDomain(value));
    }

    public static GetAiBusinessConfigQuery toGetBusinessConfigQuery(AiBusinessConfigId value) {
        return new GetAiBusinessConfigQuery(value);
    }

    public static GetAiBusinessConfigByCapabilityQuery toGetBusinessConfigByCapabilityQuery(String value) {
        return new GetAiBusinessConfigByCapabilityQuery(toBusinessCapability(value));
    }

    public static ListAiBusinessConfigsQuery toListBusinessConfigsQuery(
            AiConfigRequests.BusinessConfigListRequest request) {
        return new ListAiBusinessConfigsQuery(
                toBusinessCapability(request == null ? null : request.getCapability()),
                request == null ? null : request.getEnabled());
    }

    public static CreateAiBusinessConfigCommand toCreateBusinessConfigCommand(
            AiConfigRequests.BusinessConfigSaveRequest request) {
        return new CreateAiBusinessConfigCommand(
                AiBusinessConfigIdCodec.toDomain(request.getId()),
                AiBusinessCapability.from(request.getCapability()),
                PromptTemplateIdCodec.toDomain(request.getPromptTemplateId()),
                AiModelIdCodec.toDomain(request.getModelId()),
                request.getDefaultParamsJson(),
                request.getEnabled());
    }

    public static UpdateAiBusinessConfigCommand toUpdateBusinessConfigCommand(
            AiConfigRequests.BusinessConfigSaveRequest request) {
        return new UpdateAiBusinessConfigCommand(
                AiBusinessConfigIdCodec.toDomain(request.getId()),
                AiBusinessCapability.from(request.getCapability()),
                PromptTemplateIdCodec.toDomain(request.getPromptTemplateId()),
                AiModelIdCodec.toDomain(request.getModelId()),
                request.getDefaultParamsJson(),
                request.getEnabled());
    }

    public static DeleteAiBusinessConfigCommand toDeleteBusinessConfigCommand(Long value) {
        return new DeleteAiBusinessConfigCommand(AiBusinessConfigIdCodec.toDomain(value));
    }

    public static AiBusinessCapability toBusinessCapability(String value) {
        return isBlank(value) ? null : AiBusinessCapability.from(value);
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
                .modelName(AiModelNameCodec.toValue(model.getModelName()))
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
                .requiredModelCapabilities(capability.requiredModelCapabilities().stream()
                        .map(AiModelCapability::value)
                        .toList())
                .enabled(true)
                .build();
    }

    public static AiConfigResponses.BusinessConfigResponse toResponse(AiBusinessConfig config) {
        if (config == null) {
            return AiConfigResponses.BusinessConfigResponse.builder().build();
        }
        return AiConfigResponses.BusinessConfigResponse.builder()
                .id(AiBusinessConfigIdCodec.toValue(config.getId()))
                .capability(
                        config.getCapability() == null
                                ? null
                                : config.getCapability().value())
                .promptTemplateId(PromptTemplateIdCodec.toValue(config.getPromptTemplateId()))
                .modelId(AiModelIdCodec.toValue(config.getModelId()))
                .defaultParamsJson(config.getDefaultParamsJson())
                .enabled(config.isEnabled())
                .configuredAt(config.getConfiguredAt())
                .build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static AiApiSource toApiSource(String value) {
        return isBlank(value) ? null : AiApiSource.from(value);
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
