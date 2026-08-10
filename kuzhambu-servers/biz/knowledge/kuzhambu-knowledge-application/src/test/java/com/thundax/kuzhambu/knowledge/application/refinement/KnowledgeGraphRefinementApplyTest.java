package com.thundax.kuzhambu.knowledge.application.refinement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ApplyRefinementTaskCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementApplyResult;
import com.thundax.kuzhambu.knowledge.application.refinement.service.impl.KnowledgeGraphRefinementApplicationServiceImpl;
import com.thundax.kuzhambu.knowledge.application.refinement.support.KnowledgeRefinementManualKeySupport;
import com.thundax.kuzhambu.knowledge.application.refinement.support.QualitySummaryAggregationSupport;
import com.thundax.kuzhambu.knowledge.application.refinement.support.RefinementApplySupport;
import com.thundax.kuzhambu.knowledge.application.refinement.support.RefinementDraftBootstrapSupport;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionAiCandidateIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionAiCandidateId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionBatchJobId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionSourceContentId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.KnowledgeEntityId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.codec.RefinementTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityAnnotation;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementEntityDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageNodeDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageRelationDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementRelationDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementTask;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.valueobject.RefinementTaskId;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.QualityAnnotationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementEntityDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageNodeDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeGraphRefinementApplyTest {

    @Test
    void applyTaskShouldPersistConfirmedFactsAndMarkTaskApplied() {
        FakeRefinementTaskRepository taskRepository = new FakeRefinementTaskRepository();
        taskRepository.task = new RefinementTask(
                null,
                RefinementTaskIdCodec.toDomain(31L),
                "GRAPH",
                "SANCAI_ENTRY",
                1001L,
                "myth",
                "神话",
                71L,
                "DRAFT",
                9L,
                Instant.now(),
                null,
                null,
                null,
                null,
                null,
                null);
        FakeRefinementEntityDraftRepository entityDraftRepository = new FakeRefinementEntityDraftRepository(List.of(
                entityDraft(31L, 101L, "person:huangdi", "黄帝", "MANUAL_CONFIRMED", "UPDATED"),
                entityDraft(31L, 102L, "person:shennong", "神农", "PENDING", "DELETED")));
        FakeRefinementRelationDraftRepository relationDraftRepository =
                new FakeRefinementRelationDraftRepository(List.of(
                        relationDraft(31L, 201L, "person:huangdi->person:fuxi:ancestor", "MANUAL_CONFIRMED", "UPDATED"),
                        relationDraft(31L, 202L, "person:shennong->person:huangdi:ancestor", "PENDING", "DELETED")));
        FakeRefinementLineageNodeDraftRepository lineageNodeDraftRepository =
                new FakeRefinementLineageNodeDraftRepository(List.of(
                        lineageNodeDraft(31L, 301L, "lineage:huangdi", "MANUAL_CONFIRMED", "UPDATED"),
                        lineageNodeDraft(31L, 302L, "lineage:shennong", "PENDING", "DELETED")));
        FakeRefinementLineageRelationDraftRepository lineageRelationDraftRepository =
                new FakeRefinementLineageRelationDraftRepository(List.of(
                        lineageRelationDraft(
                                31L, 401L, "lineage:huangdi->lineage:fuxi:ancestor", "MANUAL_CONFIRMED", "UPDATED"),
                        lineageRelationDraft(
                                31L, 402L, "lineage:shennong->lineage:huangdi:ancestor", "PENDING", "DELETED")));
        FakeKnowledgeEntityRepository entityRepository = new FakeKnowledgeEntityRepository();
        FakeKnowledgeRelationRepository relationRepository = new FakeKnowledgeRelationRepository();
        FakeKnowledgeLineageNodeRepository lineageNodeRepository = new FakeKnowledgeLineageNodeRepository();
        FakeKnowledgeLineageRelationRepository lineageRelationRepository = new FakeKnowledgeLineageRelationRepository();
        KnowledgeGraphRefinementApplicationServiceImpl service = new KnowledgeGraphRefinementApplicationServiceImpl(
                new FakeGraphVersionRepository(),
                new FakeGraphExtractionTaskRepository(),
                taskRepository,
                entityDraftRepository,
                relationDraftRepository,
                lineageNodeDraftRepository,
                lineageRelationDraftRepository,
                new FakeQualityAnnotationRepository(),
                new RefinementDraftBootstrapSupport(
                        entityRepository, relationRepository, lineageNodeRepository, lineageRelationRepository),
                new RefinementApplySupport(
                        entityRepository, relationRepository, lineageNodeRepository, lineageRelationRepository),
                new QualitySummaryAggregationSupport(),
                new KnowledgeRefinementManualKeySupport());

        RefinementApplyResult result = service.applyTask(new ApplyRefinementTaskCommand(31L, 19L));

        assertEquals("APPLIED", result.getStatus());
        assertEquals(31L, result.getRefinementTaskId());
        assertEquals(71L, result.getGraphVersionId());
        assertEquals(true, result.getGraphRefreshRequired());
        assertEquals(true, result.getRegenerateSupported());
        assertEquals(88L, result.getSourceTaskId());
        assertEquals("{\"sourceContentIds\":[1001]}", result.getSelectionScopeJson());
        assertEquals("REFINEMENT_APPLIED", result.getTriggerSource());
        assertEquals(true, result.getReplaceUnconfirmedOnly());
        assertEquals("OPEN_GRAPH_VERSION", result.getNextAction());
        assertEquals(true, result.getQualityReportRefreshRequired());
        assertEquals("APPLIED", taskRepository.task.getStatus());
        assertEquals(19L, taskRepository.task.getAppliedBy());
        assertEquals(1, entityRepository.saved.size());
        assertEquals("person:huangdi", entityRepository.saved.get(0).getEntityKey());
        assertEquals(List.of("person:shennong"), entityRepository.deletedKeys);
        assertEquals(1, relationRepository.saved.size());
        assertEquals(
                "person:huangdi->person:fuxi:ancestor",
                relationRepository.saved.get(0).getRelationKey());
        assertEquals(List.of("person:shennong->person:huangdi:ancestor"), relationRepository.deletedKeys);
        assertEquals(1, lineageNodeRepository.saved.size());
        assertEquals(List.of("lineage:shennong"), lineageNodeRepository.deletedKeys);
        assertEquals(1, lineageRelationRepository.saved.size());
        assertEquals(List.of("lineage:shennong->lineage:huangdi:ancestor"), lineageRelationRepository.deletedKeys);
    }

    private static RefinementEntityDraft entityDraft(
            Long taskId, Long entityId, String entityKey, String name, String status, String operationType) {
        RefinementEntityDraft draft = new RefinementEntityDraft();
        draft.setRefinementTaskId(taskId);
        draft.setEntityId(entityId);
        draft.setEntityKey(entityKey);
        draft.setName(name);
        draft.setEntityType("PERSON");
        draft.setDescription("desc");
        draft.setConfirmationStatus(status);
        draft.setOperationType(operationType);
        return draft;
    }

    private static RefinementRelationDraft relationDraft(
            Long taskId, Long relationId, String relationKey, String status, String operationType) {
        RefinementRelationDraft draft = new RefinementRelationDraft();
        draft.setRefinementTaskId(taskId);
        draft.setRelationId(relationId);
        draft.setRelationKey(relationKey);
        draft.setSourceEntityKey("person:source");
        draft.setTargetEntityKey("person:target");
        draft.setSourceName("source");
        draft.setTargetName("target");
        draft.setRelationType("ANCESTOR");
        draft.setEvidence("evidence");
        draft.setConfirmationStatus(status);
        draft.setOperationType(operationType);
        return draft;
    }

    private static RefinementLineageNodeDraft lineageNodeDraft(
            Long taskId, Long nodeId, String nodeKey, String status, String operationType) {
        RefinementLineageNodeDraft draft = new RefinementLineageNodeDraft();
        draft.setRefinementTaskId(taskId);
        draft.setNodeId(nodeId);
        draft.setNodeKey(nodeKey);
        draft.setName("node");
        draft.setNodeType("PERSON");
        draft.setGeneration(1);
        draft.setGender("MALE");
        draft.setConfirmationStatus(status);
        draft.setOperationType(operationType);
        return draft;
    }

    private static RefinementLineageRelationDraft lineageRelationDraft(
            Long taskId, Long relationId, String relationKey, String status, String operationType) {
        RefinementLineageRelationDraft draft = new RefinementLineageRelationDraft();
        draft.setRefinementTaskId(taskId);
        draft.setRelationId(relationId);
        draft.setRelationKey(relationKey);
        draft.setSourceNodeKey("node:source");
        draft.setTargetNodeKey("node:target");
        draft.setSourceName("source");
        draft.setTargetName("target");
        draft.setRelationType("ANCESTOR");
        draft.setEvidence("evidence");
        draft.setConfirmationStatus(status);
        draft.setOperationType(operationType);
        return draft;
    }

    private static final class FakeGraphVersionRepository implements GraphVersionRepository {
        @Override
        public GraphVersion findLatest(
                GraphExtractionTaskType taskType,
                String sourceContentType,
                GraphExtractionSourceContentId sourceContentId) {
            return null;
        }

        @Override
        public GraphVersion getByVersionId(GraphVersionId versionId) {
            return new GraphVersion(
                    versionId,
                    GraphExtractionTaskIdCodec.toDomain(88L),
                    GraphExtractionAiCandidateIdCodec.toDomain(12L),
                    GraphExtractionTaskType.GRAPH,
                    "CONTENT",
                    "{}",
                    "SANCAI_ENTRY",
                    GraphExtractionSourceContentIdCodec.toDomain(1001L),
                    "myth",
                    "神话",
                    3,
                    GraphVersionStatus.APPLIED,
                    Instant.now());
        }

        @Override
        public GraphVersion getByTaskCandidate(GraphExtractionTaskId taskId, GraphExtractionAiCandidateId candidateId) {
            return null;
        }

        @Override
        public PageResult<GraphVersion> page(
                GraphExtractionTaskType taskType,
                GraphVersionStatus status,
                String sourceContentType,
                GraphExtractionSourceContentId sourceContentId,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, List.of());
        }

        @Override
        public GraphVersionId save(GraphVersion entity) {
            return new GraphVersionId(0L);
        }
    }

    private static final class FakeGraphExtractionTaskRepository implements GraphExtractionTaskRepository {
        @Override
        public GraphExtractionTask getByTaskId(GraphExtractionTaskId taskId) {
            GraphExtractionTask task = new GraphExtractionTask();
            task.setId(taskId);
            task.setSelectionScopeJson("{\"sourceContentIds\":[1001]}");
            return task;
        }

        @Override
        public GraphExtractionTaskId save(GraphExtractionTask entity) {
            return entity == null ? null : entity.getId();
        }

        @Override
        public int update(GraphExtractionTask entity) {
            return 1;
        }

        @Override
        public List<GraphExtractionTask> listByBatchJobId(GraphExtractionBatchJobId batchJobId) {
            return List.of();
        }

        @Override
        public PageResult<GraphExtractionTask> page(
                String taskType,
                GraphExtractionBatchJobId batchJobId,
                String triggerSource,
                String status,
                String sourceContentType,
                GraphExtractionSourceContentId sourceContentId,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, List.of());
        }
    }

    private static final class FakeRefinementTaskRepository implements RefinementTaskRepository {
        private RefinementTask task;

        @Override
        public RefinementTask getByTaskId(RefinementTaskId taskId) {
            return task;
        }

        @Override
        public RefinementTask findLatestDraft(
                String taskType, String sourceContentType, Long sourceContentId, Long graphVersionId) {
            return task;
        }

        @Override
        public PageResult<RefinementTask> page(
                String taskType,
                String sourceContentType,
                Long sourceContentId,
                String sourceCategoryCode,
                String status,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, task == null ? 0 : 1, task == null ? List.of() : List.of(task));
        }

        @Override
        public Long save(RefinementTask entity) {
            task = entity;
            return 1L;
        }

        @Override
        public int update(RefinementTask entity) {
            task = entity;
            return 1;
        }
    }

    private static final class FakeRefinementEntityDraftRepository implements RefinementEntityDraftRepository {
        private final List<RefinementEntityDraft> drafts;

        private FakeRefinementEntityDraftRepository(List<RefinementEntityDraft> drafts) {
            this.drafts = new ArrayList<>(drafts);
        }

        @Override
        public List<RefinementEntityDraft> listByTaskId(Long refinementTaskId) {
            return new ArrayList<>(drafts);
        }

        @Override
        public void saveOrUpdateBatch(List<RefinementEntityDraft> drafts) {}

        @Override
        public int deleteByTaskId(Long refinementTaskId) {
            return 0;
        }
    }

    private static final class FakeRefinementRelationDraftRepository implements RefinementRelationDraftRepository {
        private final List<RefinementRelationDraft> drafts;

        private FakeRefinementRelationDraftRepository(List<RefinementRelationDraft> drafts) {
            this.drafts = new ArrayList<>(drafts);
        }

        @Override
        public List<RefinementRelationDraft> listByTaskId(Long refinementTaskId) {
            return new ArrayList<>(drafts);
        }

        @Override
        public void saveOrUpdateBatch(List<RefinementRelationDraft> drafts) {}

        @Override
        public int deleteByTaskId(Long refinementTaskId) {
            return 0;
        }
    }

    private static final class FakeRefinementLineageNodeDraftRepository
            implements RefinementLineageNodeDraftRepository {
        private final List<RefinementLineageNodeDraft> drafts;

        private FakeRefinementLineageNodeDraftRepository(List<RefinementLineageNodeDraft> drafts) {
            this.drafts = new ArrayList<>(drafts);
        }

        @Override
        public List<RefinementLineageNodeDraft> listByTaskId(Long refinementTaskId) {
            return new ArrayList<>(drafts);
        }

        @Override
        public void saveOrUpdateBatch(List<RefinementLineageNodeDraft> drafts) {}

        @Override
        public int deleteByTaskId(Long refinementTaskId) {
            return 0;
        }
    }

    private static final class FakeRefinementLineageRelationDraftRepository
            implements RefinementLineageRelationDraftRepository {
        private final List<RefinementLineageRelationDraft> drafts;

        private FakeRefinementLineageRelationDraftRepository(List<RefinementLineageRelationDraft> drafts) {
            this.drafts = new ArrayList<>(drafts);
        }

        @Override
        public List<RefinementLineageRelationDraft> listByTaskId(Long refinementTaskId) {
            return new ArrayList<>(drafts);
        }

        @Override
        public void saveOrUpdateBatch(List<RefinementLineageRelationDraft> drafts) {}

        @Override
        public int deleteByTaskId(Long refinementTaskId) {
            return 0;
        }
    }

    private static final class FakeQualityAnnotationRepository implements QualityAnnotationRepository {
        @Override
        public List<QualityAnnotation> listBySource(
                String objectType, String sourceContentType, Long sourceContentId, Long graphVersionId) {
            return List.of();
        }

        @Override
        public void saveOrUpdate(QualityAnnotation annotation) {}

        @Override
        public int deleteByAnnotationId(Long annotationId) {
            return 0;
        }
    }

    private static final class FakeKnowledgeEntityRepository implements KnowledgeEntityRepository {
        private final List<KnowledgeEntity> saved = new ArrayList<>();
        private final List<String> deletedKeys = new ArrayList<>();

        @Override
        public List<KnowledgeEntity> listByEntityKeys(Collection<String> entityKeys) {
            return List.of();
        }

        @Override
        public KnowledgeEntity getByEntityId(KnowledgeEntityId entityId) {
            return null;
        }

        @Override
        public List<KnowledgeEntity> listByVersionId(GraphVersionId versionId) {
            return List.of();
        }

        @Override
        public PageResult<KnowledgeEntity> page(
                GraphVersionId versionId,
                String keyword,
                String entityType,
                KnowledgeConfirmationStatus confirmationStatus,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, List.of());
        }

        @Override
        public void saveOrUpdateBatch(List<KnowledgeEntity> entities) {
            saved.clear();
            saved.addAll(entities);
        }

        @Override
        public int deleteByEntityKeys(Collection<String> entityKeys) {
            deletedKeys.clear();
            if (entityKeys != null) {
                deletedKeys.addAll(entityKeys);
            }
            return deletedKeys.size();
        }
    }

    private static final class FakeKnowledgeRelationRepository implements KnowledgeRelationRepository {
        private final List<KnowledgeRelation> saved = new ArrayList<>();
        private final List<String> deletedKeys = new ArrayList<>();

        @Override
        public List<KnowledgeRelation> listByRelationKeys(Collection<String> relationKeys) {
            return List.of();
        }

        @Override
        public KnowledgeRelation getByRelationId(Long relationId) {
            return null;
        }

        @Override
        public List<KnowledgeRelation> listByVersionId(Long versionId) {
            return List.of();
        }

        @Override
        public PageResult<KnowledgeRelation> page(
                Long versionId,
                String keyword,
                String relationType,
                String confirmationStatus,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, List.of());
        }

        @Override
        public void saveOrUpdateBatch(List<KnowledgeRelation> relations) {
            saved.clear();
            saved.addAll(relations);
        }

        @Override
        public int deleteByRelationKeys(Collection<String> relationKeys) {
            deletedKeys.clear();
            if (relationKeys != null) {
                deletedKeys.addAll(relationKeys);
            }
            return deletedKeys.size();
        }
    }

    private static final class FakeKnowledgeLineageNodeRepository implements KnowledgeLineageNodeRepository {
        private final List<KnowledgeLineageNode> saved = new ArrayList<>();
        private final List<String> deletedKeys = new ArrayList<>();

        @Override
        public List<KnowledgeLineageNode> listByNodeKeys(Collection<String> nodeKeys) {
            return List.of();
        }

        @Override
        public KnowledgeLineageNode getByNodeId(Long nodeId) {
            return null;
        }

        @Override
        public List<KnowledgeLineageNode> listByVersionId(Long versionId) {
            return List.of();
        }

        @Override
        public PageResult<KnowledgeLineageNode> page(
                Long versionId, String keyword, String nodeType, String confirmationStatus, int pageNo, int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, List.of());
        }

        @Override
        public void saveOrUpdateBatch(List<KnowledgeLineageNode> nodes) {
            saved.clear();
            saved.addAll(nodes);
        }

        @Override
        public int deleteByNodeKeys(Collection<String> nodeKeys) {
            deletedKeys.clear();
            if (nodeKeys != null) {
                deletedKeys.addAll(nodeKeys);
            }
            return deletedKeys.size();
        }
    }

    private static final class FakeKnowledgeLineageRelationRepository implements KnowledgeLineageRelationRepository {
        private final List<KnowledgeLineageRelation> saved = new ArrayList<>();
        private final List<String> deletedKeys = new ArrayList<>();

        @Override
        public List<KnowledgeLineageRelation> listByRelationKeys(Collection<String> relationKeys) {
            return List.of();
        }

        @Override
        public KnowledgeLineageRelation getByRelationId(Long relationId) {
            return null;
        }

        @Override
        public List<KnowledgeLineageRelation> listByVersionId(Long versionId) {
            return List.of();
        }

        @Override
        public PageResult<KnowledgeLineageRelation> page(
                Long versionId,
                String keyword,
                String relationType,
                String confirmationStatus,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, List.of());
        }

        @Override
        public void saveOrUpdateBatch(List<KnowledgeLineageRelation> relations) {
            saved.clear();
            saved.addAll(relations);
        }

        @Override
        public int deleteByRelationKeys(Collection<String> relationKeys) {
            deletedKeys.clear();
            if (relationKeys != null) {
                deletedKeys.addAll(relationKeys);
            }
            return deletedKeys.size();
        }
    }
}
