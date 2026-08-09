package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.common.core.page.PageQuery;

public record PageAiBatchJobsQuery(
        String scope,
        AiBusinessCapability capability,
        AiBatchJobStatus status,
        AiContentRef contentRef,
        PageQuery pageQuery) {}
