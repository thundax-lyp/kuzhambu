package com.thundax.kuzhambu.knowledge.application.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.support.KnowledgeGraphCandidateApplySupport;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionAiCandidateId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionSourceContentId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KnowledgeGraphCandidateApplySupportTest {

    @Test
    void applyShouldCreateVersionAndPersistGraphFacts() {
        FakeGraphVersionRepository versionRepository = new FakeGraphVersionRepository();
        FakeKnowledgeEntityRepository entityRepository = new FakeKnowledgeEntityRepository();
        FakeKnowledgeRelationRepository relationRepository = new FakeKnowledgeRelationRepository();
        KnowledgeGraphCandidateApplySupport support = new KnowledgeGraphCandidateApplySupport(
                versionRepository,
                entityRepository,
                relationRepository,
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository());
        GraphExtractionTask task = new GraphExtractionTask();
        task.setId(GraphExtractionTaskIdCodec.toDomain(11L));
        task.setTaskType(GraphExtractionTaskType.GRAPH);
        task.setScopeType("ENTRY");
        task.setScopeJson("{\"entryIds\":[1]}");
        task.setSourceContentType("SANCAI_ENTRY");
        task.setSourceContentId(GraphExtractionSourceContentIdCodec.toDomain(1L));
        AiCandidateFacadeDto candidate = AiCandidateFacadeDto.builder()
                .candidateId(22L)
                .resultFormat("STRUCTURED")
                .resultPayload("{\"entities\":[{\"name\":\"黄帝\",\"entityType\":\"PERSON\",\"description\":\"始祖\"}],"
                        + "\"relations\":[{\"sourceName\":\"黄帝\",\"targetName\":\"伏羲\",\"relationType\":\"ANCESTOR\",\"evidence\":\"谱系\"}],"
                        + "\"entryRefs\":[{\"entryId\":1}]}")
                .build();

        GraphVersion version = support.apply(task, candidate);

        assertNotNull(version.getId());
        assertEquals(1, version.getVersionNo());
        assertEquals(1, entityRepository.saved.size());
        assertEquals("person:黄帝", entityRepository.saved.get(0).getEntityKey());
        assertEquals(1, relationRepository.saved.size());
        assertEquals(
                "auto:黄帝->auto:伏羲:ancestor", relationRepository.saved.get(0).getRelationKey());
    }

    private static final class FakeGraphVersionRepository implements GraphVersionRepository {
        private final List<GraphVersion> versions = new ArrayList<>();

        @Override
        public GraphVersion findLatest(
                GraphExtractionTaskType taskType,
                String sourceContentType,
                GraphExtractionSourceContentId sourceContentId) {
            return versions.stream().reduce((first, second) -> second).orElse(null);
        }

        @Override
        public GraphVersion getByTaskCandidate(GraphExtractionTaskId taskId, GraphExtractionAiCandidateId candidateId) {
            return versions.stream()
                    .filter(version -> version.getTaskId().equals(taskId)
                            && version.getCandidateId().equals(candidateId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public GraphVersion getByVersionId(GraphVersionId versionId) {
            return versions.stream()
                    .filter(version -> versionId.equals(version.getId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public PageResult<GraphVersion> page(
                GraphExtractionTaskType taskType,
                GraphVersionStatus status,
                String sourceContentType,
                GraphExtractionSourceContentId sourceContentId,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, versions.size(), versions);
        }

        @Override
        public GraphVersionId save(GraphVersion entity) {
            long versionId = versions.size() + 1L;
            entity.setId(GraphVersionIdCodec.toDomain(versionId));
            versions.add(entity);
            return entity.getId();
        }
    }

    private static final class FakeKnowledgeEntityRepository implements KnowledgeEntityRepository {
        private final Map<String, KnowledgeEntity> store = new LinkedHashMap<>();
        private final List<KnowledgeEntity> saved = new ArrayList<>();

        @Override
        public List<KnowledgeEntity> listByEntityKeys(Collection<String> entityKeys) {
            return entityKeys == null
                    ? List.of()
                    : entityKeys.stream()
                            .map(store::get)
                            .filter(item -> item != null)
                            .toList();
        }

        @Override
        public KnowledgeEntity getByEntityId(Long entityId) {
            return store.values().stream()
                    .filter(item -> entityId.equals(item.getId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<KnowledgeEntity> listByVersionId(Long versionId) {
            return store.values().stream()
                    .filter(item -> versionId == null || versionId.equals(item.getLatestVersionId()))
                    .toList();
        }

        @Override
        public PageResult<KnowledgeEntity> page(
                Long versionId,
                String keyword,
                String entityType,
                String confirmationStatus,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, store.size(), saved);
        }

        @Override
        public void saveOrUpdateBatch(List<KnowledgeEntity> entities) {
            saved.clear();
            saved.addAll(entities);
            for (KnowledgeEntity entity : entities) {
                store.put(entity.getEntityKey(), entity);
            }
        }

        @Override
        public int deleteByEntityKeys(Collection<String> entityKeys) {
            if (entityKeys == null) {
                return 0;
            }
            int removed = 0;
            for (String entityKey : entityKeys) {
                if (store.remove(entityKey) != null) {
                    removed++;
                }
            }
            return removed;
        }
    }

    private static final class FakeKnowledgeRelationRepository implements KnowledgeRelationRepository {
        private final Map<String, KnowledgeRelation> store = new LinkedHashMap<>();
        private final List<KnowledgeRelation> saved = new ArrayList<>();

        @Override
        public List<KnowledgeRelation> listByRelationKeys(Collection<String> relationKeys) {
            return relationKeys == null
                    ? List.of()
                    : relationKeys.stream()
                            .map(store::get)
                            .filter(item -> item != null)
                            .toList();
        }

        @Override
        public KnowledgeRelation getByRelationId(Long relationId) {
            return store.values().stream()
                    .filter(item -> relationId.equals(item.getId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<KnowledgeRelation> listByVersionId(Long versionId) {
            return store.values().stream()
                    .filter(item -> versionId == null || versionId.equals(item.getLatestVersionId()))
                    .toList();
        }

        @Override
        public PageResult<KnowledgeRelation> page(
                Long versionId,
                String keyword,
                String relationType,
                String confirmationStatus,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, store.size(), saved);
        }

        @Override
        public void saveOrUpdateBatch(List<KnowledgeRelation> relations) {
            saved.clear();
            saved.addAll(relations);
            for (KnowledgeRelation relation : relations) {
                store.put(relation.getRelationKey(), relation);
            }
        }

        @Override
        public int deleteByRelationKeys(Collection<String> relationKeys) {
            if (relationKeys == null) {
                return 0;
            }
            int removed = 0;
            for (String relationKey : relationKeys) {
                if (store.remove(relationKey) != null) {
                    removed++;
                }
            }
            return removed;
        }
    }

    private static final class FakeKnowledgeLineageNodeRepository implements KnowledgeLineageNodeRepository {
        @Override
        public List<com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode> listByNodeKeys(
                Collection<String> nodeKeys) {
            return List.of();
        }

        @Override
        public com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode getByNodeId(Long nodeId) {
            return null;
        }

        @Override
        public List<com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode> listByVersionId(
                Long versionId) {
            return List.of();
        }

        @Override
        public com.thundax.kuzhambu.common.core.page.PageResult<
                        com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode>
                page(
                        Long versionId,
                        String keyword,
                        String nodeType,
                        String confirmationStatus,
                        int pageNo,
                        int pageSize) {
            return com.thundax.kuzhambu.common.core.page.PageResult.of(pageNo, pageSize, 0, List.of());
        }

        @Override
        public void saveOrUpdateBatch(
                List<com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode> nodes) {}

        @Override
        public int deleteByNodeKeys(Collection<String> nodeKeys) {
            return 0;
        }
    }

    private static final class FakeKnowledgeLineageRelationRepository implements KnowledgeLineageRelationRepository {
        @Override
        public List<com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation>
                listByRelationKeys(Collection<String> relationKeys) {
            return List.of();
        }

        @Override
        public com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation getByRelationId(
                Long relationId) {
            return null;
        }

        @Override
        public List<com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation> listByVersionId(
                Long versionId) {
            return List.of();
        }

        @Override
        public com.thundax.kuzhambu.common.core.page.PageResult<
                        com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation>
                page(
                        Long versionId,
                        String keyword,
                        String relationType,
                        String confirmationStatus,
                        int pageNo,
                        int pageSize) {
            return com.thundax.kuzhambu.common.core.page.PageResult.of(pageNo, pageSize, 0, List.of());
        }

        @Override
        public void saveOrUpdateBatch(
                List<com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation> relations) {}

        @Override
        public int deleteByRelationKeys(Collection<String> relationKeys) {
            return 0;
        }
    }
}
