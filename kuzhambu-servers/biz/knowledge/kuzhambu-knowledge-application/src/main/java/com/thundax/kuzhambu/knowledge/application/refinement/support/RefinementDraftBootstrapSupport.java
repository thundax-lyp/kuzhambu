package com.thundax.kuzhambu.knowledge.application.refinement.support;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementEntityDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageNodeDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageRelationDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementRelationDraft;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RefinementDraftBootstrapSupport {

    private final KnowledgeEntityRepository entityRepository;
    private final KnowledgeRelationRepository relationRepository;
    private final KnowledgeLineageNodeRepository lineageNodeRepository;
    private final KnowledgeLineageRelationRepository lineageRelationRepository;

    public RefinementDraftBootstrapSupport(
            KnowledgeEntityRepository entityRepository,
            KnowledgeRelationRepository relationRepository,
            KnowledgeLineageNodeRepository lineageNodeRepository,
            KnowledgeLineageRelationRepository lineageRelationRepository) {
        this.entityRepository = entityRepository;
        this.relationRepository = relationRepository;
        this.lineageNodeRepository = lineageNodeRepository;
        this.lineageRelationRepository = lineageRelationRepository;
    }

    public List<RefinementEntityDraft> bootstrapEntityDrafts(Long refinementTaskId, Long versionId, Long operatorId) {
        Date now = new Date();
        List<RefinementEntityDraft> drafts = new ArrayList<>();
        int sortOrder = 1;
        for (KnowledgeEntity entity : entityRepository.listByVersionId(versionId)) {
            drafts.add(new RefinementEntityDraft(
                    null,
                    null,
                    refinementTaskId,
                    entity.getEntityId(),
                    entity.getEntityKey(),
                    "AI_EXTRACTED",
                    "UNCHANGED",
                    entity.getName(),
                    entity.getEntityType(),
                    entity.getDescription(),
                    entity.getConfirmationStatus(),
                    entity.getSourceRefsJson(),
                    sortOrder++,
                    operatorId,
                    now,
                    operatorId,
                    now));
        }
        return drafts;
    }

    public List<RefinementRelationDraft> bootstrapRelationDrafts(
            Long refinementTaskId, Long versionId, Long operatorId) {
        Date now = new Date();
        List<RefinementRelationDraft> drafts = new ArrayList<>();
        int sortOrder = 1;
        for (KnowledgeRelation relation : relationRepository.listByVersionId(versionId)) {
            drafts.add(new RefinementRelationDraft(
                    null,
                    null,
                    refinementTaskId,
                    relation.getRelationId(),
                    relation.getRelationKey(),
                    "AI_EXTRACTED",
                    "UNCHANGED",
                    relation.getSourceEntityKey(),
                    relation.getTargetEntityKey(),
                    relation.getSourceName(),
                    relation.getTargetName(),
                    relation.getRelationType(),
                    relation.getEvidence(),
                    relation.getConfirmationStatus(),
                    relation.getSourceRefsJson(),
                    sortOrder++,
                    operatorId,
                    now,
                    operatorId,
                    now));
        }
        return drafts;
    }

    public List<RefinementLineageNodeDraft> bootstrapLineageNodeDrafts(
            Long refinementTaskId, Long versionId, Long operatorId) {
        Date now = new Date();
        List<RefinementLineageNodeDraft> drafts = new ArrayList<>();
        int sortOrder = 1;
        for (KnowledgeLineageNode node : lineageNodeRepository.listByVersionId(versionId)) {
            drafts.add(new RefinementLineageNodeDraft(
                    null,
                    null,
                    refinementTaskId,
                    node.getNodeId(),
                    node.getNodeKey(),
                    "AI_EXTRACTED",
                    "UNCHANGED",
                    node.getName(),
                    node.getNodeType(),
                    node.getGeneration(),
                    node.getGender(),
                    node.getConfirmationStatus(),
                    node.getSourceRefsJson(),
                    sortOrder++,
                    operatorId,
                    now,
                    operatorId,
                    now));
        }
        return drafts;
    }

    public List<RefinementLineageRelationDraft> bootstrapLineageRelationDrafts(
            Long refinementTaskId, Long versionId, Long operatorId) {
        Date now = new Date();
        List<RefinementLineageRelationDraft> drafts = new ArrayList<>();
        int sortOrder = 1;
        for (KnowledgeLineageRelation relation : lineageRelationRepository.listByVersionId(versionId)) {
            drafts.add(new RefinementLineageRelationDraft(
                    null,
                    null,
                    refinementTaskId,
                    relation.getRelationId(),
                    relation.getRelationKey(),
                    "AI_EXTRACTED",
                    "UNCHANGED",
                    relation.getSourceNodeKey(),
                    relation.getTargetNodeKey(),
                    relation.getSourceName(),
                    relation.getTargetName(),
                    relation.getRelationType(),
                    relation.getEvidence(),
                    relation.getConfirmationStatus(),
                    relation.getSourceRefsJson(),
                    sortOrder++,
                    operatorId,
                    now,
                    operatorId,
                    now));
        }
        return drafts;
    }
}
