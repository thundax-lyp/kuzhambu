package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.util.List;

public record GraphMaterialContentSnapshot(
        ContentRef contentRef,
        String title,
        String summary,
        List<String> textSegments,
        List<String> tagNames,
        String status,
        String visibility,
        Integer currentVersionNo) {}
