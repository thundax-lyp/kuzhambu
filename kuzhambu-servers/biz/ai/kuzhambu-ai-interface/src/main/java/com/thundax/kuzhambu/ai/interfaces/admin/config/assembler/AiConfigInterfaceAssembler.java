package com.thundax.kuzhambu.ai.interfaces.admin.config.assembler;

import com.thundax.kuzhambu.ai.application.config.command.CreateAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.CreateAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeleteAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeleteAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.command.UpdateAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.UpdateAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.query.GetAiBusinessConfigByCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetAiBusinessConfigQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetAiCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetAiModelQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiBusinessConfigsQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiCapabilitiesQuery;
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
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class AiConfigInterfaceAssembler {

    private AiConfigInterfaceAssembler() {}

    @NonNull
    public static GetAiModelQuery toGetModelQuery(@NonNull Long value) {
        Objects.requireNonNull(value, "value must not be null");
        return new GetAiModelQuery(AiModelIdCodec.toDomain(value));
    }

    @NonNull
    public static GetAiModelQuery toGetModelQuery(@NonNull AiModelId value) {
        Objects.requireNonNull(value, "value must not be null");
        return new GetAiModelQuery(value);
    }

    @NonNull
    public static ListAiModelsQuery toListModelsQuery(@NonNull AiConfigRequests.ModelListRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ListAiModelsQuery(toApiSource(request.getApiSource()), request.getEnabled());
    }

    @NonNull
    public static CreateAiModelCommand toCreateModelCommand(@NonNull AiConfigRequests.ModelSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
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

    @NonNull
    public static UpdateAiModelCommand toUpdateModelCommand(@NonNull AiConfigRequests.ModelSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
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

    @NonNull
    public static DeleteAiModelCommand toDeleteModelCommand(@NonNull Long value) {
        Objects.requireNonNull(value, "value must not be null");
        return new DeleteAiModelCommand(AiModelIdCodec.toDomain(value));
    }

    @NonNull
    public static GetAiCapabilityQuery toGetCapabilityQuery(@NonNull AiConfigRequests.CapabilityQueryRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new GetAiCapabilityQuery(toBusinessCapability(request.getCapability()));
    }

    @NonNull
    public static ListAiCapabilitiesQuery toListCapabilitiesQuery(
            @NonNull AiConfigRequests.CapabilityQueryRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ListAiCapabilitiesQuery(request.getEnabled());
    }

    @NonNull
    public static GetAiBusinessConfigQuery toGetBusinessConfigQuery(@NonNull Long value) {
        Objects.requireNonNull(value, "value must not be null");
        return new GetAiBusinessConfigQuery(AiBusinessConfigIdCodec.toDomain(value));
    }

    @NonNull
    public static GetAiBusinessConfigQuery toGetBusinessConfigQuery(@NonNull AiBusinessConfigId value) {
        Objects.requireNonNull(value, "value must not be null");
        return new GetAiBusinessConfigQuery(value);
    }

    @NonNull
    public static GetAiBusinessConfigByCapabilityQuery toGetBusinessConfigByCapabilityQuery(@NonNull String value) {
        Objects.requireNonNull(value, "value must not be null");
        return new GetAiBusinessConfigByCapabilityQuery(toBusinessCapability(value));
    }

    @NonNull
    public static ListAiBusinessConfigsQuery toListBusinessConfigsQuery(
            @NonNull AiConfigRequests.BusinessConfigListRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ListAiBusinessConfigsQuery(toBusinessCapability(request.getCapability()), request.getEnabled());
    }

    @NonNull
    public static CreateAiBusinessConfigCommand toCreateBusinessConfigCommand(
            @NonNull AiConfigRequests.BusinessConfigSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new CreateAiBusinessConfigCommand(
                AiBusinessConfigIdCodec.toDomain(request.getId()),
                AiBusinessCapability.from(request.getCapability()),
                PromptTemplateIdCodec.toDomain(request.getPromptTemplateId()),
                AiModelIdCodec.toDomain(request.getModelId()),
                request.getDefaultParamsJson(),
                request.getEnabled());
    }

    @NonNull
    public static UpdateAiBusinessConfigCommand toUpdateBusinessConfigCommand(
            @NonNull AiConfigRequests.BusinessConfigSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new UpdateAiBusinessConfigCommand(
                AiBusinessConfigIdCodec.toDomain(request.getId()),
                AiBusinessCapability.from(request.getCapability()),
                PromptTemplateIdCodec.toDomain(request.getPromptTemplateId()),
                AiModelIdCodec.toDomain(request.getModelId()),
                request.getDefaultParamsJson(),
                request.getEnabled());
    }

    @NonNull
    public static DeleteAiBusinessConfigCommand toDeleteBusinessConfigCommand(@NonNull Long value) {
        Objects.requireNonNull(value, "value must not be null");
        return new DeleteAiBusinessConfigCommand(AiBusinessConfigIdCodec.toDomain(value));
    }

    private static AiBusinessCapability toBusinessCapability(String value) {
        return isBlank(value) ? null : AiBusinessCapability.from(value);
    }

    @NonNull
    public static AiConfigResponses.ModelResponse toResponse(@NonNull AiModel model) {
        Objects.requireNonNull(model, "model must not be null");
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

    @NonNull
    public static AiConfigResponses.CapabilityResponse toResponse(@NonNull AiBusinessCapability capability) {
        Objects.requireNonNull(capability, "capability must not be null");
        return AiConfigResponses.CapabilityResponse.builder()
                .capability(capability.value())
                .name(capability.value())
                .requiredTags(Collections.emptyList())
                .requiredModelCapabilities(capability.requiredModelCapabilities().stream()
                        .map(AiModelCapability::value)
                        .toList())
                .enabled(true)
                .build();
    }

    @NonNull
    public static AiConfigResponses.BusinessConfigResponse toResponse(@NonNull AiBusinessConfig config) {
        Objects.requireNonNull(config, "config must not be null");
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
