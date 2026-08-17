package com.thundax.kuzhambu.knowledge.application.graph.result;

public record GraphMaterialTreeNodeResult(
        String id,
        String parentId,
        String title,
        String nodeType,
        String contentType,
        String categoryCode,
        String volumeCode,
        boolean leaf) {}
