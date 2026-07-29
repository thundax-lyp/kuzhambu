package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import java.util.List;

public class PageAiBatchJobsByCapabilitiesQuery {

    private final String scope;
    private final List<AiBusinessCapability> capabilities;
    private final AiBatchJobStatus status;
    private final AiContentRef contentRef;
    private final PageQuery pageQuery;

    public PageAiBatchJobsByCapabilitiesQuery(
            String scope,
            List<AiBusinessCapability> capabilities,
            AiBatchJobStatus status,
            AiContentRef contentRef,
            PageQuery pageQuery) {
        this.scope = scope;
        this.capabilities = capabilities;
        this.status = status;
        this.contentRef = contentRef;
        this.pageQuery = pageQuery;
    }

    public String getScope() {
        return scope;
    }

    public List<AiBusinessCapability> getCapabilities() {
        return capabilities;
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
