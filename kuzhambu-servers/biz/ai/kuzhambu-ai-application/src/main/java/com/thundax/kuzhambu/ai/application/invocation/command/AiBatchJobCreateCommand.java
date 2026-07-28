package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiBatchJobCreateCommand {

    private String scope;
    private AiBusinessCapability capability;
    private AiContentRef contentRef;
    private int totalCount;
    private String failureSummaryJson;
}
