package com.thundax.kuzhambu.ai.application.config.prompt.command;

import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateSaveCommand {

    private Long id;
    private String capability;
    private String name;
    private String description;
    private boolean enabled = true;
    private String messageTemplatesJson;
    private String variablesSnapshotJson;
    private String outputSchemaJson;
    private String changeSummary;
    private List<VariableItem> variables = new ArrayList<>();

    public PromptTemplate toTemplate() {
        PromptTemplate template = new PromptTemplate();
        template.setId(PromptTemplateIdCodec.toDomain(id));
        template.setCapability(AiBusinessCapability.from(capability));
        template.setName(name);
        template.setDescription(description);
        template.setEnabled(enabled);
        template.setRegisteredAt(Instant.now());
        return template;
    }

    public PromptVersion toVersion(PromptTemplateId effectiveTemplateId, int versionNo, String variablesSnapshotJson) {
        PromptVersion version = new PromptVersion();
        version.setTemplateId(effectiveTemplateId);
        version.setVersionNo(versionNo);
        version.setMessageTemplatesJson(messageTemplatesJson);
        version.setVariablesSnapshotJson(variablesSnapshotJson);
        version.setOutputSchemaJson(outputSchemaJson);
        version.setChangeSummary(changeSummary);
        version.setRegisteredAt(Instant.now());
        return version;
    }

    public List<PromptVariable> toVariables(PromptTemplateId effectiveTemplateId) {
        List<PromptVariable> promptVariables = new ArrayList<>();
        if (variables == null) {
            return promptVariables;
        }
        for (int i = 0; i < variables.size(); i++) {
            VariableItem item = variables.get(i);
            if (item == null) {
                continue;
            }
            PromptVariable variable = new PromptVariable();
            variable.setTemplateId(effectiveTemplateId);
            variable.setVariableName(item.getVariableName());
            variable.setRequired(item.isRequired());
            variable.setDescription(item.getDescription());
            variable.setPriority(item.getPriority() == null ? i + 1 : item.getPriority());
            promptVariables.add(variable);
        }
        return promptVariables;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariableItem {

        private String variableName;
        private boolean required = true;
        private String description;
        private Integer priority;
    }
}
