package com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.assembler;

import com.thundax.kuzhambu.ai.application.config.prompt.command.PromptTemplateSaveCommand;
import com.thundax.kuzhambu.ai.application.config.prompt.query.PromptVersionCompareQuery;
import com.thundax.kuzhambu.ai.application.config.prompt.result.PromptVersionResult;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVariableId;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.request.PromptRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.response.PromptResponses;
import java.util.ArrayList;
import java.util.List;

public final class PromptInterfaceAssembler {

    private PromptInterfaceAssembler() {}

    public static PromptTemplateSaveCommand toSaveCommand(PromptRequests.TemplateSaveRequest request) {
        PromptTemplateSaveCommand command = new PromptTemplateSaveCommand();
        command.setId(request.getId());
        command.setScope(request.getScope());
        command.setCapability(request.getCapability());
        command.setName(request.getName());
        command.setDescription(request.getDescription());
        command.setStatus(defaultString(request.getStatus(), "ACTIVE"));
        command.setMessageTemplatesJson(request.getMessageTemplatesJson());
        command.setVariablesSnapshotJson(request.getVariablesSnapshotJson());
        command.setOutputSchemaJson(request.getOutputSchemaJson());
        command.setChangeSummary(request.getChangeSummary());
        command.setVariables(toVariableItems(request.getVariables()));
        return command;
    }

    public static PromptVersionCompareQuery toCompareQuery(PromptRequests.VersionCompareRequest request) {
        PromptVersionCompareQuery query = new PromptVersionCompareQuery();
        query.setTemplateId(request.getId());
        query.setLeftVersionNo(request.getLeftVersionNo());
        query.setRightVersionNo(request.getRightVersionNo());
        return query;
    }

    public static PromptResponses.TemplateResponse toResponse(PromptTemplate template) {
        if (template == null) {
            return PromptResponses.TemplateResponse.builder().build();
        }
        return PromptResponses.TemplateResponse.builder()
                .id(value(template.getId()))
                .scope(template.getScope())
                .capability(template.getCapability())
                .name(template.getName())
                .description(template.getDescription())
                .status(template.getStatus())
                .currentVersionNo(template.getCurrentVersionNo())
                .registeredAt(template.getRegisteredAt())
                .build();
    }

    public static PromptResponses.VersionResponse toResponse(PromptVersionResult result) {
        if (result == null) {
            return PromptResponses.VersionResponse.builder().build();
        }
        return PromptResponses.VersionResponse.builder()
                .id(result.getId())
                .templateId(result.getTemplateId())
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
                .id(value(variable.getId()))
                .templateId(value(variable.getTemplateId()))
                .variableName(variable.getVariableName())
                .required(variable.isRequired())
                .description(variable.getDescription())
                .priority(variable.getPriority())
                .build();
    }

    private static List<PromptTemplateSaveCommand.VariableItem> toVariableItems(
            List<PromptRequests.VariableItemRequest> requests) {
        List<PromptTemplateSaveCommand.VariableItem> items = new ArrayList<>();
        if (requests == null) {
            return items;
        }
        for (PromptRequests.VariableItemRequest request : requests) {
            if (request == null) {
                continue;
            }
            PromptTemplateSaveCommand.VariableItem item = new PromptTemplateSaveCommand.VariableItem();
            item.setVariableName(request.getVariableName());
            item.setRequired(request.getRequired() == null || request.getRequired());
            item.setDescription(request.getDescription());
            item.setPriority(request.getPriority());
            items.add(item);
        }
        return items;
    }

    private static String defaultString(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private static Long value(PromptTemplateId id) {
        return id == null ? null : id.value();
    }

    private static Long value(PromptVariableId id) {
        return id == null ? null : id.value();
    }
}
