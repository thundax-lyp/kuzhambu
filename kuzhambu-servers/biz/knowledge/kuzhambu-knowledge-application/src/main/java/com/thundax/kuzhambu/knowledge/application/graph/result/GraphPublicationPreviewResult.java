package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.util.List;

public record GraphPublicationPreviewResult(
        String previewToken,
        ContentRef materialRef,
        long materialLockVersion,
        List<GraphNodePublicationPreviewResult> nodes,
        List<GraphEdgePublicationPreviewResult> edges,
        List<GraphValidationIssueResult> issues,
        boolean publishable) {}
