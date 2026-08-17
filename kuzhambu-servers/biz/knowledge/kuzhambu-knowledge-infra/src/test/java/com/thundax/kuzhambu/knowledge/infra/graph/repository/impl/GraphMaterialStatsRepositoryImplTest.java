package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialStatsMapper;
import java.lang.reflect.Method;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class GraphMaterialStatsRepositoryImplTest {

    @Test
    void refreshShouldDelegateSingleMaterialAggregateUpsert() {
        GraphMaterialStatsMapper mapper = mock(GraphMaterialStatsMapper.class);
        GraphMaterialStatsRepositoryImpl repository = new GraphMaterialStatsRepositoryImpl(mapper);
        Instant calculatedAt = Instant.parse("2026-08-17T00:00:00Z");
        when(mapper.refresh(11L, calculatedAt)).thenReturn(1);

        assertThat(repository.refresh(11L, calculatedAt)).isEqualTo(1);
        assertThat(repository.refresh(null, calculatedAt)).isZero();

        verify(mapper).refresh(11L, calculatedAt);
        verify(mapper, never()).refresh(null, calculatedAt);
    }

    @Test
    void refreshSqlShouldRebuildStatsFromGroupedGraphTables() throws Exception {
        String sql = insertSql("refresh", Long.class, Instant.class);

        assertThat(sql).contains("insert into knowledge_graph_material_stats");
        assertThat(sql).contains("knowledge_graph_material_node");
        assertThat(sql).contains("knowledge_graph_material_edge");
        assertThat(sql).contains("knowledge_graph_published_node_material");
        assertThat(sql).contains("knowledge_graph_published_edge_material");
        assertThat(sql).contains("knowledge_graph_extraction_task");
        assertThat(sql).contains("group by material_id");
        assertThat(sql).contains("execution_status in ('PENDING', 'RUNNING')");
        assertThat(sql).contains("on duplicate key update");
        assertThat(sql).doesNotContain("select *");
    }

    private static String insertSql(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = GraphMaterialStatsMapper.class.getMethod(methodName, parameterTypes);
        return method.getAnnotation(org.apache.ibatis.annotations.Insert.class).value()[0];
    }
}
