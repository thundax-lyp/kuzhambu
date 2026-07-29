package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.common.core.page.PageQuery;

public class PageAiBatchJobsQuery {

    private final String scope;
    private final AiBusinessCapability capability;
    private final AiBatchJobStatus status;
    private final AiContentRef contentRef;
    private final PageQuery pageQuery;

    public PageAiBatchJobsQuery(
            String scope,
            AiBusinessCapability capability,
            AiBatchJobStatus status,
            AiContentRef contentRef,
            PageQuery pageQuery) {
        this.scope = scope;
        this.capability = capability;
        this.status = status;
        this.contentRef = contentRef;
        this.pageQuery = pageQuery;
    }

    public String getScope() {
        return scope;
    }

    public AiBusinessCapability getCapability() {
        return capability;
    }

    public AiBatchJobStatus getStatus() {
        return status;
    }

    public AiContentRef getContentRef() {
        return contentRef;
    }

    public PageQuery getPageQuery() {
        return pageQuery;
    }
}
