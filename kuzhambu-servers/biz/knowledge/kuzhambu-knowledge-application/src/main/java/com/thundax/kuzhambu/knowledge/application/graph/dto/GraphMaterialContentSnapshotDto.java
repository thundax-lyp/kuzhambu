package com.thundax.kuzhambu.knowledge.application.graph.dto;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.util.List;

public record GraphMaterialContentSnapshotDto(
        ContentRef contentRef,
        String categoryName,
        String volumeName,
        String title,
        String summary,
        List<String> textSegments,
        List<String> tagNames,
        String status,
        String visibility,
        Integer currentVersionNo) {

    public GraphMaterialContentSnapshotDto(
            ContentRef contentRef,
            String title,
            String summary,
            List<String> textSegments,
            List<String> tagNames,
            String status,
            String visibility,
            Integer currentVersionNo) {
        this(contentRef, null, null, title, summary, textSegments, tagNames, status, visibility, currentVersionNo);
    }
}
