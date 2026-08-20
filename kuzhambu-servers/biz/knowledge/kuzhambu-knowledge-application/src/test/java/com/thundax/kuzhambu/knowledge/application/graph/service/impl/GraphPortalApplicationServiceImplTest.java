package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialContentResolver;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphRecentEdgesResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphPortalApplicationServiceImplTest {

    private static final ContentRef VISIBLE_REF = new ContentRef("SANCAI_ENTRY", 1001L);
    private static final ContentRef HIDDEN_REF = new ContentRef("SANCAI_ENTRY", 1002L);

    private final GraphMaterialRepository materialRepository = mock(GraphMaterialRepository.class);
    private final GraphPublishedNodeRepository nodeRepository = mock(GraphPublishedNodeRepository.class);
    private final GraphPublishedEdgeRepository edgeRepository = mock(GraphPublishedEdgeRepository.class);
    private final GraphPublishedNodeMaterialRepository nodeMaterialRepository =
            mock(GraphPublishedNodeMaterialRepository.class);
    private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository =
            mock(GraphPublishedEdgeMaterialRepository.class);
    private final GraphMaterialContentResolver contentResolver = mock(GraphMaterialContentResolver.class);
    private final GraphWorkbenchApplicationService workbenchService = mock(GraphWorkbenchApplicationService.class);
    private final GraphPortalApplicationServiceImpl service = new GraphPortalApplicationServiceImpl(
            materialRepository,
            nodeRepository,
            edgeRepository,
            nodeMaterialRepository,
            edgeMaterialRepository,
            contentResolver,
            workbenchService);

    @Test
    void shouldAggregateOverviewOnlyFromPortalVisiblePublishedMaterials() {
        GraphPublishedNode source = node(1L);
        GraphPublishedNode target = node(2L);
        GraphPublishedNode isolated = node(3L);
        GraphPublishedEdge edge = edge(11L, 1L, 2L);
        when(materialRepository.listContentRefsByStatus(GraphMaterialStatus.PUBLISHED))
                .thenReturn(List.of(VISIBLE_REF, HIDDEN_REF));
        when(contentResolver.isPortalVisible(VISIBLE_REF)).thenReturn(true);
        when(contentResolver.isPortalVisible(HIDDEN_REF)).thenReturn(false);
        when(nodeMaterialRepository.listByMaterial(VISIBLE_REF))
                .thenReturn(List.of(
                        nodeMaterial(1L, VISIBLE_REF), nodeMaterial(2L, VISIBLE_REF), nodeMaterial(3L, VISIBLE_REF)));
        when(edgeMaterialRepository.listByMaterial(VISIBLE_REF)).thenReturn(List.of(edgeMaterial(11L, VISIBLE_REF)));
        when(nodeRepository.listByIds(List.of(
                        new GraphPublishedNodeId(1L), new GraphPublishedNodeId(2L), new GraphPublishedNodeId(3L))))
                .thenReturn(List.of(source, target, isolated));
        when(edgeRepository.getById(new GraphPublishedEdgeId(11L))).thenReturn(edge);

        var result = service.getOverview();

        assertThat(result.publishedNodeCount()).isEqualTo(3L);
        assertThat(result.publishedEdgeCount()).isEqualTo(1L);
        assertThat(result.coveredMaterialCount()).isEqualTo(1L);
        assertThat(result.isolatedNodeCount()).isEqualTo(1L);
    }

    @Test
    void shouldExcludeRecentEdgesWithoutPortalVisiblePublishedMaterial() {
        GraphPublishedNode visibleSource = node(1L);
        GraphPublishedNode visibleTarget = node(2L);
        GraphPublishedNode hiddenTarget = node(3L);
        GraphPublishedEdge visibleEdge = edge(11L, 1L, 2L);
        GraphPublishedEdge hiddenEdge = edge(12L, 1L, 3L);
        when(workbenchService.listRecentEdges())
                .thenReturn(new GraphRecentEdgesResult(
                        List.of(visibleSource, visibleTarget, hiddenTarget), List.of(visibleEdge, hiddenEdge)));
        when(nodeMaterialRepository.listByPublishedNodeId(new GraphPublishedNodeId(1L)))
                .thenReturn(List.of(nodeMaterial(1L, VISIBLE_REF)));
        when(nodeMaterialRepository.listByPublishedNodeId(new GraphPublishedNodeId(2L)))
                .thenReturn(List.of(nodeMaterial(2L, VISIBLE_REF)));
        when(nodeMaterialRepository.listByPublishedNodeId(new GraphPublishedNodeId(3L)))
                .thenReturn(List.of(nodeMaterial(3L, HIDDEN_REF)));
        when(edgeMaterialRepository.listByPublishedEdgeId(new GraphPublishedEdgeId(11L)))
                .thenReturn(List.of(edgeMaterial(11L, VISIBLE_REF)));
        when(materialRepository.getByContentRef(VISIBLE_REF)).thenReturn(material(VISIBLE_REF));
        when(materialRepository.getByContentRef(HIDDEN_REF)).thenReturn(material(HIDDEN_REF));
        when(contentResolver.isPortalVisible(VISIBLE_REF)).thenReturn(true);
        when(contentResolver.isPortalVisible(HIDDEN_REF)).thenReturn(false);

        GraphRecentEdgesResult result = service.listRecentEdges();

        assertThat(result.nodes()).containsExactly(visibleSource, visibleTarget);
        assertThat(result.edges()).containsExactly(visibleEdge);
    }

    private static GraphMaterial material(ContentRef ref) {
        return new GraphMaterial(
                1L, ref, "material", GraphMaterialStatus.PUBLISHED, Instant.EPOCH, null, null, null, 0L);
    }

    private static GraphPublishedNodeMaterial nodeMaterial(long nodeId, ContentRef ref) {
        return new GraphPublishedNodeMaterial(new GraphPublishedNodeId(nodeId), ref, "{}");
    }

    private static GraphPublishedEdgeMaterial edgeMaterial(long edgeId, ContentRef ref) {
        return new GraphPublishedEdgeMaterial(new GraphPublishedEdgeId(edgeId), ref, "{}");
    }

    private static GraphPublishedEdge edge(long id, long sourceNodeId, long targetNodeId) {
        return new GraphPublishedEdge(
                new GraphPublishedEdgeId(id),
                null,
                new GraphPublishedNodeId(sourceNodeId),
                new GraphPublishedNodeId(targetNodeId),
                "RELATED_TO",
                GraphSourceType.MANUAL,
                "{}",
                GraphPublishedStatus.ACTIVE,
                Instant.EPOCH,
                0L);
    }

    private static GraphPublishedNode node(long id) {
        return new GraphPublishedNode(
                new GraphPublishedNodeId(id),
                null,
                GraphNodeType.PERSON,
                "node-" + id,
                GraphSourceType.MANUAL,
                GraphPublishedStatus.ACTIVE,
                Instant.EPOCH,
                0L);
    }
}
