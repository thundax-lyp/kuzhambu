package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageAiBatchJobsByCapabilitiesQuery {

    private final String scope;
    private final List<AiBusinessCapability> capabilities;
    private final AiBatchJobStatus status;
    private final AiContentRef contentRef;
    private final PageQuery pageQuery;
}
