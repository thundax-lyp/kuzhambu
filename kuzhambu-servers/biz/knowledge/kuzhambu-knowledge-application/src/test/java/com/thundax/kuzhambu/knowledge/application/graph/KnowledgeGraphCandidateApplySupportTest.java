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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
        task.setInputPayloadJson("{\"source\":{\"categoryPath\":\"天文\"}}");
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
        assertEquals("天文", version.getSourceCategoryCode());
        assertEquals("天文", version.getSourceCategoryName());
        assertEquals(1, entityRepository.saved.size());
        assertEquals(textKey("黄帝"), entityRepository.saved.get(0).getEntityKey());
        assertEquals("人物", entityRepository.saved.get(0).getEntityType());
        assertEquals(1, relationRepository.saved.size());
        assertEquals(
                relationKey("黄帝", "ANCESTOR", "伏羲"),
                relationRepository.saved.get(0).getRelationKey());
    }

    @Test
    void appendShouldSkipExistingGraphFacts() {
        FakeGraphVersionRepository versionRepository = new FakeGraphVersionRepository();
        FakeKnowledgeEntityRepository entityRepository = new FakeKnowledgeEntityRepository();
        FakeKnowledgeRelationRepository relationRepository = new FakeKnowledgeRelationRepository();
        KnowledgeEntity existingEntity = new KnowledgeEntity();
        existingEntity.setEntityKey(textKey("黄帝"));
        existingEntity.setName("黄帝");
        existingEntity.setEntityType("PERSON");
        existingEntity.setDescription("既有实体");
        entityRepository.store.put(existingEntity.getEntityKey(), existingEntity);
        KnowledgeRelation existingRelation = new KnowledgeRelation();
        existingRelation.setRelationKey(relationKey("黄帝", "ANCESTOR", "伏羲"));
        existingRelation.setSourceName("黄帝");
        existingRelation.setTargetName("伏羲");
        existingRelation.setRelationType("ANCESTOR");
        existingRelation.setEvidence("既有关系");
        relationRepository.store.put(existingRelation.getRelationKey(), existingRelation);
        KnowledgeGraphCandidateApplySupport support = new KnowledgeGraphCandidateApplySupport(
                versionRepository,
                entityRepository,
                relationRepository,
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository());
        GraphExtractionTask task = new GraphExtractionTask();
        task.setId(GraphExtractionTaskIdCodec.toDomain(11L));
        task.setTaskType(GraphExtractionTaskType.GRAPH);
        task.setSourceContentType("SANCAI_ENTRY");
        task.setSourceContentId(GraphExtractionSourceContentIdCodec.toDomain(1L));
        AiCandidateFacadeDto candidate = AiCandidateFacadeDto.builder()
                .candidateId(22L)
                .resultFormat("STRUCTURED")
                .resultPayload("{\"entities\":["
                        + "{\"name\":\"黄帝\",\"entityType\":\"PERSON\",\"description\":\"候选覆盖\"},"
                        + "{\"name\":\"炎帝\",\"entityType\":\"PERSON\",\"description\":\"新增实体\"}],"
                        + "\"relations\":["
                        + "{\"sourceName\":\"黄帝\",\"targetName\":\"伏羲\",\"relationType\":\"ANCESTOR\",\"evidence\":\"候选覆盖\"},"
                        + "{\"sourceName\":\"炎帝\",\"targetName\":\"神农\",\"relationType\":\"ALIAS\",\"evidence\":\"新增关系\"}],"
                        + "\"entryRefs\":[{\"entryId\":1}]}")
                .build();

        support.apply(task, candidate, "APPEND");

        assertEquals(1, entityRepository.saved.size());
        assertEquals(textKey("炎帝"), entityRepository.saved.get(0).getEntityKey());
        assertEquals("人物", entityRepository.saved.get(0).getEntityType());
        assertEquals("既有实体", entityRepository.store.get(textKey("黄帝")).getDescription());
        assertEquals(1, relationRepository.saved.size());
        assertEquals(
                relationKey("炎帝", "ALIAS", "神农"),
                relationRepository.saved.get(0).getRelationKey());
        assertEquals(
                "既有关系",
                relationRepository
                        .store
                        .get(relationKey("黄帝", "ANCESTOR", "伏羲"))
                        .getEvidence());
    }

    @Test
    void overwriteShouldReplaceCurrentVersionFactsAndCandidateKeys() {
        FakeGraphVersionRepository versionRepository = new FakeGraphVersionRepository();
        FakeKnowledgeEntityRepository entityRepository = new FakeKnowledgeEntityRepository();
        FakeKnowledgeRelationRepository relationRepository = new FakeKnowledgeRelationRepository();
        GraphExtractionTask task = new GraphExtractionTask();
        task.setId(GraphExtractionTaskIdCodec.toDomain(11L));
        task.setTaskType(GraphExtractionTaskType.GRAPH);
        task.setSourceContentType("SANCAI_ENTRY");
        task.setSourceContentId(GraphExtractionSourceContentIdCodec.toDomain(1L));
        GraphVersion existingVersion = new GraphVersion();
        existingVersion.setId(GraphVersionIdCodec.toDomain(7L));
        existingVersion.setTaskId(task.getId());
        existingVersion.setCandidateId(new GraphExtractionAiCandidateId(22L));
        existingVersion.setTaskType(GraphExtractionTaskType.GRAPH);
        existingVersion.setSourceContentType("SANCAI_ENTRY");
        existingVersion.setSourceContentId(GraphExtractionSourceContentIdCodec.toDomain(1L));
        existingVersion.setVersionNo(1);
        existingVersion.setStatus(GraphVersionStatus.APPLIED);
        versionRepository.versions.add(existingVersion);
        KnowledgeEntity staleEntity = new KnowledgeEntity();
        staleEntity.setEntityKey(textKey("旧实体"));
        staleEntity.setName("旧实体");
        staleEntity.setLatestVersionId(existingVersion.getId());
        entityRepository.store.put(staleEntity.getEntityKey(), staleEntity);
        KnowledgeEntity confirmedEntity = new KnowledgeEntity();
        confirmedEntity.setEntityKey(textKey("黄帝"));
        confirmedEntity.setName("黄帝");
        confirmedEntity.setDescription("人工确认描述");
        confirmedEntity.setConfirmationStatus(KnowledgeConfirmationStatus.MANUAL_CONFIRMED);
        confirmedEntity.setLatestVersionId(existingVersion.getId());
        entityRepository.store.put(confirmedEntity.getEntityKey(), confirmedEntity);
        KnowledgeRelation staleRelation = new KnowledgeRelation();
        staleRelation.setRelationKey(relationKey("旧实体", "旧关系", "旧目标"));
        staleRelation.setLatestVersionId(GraphVersionIdCodec.toValue(existingVersion.getId()));
        relationRepository.store.put(staleRelation.getRelationKey(), staleRelation);
        KnowledgeRelation confirmedRelation = new KnowledgeRelation();
        confirmedRelation.setRelationKey(relationKey("黄帝", "建立", "制度"));
        confirmedRelation.setEvidence("人工确认依据");
        confirmedRelation.setConfirmationStatus("MANUAL_CONFIRMED");
        confirmedRelation.setLatestVersionId(GraphVersionIdCodec.toValue(existingVersion.getId()));
        relationRepository.store.put(confirmedRelation.getRelationKey(), confirmedRelation);
        KnowledgeGraphCandidateApplySupport support = new KnowledgeGraphCandidateApplySupport(
                versionRepository,
                entityRepository,
                relationRepository,
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository());
        AiCandidateFacadeDto candidate = AiCandidateFacadeDto.builder()
                .candidateId(22L)
                .resultFormat("STRUCTURED")
                .resultPayload("{\"entities\":[{\"name\":\"黄帝\",\"entityType\":\"PERSON\",\"description\":\"AI描述\"}],"
                        + "\"relations\":[{\"sourceName\":\"黄帝\",\"targetName\":\"制度\",\"relationType\":\"建立\",\"evidence\":\"AI依据\"}],"
                        + "\"entryRefs\":[{\"entryId\":1}]}")
                .build();

        support.apply(task, candidate, "OVERWRITE");

        assertEquals(1, entityRepository.store.size());
        assertEquals("AI描述", entityRepository.store.get(textKey("黄帝")).getDescription());
        assertEquals(
                KnowledgeConfirmationStatus.AI_EXTRACTED,
                entityRepository.store.get(textKey("黄帝")).getConfirmationStatus());
        assertEquals(1, relationRepository.store.size());
        assertEquals(
                "AI依据",
                relationRepository.store.get(relationKey("黄帝", "建立", "制度")).getEvidence());
        assertEquals(
                "AI_EXTRACTED",
                relationRepository.store.get(relationKey("黄帝", "建立", "制度")).getConfirmationStatus());
    }

    @Test
    void applyShouldAcceptSpoFieldNames() {
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
        task.setSourceContentType("SANCAI_ENTRY");
        task.setSourceContentId(GraphExtractionSourceContentIdCodec.toDomain(1L));
        AiCandidateFacadeDto candidate = AiCandidateFacadeDto.builder()
                .candidateId(22L)
                .resultFormat("STRUCTURED")
                .resultPayload("{\"entities\":[],"
                        + "\"relations\":[{\"subject\":\"黄帝\",\"predicate\":\"祖先\",\"object\":\"伏羲\"}],"
                        + "\"entryRefs\":[{\"entryId\":1}]}")
                .build();

        support.apply(task, candidate);

        assertEquals(1, relationRepository.saved.size());
        assertEquals(
                relationKey("黄帝", "祖先", "伏羲"), relationRepository.saved.get(0).getRelationKey());
        assertEquals(textKey("黄帝"), relationRepository.saved.get(0).getSourceEntityKey());
        assertEquals(textKey("伏羲"), relationRepository.saved.get(0).getTargetEntityKey());
    }

    @Test
    void applyShouldDefaultMissingEntityTypeToOther() {
        FakeGraphVersionRepository versionRepository = new FakeGraphVersionRepository();
        FakeKnowledgeEntityRepository entityRepository = new FakeKnowledgeEntityRepository();
        KnowledgeGraphCandidateApplySupport support = new KnowledgeGraphCandidateApplySupport(
                versionRepository,
                entityRepository,
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository());
        GraphExtractionTask task = new GraphExtractionTask();
        task.setId(GraphExtractionTaskIdCodec.toDomain(11L));
        task.setTaskType(GraphExtractionTaskType.GRAPH);
        task.setSourceContentType("SANCAI_ENTRY");
        task.setSourceContentId(GraphExtractionSourceContentIdCodec.toDomain(1L));
        AiCandidateFacadeDto candidate = AiCandidateFacadeDto.builder()
                .candidateId(22L)
                .resultFormat("STRUCTURED")
                .resultPayload("{\"entities\":[{\"name\":\"浑天仪\",\"description\":\"器物\"}],\"relations\":[]}")
                .build();

        support.apply(task, candidate);

        assertEquals(1, entityRepository.saved.size());
        assertEquals("其他", entityRepository.saved.get(0).getEntityType());
    }

    private static String relationKey(String subject, String predicate, String object) {
        return textKey(String.join("|", subject, predicate, object));
    }

    private static String textKey(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(normalize(text).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String normalize(String value) {
        return String.valueOf(value).trim().replaceAll("\\s+", "_").toLowerCase(Locale.ROOT);
    }

    private static final class FakeGraphVersionRepository implements GraphVersionRepository {
        private final List<GraphVersion> versions = new ArrayList<>();

        @Override
        public GraphVersion getByLatestSource(
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
        public KnowledgeEntity getByEntityId(KnowledgeEntityId entityId) {
            return store.values().stream()
                    .filter(item -> entityId.equals(item.getId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<KnowledgeEntity> listByVersionId(GraphVersionId versionId) {
            return store.values().stream()
                    .filter(item -> versionId == null || versionId.equals(item.getLatestVersionId()))
                    .toList();
        }

        @Override
        public PageResult<KnowledgeEntity> page(
                GraphVersionId versionId,
                String keyword,
                String entityType,
                KnowledgeConfirmationStatus confirmationStatus,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, store.size(), saved);
        }

        @Override
        public void batchSaveOrUpdate(List<KnowledgeEntity> entities) {
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
        public void batchSaveOrUpdate(List<KnowledgeRelation> relations) {
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
