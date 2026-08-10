package com.thundax.kuzhambu.knowledge.application.refinement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementEntityResult;
import com.thundax.kuzhambu.knowledge.application.refinement.service.impl.KnowledgeGraphRefinementApplicationServiceImpl;
import com.thundax.kuzhambu.knowledge.application.refinement.support.KnowledgeRefinementManualKeySupport;
import com.thundax.kuzhambu.knowledge.application.refinement.support.QualitySummaryAggregationSupport;
import com.thundax.kuzhambu.knowledge.application.refinement.support.RefinementApplySupport;
import com.thundax.kuzhambu.knowledge.application.refinement.support.RefinementDraftBootstrapSupport;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionAiCandidateId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionSourceContentId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.KnowledgeEntityId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KnowledgeGraphRefinementEntityWriteTest {

    @Test
    void upsertEntityShouldGenerateManualKeyForNewDraft() {
        FakeRefinementEntityDraftRepository entityDraftRepository = new FakeRefinementEntityDraftRepository();
        KnowledgeGraphRefinementApplicationServiceImpl service = service(entityDraftRepository);

        RefinementEntityResult result = service.upsertEntity(
                new UpsertRefinementEntityCommand(31L, null, null, "黄帝", "PERSON", "始祖", "[{\"entryId\":1}]", 1, 9L));

        assertTrue(result.getEntityKey().startsWith("manual:entity:"));
        assertEquals("MANUAL_CREATED", result.getOriginType());
        assertEquals("ADDED", result.getOperationType());
        assertEquals("PENDING", result.getConfirmationStatus());
        assertEquals(1, entityDraftRepository.listByTaskId(31L).size());
    }

    @Test
    void confirmEntityShouldUpdateDraftStatusOnly() {
        FakeRefinementEntityDraftRepository entityDraftRepository = new FakeRefinementEntityDraftRepository();
        entityDraftRepository.saveOrUpdateBatch(
                List.of(entityDraft(31L, 101L, "person:huangdi", "PENDING", "UPDATED")));
        KnowledgeGraphRefinementApplicationServiceImpl service = service(entityDraftRepository);

        RefinementEntityResult result =
                service.confirmEntity(new ConfirmRefinementEntityCommand(31L, "person:huangdi", 9L));

        assertEquals("MANUAL_CONFIRMED", result.getConfirmationStatus());
        assertEquals("CONFIRMED", result.getOperationType());
        assertEquals(
                "MANUAL_CONFIRMED",
                entityDraftRepository.listByTaskId(31L).get(0).getConfirmationStatus());
    }

    @Test
    void deleteEntityShouldMarkDraftAsDeleted() {
        FakeRefinementEntityDraftRepository entityDraftRepository = new FakeRefinementEntityDraftRepository();
        entityDraftRepository.saveOrUpdateBatch(
                List.of(entityDraft(31L, 101L, "person:huangdi", "PENDING", "UPDATED")));
        KnowledgeGraphRefinementApplicationServiceImpl service = service(entityDraftRepository);

        service.deleteEntity(new DeleteRefinementEntityCommand(31L, "person:huangdi", 9L));

        assertEquals("DELETED", entityDraftRepository.listByTaskId(31L).get(0).getOperationType());
    }

    private static RefinementEntityDraft entityDraft(
            Long taskId, Long entityId, String entityKey, String confirmationStatus, String operationType) {
        RefinementEntityDraft draft = new RefinementEntityDraft();
        draft.setRefinementTaskId(taskId);
        draft.setEntityId(entityId);
        draft.setEntityKey(entityKey);
        draft.setName("黄帝");
        draft.setEntityType("PERSON");
        draft.setDescription("始祖");
        draft.setConfirmationStatus(confirmationStatus);
        draft.setOperationType(operationType);
        draft.setOriginType("AI_EXTRACTED");
        return draft;
    }

    private static KnowledgeGraphRefinementApplicationServiceImpl service(
            FakeRefinementEntityDraftRepository entityDraftRepository) {
        NoopGraphVersionRepository graphVersionRepository = new NoopGraphVersionRepository();
        NoopKnowledgeEntityRepository entityRepository = new NoopKnowledgeEntityRepository();
        NoopKnowledgeRelationRepository relationRepository = new NoopKnowledgeRelationRepository();
        NoopKnowledgeLineageNodeRepository lineageNodeRepository = new NoopKnowledgeLineageNodeRepository();
        NoopKnowledgeLineageRelationRepository lineageRelationRepository = new NoopKnowledgeLineageRelationRepository();
        return new KnowledgeGraphRefinementApplicationServiceImpl(
                graphVersionRepository,
                new NoopGraphExtractionTaskRepository(),
                new FakeRefinementTaskRepository(),
                entityDraftRepository,
                new FakeRefinementRelationDraftRepository(),
                new FakeRefinementLineageNodeDraftRepository(),
                new FakeRefinementLineageRelationDraftRepository(),
                new FakeQualityAnnotationRepository(),
                new RefinementDraftBootstrapSupport(
                        entityRepository, relationRepository, lineageNodeRepository, lineageRelationRepository),
                new RefinementApplySupport(
                        entityRepository, relationRepository, lineageNodeRepository, lineageRelationRepository),
                new QualitySummaryAggregationSupport(),
                new KnowledgeRefinementManualKeySupport());
    }

    private static final class FakeRefinementTaskRepository implements RefinementTaskRepository {
        @Override
        public RefinementTask getByTaskId(RefinementTaskId taskId) {
            return null;
        }

        @Override
        public RefinementTask getByLatestDraft(
                String taskType, String sourceContentType, Long sourceContentId, Long graphVersionId) {
            return null;
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
            return PageResult.of(pageNo, pageSize, 0, List.of());
        }

        @Override
        public Long save(RefinementTask entity) {
            return 1L;
        }

        @Override
        public int update(RefinementTask entity) {
            return 1;
        }
    }

    private static final class FakeRefinementEntityDraftRepository implements RefinementEntityDraftRepository {
        private final Map<Long, List<RefinementEntityDraft>> draftsByTaskId = new LinkedHashMap<>();
        private long nextDraftId = 1L;

        @Override
        public List<RefinementEntityDraft> listByTaskId(Long refinementTaskId) {
            return new ArrayList<>(draftsByTaskId.getOrDefault(refinementTaskId, List.of()));
        }

        @Override
        public void saveOrUpdateBatch(List<RefinementEntityDraft> drafts) {
            if (drafts == null || drafts.isEmpty()) {
                return;
            }
            Long taskId = drafts.get(0).getRefinementTaskId();
            List<RefinementEntityDraft> stored = new ArrayList<>();
            for (RefinementEntityDraft draft : drafts) {
                if (draft.getDraftId() == null) {
                    draft.setDraftId(nextDraftId++);
                }
                stored.add(draft);
            }
            draftsByTaskId.put(taskId, stored);
        }

        @Override
        public int deleteByTaskId(Long refinementTaskId) {
            return draftsByTaskId.remove(refinementTaskId) == null ? 0 : 1;
        }
    }

    private static final class FakeRefinementRelationDraftRepository implements RefinementRelationDraftRepository {
        @Override
        public List<RefinementRelationDraft> listByTaskId(Long refinementTaskId) {
            return List.of();
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
        @Override
        public List<RefinementLineageNodeDraft> listByTaskId(Long refinementTaskId) {
            return List.of();
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
        @Override
        public List<RefinementLineageRelationDraft> listByTaskId(Long refinementTaskId) {
            return List.of();
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
        public void save(QualityAnnotation annotation) {}

        @Override
        public int deleteByAnnotationId(Long annotationId) {
            return 0;
        }
    }

    private static final class NoopGraphVersionRepository implements GraphVersionRepository {
        @Override
        public GraphVersion getByLatestSource(
                GraphExtractionTaskType taskType,
                String sourceContentType,
                GraphExtractionSourceContentId sourceContentId) {
            return null;
        }

        @Override
        public GraphVersion getByVersionId(GraphVersionId versionId) {
            return new GraphVersion();
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

    private static final class NoopKnowledgeEntityRepository implements KnowledgeEntityRepository {
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
        public void batchSaveOrUpdate(List<KnowledgeEntity> entities) {}

        @Override
        public int deleteByEntityKeys(Collection<String> entityKeys) {
            return 0;
        }
    }

    private static final class NoopKnowledgeRelationRepository implements KnowledgeRelationRepository {
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
        public void batchSaveOrUpdate(List<KnowledgeRelation> relations) {}

        @Override
        public int deleteByRelationKeys(Collection<String> relationKeys) {
            return 0;
        }
    }

    private static final class NoopKnowledgeLineageNodeRepository implements KnowledgeLineageNodeRepository {
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
        public void batchSaveOrUpdate(List<KnowledgeLineageNode> nodes) {}

        @Override
        public int deleteByNodeKeys(Collection<String> nodeKeys) {
            return 0;
        }
    }

    private static final class NoopKnowledgeLineageRelationRepository implements KnowledgeLineageRelationRepository {
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
        public void batchSaveOrUpdate(List<KnowledgeLineageRelation> relations) {}

        @Override
        public int deleteByRelationKeys(Collection<String> relationKeys) {
            return 0;
        }
    }
}
