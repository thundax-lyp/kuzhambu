package com.thundax.kuzhambu.ai.infra.refinement.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import com.thundax.kuzhambu.ai.infra.refinement.persistence.dataobject.AiRefinementTaskDO;
import com.thundax.kuzhambu.ai.infra.refinement.persistence.mapper.AiRefinementTaskMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AiRefinementTaskRepositoryIT {

    @Test
    void schemaSqlShouldDeclareRefinementTaskPersistenceObjects() throws IOException {
        String schemaSql = readRequiredSql("db/schema/ai.sql");

        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_refinement_task`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_refinement_task_id`"));
        assertTrue(schemaSql.contains("`status`"));
        assertTrue(schemaSql.contains("`task_id`"));
        assertTrue(schemaSql.contains("`capability`"));
        assertTrue(schemaSql.contains("`content_type`"));
        assertTrue(schemaSql.contains("`requested_by`"));
        assertTrue(schemaSql.contains("`call_id`"));
        assertTrue(schemaSql.contains("`candidate_id`"));
        assertTrue(schemaSql.contains("`failure_stage`"));
        assertTrue(schemaSql.contains("`error_type`"));
        assertTrue(schemaSql.contains("`error_message`"));
        assertTrue(schemaSql.contains("`result_preview`"));
    }

    @Test
    void repositoryShouldMapTaskWritesAndReads() {
        AiRefinementTaskMapper mapper = mock(AiRefinementTaskMapper.class);
        AiRefinementTaskRepositoryImpl repository = new AiRefinementTaskRepositoryImpl(mapper);
        Instant requestedAt = Instant.parse("2026-01-07T00:00:00Z");

        AiRefinementTask task = new AiRefinementTask();
        task.setTaskId(7201L);
        task.setScope("classics");
        task.setCapability("summary");
        task.setContentType("ENTRY");
        task.setContentId(9101L);
        task.setObjectId(9201L);
        task.setRequestedBy(1001L);
        task.setRequestId("task-req-1");
        task.setTraceId("trace-1");
        task.setStatus("RUNNING");
        task.setServiceRole("PRIMARY");
        task.setModelId(2001L);
        task.setModelName("gpt-test");
        task.setPromptVersionId(5001L);
        task.setCallId(7001L);
        task.setCandidateId(7101L);
        task.setFailureStage("WORKER_RESULT");
        task.setErrorType("WARN");
        task.setErrorMessage("短暂错误");
        task.setResultFormat("TEXT");
        task.setResultPreview("摘要文本");
        task.setStreamEnabled(true);
        task.setRequestedAt(requestedAt);
        task.setStartedAt(requestedAt);
        task.setCompletedAt(requestedAt);

        Long taskId = repository.saveTask(task);

        ArgumentCaptor<AiRefinementTaskDO> taskCaptor = ArgumentCaptor.forClass(AiRefinementTaskDO.class);
        verify(mapper).insertTask(taskCaptor.capture());
        AiRefinementTaskDO savedTask = taskCaptor.getValue();
        assertEquals(7201L, taskId);
        assertEquals("summary", savedTask.getCapability());
        assertEquals("WARN", savedTask.getErrorType());
        assertEquals("TEXT", savedTask.getResultFormat());
        assertEquals("摘要文本", savedTask.getResultPreview());
        assertEquals(true, savedTask.getStreamEnabled());
        assertEquals(requestedAt, savedTask.getRequestedAt());
        assertEquals(requestedAt, savedTask.getStartedAt());

        when(mapper.selectTask(7201L)).thenReturn(savedTask);
        when(mapper.selectTasks("summary", "RUNNING", "ENTRY", 9101L, 1001L, 0, 20))
                .thenReturn(List.of(savedTask));
        when(mapper.countTasks("summary", "RUNNING", "ENTRY", 9101L, 1001L)).thenReturn(1L);

        AiRefinementTask loadedTask = repository.getTask(7201L);
        List<AiRefinementTask> taskList = repository.listTasks("summary", "RUNNING", "ENTRY", 9101L, 1001L, 1, 20);
        long total = repository.countTasks("summary", "RUNNING", "ENTRY", 9101L, 1001L);

        assertEquals("gpt-test", loadedTask.getModelName());
        assertEquals("摘要文本", loadedTask.getResultPreview());
        assertTrue(loadedTask.isStreamEnabled());
        assertEquals(1, taskList.size());
        assertEquals("WORKER_RESULT", taskList.get(0).getFailureStage());
        assertEquals(1L, total);
    }

    private static String readRequiredSql(String path) throws IOException {
        for (Path candidate : List.of(Path.of(path), Path.of("../" + path), Path.of("../../../../" + path))) {
            if (Files.exists(candidate)) {
                return Files.readString(candidate);
            }
        }
        throw new IOException("Required SQL file not found: " + path);
    }
}
