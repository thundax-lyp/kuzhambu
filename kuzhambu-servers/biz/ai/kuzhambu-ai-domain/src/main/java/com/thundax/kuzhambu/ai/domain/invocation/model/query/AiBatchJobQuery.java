package com.thundax.kuzhambu.ai.domain.invocation.model.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiBatchJobQuery {

    private final String scope;
    private final AiBusinessCapability capability;
    private final List<AiBusinessCapability> capabilities;
    private final AiBatchJobStatus status;
    private final String contentType;
    private final Long contentId;
    private final int pageNo;
    private final int pageSize;

    public AiBatchJobQuery(
            String scope,
            AiBusinessCapability capability,
            AiBatchJobStatus status,
            String contentType,
            Long contentId,
            int pageNo,
            int pageSize) {
        this(scope, capability, null, status, contentType, contentId, pageNo, pageSize);
    }

    public AiBatchJobQuery(
            String scope,
            List<AiBusinessCapability> capabilities,
            AiBatchJobStatus status,
            String contentType,
            Long contentId,
            int pageNo,
            int pageSize) {
        this(scope, null, capabilities, status, contentType, contentId, pageNo, pageSize);
    }
}
