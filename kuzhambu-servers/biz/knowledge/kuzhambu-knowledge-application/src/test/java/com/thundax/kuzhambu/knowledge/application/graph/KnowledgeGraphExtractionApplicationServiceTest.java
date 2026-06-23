package com.thundax.kuzhambu.knowledge.application.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionRequest;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.domain.knowledge.service.KnowledgeAiExtractionDomainService;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.impl.KnowledgeGraphExtractionApplicationServiceImpl;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeGraphExtractionApplicationServiceTest {

    @Test
    void requestRelationExtractionShouldPersistTaskAndSyncAiResult() {
        FakeRepository repository = new FakeRepository();
        FakeKnowledgeAiExtractionDomainService aiService = new FakeKnowledgeAiExtractionDomainService();
        KnowledgeGraphExtractionApplicationServiceImpl service =
                new KnowledgeGraphExtractionApplicationServiceImpl(repository, aiService);

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
                repository, new FakeKnowledgeAiExtractionDomainService());

        PageResult<GraphExtractionTaskResult> page = service.pageTasks("GRAPH", null, null, null, new PageQuery(1, 10));

        assertEquals(1, page.getRecords().size());
        assertEquals("11", page.getRecords().get(0).getTaskId());
        assertEquals("GRAPH", page.getRecords().get(0).getTaskType());
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
}
