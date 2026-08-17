package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;

public record GraphMaterialListQuery(
        String subjectId,
        String keyword,
        GraphMaterialStatus status,
        String contentType,
        String categoryCode,
        String volumeCode) {}
