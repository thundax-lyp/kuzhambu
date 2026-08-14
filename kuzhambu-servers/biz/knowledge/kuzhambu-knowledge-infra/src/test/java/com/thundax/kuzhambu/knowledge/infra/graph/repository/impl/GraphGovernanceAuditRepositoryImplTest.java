package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphGovernanceOperation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphManualSource;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphGovernanceOperationId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphManualSourceId;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphGovernanceOperationDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphManualSourceDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphGovernanceOperationMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphManualSourceMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphGovernanceAuditRepositoryImplTest {

    @Test
    void assemblerShouldMapGovernanceOperationReasonSnapshotsAndAuditLogId() {
        Instant operatedAt = Instant.parse("2026-08-14T08:00:00Z");
        GraphGovernanceOperation operation = new GraphGovernanceOperation(
                new GraphGovernanceOperationId(1001L),
                "NODE_UPDATE",
                "NODE",
                2001L,
                "{\"name\":\"old\"}",
                "{\"name\":\"new\"}",
                "订正节点名称",
                3001L,
                operatedAt);

        GraphGovernanceOperationDO dataObject = GraphPersistenceAssembler.toObject(operation);
        GraphGovernanceOperation restored = GraphPersistenceAssembler.toDomain(dataObject);

        assertThat(dataObject.getReason()).isEqualTo("订正节点名称");
        assertThat(dataObject.getBeforeSnapshotJson()).isEqualTo("{\"name\":\"old\"}");
        assertThat(dataObject.getAfterSnapshotJson()).isEqualTo("{\"name\":\"new\"}");
        assertThat(dataObject.getAuditLogId()).isEqualTo(3001L);
        assertThat(restored.getAuditLogId()).isEqualTo(3001L);
        assertThat(restored.getOperatedAt()).isEqualTo(operatedAt);
    }

    @Test
    void assemblerShouldMapManualSourceReasonAndAuditLogId() {
        Instant recordedAt = Instant.parse("2026-08-14T08:01:00Z");
        GraphManualSource source = new GraphManualSource(
                new GraphManualSourceId(1002L), "NODE_PROPERTY", 2002L, "人工补充别名", 3002L, recordedAt);

        GraphManualSourceDO dataObject = GraphPersistenceAssembler.toObject(source);
        GraphManualSource restored = GraphPersistenceAssembler.toDomain(dataObject);

        assertThat(dataObject.getTargetType()).isEqualTo("NODE_PROPERTY");
        assertThat(dataObject.getReason()).isEqualTo("人工补充别名");
        assertThat(dataObject.getAuditLogId()).isEqualTo(3002L);
        assertThat(restored.getAuditLogId()).isEqualTo(3002L);
        assertThat(restored.getRecordedAt()).isEqualTo(recordedAt);
    }

    @Test
    void governanceOperationRepositoryShouldNotQueryManualSourceTable() {
        GraphGovernanceOperationMapper mapper = mock(GraphGovernanceOperationMapper.class);
        GraphGovernanceOperationDO row = new GraphGovernanceOperationDO(
                1001L,
                "NODE_UPDATE",
                "NODE",
                2001L,
                "{\"name\":\"old\"}",
                "{\"name\":\"new\"}",
                "订正节点名称",
                3001L,
                Instant.parse("2026-08-14T08:00:00Z"));
        when(mapper.selectList(any())).thenReturn(List.of(row));
        GraphGovernanceOperationRepositoryImpl repository = new GraphGovernanceOperationRepositoryImpl(mapper);

        List<GraphGovernanceOperation> operations = repository.listByTarget("NODE", 2001L);

        assertThat(operations)
                .singleElement()
                .extracting(GraphGovernanceOperation::getAuditLogId)
                .isEqualTo(3001L);
        verify(mapper).selectList(any());
    }

    @Test
    void manualSourceRepositoryShouldNotQueryGovernanceOperationTable() {
        GraphManualSourceMapper mapper = mock(GraphManualSourceMapper.class);
        GraphManualSourceDO row = new GraphManualSourceDO(
                1002L, "NODE_PROPERTY", 2002L, "人工补充别名", 3002L, Instant.parse("2026-08-14T08:01:00Z"));
        when(mapper.selectList(any())).thenReturn(List.of(row));
        GraphManualSourceRepositoryImpl repository = new GraphManualSourceRepositoryImpl(mapper);

        List<GraphManualSource> sources = repository.listByTarget("NODE_PROPERTY", 2002L);

        assertThat(sources)
                .singleElement()
                .extracting(GraphManualSource::getAuditLogId)
                .isEqualTo(3002L);
        verify(mapper).selectList(any());
    }
}
