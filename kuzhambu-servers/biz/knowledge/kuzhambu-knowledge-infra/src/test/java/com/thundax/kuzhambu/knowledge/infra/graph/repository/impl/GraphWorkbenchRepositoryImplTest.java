package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchMetrics;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedEdgeMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedNodeMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphWorkbenchMapper;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphWorkbenchRepositoryImplTest {

    @Test
    void overviewShouldExposeActivityAndPendingConflictReadModels() {
        GraphWorkbenchMapper mapper = mock(GraphWorkbenchMapper.class);
        GraphWorkbenchRepositoryImpl repository = new GraphWorkbenchRepositoryImpl(
                mapper, mock(GraphPublishedNodeMapper.class), mock(GraphPublishedEdgeMapper.class));
        GraphWorkbenchMapper.ActivityRow activity = new GraphWorkbenchMapper.ActivityRow();
        activity.setType("PUBLICATION");
        activity.setContentType("SANCAI_ENTRY");
        activity.setContentRefId(1001L);
        activity.setOccurredAt(Instant.parse("2026-08-14T08:00:00Z"));
        activity.setSummary("发布素材 三才图会 PUBLISHED");
        when(mapper.countActiveNodes()).thenReturn(12L);
        when(mapper.countActiveEdges()).thenReturn(18L);
        when(mapper.countCoveredMaterials()).thenReturn(4L);
        when(mapper.countIsolatedNodes(null)).thenReturn(1L);
        when(mapper.listRecentActivities(anyInt())).thenReturn(List.of(activity));
        when(mapper.countPendingPublicationConflicts()).thenReturn(3L);

        GraphWorkbenchMetrics metrics = repository.getByOverview(List.of());

        assertThat(metrics.publishedNodeCount()).isEqualTo(12L);
        assertThat(metrics.recentActivities()).hasSize(1);
        assertThat(metrics.recentActivities().get(0).contentRef()).isEqualTo(new ContentRef("SANCAI_ENTRY", 1001L));
        assertThat(metrics.recentActivities().get(0).occurredAt()).isEqualTo(Instant.parse("2026-08-14T08:00:00Z"));
        assertThat(metrics.pendingConflictCount()).isEqualTo(3L);
        verify(mapper).listRecentActivities(10);
        verify(mapper).countPendingPublicationConflicts();
    }

    @Test
    void mapperSqlShouldReadActivitiesAndConflictsFromGraphTables() throws Exception {
        String activitySql = selectSql("listRecentActivities", int.class);
        String conflictSql = selectSql("countPendingPublicationConflicts");

        assertThat(activitySql).contains("knowledge_graph_publish_record");
        assertThat(activitySql).contains("knowledge_graph_governance_operation");
        assertThat(activitySql).contains("knowledge_graph_material_deletion_change");
        assertThat(activitySql).contains("order by occurredAt desc");
        assertThat(conflictSql).contains("knowledge_graph_publication_preview_token");
        assertThat(conflictSql).contains("consumed_at is null");
        assertThat(conflictSql).contains("CONFLICT");
        assertThat(conflictSql).contains("$.nodes[*].matchType");
        assertThat(conflictSql).contains("$.edges[*].matchType");
    }

    private static String selectSql(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = GraphWorkbenchMapper.class.getMethod(methodName, parameterTypes);
        return method.getAnnotation(org.apache.ibatis.annotations.Select.class).value()[0];
    }
}
