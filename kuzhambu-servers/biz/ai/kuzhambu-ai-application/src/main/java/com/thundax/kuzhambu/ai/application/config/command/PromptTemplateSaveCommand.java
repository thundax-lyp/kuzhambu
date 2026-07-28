package com.thundax.kuzhambu.ai.application.config.command;

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
