package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionChange;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionChangeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionTaskId;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDeletionTaskDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialDeletionTaskMapper;
import java.lang.reflect.Method;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GraphMaterialDeletionTaskRepositoryImplTest {

    @Test
    void assemblerShouldMapIdempotencyKeyAndLockVersion() {
        Instant requestedAt = Instant.parse("2026-08-14T08:00:00Z");
        GraphMaterialDeletionTask task = new GraphMaterialDeletionTask(
                new GraphMaterialDeletionTaskId(1001L),
                new GraphMaterialDeletionChangeId(2001L),
                "delete:SANCAI_ENTRY:3001",
                GraphMaterialDeletionStatus.PENDING,
                3L,
                25,
                null,
                "{\"stage\":\"mapping\"}",
                requestedAt,
                null);

        GraphMaterialDeletionTaskDO dataObject = GraphPersistenceAssembler.toObject(task);
        GraphMaterialDeletionTask restored = GraphPersistenceAssembler.toDomain(dataObject);

        assertThat(dataObject.getIdempotencyKey()).isEqualTo("delete:SANCAI_ENTRY:3001");
        assertThat(dataObject.getLockVersion()).isEqualTo(3L);
        assertThat(restored.getDeletionChangeId()).isEqualTo(new GraphMaterialDeletionChangeId(2001L));
        assertThat(restored.getProgress()).isEqualTo(25);
    }

    @Test
    void insertShouldReturnExistingTaskForSameIdempotencyKey() {
        GraphMaterialDeletionTaskMapper mapper = mock(GraphMaterialDeletionTaskMapper.class);
        GraphMaterialDeletionTaskRepositoryImpl repository = new GraphMaterialDeletionTaskRepositoryImpl(mapper);
        GraphMaterialDeletionTask task = new GraphMaterialDeletionTask();
        task.setIdempotencyKey("delete:SANCAI_ENTRY:3001");
        when(mapper.insert(any(GraphMaterialDeletionTaskDO.class)))
                .thenThrow(new org.springframework.dao.DuplicateKeyException("dup"));
        GraphMaterialDeletionTaskDO existing = new GraphMaterialDeletionTaskDO();
        existing.setId(1001L);
        existing.setDeletionChangeId(2001L);
        existing.setIdempotencyKey("delete:SANCAI_ENTRY:3001");
        existing.setStatus(GraphMaterialDeletionStatus.PENDING.value());
        existing.setLockVersion(0L);
        existing.setProgress(0);
        existing.setRequestedAt(Instant.parse("2026-08-14T08:00:00Z"));
        when(mapper.selectOne(any())).thenReturn(existing);

        assertThat(repository.insert(task)).isEqualTo(new GraphMaterialDeletionTaskId(1001L));
    }

    @Test
    void updateShouldReturnGraphLockConflictWhenVersionMismatch() {
        GraphMaterialDeletionTaskMapper mapper = mock(GraphMaterialDeletionTaskMapper.class);
        GraphMaterialDeletionTaskRepositoryImpl repository = new GraphMaterialDeletionTaskRepositoryImpl(mapper);
        GraphMaterialDeletionTask task = new GraphMaterialDeletionTask();
        task.setId(new GraphMaterialDeletionTaskId(1001L));
        when(mapper.updateIfLockVersion(any(), any())).thenReturn(0);

        assertThatThrownBy(() -> repository.updateIfLockVersion(task, 6L))
                .isInstanceOf(DomainException.class)
                .extracting("code")
                .isEqualTo(GraphMaterialDeletionChange.LOCK_CONFLICT_CODE);

        ArgumentCaptor<Long> lockVersion = ArgumentCaptor.forClass(Long.class);
        verify(mapper).updateIfLockVersion(any(), lockVersion.capture());
        assertThat(lockVersion.getValue()).isEqualTo(6L);
    }

    @Test
    void mapperUpdateShouldUseTaskLockVersionOnly() throws Exception {
        Method method = GraphMaterialDeletionTaskMapper.class.getMethod(
                "updateIfLockVersion", GraphMaterialDeletionTaskDO.class, Long.class);
        String sql =
                method.getAnnotation(org.apache.ibatis.annotations.Update.class).value()[0];

        assertThat(sql).contains("update knowledge_graph_material_deletion_task");
        assertThat(sql).contains("lock_version = lock_version + 1");
        assertThat(sql).contains("and lock_version = #{expectedLockVersion}");
        assertThat(sql).doesNotContain("knowledge_graph_material_deletion_change set");
        assertThat(sql).doesNotContain("knowledge_graph_material_node_mapping");
        assertThat(sql).doesNotContain("knowledge_graph_material_edge_mapping");
    }
}
