package com.thundax.kuzhambu.knowledge.application.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.knowledge.application.graph.support.KnowledgeGraphCandidateApplySupport;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
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
        task.setTaskId(GraphExtractionTaskId.of(11L));
        task.setTaskType("GRAPH");
        task.setScopeType("ENTRY");
        task.setScopeJson("{\"entryIds\":[1]}");
        task.setSourceContentType("SANCAI_ENTRY");
        task.setSourceContentId(1L);
        AiCandidate candidate = new AiCandidate();
        candidate.setCandidateId(22L);
        candidate.setResultFormat("STRUCTURED");
        candidate.setResultPayload("{\"entities\":[{\"name\":\"黄帝\",\"entityType\":\"PERSON\",\"description\":\"始祖\"}],"
                + "\"relations\":[{\"sourceName\":\"黄帝\",\"targetName\":\"伏羲\",\"relationType\":\"ANCESTOR\",\"evidence\":\"谱系\"}],"
                + "\"entryRefs\":[{\"entryId\":1}]}");

        GraphVersion version = support.apply(task, candidate);

        assertNotNull(version.getVersionId());
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
        public GraphVersion findLatest(String taskType, String sourceContentType, Long sourceContentId) {
            return versions.stream().reduce((first, second) -> second).orElse(null);
        }

        @Override
        public GraphVersion getByTaskCandidate(GraphExtractionTaskId taskId, Long candidateId) {
            return versions.stream()
                    .filter(version -> version.getTaskId().equals(taskId)
                            && version.getCandidateId().equals(candidateId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Long save(GraphVersion entity) {
            long versionId = versions.size() + 1L;
            entity.setVersionId(versionId);
            versions.add(entity);
            return versionId;
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
        public void saveOrUpdateBatch(List<KnowledgeEntity> entities) {
            saved.clear();
            saved.addAll(entities);
            for (KnowledgeEntity entity : entities) {
                store.put(entity.getEntityKey(), entity);
            }
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
        public void saveOrUpdateBatch(List<KnowledgeRelation> relations) {
            saved.clear();
            saved.addAll(relations);
            for (KnowledgeRelation relation : relations) {
                store.put(relation.getRelationKey(), relation);
            }
        }
    }

    private static final class FakeKnowledgeLineageNodeRepository implements KnowledgeLineageNodeRepository {
        @Override
        public List<com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode> listByNodeKeys(
                Collection<String> nodeKeys) {
            return List.of();
        }

        @Override
        public void saveOrUpdateBatch(
                List<com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode> nodes) {}
    }

    private static final class FakeKnowledgeLineageRelationRepository implements KnowledgeLineageRelationRepository {
        @Override
        public List<com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation>
                listByRelationKeys(Collection<String> relationKeys) {
            return List.of();
        }

        @Override
        public void saveOrUpdateBatch(
                List<com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation> relations) {}
    }
}
