package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;

public record GraphMaterialSourceResult(
        ContentRef contentRef,
        String title,
        String summary,
        String contentType,
        String categoryCode,
        String categoryName,
        String volumeCode,
        String volumeName,
        boolean graphable) {}
