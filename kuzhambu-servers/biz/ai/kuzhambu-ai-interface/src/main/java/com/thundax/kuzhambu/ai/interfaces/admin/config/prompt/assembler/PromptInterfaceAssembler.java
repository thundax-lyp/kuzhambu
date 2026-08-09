package com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.assembler;

import com.thundax.kuzhambu.ai.application.config.command.BuildPromptOptimizationSuggestionCommand;
import com.thundax.kuzhambu.ai.application.config.command.ChangePromptTemplateStatusCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeletePromptTemplateCommand;
import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateSaveCommand;
import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateVariableItem;
import com.thundax.kuzhambu.ai.application.config.command.RollbackPromptVersionCommand;
import com.thundax.kuzhambu.ai.application.config.command.ValidatePromptVariablesCommand;
import com.thundax.kuzhambu.ai.application.config.query.GetCurrentPromptVersionQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetPromptByCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetPromptQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptCapabilityVariablesQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptVariablesQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptVersionsQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptsQuery;
import com.thundax.kuzhambu.ai.application.config.query.PromptVersionCompareQuery;
import com.thundax.kuzhambu.ai.application.config.result.PromptCapabilityVariableResult;
import com.thundax.kuzhambu.ai.application.config.result.PromptVersionResult;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVariableIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.request.PromptRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.response.PromptResponses;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class PromptInterfaceAssembler {

    private PromptInterfaceAssembler() {}

    @NonNull
    public static GetPromptQuery toGetPromptQuery(@NonNull PromptRequests.TemplateIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new GetPromptQuery(toTemplateId(request.getId()));
    }

    @NonNull
    public static GetPromptQuery toGetPromptQuery(@NonNull PromptTemplateId templateId) {
        Objects.requireNonNull(templateId, "templateId must not be null");
        return new GetPromptQuery(templateId);
    }

    @NonNull
    public static ChangePromptTemplateStatusCommand toChangeStatusCommand(
            @NonNull PromptRequests.TemplateStatusRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ChangePromptTemplateStatusCommand(toTemplateId(request.getId()), request.getEnabled());
    }

    @NonNull
    public static DeletePromptTemplateCommand toDeleteCommand(@NonNull PromptRequests.TemplateIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new DeletePromptTemplateCommand(toTemplateId(request.getId()));
    }

    @NonNull
    public static ListPromptCapabilityVariablesQuery toListCapabilityVariablesQuery(
            @NonNull PromptRequests.CapabilityVariableListRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ListPromptCapabilityVariablesQuery(toCapability(request.getCapability()));
    }

    @NonNull
    public static GetPromptByCapabilityQuery toGetPromptByCapabilityQuery(
            @NonNull PromptRequests.TemplateQueryRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new GetPromptByCapabilityQuery(toCapability(request.getCapability()));
    }

    @NonNull
    public static ListPromptsQuery toListPromptsQuery(@NonNull PromptRequests.TemplateQueryRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ListPromptsQuery(toCapability(request.getCapability()), request.getEnabled());
    }

    @NonNull
    public static PromptTemplateSaveCommand toSaveCommand(@NonNull PromptRequests.TemplateSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new PromptTemplateSaveCommand(
                PromptTemplateIdCodec.toDomain(request.getId()),
                AiBusinessCapability.from(request.getCapability()),
                request.getName(),
                request.getDescription(),
                request.getEnabled() == null || request.getEnabled(),
                request.getMessageTemplatesJson(),
                request.getVariablesSnapshotJson(),
                request.getOutputSchemaJson(),
                request.getChangeSummary(),
                toVariableItems(request.getVariables()));
    }

    @NonNull
    public static GetCurrentPromptVersionQuery toGetCurrentPromptVersionQuery(
            @NonNull PromptRequests.TemplateIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new GetCurrentPromptVersionQuery(toTemplateId(request.getId()));
    }

    @NonNull
    public static ListPromptVersionsQuery toListPromptVersionsQuery(@NonNull PromptRequests.TemplateIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ListPromptVersionsQuery(toTemplateId(request.getId()));
    }

    @NonNull
    public static PromptVersionCompareQuery toCompareQuery(@NonNull PromptRequests.VersionCompareRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new PromptVersionCompareQuery(
                PromptTemplateIdCodec.toDomain(request.getId()),
                request.getLeftVersionNo(),
                request.getRightVersionNo());
    }

    @NonNull
    public static RollbackPromptVersionCommand toRollbackPromptVersionCommand(
            @NonNull PromptRequests.VersionRollbackRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RollbackPromptVersionCommand(toTemplateId(request.getId()), request.getVersionNo());
    }

    @NonNull
    public static ListPromptVariablesQuery toListPromptVariablesQuery(
            @NonNull PromptRequests.TemplateIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ListPromptVariablesQuery(toTemplateId(request.getId()));
    }

    @NonNull
    public static ValidatePromptVariablesCommand toValidatePromptVariablesCommand(
            @NonNull PromptRequests.VariableValidateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ValidatePromptVariablesCommand(toTemplateId(request.getId()), request.getProvidedNames());
    }

    @NonNull
    public static BuildPromptOptimizationSuggestionCommand toBuildOptimizationSuggestionCommand(
            @NonNull PromptRequests.OptimizationRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new BuildPromptOptimizationSuggestionCommand(toTemplateId(request.getId()), request.getChangeSummary());
    }

    private static PromptTemplateId toTemplateId(Long value) {
        return PromptTemplateIdCodec.toDomain(value);
    }

    private static AiBusinessCapability toCapability(String value) {
        return isBlank(value) ? null : AiBusinessCapability.from(value);
    }

    @NonNull
    public static PromptResponses.TemplateResponse toResponse(@NonNull PromptTemplate template) {
        Objects.requireNonNull(template, "template must not be null");
        return PromptResponses.TemplateResponse.builder()
                .id(PromptTemplateIdCodec.toValue(template.getId()))
                .capability(
                        template.getCapability() == null
                                ? null
                                : template.getCapability().value())
                .name(template.getName())
                .description(template.getDescription())
                .enabled(template.isEnabled())
                .currentVersionNo(template.getCurrentVersionNo())
                .registeredAt(template.getRegisteredAt())
                .build();
    }

    @NonNull
    public static PromptResponses.VersionResponse toResponse(@NonNull PromptVersionResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return PromptResponses.VersionResponse.builder()
                .id(PromptVersionIdCodec.toValue(result.getId()))
                .templateId(PromptTemplateIdCodec.toValue(result.getTemplateId()))
                .versionNo(result.getVersionNo())
                .messageTemplatesJson(result.getMessageTemplatesJson())
                .variablesSnapshotJson(result.getVariablesSnapshotJson())
                .outputSchemaJson(result.getOutputSchemaJson())
                .changeSummary(result.getChangeSummary())
                .registeredAt(result.getRegisteredAt())
                .build();
    }

    @NonNull
    public static PromptResponses.VariableResponse toResponse(@NonNull PromptVariable variable) {
        Objects.requireNonNull(variable, "variable must not be null");
        return PromptResponses.VariableResponse.builder()
                .id(PromptVariableIdCodec.toValue(variable.getId()))
                .templateId(PromptTemplateIdCodec.toValue(variable.getTemplateId()))
                .variableName(variable.getVariableName())
                .required(variable.isRequired())
                .description(variable.getDescription())
                .build();
    }

    @NonNull
    public static PromptResponses.CapabilityVariableResponse toResponse(
            @NonNull PromptCapabilityVariableResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return PromptResponses.CapabilityVariableResponse.builder()
                .variableName(result.variableName())
                .required(result.required())
                .description(result.description())
                .build();
    }

    private static List<PromptTemplateVariableItem> toVariableItems(List<PromptRequests.VariableItemRequest> requests) {
        List<PromptTemplateVariableItem> items = new ArrayList<>();
        if (requests == null) {
            return items;
        }
        for (PromptRequests.VariableItemRequest request : requests) {
            if (request == null) {
                continue;
            }
            items.add(new PromptTemplateVariableItem(
                    request.getVariableName(),
                    request.getRequired() == null || request.getRequired(),
                    request.getDescription(),
                    null));
        }
        return items;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
