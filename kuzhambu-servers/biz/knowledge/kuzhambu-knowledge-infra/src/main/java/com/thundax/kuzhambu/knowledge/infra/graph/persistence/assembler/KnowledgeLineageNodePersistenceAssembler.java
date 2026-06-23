package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeLineageNodeDO;
import java.util.List;

public final class KnowledgeLineageNodePersistenceAssembler {

    private KnowledgeLineageNodePersistenceAssembler() {}

    public static KnowledgeLineageNodeDO toObject(KnowledgeLineageNode entity) {
        if (entity == null) {
            return null;
        }
        KnowledgeLineageNodeDO dataObject = new KnowledgeLineageNodeDO();
        dataObject.setId(entity.getId());
        dataObject.setNodeId(entity.getNodeId());
        dataObject.setNodeKey(entity.getNodeKey());
        dataObject.setName(entity.getName());
        dataObject.setNodeType(entity.getNodeType());
        dataObject.setGeneration(entity.getGeneration());
        dataObject.setGender(entity.getGender());
        dataObject.setConfirmationStatus(entity.getConfirmationStatus());
        dataObject.setLatestVersionId(entity.getLatestVersionId());
        dataObject.setSourceRefsJson(entity.getSourceRefsJson());
        dataObject.setFirstExtractedAt(entity.getFirstExtractedAt());
        dataObject.setLastExtractedAt(entity.getLastExtractedAt());
        dataObject.setConfirmedAt(entity.getConfirmedAt());
        return dataObject;
    }

    public static KnowledgeLineageNode toDomain(KnowledgeLineageNodeDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        KnowledgeLineageNode entity = new KnowledgeLineageNode();
        entity.setId(dataObject.getId());
        entity.setNodeId(dataObject.getNodeId());
        entity.setNodeKey(dataObject.getNodeKey());
        entity.setName(dataObject.getName());
        entity.setNodeType(dataObject.getNodeType());
        entity.setGeneration(dataObject.getGeneration());
        entity.setGender(dataObject.getGender());
        entity.setConfirmationStatus(dataObject.getConfirmationStatus());
        entity.setLatestVersionId(dataObject.getLatestVersionId());
        entity.setSourceRefsJson(dataObject.getSourceRefsJson());
        entity.setFirstExtractedAt(dataObject.getFirstExtractedAt());
        entity.setLastExtractedAt(dataObject.getLastExtractedAt());
        entity.setConfirmedAt(dataObject.getConfirmedAt());
        return entity;
    }

    public static List<KnowledgeLineageNode> toDomainList(List<KnowledgeLineageNodeDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream()
                        .map(KnowledgeLineageNodePersistenceAssembler::toDomain)
                        .toList();
    }
}
