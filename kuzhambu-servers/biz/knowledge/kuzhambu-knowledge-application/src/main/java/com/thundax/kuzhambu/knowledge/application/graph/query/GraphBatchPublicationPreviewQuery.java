package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.util.List;

public record GraphBatchPublicationPreviewQuery(List<ContentRef> materialRefs) {}
