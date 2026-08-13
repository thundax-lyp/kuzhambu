package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.util.List;

public record GraphPublicationPreviewResult(
        ContentRef materialRef,
        long materialLockVersion,
        List<GraphNodePublicationPlanResult> nodes,
        List<GraphEdgePublicationPlanResult> edges,
        List<GraphValidationIssueResult> issues,
        boolean publishable) {}
