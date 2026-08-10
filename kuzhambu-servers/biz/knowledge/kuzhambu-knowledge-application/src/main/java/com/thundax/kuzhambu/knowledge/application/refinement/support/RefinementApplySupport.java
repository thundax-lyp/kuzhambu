package com.thundax.kuzhambu.knowledge.application.refinement.support;

import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.KnowledgeEntityIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementEntityDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageNodeDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageRelationDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementRelationDraft;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RefinementApplySupport {

    private final KnowledgeEntityRepository entityRepository;
    private final KnowledgeRelationRepository relationRepository;
    private final KnowledgeLineageNodeRepository lineageNodeRepository;
    private final KnowledgeLineageRelationRepository lineageRelationRepository;

    public RefinementApplySupport(
            KnowledgeEntityRepository entityRepository,
            KnowledgeRelationRepository relationRepository,
            KnowledgeLineageNodeRepository lineageNodeRepository,
            KnowledgeLineageRelationRepository lineageRelationRepository) {
        this.entityRepository = entityRepository;
        this.relationRepository = relationRepository;
        this.lineageNodeRepository = lineageNodeRepository;
        this.lineageRelationRepository = lineageRelationRepository;
    }

    public void applyEntities(Long versionId, List<RefinementEntityDraft> drafts) {
        entityRepository.deleteByEntityKeys(entityKeys(drafts));
        entityRepository.batchSaveOrUpdate(drafts.stream()
                .filter(draft -> !"DELETED".equals(draft.getOperationType()))
                .map(draft -> toEntity(versionId, draft))
                .toList());
    }

    public void applyRelations(Long versionId, List<RefinementRelationDraft> drafts) {
        relationRepository.deleteByRelationKeys(relationKeys(drafts));
        relationRepository.batchSaveOrUpdate(drafts.stream()
                .filter(draft -> !"DELETED".equals(draft.getOperationType()))
                .map(draft -> toRelation(versionId, draft))
                .toList());
    }

    public void applyLineageNodes(Long versionId, List<RefinementLineageNodeDraft> drafts) {
        lineageNodeRepository.deleteByNodeKeys(lineageNodeKeys(drafts));
        lineageNodeRepository.saveOrUpdateBatch(drafts.stream()
                .filter(draft -> !"DELETED".equals(draft.getOperationType()))
                .map(draft -> toLineageNode(versionId, draft))
                .toList());
    }

    public void applyLineageRelations(Long versionId, List<RefinementLineageRelationDraft> drafts) {
        lineageRelationRepository.deleteByRelationKeys(lineageRelationKeys(drafts));
        lineageRelationRepository.saveOrUpdateBatch(drafts.stream()
                .filter(draft -> !"DELETED".equals(draft.getOperationType()))
                .map(draft -> toLineageRelation(versionId, draft))
                .toList());
    }

    private List<String> entityKeys(List<RefinementEntityDraft> drafts) {
        return drafts == null
                ? List.of()
                : drafts.stream()
                        .filter(draft -> "DELETED".equals(draft.getOperationType()))
                        .map(RefinementEntityDraft::getEntityKey)
                        .toList();
    }

    private List<String> relationKeys(List<RefinementRelationDraft> drafts) {
        return drafts == null
                ? List.of()
                : drafts.stream()
                        .filter(draft -> "DELETED".equals(draft.getOperationType()))
                        .map(RefinementRelationDraft::getRelationKey)
                        .toList();
    }

    private List<String> lineageNodeKeys(List<RefinementLineageNodeDraft> drafts) {
        return drafts == null
                ? List.of()
                : drafts.stream()
                        .filter(draft -> "DELETED".equals(draft.getOperationType()))
                        .map(RefinementLineageNodeDraft::getNodeKey)
                        .toList();
    }

    private List<String> lineageRelationKeys(List<RefinementLineageRelationDraft> drafts) {
        return drafts == null
                ? List.of()
                : drafts.stream()
                        .filter(draft -> "DELETED".equals(draft.getOperationType()))
                        .map(RefinementLineageRelationDraft::getRelationKey)
                        .toList();
    }

    private KnowledgeEntity toEntity(Long versionId, RefinementEntityDraft draft) {
        Instant now = Instant.now();
        return new KnowledgeEntity(
                KnowledgeEntityIdCodec.toDomain(draft.getEntityId()),
                draft.getEntityKey(),
                draft.getName(),
                draft.getEntityType(),
                draft.getDescription(),
                KnowledgeConfirmationStatus.from(draft.getConfirmationStatus()),
                GraphVersionIdCodec.toDomain(versionId),
                draft.getSourceRefsJson(),
                now,
                now,
                "MANUAL_CONFIRMED".equals(draft.getConfirmationStatus()) ? now : null);
    }

    private KnowledgeRelation toRelation(Long versionId, RefinementRelationDraft draft) {
        Instant now = Instant.now();
        return new KnowledgeRelation(
                draft.getRelationId(),
                draft.getRelationKey(),
                draft.getSourceEntityKey(),
                draft.getTargetEntityKey(),
                draft.getSourceName(),
                draft.getTargetName(),
                draft.getRelationType(),
                draft.getEvidence(),
                draft.getConfirmationStatus(),
                versionId,
                draft.getSourceRefsJson(),
                now,
                now,
                "MANUAL_CONFIRMED".equals(draft.getConfirmationStatus()) ? now : null);
    }

    private KnowledgeLineageNode toLineageNode(Long versionId, RefinementLineageNodeDraft draft) {
        Instant now = Instant.now();
        return new KnowledgeLineageNode(
                draft.getNodeId(),
                draft.getNodeKey(),
                draft.getName(),
                draft.getNodeType(),
                draft.getGeneration(),
                draft.getGender(),
                draft.getConfirmationStatus(),
                versionId,
                draft.getSourceRefsJson(),
                now,
                now,
                "MANUAL_CONFIRMED".equals(draft.getConfirmationStatus()) ? now : null);
    }

    private KnowledgeLineageRelation toLineageRelation(Long versionId, RefinementLineageRelationDraft draft) {
        Instant now = Instant.now();
        return new KnowledgeLineageRelation(
                draft.getRelationId(),
                draft.getRelationKey(),
                draft.getSourceNodeKey(),
                draft.getTargetNodeKey(),
                draft.getSourceName(),
                draft.getTargetName(),
                draft.getRelationType(),
                draft.getEvidence(),
                draft.getConfirmationStatus(),
                versionId,
                draft.getSourceRefsJson(),
                now,
                now,
                "MANUAL_CONFIRMED".equals(draft.getConfirmationStatus()) ? now : null);
    }
}
