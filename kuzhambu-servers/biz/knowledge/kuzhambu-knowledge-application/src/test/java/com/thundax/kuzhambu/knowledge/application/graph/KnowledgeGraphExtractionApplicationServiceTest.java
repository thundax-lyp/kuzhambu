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
import com.thundax.kuzhambu.knowledge.application.graph.service.impl.KnowledgeGraphExtractionApplicationServiceImpl;
import com.thundax.kuzhambu.knowledge.application.graph.support.KnowledgeGraphCandidateApplySupport;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import java.time.Instant;
import java.util.ArrayList;
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
