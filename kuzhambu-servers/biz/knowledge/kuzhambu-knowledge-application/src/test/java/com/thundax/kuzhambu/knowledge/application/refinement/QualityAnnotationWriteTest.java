package com.thundax.kuzhambu.knowledge.application.refinement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteQualityAnnotationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertQualityAnnotationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityAnnotationResult;
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class QualityAnnotationWriteTest {

    @Test
    void upsertAnnotationShouldPersistPayload() {
        FakeQualityAnnotationRepository annotationRepository = new FakeQualityAnnotationRepository();
        KnowledgeGraphRefinementApplicationServiceImpl service = service(annotationRepository);

        QualityAnnotationResult result = service.upsertAnnotation(new UpsertQualityAnnotationCommand(
                501L, "ENTITY", "entity:huangdi", "CLASSIC", 1001L, 88L, "VALID", "ACCURATE", "人工校验通过", 9L));

        assertEquals(501L, result.getAnnotationId());
        assertEquals("ENTITY", result.getObjectType());
        assertEquals("entity:huangdi", result.getObjectKey());
        assertEquals("VALID", result.getAnnotationStatus());
        assertEquals("ACCURATE", result.getAnnotationLabel());
        assertEquals("人工校验通过", result.getComment());
        assertEquals(1, annotationRepository.annotations().size());
        QualityAnnotation stored = annotationRepository.annotations().get(501L);
        assertEquals("CLASSIC", stored.getSourceContentType());
        assertEquals(1001L, stored.getSourceContentId());
        assertEquals(88L, stored.getGraphVersionId());
        assertEquals(9L, stored.getCreatedBy());
        assertEquals(9L, stored.getUpdatedBy());
    }

    @Test
    void deleteAnnotationShouldRemoveStoredRecord() {
        FakeQualityAnnotationRepository annotationRepository = new FakeQualityAnnotationRepository();
        annotationRepository.save(new QualityAnnotation(
                1L,
                501L,
                "RELATION",
                "entity:huangdi->entity:fuxi:ancestor",
                "CLASSIC",
                1001L,
                88L,
                "VALID",
                "ACCURATE",
                "人工校验通过",
                9L,
                null,
                9L,
                null));
        KnowledgeGraphRefinementApplicationServiceImpl service = service(annotationRepository);

        service.deleteAnnotation(new DeleteQualityAnnotationCommand(501L));

        assertNull(annotationRepository.annotations().get(501L));
    }

    private static KnowledgeGraphRefinementApplicationServiceImpl service(
            FakeQualityAnnotationRepository annotationRepository) {
        NoopGraphVersionRepository graphVersionRepository = new NoopGraphVersionRepository();
        NoopKnowledgeEntityRepository entityRepository = new NoopKnowledgeEntityRepository();
        NoopKnowledgeRelationRepository relationRepository = new NoopKnowledgeRelationRepository();
        NoopKnowledgeLineageNodeRepository lineageNodeRepository = new NoopKnowledgeLineageNodeRepository();
        NoopKnowledgeLineageRelationRepository lineageRelationRepository = new NoopKnowledgeLineageRelationRepository();
        return new KnowledgeGraphRefinementApplicationServiceImpl(
                graphVersionRepository,
                new NoopGraphExtractionTaskRepository(),
                new FakeRefinementTaskRepository(),
                new FakeRefinementEntityDraftRepository(),
                new FakeRefinementRelationDraftRepository(),
                new FakeRefinementLineageNodeDraftRepository(),
                new FakeRefinementLineageRelationDraftRepository(),
                annotationRepository,
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
        @Override
        public List<RefinementEntityDraft> listByTaskId(Long refinementTaskId) {
            return List.of();
        }

        @Override
        public void saveOrUpdateBatch(List<RefinementEntityDraft> drafts) {}

        @Override
        public int deleteByTaskId(Long refinementTaskId) {
            return 0;
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
        private final Map<Long, QualityAnnotation> annotations = new LinkedHashMap<>();

        @Override
        public List<QualityAnnotation> listBySource(
                String objectType, String sourceContentType, Long sourceContentId, Long graphVersionId) {
            return annotations.values().stream()
                    .filter(item -> objectType == null || objectType.equals(item.getObjectType()))
                    .filter(item -> sourceContentType == null || sourceContentType.equals(item.getSourceContentType()))
                    .filter(item -> sourceContentId == null || sourceContentId.equals(item.getSourceContentId()))
                    .filter(item -> graphVersionId == null || graphVersionId.equals(item.getGraphVersionId()))
                    .toList();
        }

        @Override
        public void save(QualityAnnotation annotation) {
            annotations.put(annotation.getAnnotationId(), annotation);
        }

        @Override
        public int deleteByAnnotationId(Long annotationId) {
            return annotations.remove(annotationId) == null ? 0 : 1;
        }

        private Map<Long, QualityAnnotation> annotations() {
            return annotations;
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
        public GraphVersionId save(GraphVersion version) {
            return new GraphVersionId(1L);
        }
    }

    private static final class NoopKnowledgeEntityRepository implements KnowledgeEntityRepository {
        @Override
        public List<KnowledgeEntity> listByEntityKeys(Collection<String> entityKeys) {
            return List.of();
        }

        @Override
        public List<KnowledgeEntity> listByVersionId(GraphVersionId versionId) {
            return List.of();
        }

        @Override
        public KnowledgeEntity getByEntityId(KnowledgeEntityId entityId) {
            return null;
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
        public List<KnowledgeRelation> listByVersionId(Long versionId) {
            return List.of();
        }

        @Override
        public KnowledgeRelation getByRelationId(Long relationId) {
            return null;
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
        public List<KnowledgeLineageNode> listByVersionId(Long versionId) {
            return List.of();
        }

        @Override
        public KnowledgeLineageNode getByNodeId(Long nodeId) {
            return null;
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
        public List<KnowledgeLineageRelation> listByVersionId(Long versionId) {
            return List.of();
        }

        @Override
        public KnowledgeLineageRelation getByRelationId(Long relationId) {
            return null;
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
