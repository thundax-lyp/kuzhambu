package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.util.List;

public record GraphTaskQuery(
        String keyword,
        String contentType,
        String categoryCode,
        String volumeCode,
        List<ContentRef> contentRefs,
        String batchId,
        String executionStatus,
        String disposition,
        String groupBy) {}
