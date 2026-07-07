package com.thundax.kuzhambu.knowledge.application.refinement.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReextractLowQualityCategoryCommand {
    private Long reportId;
    private String sourceCategoryCode;
    private String taskType;
    private Boolean replaceUnconfirmedOnly;
    private Long modelId;
    private String modelName;
    private String promptMessagesJson;
    private String inputPayloadJson;
    private Long requestedBy;
}
