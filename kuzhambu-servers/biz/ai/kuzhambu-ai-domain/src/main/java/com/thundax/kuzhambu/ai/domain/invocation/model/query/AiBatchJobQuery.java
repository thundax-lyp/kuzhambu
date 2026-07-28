package com.thundax.kuzhambu.ai.domain.invocation.model.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiBatchJobQuery {

    private final String scope;
    private final AiBusinessCapability capability;
    private final AiBatchJobStatus status;
    private final String contentType;
    private final Long contentId;
    private final int pageNo;
    private final int pageSize;
}
