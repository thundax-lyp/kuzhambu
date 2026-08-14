package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionChange;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionDecision;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionChangeId;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDeletionChangeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialDeletionChangeMapper;
import java.lang.reflect.Method;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GraphMaterialDeletionChangeRepositoryImplTest {

    @Test
    void assemblerShouldMapLockVersionAndSnapshotFields() {
        Instant requestedAt = Instant.parse("2026-08-14T08:00:00Z");
        Instant completedAt = Instant.parse("2026-08-14T08:01:00Z");
        GraphMaterialDeletionChange change = new GraphMaterialDeletionChange(
                new GraphMaterialDeletionChangeId(1001L),
                2001L,
                new ContentRef("SANCAI_ENTRY", 3001L),
                "{\"nodes\":[]}",
                GraphMaterialDeletionDecision.PRESERVE_CONTRIBUTION,
                GraphMaterialDeletionStatus.PENDING,
                7L,
                "{\"task\":\"created\"}",
                requestedAt,
                completedAt);

        GraphMaterialDeletionChangeDO dataObject = GraphPersistenceAssembler.toObject(change);
        GraphMaterialDeletionChange restored = GraphPersistenceAssembler.toDomain(dataObject);

        assertThat(dataObject.getLockVersion()).isEqualTo(7L);
        assertThat(dataObject.getMaterialSnapshotJson()).isEqualTo("{\"nodes\":[]}");
        assertThat(restored.getDecision()).isEqualTo(GraphMaterialDeletionDecision.PRESERVE_CONTRIBUTION);
        assertThat(restored.getStatus()).isEqualTo(GraphMaterialDeletionStatus.PENDING);
        assertThat(restored.getLockVersion()).isEqualTo(7L);
    }

    @Test
    void updateShouldReturnGraphLockConflictWhenVersionMismatch() {
        GraphMaterialDeletionChangeMapper mapper = mock(GraphMaterialDeletionChangeMapper.class);
        GraphMaterialDeletionChangeRepositoryImpl repository = new GraphMaterialDeletionChangeRepositoryImpl(mapper);
        GraphMaterialDeletionChange change = new GraphMaterialDeletionChange();
        change.setId(new GraphMaterialDeletionChangeId(1001L));
        when(mapper.updateIfLockVersion(any(), any())).thenReturn(0);

        assertThatThrownBy(() -> repository.updateIfLockVersion(change, 6L))
                .isInstanceOf(DomainException.class)
                .extracting("code")
                .isEqualTo(GraphMaterialDeletionChange.LOCK_CONFLICT_CODE);

        ArgumentCaptor<Long> lockVersion = ArgumentCaptor.forClass(Long.class);
        verify(mapper).updateIfLockVersion(any(), lockVersion.capture());
        assertThat(lockVersion.getValue()).isEqualTo(6L);
    }

    @Test
    void mapperUpdateShouldOnlyTouchDeletionChangeTableAndLockVersion() throws Exception {
        Method method = GraphMaterialDeletionChangeMapper.class.getMethod(
                "updateIfLockVersion", GraphMaterialDeletionChangeDO.class, Long.class);
        String sql =
                method.getAnnotation(org.apache.ibatis.annotations.Update.class).value()[0];

        assertThat(sql).contains("update knowledge_graph_material_deletion_change");
        assertThat(sql).contains("lock_version = lock_version + 1");
        assertThat(sql).contains("and lock_version = #{expectedLockVersion}");
        assertThat(sql).doesNotContain("knowledge_graph_material_deletion_task");
        assertThat(sql).doesNotContain("knowledge_graph_material_node_mapping");
        assertThat(sql).doesNotContain("knowledge_graph_material_edge_mapping");
    }
}
