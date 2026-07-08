package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegenerateGraphExtractionCommand {
    private String taskType;
    private GraphExtractionTaskId sourceTaskId;
    private String triggerSource;
    private String selectionScopeJson;
    private Boolean replaceUnconfirmedOnly;
    private Long requestedBy;
}
