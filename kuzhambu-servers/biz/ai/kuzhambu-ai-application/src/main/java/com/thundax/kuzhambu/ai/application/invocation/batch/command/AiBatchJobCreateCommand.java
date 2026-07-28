package com.thundax.kuzhambu.ai.application.invocation.batch.command;

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
    private String capability;
    private String contentType;
    private Long contentId;
    private int totalCount;
    private String failureSummaryJson;
}
