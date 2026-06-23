package com.thundax.kuzhambu.knowledge.application.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateDomainService;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionRequest;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.domain.knowledge.service.KnowledgeAiExtractionDomainService;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeEntityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageNodeResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageRelationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeRelationResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.impl.KnowledgeGraphExtractionApplicationServiceImpl;
import com.thundax.kuzhambu.knowledge.application.graph.support.KnowledgeGraphCandidateApplySupport;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeGraphExtractionApplicationServiceTest {

    @Test
    void requestRelationExtractionShouldPersistTaskAndSyncAiResult() {
        FakeRepository repository = new FakeRepository();
        FakeKnowledgeAiExtractionDomainService aiService = new FakeKnowledgeAiExtractionDomainService();
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                aiService,
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        GraphExtractionTaskResult result = service.requestRelationExtraction(relationCommand());

        assertNotNull(result);
        assertEquals("RELATION", result.getTaskType());
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals(301L, result.getAiCallId());
        assertEquals(302L, result.getAiCandidateId());
        assertEquals("RELATION", aiService.lastTaskType);
        assertEquals("SANCAI_ENTRY", aiService.lastRequest.getSourceContentType());
    }

    @Test
    void pageTasksShouldMapPersistedTasks() {
        FakeRepository repository = new FakeRepository();
        GraphExtractionTask task = new GraphExtractionTask();
        task.setTaskId(GraphExtractionTaskId.of(11L));
        task.setTaskType("GRAPH");
        task.setStatus("FAILED");
        repository.tasks.add(task);
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        PageResult<GraphExtractionTaskResult> page = service.pageTasks("GRAPH", null, null, null, new PageQuery(1, 10));

        assertEquals(1, page.getRecords().size());
        assertEquals("11", page.getRecords().get(0).getTaskId());
        assertEquals("GRAPH", page.getRecords().get(0).getTaskType());
    }

    @Test
    void getTaskDetailShouldOverlayAiCandidateAndCallState() {
        FakeRepository repository = new FakeRepository();
        GraphExtractionTask task = new GraphExtractionTask();
        task.setTaskId(GraphExtractionTaskId.of(21L));
        task.setTaskType("LINEAGE");
        task.setStatus("REQUESTED");
        task.setAiCallId(901L);
        task.setAiCandidateId(902L);
        repository.tasks.add(task);
        FakeAiInvocationRepository aiInvocationRepository = new FakeAiInvocationRepository();
        aiInvocationRepository.callRecord.setCallId(901L);
        aiInvocationRepository.callRecord.setStatus("SUCCEEDED");
        aiInvocationRepository.callRecord.setCompletedAt(Instant.parse("2026-06-23T00:00:00Z"));
        aiInvocationRepository.candidate.setCandidateId(902L);
        aiInvocationRepository.candidate.setAppliedAt(Instant.parse("2026-06-23T00:01:00Z"));
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                aiInvocationRepository,
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(aiInvocationRepository),
                null);

        GraphExtractionTaskResult detail = service.getTaskDetail(GraphExtractionTaskId.of(21L));

        assertEquals("APPLIED", detail.getStatus());
        assertEquals(Instant.parse("2026-06-23T00:00:00Z").toEpochMilli(), detail.getCompletedAt());
        assertEquals(Instant.parse("2026-06-23T00:01:00Z").toEpochMilli(), detail.getAppliedAt());
    }

    @Test
    void pageVersionsShouldMapVersionRecords() {
        FakeGraphVersionRepository graphVersionRepository = new FakeGraphVersionRepository();
        GraphVersion version = new GraphVersion();
        version.setVersionId(61L);
        version.setTaskId(GraphExtractionTaskId.of(31L));
        version.setCandidateId(901L);
        version.setTaskType("GRAPH");
        version.setSourceContentType("SANCAI_ENTRY");
        version.setSourceContentId(1001L);
        version.setVersionNo(2);
        version.setStatus("APPLIED");
        version.setAppliedAt(new Date(1_719_100_800_000L));
        graphVersionRepository.versions.add(version);
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                new FakeRepository(),
                graphVersionRepository,
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        PageResult<GraphVersionResult> page = service.pageVersions("GRAPH", "APPLIED", "SANCAI_ENTRY", 1001L, null);

        assertEquals(1, page.getRecords().size());
        assertEquals(61L, page.getRecords().get(0).getVersionId());
        assertEquals("31", page.getRecords().get(0).getTaskId());
        assertEquals(901L, page.getRecords().get(0).getCandidateId());
    }

    @Test
    void getVersionDetailShouldMapSingleVersionRecord() {
        FakeGraphVersionRepository graphVersionRepository = new FakeGraphVersionRepository();
        GraphVersion version = new GraphVersion();
        version.setVersionId(71L);
        version.setTaskId(GraphExtractionTaskId.of(41L));
        version.setCandidateId(902L);
        version.setTaskType("LINEAGE");
        version.setSourceContentType("SANCAI_ENTRY");
        version.setSourceContentId(1002L);
        version.setVersionNo(3);
        version.setStatus("APPLIED");
        version.setAppliedAt(new Date(1_719_187_200_000L));
        graphVersionRepository.versions.add(version);
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                new FakeRepository(),
                graphVersionRepository,
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        GraphVersionResult detail = service.getVersionDetail(71L);

        assertEquals(71L, detail.getVersionId());
        assertEquals("41", detail.getTaskId());
        assertEquals(902L, detail.getCandidateId());
        assertEquals("LINEAGE", detail.getTaskType());
    }

    @Test
    void pageEntitiesShouldMapReadableFields() {
        FakeKnowledgeEntityRepository knowledgeEntityRepository = new FakeKnowledgeEntityRepository();
        KnowledgeEntity entity = new KnowledgeEntity();
        entity.setEntityId(1001L);
        entity.setEntityKey("person:huangdi");
        entity.setName("黄帝");
        entity.setEntityType("PERSON");
        entity.setDescription("始祖");
        entity.setConfirmationStatus("CONFIRMED");
        entity.setLatestVersionId(71L);
        entity.setSourceRefsJson("[{\"entryId\":1}]");
        entity.setFirstExtractedAt(new Date(1_719_100_800_000L));
        entity.setLastExtractedAt(new Date(1_719_187_200_000L));
        knowledgeEntityRepository.entities.add(entity);
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                knowledgeEntityRepository,
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        PageResult<KnowledgeEntityResult> page = service.pageEntities(71L, "黄帝", "PERSON", "CONFIRMED", null);

        assertEquals(1, page.getRecords().size());
        assertEquals(1001L, page.getRecords().get(0).getEntityId());
        assertEquals("person:huangdi", page.getRecords().get(0).getEntityKey());
        assertEquals("CONFIRMED", page.getRecords().get(0).getConfirmationStatus());
    }

    @Test
    void getEntityDetailShouldMapSingleEntityRecord() {
        FakeKnowledgeEntityRepository knowledgeEntityRepository = new FakeKnowledgeEntityRepository();
        KnowledgeEntity entity = new KnowledgeEntity();
        entity.setEntityId(1002L);
        entity.setEntityKey("person:fuxi");
        entity.setName("伏羲");
        entity.setEntityType("PERSON");
        entity.setDescription("始祖");
        entity.setConfirmationStatus("AI_EXTRACTED");
        entity.setLatestVersionId(72L);
        entity.setSourceRefsJson("[{\"entryId\":2}]");
        knowledgeEntityRepository.entities.add(entity);
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                knowledgeEntityRepository,
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        KnowledgeEntityResult detail = service.getEntityDetail(1002L);

        assertEquals(1002L, detail.getEntityId());
        assertEquals("person:fuxi", detail.getEntityKey());
        assertEquals("伏羲", detail.getName());
    }

    @Test
    void pageRelationsShouldMapReadableFields() {
        FakeKnowledgeRelationRepository knowledgeRelationRepository = new FakeKnowledgeRelationRepository();
        KnowledgeRelation relation = new KnowledgeRelation();
        relation.setRelationId(2001L);
        relation.setRelationKey("person:huangdi->person:fuxi:ancestor");
        relation.setSourceName("黄帝");
        relation.setTargetName("伏羲");
        relation.setRelationType("ANCESTOR");
        relation.setEvidence("谱系");
        relation.setConfirmationStatus("CONFIRMED");
        relation.setLatestVersionId(71L);
        relation.setSourceRefsJson("[{\"entryId\":1}]");
        knowledgeRelationRepository.relations.add(relation);
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                knowledgeRelationRepository,
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        PageResult<KnowledgeRelationResult> page = service.pageRelations(71L, "黄帝", "ANCESTOR", "CONFIRMED", null);

        assertEquals(1, page.getRecords().size());
        assertEquals(2001L, page.getRecords().get(0).getRelationId());
        assertEquals("黄帝", page.getRecords().get(0).getSourceName());
        assertEquals("ANCESTOR", page.getRecords().get(0).getRelationType());
    }

    @Test
    void getRelationDetailShouldMapSingleRelationRecord() {
        FakeKnowledgeRelationRepository knowledgeRelationRepository = new FakeKnowledgeRelationRepository();
        KnowledgeRelation relation = new KnowledgeRelation();
        relation.setRelationId(2002L);
        relation.setRelationKey("person:fuxi->person:huangdi:ancestor");
        relation.setSourceName("伏羲");
        relation.setTargetName("黄帝");
        relation.setRelationType("ANCESTOR");
        relation.setEvidence("谱系");
        relation.setConfirmationStatus("AI_EXTRACTED");
        relation.setLatestVersionId(72L);
        knowledgeRelationRepository.relations.add(relation);
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                knowledgeRelationRepository,
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        KnowledgeRelationResult detail = service.getRelationDetail(2002L);

        assertEquals(2002L, detail.getRelationId());
        assertEquals("伏羲", detail.getSourceName());
        assertEquals("黄帝", detail.getTargetName());
    }

    @Test
    void pageLineageNodesShouldMapReadableFields() {
        FakeKnowledgeLineageNodeRepository knowledgeLineageNodeRepository = new FakeKnowledgeLineageNodeRepository();
        KnowledgeLineageNode node = new KnowledgeLineageNode();
        node.setNodeId(3001L);
        node.setNodeKey("person:huangdi");
        node.setName("黄帝");
        node.setNodeType("PERSON");
        node.setGeneration(1);
        node.setGender("MALE");
        node.setConfirmationStatus("CONFIRMED");
        node.setLatestVersionId(71L);
        node.setSourceRefsJson("[{\"entryId\":1}]");
        knowledgeLineageNodeRepository.nodes.add(node);
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                knowledgeLineageNodeRepository,
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        PageResult<KnowledgeLineageNodeResult> page = service.pageLineageNodes(71L, "黄帝", "PERSON", "CONFIRMED", null);

        assertEquals(1, page.getRecords().size());
        assertEquals(3001L, page.getRecords().get(0).getNodeId());
        assertEquals("person:huangdi", page.getRecords().get(0).getNodeKey());
        assertEquals("CONFIRMED", page.getRecords().get(0).getConfirmationStatus());
    }

    @Test
    void getLineageNodeDetailShouldMapSingleNodeRecord() {
        FakeKnowledgeLineageNodeRepository knowledgeLineageNodeRepository = new FakeKnowledgeLineageNodeRepository();
        KnowledgeLineageNode node = new KnowledgeLineageNode();
        node.setNodeId(3002L);
        node.setNodeKey("person:fuxi");
        node.setName("伏羲");
        node.setNodeType("PERSON");
        node.setGeneration(0);
        node.setGender("MALE");
        node.setConfirmationStatus("AI_EXTRACTED");
        node.setLatestVersionId(72L);
        knowledgeLineageNodeRepository.nodes.add(node);
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                knowledgeLineageNodeRepository,
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        KnowledgeLineageNodeResult detail = service.getLineageNodeDetail(3002L);

        assertEquals(3002L, detail.getNodeId());
        assertEquals("person:fuxi", detail.getNodeKey());
        assertEquals("伏羲", detail.getName());
    }

    @Test
    void pageLineageRelationsShouldMapReadableFields() {
        FakeKnowledgeLineageRelationRepository knowledgeLineageRelationRepository =
                new FakeKnowledgeLineageRelationRepository();
        KnowledgeLineageRelation relation = new KnowledgeLineageRelation();
        relation.setRelationId(4001L);
        relation.setRelationKey("person:huangdi->person:fuxi:ancestor");
        relation.setSourceName("黄帝");
        relation.setTargetName("伏羲");
        relation.setRelationType("ANCESTOR");
        relation.setEvidence("谱系");
        relation.setConfirmationStatus("CONFIRMED");
        relation.setLatestVersionId(71L);
        relation.setSourceRefsJson("[{\"entryId\":1}]");
        knowledgeLineageRelationRepository.relations.add(relation);
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                knowledgeLineageRelationRepository,
                new FakeAiInvocationRepository(),
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        PageResult<KnowledgeLineageRelationResult> page =
                service.pageLineageRelations(71L, "黄帝", "ANCESTOR", "CONFIRMED", null);

        assertEquals(1, page.getRecords().size());
        assertEquals(4001L, page.getRecords().get(0).getRelationId());
        assertEquals("黄帝", page.getRecords().get(0).getSourceName());
        assertEquals("ANCESTOR", page.getRecords().get(0).getRelationType());
    }

    @Test
    void getLineageRelationDetailShouldMapSingleRelationRecord() {
        FakeKnowledgeLineageRelationRepository knowledgeLineageRelationRepository =
                new FakeKnowledgeLineageRelationRepository();
        KnowledgeLineageRelation relation = new KnowledgeLineageRelation();
        relation.setRelationId(4002L);
        relation.setRelationKey("person:fuxi->person:huangdi:ancestor");
        relation.setSourceName("伏羲");
        relation.setTargetName("黄帝");
        relation.setRelationType("ANCESTOR");
        relation.setEvidence("谱系");
        relation.setConfirmationStatus("AI_EXTRACTED");
        relation.setLatestVersionId(72L);
        knowledgeLineageRelationRepository.relations.add(relation);
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                knowledgeLineageRelationRepository,
                new FakeAiInvocationRepository(),
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        KnowledgeLineageRelationResult detail = service.getLineageRelationDetail(4002L);

        assertEquals(4002L, detail.getRelationId());
        assertEquals("伏羲", detail.getSourceName());
        assertEquals("黄帝", detail.getTargetName());
    }

    @Test
    void applyTaskCandidateShouldMarkTaskApplied() {
        FakeRepository repository = new FakeRepository();
        GraphExtractionTask task = new GraphExtractionTask();
        task.setTaskId(GraphExtractionTaskId.of(31L));
        task.setTaskType("GRAPH");
        task.setStatus("SUCCEEDED");
        task.setSourceContentType("SANCAI_ENTRY");
        task.setSourceContentId(1L);
        task.setAiCandidateId(902L);
        repository.tasks.add(task);
        FakeAiInvocationRepository aiInvocationRepository = new FakeAiInvocationRepository();
        aiInvocationRepository.candidate.setCandidateId(902L);
        aiInvocationRepository.candidate.setContentType("SANCAI_ENTRY");
        aiInvocationRepository.candidate.setContentId(1L);
        aiInvocationRepository.candidate.setCapability("knowledge_graph");
        aiInvocationRepository.candidate.setResultFormat("STRUCTURED");
        aiInvocationRepository.candidate.setResultPayload("{\"entities\":[],\"relations\":[],\"entryRefs\":[]}");
        FakeCandidateApplySupport candidateApplySupport = new FakeCandidateApplySupport();
        KnowledgeGraphExtractionApplicationServiceImpl service = new KnowledgeGraphExtractionApplicationServiceImpl(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                aiInvocationRepository,
                new FakeKnowledgeAiExtractionDomainService(),
                new AiCandidateDomainService(aiInvocationRepository),
                candidateApplySupport);

        GraphExtractionTaskResult result = service.applyTaskCandidate(GraphExtractionTaskId.of(31L));

        assertEquals("APPLIED", result.getStatus());
        assertEquals(902L, candidateApplySupport.appliedCandidateId);
    }

    private RequestRelationExtractionCommand relationCommand() {
        return new RequestRelationExtractionCommand(
                "ENTRY",
                "{\"entryIds\":[1]}",
                "SANCAI_ENTRY",
                1L,
                2L,
                3L,
                "knowledge-admin",
                10L,
                "model-a",
                20L,
                "req-1",
                "trace-1",
                "[{\"role\":\"user\",\"content\":\"hello\"}]",
                null,
                null,
                "{\"text\":\"hello\"}",
                "{\"type\":\"object\"}",
                true,
                "zh-CN");
    }

    private static final class FakeKnowledgeAiExtractionDomainService implements KnowledgeAiExtractionDomainService {
        private KnowledgeAiExtractionRequest lastRequest;
        private String lastTaskType;

        @Override
        public KnowledgeAiExtractionResult extractRelations(KnowledgeAiExtractionRequest request) {
            lastRequest = request;
            lastTaskType = "RELATION";
            return new KnowledgeAiExtractionResult(
                    301L, 302L, "SUCCEEDED", "relation_extraction", "STRUCTURED", "{}", null, null);
        }

        @Override
        public KnowledgeAiExtractionResult extractGraph(KnowledgeAiExtractionRequest request) {
            lastRequest = request;
            lastTaskType = "GRAPH";
            return new KnowledgeAiExtractionResult(
                    401L, 402L, "SUCCEEDED", "knowledge_graph", "STRUCTURED", "{}", null, null);
        }

        @Override
        public KnowledgeAiExtractionResult extractLineage(KnowledgeAiExtractionRequest request) {
            lastRequest = request;
            lastTaskType = "LINEAGE";
            return new KnowledgeAiExtractionResult(
                    501L, 502L, "SUCCEEDED", "lineage_extraction", "STRUCTURED", "{}", null, null);
        }
    }

    private static final class FakeRepository implements GraphExtractionTaskRepository {
        private final List<GraphExtractionTask> tasks = new ArrayList<>();

        @Override
        public GraphExtractionTask getByTaskId(GraphExtractionTaskId taskId) {
            return tasks.stream()
                    .filter(task -> task.getTaskId() != null && task.getTaskId().equals(taskId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public GraphExtractionTaskId save(GraphExtractionTask entity) {
            GraphExtractionTaskId taskId = GraphExtractionTaskId.of((long) (tasks.size() + 1));
            entity.setTaskId(taskId);
            tasks.add(entity);
            return taskId;
        }

        @Override
        public int update(GraphExtractionTask entity) {
            return 1;
        }

        @Override
        public PageResult<GraphExtractionTask> page(
                String taskType,
                String status,
                String sourceContentType,
                Long sourceContentId,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, tasks.size(), tasks);
        }
    }

    private static final class FakeGraphVersionRepository implements GraphVersionRepository {
        private final List<GraphVersion> versions = new ArrayList<>();

        @Override
        public GraphVersion findLatest(String taskType, String sourceContentType, Long sourceContentId) {
            return versions.stream()
                    .filter(version -> taskType.equals(version.getTaskType()))
                    .filter(version -> sourceContentType.equals(version.getSourceContentType()))
                    .filter(version -> sourceContentId.equals(version.getSourceContentId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public GraphVersion getByVersionId(Long versionId) {
            return versions.stream()
                    .filter(version -> versionId.equals(version.getVersionId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public GraphVersion getByTaskCandidate(GraphExtractionTaskId taskId, Long candidateId) {
            return versions.stream()
                    .filter(version ->
                            version.getTaskId() != null && version.getTaskId().equals(taskId))
                    .filter(version -> candidateId.equals(version.getCandidateId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public PageResult<GraphVersion> page(
                String taskType,
                String status,
                String sourceContentType,
                Long sourceContentId,
                int pageNo,
                int pageSize) {
            return PageResult.of(
                    pageNo,
                    pageSize,
                    versions.size(),
                    versions.stream()
                            .filter(version -> taskType == null || taskType.equals(version.getTaskType()))
                            .filter(version -> status == null || status.equals(version.getStatus()))
                            .filter(version -> sourceContentType == null
                                    || sourceContentType.equals(version.getSourceContentType()))
                            .filter(version ->
                                    sourceContentId == null || sourceContentId.equals(version.getSourceContentId()))
                            .toList());
        }

        @Override
        public Long save(GraphVersion entity) {
            versions.add(entity);
            return entity.getVersionId();
        }
    }

    private static final class FakeKnowledgeEntityRepository implements KnowledgeEntityRepository {
        private final List<KnowledgeEntity> entities = new ArrayList<>();

        @Override
        public List<KnowledgeEntity> listByEntityKeys(Collection<String> entityKeys) {
            return entityKeys == null ? List.of() : entities;
        }

        @Override
        public KnowledgeEntity getByEntityId(Long entityId) {
            return entities.stream()
                    .filter(entity -> entityId.equals(entity.getEntityId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public PageResult<KnowledgeEntity> page(
                Long versionId,
                String keyword,
                String entityType,
                String confirmationStatus,
                int pageNo,
                int pageSize) {
            return PageResult.of(
                    pageNo,
                    pageSize,
                    entities.size(),
                    entities.stream()
                            .filter(entity -> versionId == null || versionId.equals(entity.getLatestVersionId()))
                            .filter(entity ->
                                    keyword == null || entity.getName().contains(keyword))
                            .filter(entity -> entityType == null || entityType.equals(entity.getEntityType()))
                            .filter(entity -> confirmationStatus == null
                                    || confirmationStatus.equals(entity.getConfirmationStatus()))
                            .toList());
        }

        @Override
        public void saveOrUpdateBatch(List<KnowledgeEntity> entities) {
            this.entities.clear();
            this.entities.addAll(entities);
        }
    }

    private static final class FakeKnowledgeRelationRepository implements KnowledgeRelationRepository {
        private final List<KnowledgeRelation> relations = new ArrayList<>();

        @Override
        public List<KnowledgeRelation> listByRelationKeys(Collection<String> relationKeys) {
            return relationKeys == null ? List.of() : relations;
        }

        @Override
        public KnowledgeRelation getByRelationId(Long relationId) {
            return relations.stream()
                    .filter(relation -> relationId.equals(relation.getRelationId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public PageResult<KnowledgeRelation> page(
                Long versionId,
                String keyword,
                String relationType,
                String confirmationStatus,
                int pageNo,
                int pageSize) {
            return PageResult.of(
                    pageNo,
                    pageSize,
                    relations.size(),
                    relations.stream()
                            .filter(relation -> versionId == null || versionId.equals(relation.getLatestVersionId()))
                            .filter(relation -> keyword == null
                                    || relation.getSourceName().contains(keyword)
                                    || relation.getTargetName().contains(keyword))
                            .filter(relation -> relationType == null || relationType.equals(relation.getRelationType()))
                            .filter(relation -> confirmationStatus == null
                                    || confirmationStatus.equals(relation.getConfirmationStatus()))
                            .toList());
        }

        @Override
        public void saveOrUpdateBatch(List<KnowledgeRelation> relations) {
            this.relations.clear();
            this.relations.addAll(relations);
        }
    }

    private static final class FakeKnowledgeLineageNodeRepository implements KnowledgeLineageNodeRepository {
        private final List<KnowledgeLineageNode> nodes = new ArrayList<>();

        @Override
        public List<KnowledgeLineageNode> listByNodeKeys(Collection<String> nodeKeys) {
            return nodeKeys == null ? List.of() : nodes;
        }

        @Override
        public KnowledgeLineageNode getByNodeId(Long nodeId) {
            return nodes.stream()
                    .filter(node -> nodeId.equals(node.getNodeId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public PageResult<KnowledgeLineageNode> page(
                Long versionId, String keyword, String nodeType, String confirmationStatus, int pageNo, int pageSize) {
            return PageResult.of(
                    pageNo,
                    pageSize,
                    nodes.size(),
                    nodes.stream()
                            .filter(node -> versionId == null || versionId.equals(node.getLatestVersionId()))
                            .filter(node -> keyword == null || node.getName().contains(keyword))
                            .filter(node -> nodeType == null || nodeType.equals(node.getNodeType()))
                            .filter(node -> confirmationStatus == null
                                    || confirmationStatus.equals(node.getConfirmationStatus()))
                            .toList());
        }

        @Override
        public void saveOrUpdateBatch(List<KnowledgeLineageNode> nodes) {
            this.nodes.clear();
            this.nodes.addAll(nodes);
        }
    }

    private static final class FakeKnowledgeLineageRelationRepository implements KnowledgeLineageRelationRepository {
        private final List<KnowledgeLineageRelation> relations = new ArrayList<>();

        @Override
        public List<KnowledgeLineageRelation> listByRelationKeys(Collection<String> relationKeys) {
            return relationKeys == null ? List.of() : relations;
        }

        @Override
        public KnowledgeLineageRelation getByRelationId(Long relationId) {
            return relations.stream()
                    .filter(relation -> relationId.equals(relation.getRelationId()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public PageResult<KnowledgeLineageRelation> page(
                Long versionId,
                String keyword,
                String relationType,
                String confirmationStatus,
                int pageNo,
                int pageSize) {
            return PageResult.of(
                    pageNo,
                    pageSize,
                    relations.size(),
                    relations.stream()
                            .filter(relation -> versionId == null || versionId.equals(relation.getLatestVersionId()))
                            .filter(relation -> keyword == null
                                    || relation.getSourceName().contains(keyword)
                                    || relation.getTargetName().contains(keyword))
                            .filter(relation -> relationType == null || relationType.equals(relation.getRelationType()))
                            .filter(relation -> confirmationStatus == null
                                    || confirmationStatus.equals(relation.getConfirmationStatus()))
                            .toList());
        }

        @Override
        public void saveOrUpdateBatch(List<KnowledgeLineageRelation> relations) {
            this.relations.clear();
            this.relations.addAll(relations);
        }
    }

    private static final class FakeAiInvocationRepository implements AiInvocationRepository {
        private final AiCallRecord callRecord = new AiCallRecord();
        private final AiCandidate candidate = new AiCandidate();

        @Override
        public AiCallRecord getCallRecord(Long callId) {
            return callRecord.getCallId() != null && callRecord.getCallId().equals(callId) ? callRecord : null;
        }

        @Override
        public Long saveCallRecord(AiCallRecord callRecord) {
            return null;
        }

        @Override
        public int updateCallRecord(AiCallRecord callRecord) {
            return 0;
        }

        @Override
        public AiCandidate getCandidate(Long candidateId) {
            return candidate.getCandidateId() != null
                            && candidate.getCandidateId().equals(candidateId)
                    ? candidate
                    : null;
        }

        @Override
        public Long saveCandidate(AiCandidate candidate) {
            return null;
        }

        @Override
        public int updateCandidate(AiCandidate candidate) {
            if (candidate == null || this.candidate.getCandidateId() == null) {
                return 0;
            }
            this.candidate.setResultFormat(candidate.getResultFormat());
            this.candidate.setResultPayload(candidate.getResultPayload());
            this.candidate.setStatus(candidate.getStatus());
            this.candidate.setAppliedAt(candidate.getAppliedAt());
            this.candidate.setErrorType(candidate.getErrorType());
            this.candidate.setErrorMessage(candidate.getErrorMessage());
            return 1;
        }

        @Override
        public List<AiCandidate> listCandidates(String contentType, Long contentId, String capability, String status) {
            return List.of();
        }
    }

    private static final class FakeCandidateApplySupport extends KnowledgeGraphCandidateApplySupport {
        private Long appliedCandidateId;

        private FakeCandidateApplySupport() {
            super(null, null, null, null, null);
        }

        @Override
        public com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion apply(
                GraphExtractionTask task, AiCandidate candidate) {
            appliedCandidateId = candidate == null ? null : candidate.getCandidateId();
            return null;
        }
    }
}
