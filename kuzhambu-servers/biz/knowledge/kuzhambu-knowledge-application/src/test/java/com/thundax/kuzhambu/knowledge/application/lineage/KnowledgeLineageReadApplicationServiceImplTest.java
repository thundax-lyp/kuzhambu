package com.thundax.kuzhambu.knowledge.application.lineage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.lineage.query.LineageCanvasQuery;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult;
import com.thundax.kuzhambu.knowledge.application.lineage.service.impl.KnowledgeLineageReadApplicationServiceImpl;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeLineageReadApplicationServiceImplTest {

    @Test
    void getCanvasShouldReturnNoVersionEmptyStateWhenVersionIsMissing() {
        GraphVersionRepository versionRepository = mock(GraphVersionRepository.class);
        KnowledgeLineageNodeRepository nodeRepository = mock(KnowledgeLineageNodeRepository.class);
        KnowledgeLineageRelationRepository relationRepository = mock(KnowledgeLineageRelationRepository.class);
        KnowledgeLineageReadApplicationServiceImpl service =
                new KnowledgeLineageReadApplicationServiceImpl(versionRepository, nodeRepository, relationRepository);
        when(versionRepository.page(GraphExtractionTaskType.LINEAGE, GraphVersionStatus.APPLIED, null, null, 1, 200))
                .thenReturn(PageResult.of(1, 200, 0, List.of()));

        LineageCanvasResult result = service.getCanvas(new LineageCanvasQuery());

        assertEquals("NO_VERSION", result.getEmpty().getReason());
        assertEquals(0L, result.getSummary().getNodeCount());
        assertTrue(result.getAvailableFilters().getVersions().isEmpty());
        verify(versionRepository, never()).getByVersionId(GraphVersionIdCodec.toDomain(71L));
    }

    @Test
    void getLatestAppliedCanvasShouldUseLatestVersionAndMapCanvasFields() {
        GraphVersionRepository versionRepository = mock(GraphVersionRepository.class);
        KnowledgeLineageNodeRepository nodeRepository = mock(KnowledgeLineageNodeRepository.class);
        KnowledgeLineageRelationRepository relationRepository = mock(KnowledgeLineageRelationRepository.class);
        KnowledgeLineageReadApplicationServiceImpl service =
                new KnowledgeLineageReadApplicationServiceImpl(versionRepository, nodeRepository, relationRepository);
        GraphVersion version = lineageVersion();
        KnowledgeLineageNode father = node(301L, "person:father", "贾代善", "MALE", "CONFIRMED");
        KnowledgeLineageNode son = node(302L, "person:son", "贾政", "MALE", "PENDING");
        KnowledgeLineageRelation relation = relation();
        when(versionRepository.page(GraphExtractionTaskType.LINEAGE, GraphVersionStatus.APPLIED, null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 1, List.of(version)));
        when(versionRepository.page(GraphExtractionTaskType.LINEAGE, GraphVersionStatus.APPLIED, null, null, 1, 200))
                .thenReturn(PageResult.of(1, 200, 1, List.of(version)));
        when(versionRepository.getByVersionId(GraphVersionIdCodec.toDomain(71L)))
                .thenReturn(version);
        when(nodeRepository.listByVersionId(71L)).thenReturn(List.of(father, son));
        when(relationRepository.listByVersionId(71L)).thenReturn(List.of(relation));
        LineageCanvasQuery query = new LineageCanvasQuery();
        query.setFocusNodeId(302L);
        query.setDepth(1);

        LineageCanvasResult result = service.getLatestAppliedCanvas(query);

        assertEquals(71L, result.getVersion().getVersionId());
        assertEquals(2L, result.getSummary().getNodeCount());
        assertEquals(1L, result.getSummary().getRelationCount());
        assertEquals(1L, result.getSummary().getConfirmedNodeCount());
        assertEquals(1L, result.getSummary().getConfirmedRelationCount());
        assertEquals(302L, result.getSelectedNode().getNodeId());
        assertEquals("person:son", result.getSelectedNode().getNodeKey());
        assertEquals(
                "SANCAI_ENTRY", result.getSelectedNode().getSourceRefs().get(0).getSourceContentType());
        assertEquals(1001L, result.getSelectedNode().getSourceRefs().get(0).getSourceContentId());
        assertEquals("红楼梦", result.getSelectedNode().getSourceRefs().get(0).getSourceTitle());
        assertEquals(301L, result.getRelations().get(0).getSourceNodeId());
        assertEquals(302L, result.getRelations().get(0).getTargetNodeId());
        assertEquals("PARENT_CHILD", result.getRelations().get(0).getRelationLabel());
        assertEquals(List.of("PERSON"), result.getAvailableFilters().getNodeTypes());
        assertEquals(List.of("PARENT_CHILD"), result.getAvailableFilters().getRelationTypes());
        assertEquals(
                List.of("CONFIRMED", "PENDING"), result.getAvailableFilters().getConfirmationStatuses());
        assertNull(result.getEmpty());
    }

    @Test
    void getCanvasShouldReturnFilterEmptyStateWhenNoNodeOrRelationMatches() {
        GraphVersionRepository versionRepository = mock(GraphVersionRepository.class);
        KnowledgeLineageNodeRepository nodeRepository = mock(KnowledgeLineageNodeRepository.class);
        KnowledgeLineageRelationRepository relationRepository = mock(KnowledgeLineageRelationRepository.class);
        KnowledgeLineageReadApplicationServiceImpl service =
                new KnowledgeLineageReadApplicationServiceImpl(versionRepository, nodeRepository, relationRepository);
        GraphVersion version = lineageVersion();
        when(versionRepository.page(GraphExtractionTaskType.LINEAGE, GraphVersionStatus.APPLIED, null, null, 1, 200))
                .thenReturn(PageResult.of(1, 200, 1, List.of(version)));
        when(versionRepository.getByVersionId(GraphVersionIdCodec.toDomain(71L)))
                .thenReturn(version);
        when(nodeRepository.listByVersionId(71L))
                .thenReturn(List.of(node(301L, "person:father", "贾代善", "MALE", "CONFIRMED")));
        when(relationRepository.listByVersionId(71L)).thenReturn(List.of());
        LineageCanvasQuery query = new LineageCanvasQuery();
        query.setVersionId(71L);
        query.setKeyword("不存在");

        LineageCanvasResult result = service.getCanvas(query);

        assertEquals("FILTER_NO_RESULT", result.getEmpty().getReason());
        assertEquals("重置筛选", result.getEmpty().getActionLabel());
        assertTrue(result.getNodes().isEmpty());
        assertTrue(result.getRelations().isEmpty());
    }

    private static GraphVersion lineageVersion() {
        return new GraphVersion(
                GraphVersionIdCodec.toDomain(71L),
                null,
                null,
                GraphExtractionTaskType.LINEAGE,
                null,
                null,
                "SANCAI_ENTRY",
                GraphExtractionSourceContentIdCodec.toDomain(1001L),
                "PEOPLE",
                "人物",
                3,
                GraphVersionStatus.APPLIED,
                Instant.ofEpochMilli(3_000L));
    }

    private static KnowledgeLineageNode node(
            Long nodeId, String nodeKey, String name, String gender, String confirmationStatus) {
        return new KnowledgeLineageNode(
                nodeId,
                nodeKey,
                name,
                "PERSON",
                1,
                gender,
                confirmationStatus,
                71L,
                """
                [{
                  "sourceContentType": "SANCAI_ENTRY",
                  "sourceContentId": 1001,
                  "sourceTitle": "红楼梦",
                  "snippet": "来源片段",
                  "href": "/knowledge/source/1001"
                }]
                """,
                new Date(1_000L),
                new Date(2_000L),
                new Date(3_000L));
    }

    private static KnowledgeLineageRelation relation() {
        return new KnowledgeLineageRelation(
                401L,
                "rel:father-son",
                "person:father",
                "person:son",
                "贾代善",
                "贾政",
                "PARENT_CHILD",
                "父子关系证据",
                "CONFIRMED",
                71L,
                """
                [{
                  "sourceContentType": "SANCAI_ENTRY",
                  "sourceContentId": 1001,
                  "sourceTitle": "红楼梦",
                  "snippet": "关系来源"
                }]
                """,
                new Date(1_000L),
                new Date(2_000L),
                new Date(3_000L));
    }
}
