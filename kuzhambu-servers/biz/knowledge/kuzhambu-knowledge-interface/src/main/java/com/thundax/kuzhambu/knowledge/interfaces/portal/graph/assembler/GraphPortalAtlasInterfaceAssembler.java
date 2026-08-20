package com.thundax.kuzhambu.knowledge.interfaces.portal.graph.assembler;

import com.thundax.kuzhambu.knowledge.application.graph.query.GraphOneHopEdgesQuery;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.request.GraphPortalAtlasRequests;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.response.GraphPortalAtlasResponses;
import org.springframework.lang.NonNull;

public final class GraphPortalAtlasInterfaceAssembler {
    private GraphPortalAtlasInterfaceAssembler() {}

    @NonNull
    public static GraphOneHopEdgesQuery toQuery(@NonNull GraphPortalAtlasRequests.OneHopEdgesListRequest request) {
        return new GraphOneHopEdgesQuery(
                request.getNodeIds().stream()
                        .map(Long::valueOf)
                        .map(GraphPublishedNodeIdCodec::toDomain)
                        .toList(),
                GraphPublishedEdgeIdCodec.toDomain(
                        request.getAfterEdgeId() == null ? null : Long.valueOf(request.getAfterEdgeId())));
    }

    @NonNull
    public static GraphPortalAtlasResponses.NodeData toNodeData(@NonNull GraphPublishedNode value) {
        return new GraphPortalAtlasResponses.NodeData(
                String.valueOf(GraphPublishedNodeIdCodec.toValue(value.getId())),
                value.getNodeType().name(),
                value.getName());
    }

    @NonNull
    public static GraphPortalAtlasResponses.EdgeData toEdgeData(@NonNull GraphPublishedEdge value) {
        return new GraphPortalAtlasResponses.EdgeData(
                String.valueOf(GraphPublishedEdgeIdCodec.toValue(value.getId())),
                String.valueOf(GraphPublishedNodeIdCodec.toValue(value.getSourceNodeId())),
                String.valueOf(GraphPublishedNodeIdCodec.toValue(value.getTargetNodeId())),
                value.getRelationType());
    }
}
