package com.thundax.kuzhambu.knowledge.application.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.DiscoveryAiStreamHandler;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiInvocationLogFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.AiBatchJobFailureFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.CreateAiBatchJobFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiInvocationLogFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RejectAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.KnowledgeAiExtractionFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.RegenerateGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionBatchCancelResult;
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
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementTask;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.valueobject.RefinementTaskId;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;

class KnowledgeGraphExtractionApplicationServiceTest {

    private static KnowledgeGraphExtractionApplicationServiceImpl service(
            GraphExtractionTaskRepository repository,
            GraphVersionRepository graphVersionRepository,
            KnowledgeEntityRepository knowledgeEntityRepository,
            KnowledgeRelationRepository knowledgeRelationRepository,
            KnowledgeLineageNodeRepository knowledgeLineageNodeRepository,
            KnowledgeLineageRelationRepository knowledgeLineageRelationRepository,
            AiInvocationRepository aiInvocationRepository,
            AiBatchJobApplicationService aiBatchJobApplicationService,
            KnowledgeAiExtractionRepository knowledgeAiExtractionRepository,
            AiCandidateDomainService aiCandidateDomainService,
            KnowledgeGraphCandidateApplySupport candidateApplySupport) {
        return service(
                repository,
                graphVersionRepository,
                knowledgeEntityRepository,
                knowledgeRelationRepository,
                knowledgeLineageNodeRepository,
                knowledgeLineageRelationRepository,
                null,
                aiInvocationRepository,
                aiBatchJobApplicationService,
                knowledgeAiExtractionRepository,
                aiCandidateDomainService,
                candidateApplySupport);
    }

    private static KnowledgeGraphExtractionApplicationServiceImpl service(
            GraphExtractionTaskRepository repository,
            GraphVersionRepository graphVersionRepository,
            KnowledgeEntityRepository knowledgeEntityRepository,
            KnowledgeRelationRepository knowledgeRelationRepository,
            KnowledgeLineageNodeRepository knowledgeLineageNodeRepository,
            KnowledgeLineageRelationRepository knowledgeLineageRelationRepository,
            RefinementTaskRepository refinementTaskRepository,
            AiInvocationRepository aiInvocationRepository,
            AiBatchJobApplicationService aiBatchJobApplicationService,
            KnowledgeAiExtractionRepository knowledgeAiExtractionRepository,
            AiCandidateDomainService aiCandidateDomainService,
            KnowledgeGraphCandidateApplySupport candidateApplySupport) {
        return new KnowledgeGraphExtractionApplicationServiceImpl(
                repository,
                graphVersionRepository,
                knowledgeEntityRepository,
                knowledgeRelationRepository,
                knowledgeLineageNodeRepository,
                knowledgeLineageRelationRepository,
                refinementTaskRepository,
                new FakeAiFacade(
                        aiInvocationRepository,
                        aiBatchJobApplicationService,
                        knowledgeAiExtractionRepository,
                        aiCandidateDomainService),
                candidateApplySupport);
    }

    @Test
    void requestRelationExtractionShouldPersistTaskAndSyncAiResult() {
        FakeRepository repository = new FakeRepository();
        FakeKnowledgeAiExtractionRepository aiService = new FakeKnowledgeAiExtractionRepository();
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                null,
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
    void requestRelationExtractionShouldCreateBatchTasksWhenSelectionScopeContainsManyTargets() {
        FakeRepository repository = new FakeRepository();
        FakeKnowledgeAiExtractionRepository aiService = new FakeKnowledgeAiExtractionRepository();
        FakeAiBatchJobApplicationService batchService = new FakeAiBatchJobApplicationService();
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                batchService,
                aiService,
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);
        RequestRelationExtractionCommand command = relationCommand();
        command.setSelectionScopeJson("{\"sourceContentIds\":[11,12]}");
        command.setTriggerSource("QUALITY_REPORT");
        command.setReplaceUnconfirmedOnly(Boolean.TRUE);

        GraphExtractionTaskResult result = service.requestRelationExtraction(command);

        assertNotNull(result);
        assertEquals(Long.valueOf(1001L), result.getBatchJobId());
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals(3, repository.tasks.size());
        GraphExtractionTask parentTask = repository.tasks.get(0);
        assertEquals(Long.valueOf(1001L), parentTask.getBatchJobId());
        assertEquals("QUALITY_REPORT", parentTask.getTriggerSource());
        assertEquals(Boolean.TRUE, parentTask.getReplaceUnconfirmedOnly());
        assertEquals("SUCCEEDED", parentTask.getStatus());
        GraphExtractionTask firstChild = repository.tasks.get(1);
        GraphExtractionTask secondChild = repository.tasks.get(2);
        assertEquals(parentTask.getTaskId(), firstChild.getParentTaskId());
        assertEquals(parentTask.getTaskId(), secondChild.getParentTaskId());
        assertEquals(Long.valueOf(11L), firstChild.getSourceContentId());
        assertEquals(Long.valueOf(12L), secondChild.getSourceContentId());
        assertEquals(2, batchService.recordSuccessCalls);
        assertEquals(0, batchService.recordFailureCalls);
        assertEquals(2, batchService.lastResult.getSuccessCount());
        assertEquals("RELATION", aiService.lastTaskType);
    }

    @Test
    void requestRelationExtractionShouldRejectInvalidSelectionScopeJson() {
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                new FakeAiBatchJobApplicationService(),
                new FakeKnowledgeAiExtractionRepository(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);
        RequestRelationExtractionCommand command = relationCommand();
        command.setSelectionScopeJson("{bad-json");

        BizException exception = assertThrows(BizException.class, () -> service.requestRelationExtraction(command));

        assertEquals("Knowledge graph selectionScopeJson is invalid", exception.getMessage());
    }

    @Test
    void requestGraphExtractionShouldAllowMissingModelBeforeAiResolverFillsIt() {
        FakeRepository repository = new FakeRepository();
        FakeKnowledgeAiExtractionRepository aiService = new FakeKnowledgeAiExtractionRepository();
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                null,
                aiService,
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);
        RequestGraphExtractionCommand command = graphCommand();
        command.setModelId(null);
        command.setModelName(null);

        GraphExtractionTaskResult result = service.requestGraphExtraction(command);

        assertNotNull(result);
        assertEquals("GRAPH", result.getTaskType());
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals("GRAPH", aiService.lastTaskType);
        assertEquals("SANCAI_ENTRY", aiService.lastRequest.getSourceContentType());
        assertEquals(null, aiService.lastRequest.getModelId());
        assertEquals(null, aiService.lastRequest.getModelName());
    }

    @Test
    void cancelBatchShouldMarkPendingChildrenCancelled() {
        FakeRepository repository = new FakeRepository();
        GraphExtractionTask parentTask = new GraphExtractionTask();
        parentTask.setTaskId(GraphExtractionTaskId.of(1L));
        parentTask.setBatchJobId(1001L);
        parentTask.setTaskType("RELATION");
        parentTask.setStatus("RUNNING");
        repository.tasks.add(parentTask);
        GraphExtractionTask pendingChild = new GraphExtractionTask();
        pendingChild.setTaskId(GraphExtractionTaskId.of(2L));
        pendingChild.setBatchJobId(1001L);
        pendingChild.setParentTaskId(GraphExtractionTaskId.of(1L));
        pendingChild.setTaskType("RELATION");
        pendingChild.setStatus("REQUESTED");
        repository.tasks.add(pendingChild);
        GraphExtractionTask finishedChild = new GraphExtractionTask();
        finishedChild.setTaskId(GraphExtractionTaskId.of(3L));
        finishedChild.setBatchJobId(1001L);
        finishedChild.setParentTaskId(GraphExtractionTaskId.of(1L));
        finishedChild.setTaskType("RELATION");
        finishedChild.setStatus("SUCCEEDED");
        repository.tasks.add(finishedChild);
        FakeAiBatchJobApplicationService batchService = new FakeAiBatchJobApplicationService();
        batchService.create(new AiBatchJobCreateCommand("{}", "relation_extraction", "SANCAI_ENTRY", 2, null));
        batchService.recordSuccess(1001L);
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                batchService,
                new FakeKnowledgeAiExtractionRepository(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        GraphExtractionBatchCancelResult result = service.cancelBatch(1001L, 99L);

        assertEquals(Long.valueOf(1001L), result.getBatchJobId());
        assertEquals("CANCELLED", result.getStatus());
        assertEquals(Integer.valueOf(1), result.getCancelledCount());
        assertEquals(Integer.valueOf(1), result.getCompletedCount());
        assertEquals("CANCELLED", pendingChild.getStatus());
        assertEquals(Long.valueOf(99L), pendingChild.getRequestedBy());
        assertEquals("CANCELLED", parentTask.getStatus());
    }

    @Test
    void regenerateTaskShouldReuseStoredRequestSnapshot() {
        FakeRepository repository = new FakeRepository();
        GraphExtractionTask sourceTask = new GraphExtractionTask();
        sourceTask.setTaskId(GraphExtractionTaskId.of(88L));
        sourceTask.setTaskType("RELATION");
        sourceTask.setScopeType("CLASSICS_ENTRY");
        sourceTask.setScopeJson("{\"entryId\":88}");
        sourceTask.setTriggerSource("QUALITY_REPORT");
        sourceTask.setSelectionScopeJson("{\"sourceContentIds\":[88,89]}");
        sourceTask.setReplaceUnconfirmedOnly(Boolean.TRUE);
        sourceTask.setSourceContentType("SANCAI_ENTRY");
        sourceTask.setSourceContentId(88L);
        sourceTask.setRequestedBy(7L);
        sourceTask.setModelId(5001L);
        sourceTask.setModelName("gpt-5.5");
        sourceTask.setPromptVersionId(61L);
        sourceTask.setRequestId("req-source");
        sourceTask.setTraceId("trace-source");
        sourceTask.setPromptMessagesJson("[{\"role\":\"system\",\"content\":\"extract\"}]");
        sourceTask.setPromptVariablesJson("{\"locale\":\"zh-CN\"}");
        sourceTask.setPromptHash("hash-source");
        sourceTask.setInputPayloadJson("{\"content\":\"天地玄黄\"}");
        sourceTask.setOutputSchemaJson("{\"type\":\"object\"}");
        sourceTask.setForceJson(Boolean.TRUE);
        sourceTask.setLocale("zh-CN");
        repository.tasks.add(sourceTask);
        FakeKnowledgeAiExtractionRepository aiService = new FakeKnowledgeAiExtractionRepository();
        FakeAiBatchJobApplicationService batchService = new FakeAiBatchJobApplicationService();
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                batchService,
                aiService,
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        GraphExtractionTaskResult result =
                service.regenerateTask("RELATION", GraphExtractionTaskId.of(88L), null, Boolean.FALSE, 99L);

        assertNotNull(result);
        assertEquals("REGENERATE", result.getTriggerSource());
        assertEquals(Long.valueOf(1001L), result.getBatchJobId());
        assertEquals(4, repository.tasks.size());
        GraphExtractionTask parentTask = repository.tasks.get(1);
        assertEquals(Long.valueOf(88L), parentTask.getParentTaskId().value());
        assertEquals("REGENERATE", parentTask.getTriggerSource());
        assertEquals(Boolean.FALSE, parentTask.getReplaceUnconfirmedOnly());
        GraphExtractionTask childTask = repository.tasks.get(2);
        assertEquals(Long.valueOf(5001L), childTask.getModelId());
        assertEquals("gpt-5.5", childTask.getModelName());
        assertEquals("[{\"role\":\"system\",\"content\":\"extract\"}]", childTask.getPromptMessagesJson());
        assertEquals("{\"content\":\"天地玄黄\"}", childTask.getInputPayloadJson());
        assertEquals(Long.valueOf(99L), childTask.getRequestedBy());
        assertEquals("RELATION", aiService.lastTaskType);
    }

    @Test
    void regenerateTaskShouldKeepRefinementAppliedSourceAndDefaultReplaceUnconfirmedOnly() {
        FakeRepository repository = new FakeRepository();
        GraphExtractionTask sourceTask = new GraphExtractionTask();
        sourceTask.setTaskId(GraphExtractionTaskId.of(88L));
        sourceTask.setTaskType("GRAPH");
        sourceTask.setScopeType("CLASSICS_ENTRY");
        sourceTask.setScopeJson("{\"entryId\":88}");
        sourceTask.setSelectionScopeJson("{\"sourceContentIds\":[88,89]}");
        sourceTask.setSourceContentType("SANCAI_ENTRY");
        sourceTask.setSourceContentId(88L);
        sourceTask.setRequestedBy(7L);
        sourceTask.setModelId(5001L);
        sourceTask.setModelName("gpt-5.5");
        sourceTask.setRequestId("req-source");
        sourceTask.setTraceId("trace-source");
        sourceTask.setPromptMessagesJson("[{\"role\":\"system\",\"content\":\"extract\"}]");
        sourceTask.setInputPayloadJson("{\"content\":\"天地玄黄\"}");
        sourceTask.setForceJson(Boolean.TRUE);
        sourceTask.setLocale("zh-CN");
        repository.tasks.add(sourceTask);
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                new FakeAiBatchJobApplicationService(),
                new FakeKnowledgeAiExtractionRepository(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        GraphExtractionTaskResult result = service.regenerateTask(new RegenerateGraphExtractionCommand(
                "GRAPH",
                GraphExtractionTaskId.of(88L),
                "REFINEMENT_APPLIED",
                "{\"sourceContentIds\":[88]}",
                null,
                99L));

        assertEquals("REFINEMENT_APPLIED", result.getTriggerSource());
        GraphExtractionTask parentTask = repository.tasks.get(1);
        assertEquals("REFINEMENT_APPLIED", parentTask.getTriggerSource());
        assertEquals(Boolean.TRUE, parentTask.getReplaceUnconfirmedOnly());
        assertEquals("{\"sourceContentIds\":[88]}", parentTask.getSelectionScopeJson());
    }

    @Test
    void pageTasksShouldMapPersistedTasks() {
        FakeRepository repository = new FakeRepository();
        GraphExtractionTask task = new GraphExtractionTask();
        task.setTaskId(GraphExtractionTaskId.of(11L));
        task.setBatchJobId(1001L);
        task.setTaskType("GRAPH");
        task.setTriggerSource("QUALITY_REPORT");
        task.setStatus("FAILED");
        repository.tasks.add(task);
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        PageResult<GraphExtractionTaskResult> page =
                service.pageTasks("GRAPH", 1001L, "QUALITY_REPORT", null, null, null, new PageQuery(1, 10));

        assertEquals(1, page.getRecords().size());
        assertEquals("11", page.getRecords().get(0).getTaskId());
        assertEquals(Long.valueOf(1001L), page.getRecords().get(0).getBatchJobId());
        assertEquals("GRAPH", page.getRecords().get(0).getTaskType());
        assertEquals("QUALITY_REPORT", page.getRecords().get(0).getTriggerSource());
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
        aiInvocationRepository.invocationLog.setCallId(901L);
        aiInvocationRepository.invocationLog.setStatus("SUCCEEDED");
        aiInvocationRepository.invocationLog.setCompletedAt(Instant.parse("2026-06-23T00:00:00Z"));
        aiInvocationRepository.candidate.setCandidateId(902L);
        aiInvocationRepository.candidate.setAppliedAt(Instant.parse("2026-06-23T00:01:00Z"));
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                aiInvocationRepository,
                null,
                new FakeKnowledgeAiExtractionRepository(),
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
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                graphVersionRepository,
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
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
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                graphVersionRepository,
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        GraphVersionResult detail = service.getVersionDetail(71L);

        assertEquals(71L, detail.getVersionId());
        assertEquals("41", detail.getTaskId());
        assertEquals(902L, detail.getCandidateId());
        assertEquals("LINEAGE", detail.getTaskType());
    }

    @Test
    void pageVersionsShouldExposeLatestAppliedRefinement() {
        FakeGraphVersionRepository graphVersionRepository = new FakeGraphVersionRepository();
        GraphVersion version = new GraphVersion();
        version.setVersionId(71L);
        version.setTaskId(GraphExtractionTaskId.of(41L));
        version.setCandidateId(902L);
        version.setTaskType("GRAPH");
        version.setSourceContentType("SANCAI_ENTRY");
        version.setSourceContentId(1002L);
        version.setVersionNo(3);
        version.setStatus("APPLIED");
        version.setAppliedAt(new Date(1_719_187_200_000L));
        graphVersionRepository.versions.add(version);
        FakeRefinementTaskRepository refinementTaskRepository = new FakeRefinementTaskRepository();
        refinementTaskRepository.latestApplied = new RefinementTask(
                null,
                RefinementTaskId.of(31L),
                "GRAPH",
                "SANCAI_ENTRY",
                1002L,
                "myth",
                "神话",
                71L,
                "APPLIED",
                9L,
                new Date(),
                null,
                null,
                19L,
                new Date(1_719_187_260_000L),
                null,
                null);
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                graphVersionRepository,
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                refinementTaskRepository,
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
                new AiCandidateDomainService(new FakeAiInvocationRepository()),
                null);

        PageResult<GraphVersionResult> page = service.pageVersions("GRAPH", "APPLIED", "SANCAI_ENTRY", 1002L, null);

        GraphVersionResult result = page.getRecords().get(0);
        assertEquals(true, result.getRefinementApplied());
        assertEquals(31L, result.getLastRefinementTaskId());
        assertEquals(1_719_187_260_000L, result.getLastRefinementAppliedAt());
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
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                knowledgeEntityRepository,
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
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
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                knowledgeEntityRepository,
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
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
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                knowledgeRelationRepository,
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
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
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                knowledgeRelationRepository,
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
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
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                knowledgeLineageNodeRepository,
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
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
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                knowledgeLineageNodeRepository,
                new FakeKnowledgeLineageRelationRepository(),
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
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
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                knowledgeLineageRelationRepository,
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
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
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                new FakeRepository(),
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                knowledgeLineageRelationRepository,
                new FakeAiInvocationRepository(),
                null,
                new FakeKnowledgeAiExtractionRepository(),
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
        KnowledgeGraphExtractionApplicationServiceImpl service = service(
                repository,
                new FakeGraphVersionRepository(),
                new FakeKnowledgeEntityRepository(),
                new FakeKnowledgeRelationRepository(),
                new FakeKnowledgeLineageNodeRepository(),
                new FakeKnowledgeLineageRelationRepository(),
                aiInvocationRepository,
                null,
                new FakeKnowledgeAiExtractionRepository(),
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

    private RequestGraphExtractionCommand graphCommand() {
        return new RequestGraphExtractionCommand(
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

    @Getter
    @AllArgsConstructor
    private static final class AiBatchJobCreateCommand {
        private final String scope;
        private final String capability;
        private final String contentType;
        private final int totalCount;
        private final String failureSummaryJson;
    }

    @Getter
    @AllArgsConstructor
    private static final class AiBatchJobResult {
        private final Long batchId;
        private final String scope;
        private final String capability;
        private final String contentType;
        private final String status;
        private final int totalCount;
        private final int successCount;
        private final int failedCount;
        private final int cancelledCount;
        private final String failureSummaryJson;
        private final Instant requestedAt;
        private final Instant cancelledAt;
        private final Instant completedAt;
    }

    private interface AiBatchJobApplicationService {
        AiBatchJobResult get(Long batchId);

        Long create(AiBatchJobCreateCommand command);

        boolean canDispatchNextUnit(Long batchId);

        AiBatchJobResult recordSuccess(Long batchId);

        AiBatchJobResult recordFailure(Long batchId, String failureSummaryJson);

        AiBatchJobResult cancel(Long batchId);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static final class AiInvocationLog {
        private Long callId;
        private Long batchId;
        private String scope;
        private String capability;
        private String contentType;
        private Long contentId;
        private Long objectId;
        private Long serviceId;
        private String serviceRole;
        private Long modelId;
        private String modelName;
        private Long promptVersionId;
        private String requestId;
        private String traceId;
        private String status;
        private boolean streamUsed;
        private boolean streamCompleted;
        private boolean fallbackUsed;
        private String errorType;
        private String errorMessage;
        private String warningsJson;
        private Instant requestedAt;
        private Instant completedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static final class AiCandidate {
        private Long candidateId;
        private Long callId;
        private Long batchId;
        private String capability;
        private String contentType;
        private Long contentId;
        private Long objectId;
        private String resultFormat;
        private String resultPayload;
        private String status = "PENDING";
        private Long promptVersionId;
        private String modelName;
        private String errorType;
        private String errorMessage;
        private Instant requestedAt;
        private Instant appliedAt;
    }

    private interface AiInvocationRepository {
        AiInvocationLog getInvocationLog(Long callId);

        Long saveInvocationLog(AiInvocationLog invocationLog);

        int updateInvocationLog(AiInvocationLog invocationLog);

        List<AiInvocationLog> listInvocationLogs(Instant requestedAtStart, Instant requestedAtEnd);

        AiCandidate getCandidate(Long candidateId);

        Long saveCandidate(AiCandidate candidate);

        int updateCandidate(AiCandidate candidate);

        List<AiCandidate> listCandidates(
                String contentType, Long contentId, Long objectId, String capability, String status);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static final class AiCandidateApplyCheck {
        private Long candidateId;
        private String contentType;
        private Long contentId;
        private String capability;
    }

    private static final class AiCandidateDomainService {
        private final AiInvocationRepository repository;

        private AiCandidateDomainService(AiInvocationRepository repository) {
            this.repository = repository;
        }

        private AiCandidate requirePendingForApply(AiCandidateApplyCheck check) {
            AiCandidate candidate = repository.getCandidate(check.getCandidateId());
            if (candidate == null) {
                throw new BizException("AI candidate not found: " + check.getCandidateId());
            }
            if (!"PENDING".equals(candidate.getStatus())) {
                throw new BizException("AI candidate is not pending: " + check.getCandidateId());
            }
            if (!java.util.Objects.equals(check.getContentType(), candidate.getContentType())
                    || !java.util.Objects.equals(check.getContentId(), candidate.getContentId())
                    || !java.util.Objects.equals(check.getCapability(), candidate.getCapability())) {
                throw new BizException("AI candidate scope does not match apply target");
            }
            return candidate;
        }

        private AiCandidate markApplied(
                Long candidateId, String resultFormat, String resultPayload, Instant appliedAt) {
            AiCandidate candidate = repository.getCandidate(candidateId);
            if (candidate == null) {
                throw new BizException("AI candidate not found: " + candidateId);
            }
            candidate.setResultFormat(resultFormat);
            candidate.setResultPayload(resultPayload);
            candidate.setStatus("APPLIED");
            candidate.setAppliedAt(appliedAt);
            repository.updateCandidate(candidate);
            return candidate;
        }

        private AiCandidate reject(Long candidateId, String errorType, String errorMessage) {
            AiCandidate candidate = repository.getCandidate(candidateId);
            if (candidate == null) {
                throw new BizException("AI candidate not found: " + candidateId);
            }
            candidate.setStatus("REJECTED");
            candidate.setErrorType(errorType);
            candidate.setErrorMessage(errorMessage);
            repository.updateCandidate(candidate);
            return candidate;
        }
    }

    @Getter
    @AllArgsConstructor
    private static final class KnowledgeAiExtractionRequest {
        private final String taskType;
        private final String scopeType;
        private final String scopeJson;
        private final String sourceContentType;
        private final Long sourceContentId;
        private final Long requestedBy;
        private final Long serviceId;
        private final String serviceRole;
        private final Long modelId;
        private final String modelName;
        private final Long promptVersionId;
        private final String requestId;
        private final String traceId;
        private final String promptMessagesJson;
        private final String promptVariablesJson;
        private final String promptHash;
        private final String inputPayloadJson;
        private final String outputSchemaJson;
        private final boolean forceJson;
        private final String locale;
    }

    @Getter
    @AllArgsConstructor
    private static final class KnowledgeAiExtractionResult {
        private final Long callId;
        private final Long candidateId;
        private final String status;
        private final String capability;
        private final String resultFormat;
        private final String resultPayload;
        private final String errorType;
        private final String errorMessage;
    }

    private interface KnowledgeAiExtractionRepository {
        KnowledgeAiExtractionResult extractRelations(KnowledgeAiExtractionRequest request);

        KnowledgeAiExtractionResult extractGraph(KnowledgeAiExtractionRequest request);

        KnowledgeAiExtractionResult extractLineage(KnowledgeAiExtractionRequest request);

        KnowledgeAiExtractionResult extractTags(KnowledgeAiExtractionRequest request);
    }

    private static final class FakeKnowledgeAiExtractionRepository implements KnowledgeAiExtractionRepository {
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

        @Override
        public KnowledgeAiExtractionResult extractTags(KnowledgeAiExtractionRequest request) {
            lastRequest = request;
            lastTaskType = "TAG";
            return new KnowledgeAiExtractionResult(
                    601L, 602L, "SUCCEEDED", "tag_extraction", "STRUCTURED", "{\"tags\":[]}", null, null);
        }
    }

    private static final class FakeAiFacade implements AiFacade {
        private final AiInvocationRepository aiInvocationRepository;
        private final AiBatchJobApplicationService aiBatchJobApplicationService;
        private final KnowledgeAiExtractionRepository knowledgeAiExtractionRepository;
        private final AiCandidateDomainService aiCandidateDomainService;

        private FakeAiFacade(
                AiInvocationRepository aiInvocationRepository,
                AiBatchJobApplicationService aiBatchJobApplicationService,
                KnowledgeAiExtractionRepository knowledgeAiExtractionRepository,
                AiCandidateDomainService aiCandidateDomainService) {
            this.aiInvocationRepository = aiInvocationRepository;
            this.aiBatchJobApplicationService = aiBatchJobApplicationService;
            this.knowledgeAiExtractionRepository = knowledgeAiExtractionRepository;
            this.aiCandidateDomainService = aiCandidateDomainService;
        }

        @Override
        public AiReportSummaryFacadeResponse summary(AiReportSummaryFacadeRequest request) {
            return null;
        }

        @Override
        public DiscoveryAiFacadeResponse understandDiscoveryQuery(DiscoveryAiFacadeRequest request) {
            return null;
        }

        @Override
        public DiscoveryAiFacadeResponse generateDiscoveryAnswer(DiscoveryAiFacadeRequest request) {
            return null;
        }

        @Override
        public DiscoveryAiFacadeResponse streamDiscoveryAnswer(
                DiscoveryAiFacadeRequest request, DiscoveryAiStreamHandler streamHandler) {
            return null;
        }

        @Override
        public KnowledgeAiExtractionFacadeResponse extractKnowledgeRelations(
                KnowledgeAiExtractionFacadeRequest request) {
            return toFacadeResponse(
                    knowledgeAiExtractionRepository == null
                            ? null
                            : knowledgeAiExtractionRepository.extractRelations(toLegacyRequest(request)));
        }

        @Override
        public KnowledgeAiExtractionFacadeResponse extractKnowledgeGraph(KnowledgeAiExtractionFacadeRequest request) {
            return toFacadeResponse(
                    knowledgeAiExtractionRepository == null
                            ? null
                            : knowledgeAiExtractionRepository.extractGraph(toLegacyRequest(request)));
        }

        @Override
        public KnowledgeAiExtractionFacadeResponse extractKnowledgeLineage(KnowledgeAiExtractionFacadeRequest request) {
            return toFacadeResponse(
                    knowledgeAiExtractionRepository == null
                            ? null
                            : knowledgeAiExtractionRepository.extractLineage(toLegacyRequest(request)));
        }

        @Override
        public KnowledgeAiExtractionFacadeResponse extractKnowledgeTags(KnowledgeAiExtractionFacadeRequest request) {
            return toFacadeResponse(
                    knowledgeAiExtractionRepository == null
                            ? null
                            : knowledgeAiExtractionRepository.extractTags(toLegacyRequest(request)));
        }

        @Override
        public AiBatchJobFacadeResponse getBatchJob(Long batchId) {
            return toBatchResponse(
                    aiBatchJobApplicationService == null ? null : aiBatchJobApplicationService.get(batchId));
        }

        @Override
        public AiBatchJobActionFacadeResponse createBatchJob(CreateAiBatchJobFacadeRequest request) {
            Long batchId = aiBatchJobApplicationService == null
                    ? null
                    : aiBatchJobApplicationService.create(new AiBatchJobCreateCommand(
                            request.getScope(),
                            request.getCapability(),
                            request.getContentType(),
                            request.getTotalCount(),
                            request.getFailureSummaryJson()));
            return AiBatchJobActionFacadeResponse.builder().batchId(batchId).build();
        }

        @Override
        public boolean canDispatchNextBatchUnit(Long batchId) {
            return aiBatchJobApplicationService != null && aiBatchJobApplicationService.canDispatchNextUnit(batchId);
        }

        @Override
        public AiBatchJobFacadeResponse recordBatchSuccess(Long batchId) {
            return toBatchResponse(
                    aiBatchJobApplicationService == null ? null : aiBatchJobApplicationService.recordSuccess(batchId));
        }

        @Override
        public AiBatchJobFacadeResponse recordBatchFailure(AiBatchJobFailureFacadeRequest request) {
            return toBatchResponse(
                    aiBatchJobApplicationService == null
                            ? null
                            : aiBatchJobApplicationService.recordFailure(
                                    request.getBatchId(), request.getFailureSummaryJson()));
        }

        @Override
        public AiBatchJobFacadeResponse cancelBatchJob(Long batchId) {
            return toBatchResponse(
                    aiBatchJobApplicationService == null ? null : aiBatchJobApplicationService.cancel(batchId));
        }

        @Override
        public AiInvocationLogFacadeDto getInvocationLog(GetAiInvocationLogFacadeRequest request) {
            return toInvocationLogFacadeDto(
                    aiInvocationRepository == null
                            ? null
                            : aiInvocationRepository.getInvocationLog(request.getCallId()));
        }

        @Override
        public AiCandidateFacadeDto getCandidate(GetAiCandidateFacadeRequest request) {
            return toCandidateFacadeDto(
                    aiInvocationRepository == null
                            ? null
                            : aiInvocationRepository.getCandidate(request.getCandidateId()));
        }

        @Override
        public AiCandidateFacadeDto requirePendingCandidate(RequirePendingAiCandidateFacadeRequest request) {
            if (aiCandidateDomainService == null) {
                return null;
            }
            var check = new AiCandidateApplyCheck();
            check.setCandidateId(request.getCandidateId());
            check.setContentType(request.getContentType());
            check.setContentId(request.getContentId());
            check.setCapability(request.getCapability());
            return toCandidateFacadeDto(aiCandidateDomainService.requirePendingForApply(check));
        }

        @Override
        public AiCandidateFacadeDto markCandidateApplied(MarkAiCandidateAppliedFacadeRequest request) {
            if (aiCandidateDomainService == null) {
                return null;
            }
            return toCandidateFacadeDto(aiCandidateDomainService.markApplied(
                    request.getCandidateId(),
                    request.getResultFormat(),
                    request.getResultPayload(),
                    request.getAppliedAt()));
        }

        @Override
        public AiCandidateFacadeDto rejectCandidate(RejectAiCandidateFacadeRequest request) {
            if (aiCandidateDomainService == null) {
                return null;
            }
            return toCandidateFacadeDto(aiCandidateDomainService.reject(
                    request.getCandidateId(), request.getErrorType(), request.getErrorMessage()));
        }

        private KnowledgeAiExtractionRequest toLegacyRequest(KnowledgeAiExtractionFacadeRequest request) {
            if (request == null) {
                return null;
            }
            return new KnowledgeAiExtractionRequest(
                    request.getTaskType(),
                    request.getScopeType(),
                    request.getScopeJson(),
                    request.getSourceContentType(),
                    request.getSourceContentId(),
                    request.getRequestedBy(),
                    request.getServiceId(),
                    request.getServiceRole(),
                    request.getModelId(),
                    request.getModelName(),
                    request.getPromptVersionId(),
                    request.getRequestId(),
                    request.getTraceId(),
                    request.getPromptMessagesJson(),
                    request.getPromptVariablesJson(),
                    request.getPromptHash(),
                    request.getInputPayloadJson(),
                    request.getOutputSchemaJson(),
                    request.isForceJson(),
                    request.getLocale());
        }

        private KnowledgeAiExtractionFacadeResponse toFacadeResponse(KnowledgeAiExtractionResult result) {
            if (result == null) {
                return null;
            }
            return KnowledgeAiExtractionFacadeResponse.builder()
                    .callId(result.getCallId())
                    .candidateId(result.getCandidateId())
                    .status(result.getStatus())
                    .capability(result.getCapability())
                    .resultFormat(result.getResultFormat())
                    .resultPayload(result.getResultPayload())
                    .errorType(result.getErrorType())
                    .errorMessage(result.getErrorMessage())
                    .build();
        }

        private AiBatchJobFacadeResponse toBatchResponse(AiBatchJobResult result) {
            if (result == null) {
                return null;
            }
            return AiBatchJobFacadeResponse.builder()
                    .batchId(result.getBatchId())
                    .scope(result.getScope())
                    .capability(result.getCapability())
                    .contentType(result.getContentType())
                    .status(result.getStatus())
                    .totalCount(result.getTotalCount())
                    .successCount(result.getSuccessCount())
                    .failedCount(result.getFailedCount())
                    .cancelledCount(result.getCancelledCount())
                    .failureSummaryJson(result.getFailureSummaryJson())
                    .requestedAt(result.getRequestedAt())
                    .cancelledAt(result.getCancelledAt())
                    .completedAt(result.getCompletedAt())
                    .build();
        }

        private AiInvocationLogFacadeDto toInvocationLogFacadeDto(AiInvocationLog invocationLog) {
            if (invocationLog == null) {
                return null;
            }
            return AiInvocationLogFacadeDto.builder()
                    .callId(invocationLog.getCallId())
                    .batchId(invocationLog.getBatchId())
                    .scope(invocationLog.getScope())
                    .capability(invocationLog.getCapability())
                    .contentType(invocationLog.getContentType())
                    .contentId(invocationLog.getContentId())
                    .objectId(invocationLog.getObjectId())
                    .serviceId(invocationLog.getServiceId())
                    .serviceRole(invocationLog.getServiceRole())
                    .modelId(invocationLog.getModelId())
                    .modelName(invocationLog.getModelName())
                    .promptVersionId(invocationLog.getPromptVersionId())
                    .requestId(invocationLog.getRequestId())
                    .traceId(invocationLog.getTraceId())
                    .status(invocationLog.getStatus())
                    .streamUsed(invocationLog.isStreamUsed())
                    .streamCompleted(invocationLog.isStreamCompleted())
                    .fallbackUsed(invocationLog.isFallbackUsed())
                    .errorType(invocationLog.getErrorType())
                    .errorMessage(invocationLog.getErrorMessage())
                    .warningsJson(invocationLog.getWarningsJson())
                    .requestedAt(invocationLog.getRequestedAt())
                    .completedAt(invocationLog.getCompletedAt())
                    .build();
        }

        private AiCandidateFacadeDto toCandidateFacadeDto(AiCandidate candidate) {
            if (candidate == null) {
                return null;
            }
            return AiCandidateFacadeDto.builder()
                    .candidateId(candidate.getCandidateId())
                    .callId(candidate.getCallId())
                    .batchId(candidate.getBatchId())
                    .capability(candidate.getCapability())
                    .contentType(candidate.getContentType())
                    .contentId(candidate.getContentId())
                    .objectId(candidate.getObjectId())
                    .resultFormat(candidate.getResultFormat())
                    .resultPayload(candidate.getResultPayload())
                    .status(candidate.getStatus())
                    .promptVersionId(candidate.getPromptVersionId())
                    .modelName(candidate.getModelName())
                    .errorType(candidate.getErrorType())
                    .errorMessage(candidate.getErrorMessage())
                    .requestedAt(candidate.getRequestedAt())
                    .appliedAt(candidate.getAppliedAt())
                    .build();
        }
    }

    private static final class FakeAiBatchJobApplicationService implements AiBatchJobApplicationService {
        private AiBatchJobResult lastResult;
        private int recordSuccessCalls;
        private int recordFailureCalls;

        @Override
        public AiBatchJobResult get(Long batchId) {
            return lastResult;
        }

        @Override
        public Long create(AiBatchJobCreateCommand command) {
            lastResult = new AiBatchJobResult(
                    1001L,
                    command.getScope(),
                    command.getCapability(),
                    command.getContentType(),
                    "RUNNING",
                    command.getTotalCount(),
                    0,
                    0,
                    0,
                    command.getFailureSummaryJson(),
                    Instant.parse("2026-06-26T00:00:00Z"),
                    null,
                    null);
            return lastResult.getBatchId();
        }

        @Override
        public boolean canDispatchNextUnit(Long batchId) {
            return lastResult != null && !"CANCELLED".equals(lastResult.getStatus());
        }

        @Override
        public AiBatchJobResult recordSuccess(Long batchId) {
            recordSuccessCalls++;
            lastResult = new AiBatchJobResult(
                    batchId,
                    lastResult.getScope(),
                    lastResult.getCapability(),
                    lastResult.getContentType(),
                    recordSuccessCalls + recordFailureCalls >= lastResult.getTotalCount() ? "SUCCEEDED" : "RUNNING",
                    lastResult.getTotalCount(),
                    lastResult.getSuccessCount() + 1,
                    lastResult.getFailedCount(),
                    lastResult.getCancelledCount(),
                    lastResult.getFailureSummaryJson(),
                    lastResult.getRequestedAt(),
                    lastResult.getCancelledAt(),
                    recordSuccessCalls + recordFailureCalls >= lastResult.getTotalCount()
                            ? Instant.parse("2026-06-26T00:10:00Z")
                            : null);
            return lastResult;
        }

        @Override
        public AiBatchJobResult recordFailure(Long batchId, String failureSummaryJson) {
            recordFailureCalls++;
            lastResult = new AiBatchJobResult(
                    batchId,
                    lastResult.getScope(),
                    lastResult.getCapability(),
                    lastResult.getContentType(),
                    "PARTIAL",
                    lastResult.getTotalCount(),
                    lastResult.getSuccessCount(),
                    lastResult.getFailedCount() + 1,
                    lastResult.getCancelledCount(),
                    failureSummaryJson,
                    lastResult.getRequestedAt(),
                    lastResult.getCancelledAt(),
                    recordSuccessCalls + recordFailureCalls >= lastResult.getTotalCount()
                            ? Instant.parse("2026-06-26T00:10:00Z")
                            : null);
            return lastResult;
        }

        @Override
        public AiBatchJobResult cancel(Long batchId) {
            lastResult = new AiBatchJobResult(
                    batchId,
                    lastResult.getScope(),
                    lastResult.getCapability(),
                    lastResult.getContentType(),
                    "CANCELLED",
                    lastResult.getTotalCount(),
                    lastResult.getSuccessCount(),
                    lastResult.getFailedCount(),
                    Math.max(
                            0, lastResult.getTotalCount() - lastResult.getSuccessCount() - lastResult.getFailedCount()),
                    lastResult.getFailureSummaryJson(),
                    lastResult.getRequestedAt(),
                    Instant.parse("2026-06-26T00:05:00Z"),
                    null);
            return lastResult;
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
        public List<GraphExtractionTask> listByBatchJobId(Long batchJobId) {
            return tasks.stream()
                    .filter(task -> batchJobId != null && batchJobId.equals(task.getBatchJobId()))
                    .toList();
        }

        @Override
        public PageResult<GraphExtractionTask> page(
                String taskType,
                Long batchJobId,
                String triggerSource,
                String status,
                String sourceContentType,
                Long sourceContentId,
                int pageNo,
                int pageSize) {
            List<GraphExtractionTask> filteredTasks = tasks.stream()
                    .filter(task -> taskType == null || taskType.equals(task.getTaskType()))
                    .filter(task -> batchJobId == null || batchJobId.equals(task.getBatchJobId()))
                    .filter(task -> triggerSource == null || triggerSource.equals(task.getTriggerSource()))
                    .filter(task -> status == null || status.equals(task.getStatus()))
                    .filter(task -> sourceContentType == null || sourceContentType.equals(task.getSourceContentType()))
                    .filter(task -> sourceContentId == null || sourceContentId.equals(task.getSourceContentId()))
                    .toList();
            return PageResult.of(pageNo, pageSize, filteredTasks.size(), filteredTasks);
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

    private static final class FakeRefinementTaskRepository implements RefinementTaskRepository {
        private RefinementTask latestApplied;

        @Override
        public RefinementTask getByTaskId(RefinementTaskId taskId) {
            return null;
        }

        @Override
        public RefinementTask findLatestDraft(
                String taskType, String sourceContentType, Long sourceContentId, Long graphVersionId) {
            return null;
        }

        @Override
        public RefinementTask findLatestAppliedByGraphVersionId(Long graphVersionId) {
            return latestApplied != null && graphVersionId.equals(latestApplied.getGraphVersionId())
                    ? latestApplied
                    : null;
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
            return 0L;
        }

        @Override
        public int update(RefinementTask entity) {
            return 0;
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
        public List<KnowledgeEntity> listByVersionId(Long versionId) {
            return entities.stream()
                    .filter(entity -> versionId == null || versionId.equals(entity.getLatestVersionId()))
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

        @Override
        public int deleteByEntityKeys(Collection<String> entityKeys) {
            return 0;
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
        public List<KnowledgeRelation> listByVersionId(Long versionId) {
            return relations.stream()
                    .filter(relation -> versionId == null || versionId.equals(relation.getLatestVersionId()))
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

        @Override
        public int deleteByRelationKeys(Collection<String> relationKeys) {
            return 0;
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
        public List<KnowledgeLineageNode> listByVersionId(Long versionId) {
            return nodes.stream()
                    .filter(node -> versionId == null || versionId.equals(node.getLatestVersionId()))
                    .toList();
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

        @Override
        public int deleteByNodeKeys(Collection<String> nodeKeys) {
            return 0;
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
        public List<KnowledgeLineageRelation> listByVersionId(Long versionId) {
            return relations.stream()
                    .filter(relation -> versionId == null || versionId.equals(relation.getLatestVersionId()))
                    .toList();
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

        @Override
        public int deleteByRelationKeys(Collection<String> relationKeys) {
            return 0;
        }
    }

    private static final class FakeAiInvocationRepository implements AiInvocationRepository {
        private final AiInvocationLog invocationLog = new AiInvocationLog();
        private final AiCandidate candidate = new AiCandidate();

        @Override
        public AiInvocationLog getInvocationLog(Long callId) {
            return invocationLog.getCallId() != null
                            && invocationLog.getCallId().equals(callId)
                    ? invocationLog
                    : null;
        }

        @Override
        public Long saveInvocationLog(AiInvocationLog invocationLog) {
            return null;
        }

        @Override
        public int updateInvocationLog(AiInvocationLog invocationLog) {
            return 0;
        }

        @Override
        public List<AiInvocationLog> listInvocationLogs(Instant requestedAtStart, Instant requestedAtEnd) {
            return List.of();
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
        public List<AiCandidate> listCandidates(
                String contentType, Long contentId, Long objectId, String capability, String status) {
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
                GraphExtractionTask task, AiCandidateFacadeDto candidate) {
            appliedCandidateId = candidate == null ? null : candidate.getCandidateId();
            return null;
        }
    }
}
