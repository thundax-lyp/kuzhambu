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

public final class PromptInterfaceAssembler {

    private PromptInterfaceAssembler() {}

    public static GetPromptQuery toGetPromptQuery(PromptRequests.TemplateIdRequest request) {
        return new GetPromptQuery(toTemplateId(request == null ? null : request.getId()));
    }

    public static GetPromptQuery toGetPromptQuery(PromptTemplateId templateId) {
        return new GetPromptQuery(templateId);
    }

    public static ChangePromptTemplateStatusCommand toChangeStatusCommand(
            PromptRequests.TemplateStatusRequest request) {
        return new ChangePromptTemplateStatusCommand(toTemplateId(request.getId()), request.getEnabled());
    }

    public static DeletePromptTemplateCommand toDeleteCommand(PromptRequests.TemplateIdRequest request) {
        return new DeletePromptTemplateCommand(toTemplateId(request.getId()));
    }

    public static ListPromptCapabilityVariablesQuery toListCapabilityVariablesQuery(
            PromptRequests.CapabilityVariableListRequest request) {
        return new ListPromptCapabilityVariablesQuery(toCapability(request.getCapability()));
    }

    public static GetPromptByCapabilityQuery toGetPromptByCapabilityQuery(PromptRequests.TemplateQueryRequest request) {
        return new GetPromptByCapabilityQuery(toCapability(request == null ? null : request.getCapability()));
    }

    public static ListPromptsQuery toListPromptsQuery(PromptRequests.TemplateQueryRequest request) {
        if (request == null) {
            return new ListPromptsQuery(null, null);
        }
        return new ListPromptsQuery(toCapability(request.getCapability()), request.getEnabled());
    }

    public static PromptTemplateSaveCommand toSaveCommand(PromptRequests.TemplateSaveRequest request) {
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

    public static GetCurrentPromptVersionQuery toGetCurrentPromptVersionQuery(
            PromptRequests.TemplateIdRequest request) {
        return new GetCurrentPromptVersionQuery(toTemplateId(request == null ? null : request.getId()));
    }

    public static ListPromptVersionsQuery toListPromptVersionsQuery(PromptRequests.TemplateIdRequest request) {
        return new ListPromptVersionsQuery(toTemplateId(request == null ? null : request.getId()));
    }

    public static PromptVersionCompareQuery toCompareQuery(PromptRequests.VersionCompareRequest request) {
        return new PromptVersionCompareQuery(
                PromptTemplateIdCodec.toDomain(request.getId()),
                request.getLeftVersionNo(),
                request.getRightVersionNo());
    }

    public static RollbackPromptVersionCommand toRollbackPromptVersionCommand(
            PromptRequests.VersionRollbackRequest request) {
        if (request == null) {
            return new RollbackPromptVersionCommand(null, 0);
        }
        return new RollbackPromptVersionCommand(toTemplateId(request.getId()), request.getVersionNo());
    }

    public static ListPromptVariablesQuery toListPromptVariablesQuery(PromptRequests.TemplateIdRequest request) {
        return new ListPromptVariablesQuery(toTemplateId(request == null ? null : request.getId()));
    }

    public static ValidatePromptVariablesCommand toValidatePromptVariablesCommand(
            PromptRequests.VariableValidateRequest request) {
        if (request == null) {
            return new ValidatePromptVariablesCommand(null, null);
        }
        return new ValidatePromptVariablesCommand(toTemplateId(request.getId()), request.getProvidedNames());
    }

    public static BuildPromptOptimizationSuggestionCommand toBuildOptimizationSuggestionCommand(
            PromptRequests.OptimizationRequest request) {
        if (request == null) {
            return new BuildPromptOptimizationSuggestionCommand(null, null);
        }
        return new BuildPromptOptimizationSuggestionCommand(toTemplateId(request.getId()), request.getChangeSummary());
    }

    public static PromptTemplateId toTemplateId(Long value) {
        return PromptTemplateIdCodec.toDomain(value);
    }

    public static AiBusinessCapability toCapability(String value) {
        return isBlank(value) ? null : AiBusinessCapability.from(value);
    }

    public static PromptResponses.TemplateResponse toResponse(PromptTemplate template) {
        if (template == null) {
            return PromptResponses.TemplateResponse.builder().build();
        }
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

    public static PromptResponses.VersionResponse toResponse(PromptVersionResult result) {
        if (result == null) {
            return PromptResponses.VersionResponse.builder().build();
        }
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

    public static PromptResponses.VariableResponse toResponse(PromptVariable variable) {
        if (variable == null) {
            return PromptResponses.VariableResponse.builder().build();
        }
        return PromptResponses.VariableResponse.builder()
                .id(PromptVariableIdCodec.toValue(variable.getId()))
                .templateId(PromptTemplateIdCodec.toValue(variable.getTemplateId()))
                .variableName(variable.getVariableName())
                .required(variable.isRequired())
                .description(variable.getDescription())
                .build();
    }

    public static PromptResponses.CapabilityVariableResponse toResponse(PromptCapabilityVariableResult result) {
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
