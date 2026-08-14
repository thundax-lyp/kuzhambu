package com.thundax.kuzhambu.knowledge.interfaces.portal.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedGraphResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPortalApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.request.GraphPortalMaterialRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GraphPortalControllerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void shouldQueryPortalVisiblePublishedMaterialGraphWithoutAdminFields() throws Exception {
        GraphPortalApplicationService service = mock(GraphPortalApplicationService.class);
        ContentRef materialRef = new ContentRef("SANCAI_ENTRY", 1001L);
        when(service.getMaterialGraph(any()))
                .thenReturn(new GraphPublishedGraphResult(
                        materialRef, true, List.of(publishedNode()), List.of(publishedEdge())));
        GraphPortalController controller = new GraphPortalController(service);

        var response = controller.getMaterial(request());

        ArgumentCaptor<GraphMaterialQuery> captor = ArgumentCaptor.forClass(GraphMaterialQuery.class);
        verify(service).getMaterialGraph(captor.capture());
        assertThat(captor.getValue().materialRef()).isEqualTo(materialRef);
        assertThat(response.isVisible()).isTrue();
        assertThat(response.getContentRef().getContentRefId()).isEqualTo("1001");
        assertThat(response.getNodes()).hasSize(1);
        assertThat(response.getEdges()).hasSize(1);
        String json = OBJECT_MAPPER.writeValueAsString(response);
        assertThat(json).doesNotContain("\"source\"");
        assertThat(json).doesNotContain("mapping");
        assertThat(json).doesNotContain("operation");
    }

    @Test
    void shouldReturnInvisibleEmptyGraphForHiddenMaterialResult() {
        GraphPortalApplicationService service = mock(GraphPortalApplicationService.class);
        ContentRef materialRef = new ContentRef("SANCAI_ENTRY", 1001L);
        when(service.getMaterialGraph(any()))
                .thenReturn(new GraphPublishedGraphResult(materialRef, false, List.of(), List.of()));
        GraphPortalController controller = new GraphPortalController(service);

        var response = controller.getMaterial(request());

        assertThat(response.isVisible()).isFalse();
        assertThat(response.getContentRef().getContentType()).isEqualTo("SANCAI_ENTRY");
        assertThat(response.getNodes()).isEmpty();
        assertThat(response.getEdges()).isEmpty();
    }

    private static GraphPortalMaterialRequest request() {
        GraphPortalMaterialRequest request = new GraphPortalMaterialRequest();
        request.setContentType("SANCAI_ENTRY");
        request.setContentRefId("1001");
        return request;
    }

    private static GraphPublishedNode publishedNode() {
        GraphPublishedNode node = new GraphPublishedNode();
        node.setId(new GraphPublishedNodeId(11L));
        node.setNodeType(GraphNodeType.PERSON);
        node.setName("张三");
        node.setSource(GraphSourceType.MANUAL);
        node.setStatus(GraphPublishedStatus.ACTIVE);
        node.setLockVersion(3L);
        return node;
    }

    private static GraphPublishedEdge publishedEdge() {
        GraphPublishedEdge edge = new GraphPublishedEdge();
        edge.setId(new GraphPublishedEdgeId(21L));
        edge.setSourceNodeId(new GraphPublishedNodeId(11L));
        edge.setTargetNodeId(new GraphPublishedNodeId(12L));
        edge.setRelationType("师承");
        edge.setSource(GraphSourceType.MANUAL);
        edge.setQualifiersJson("{\"period\":\"明代\"}");
        edge.setStatus(GraphPublishedStatus.ACTIVE);
        edge.setLockVersion(5L);
        return edge;
    }
}
