package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler;

import com.thundax.kuzhambu.knowledge.application.graph.query.GraphIncidentEdgesQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphQualityQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphSearchQuery;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphWorkbenchRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphPublishedResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphWorkbenchResponses;

/** 管理端图谱 HTTP 协议转换。 */
public final class GraphInterfaceAssembler {
    private GraphInterfaceAssembler() {}

    public static GraphIncidentEdgesQuery toQuery(GraphWorkbenchRequests.IncidentEdgesListRequest request) {
        return new GraphIncidentEdgesQuery(
                request.getNodeIds().stream()
                        .map(Long::valueOf)
                        .map(GraphPublishedNodeIdCodec::toDomain)
                        .toList(),
                GraphPublishedEdgeIdCodec.toDomain(
                        request.getAfterEdgeId() == null ? null : Long.valueOf(request.getAfterEdgeId())));
    }

    public static GraphSearchQuery toQuery(GraphWorkbenchRequests.SearchPageRequest request) {
        return new GraphSearchQuery(request.getKeyword(), null, request.getRelationType());
    }

    public static GraphQualityQuery toQuery(GraphWorkbenchRequests.QualityGetRequest request) {
        return new GraphQualityQuery(request.getIssueType(), null);
    }

    public static GraphPublishedResponses.NodeData toNodeData(GraphPublishedNode value) {
        return new GraphPublishedResponses.NodeData(
                String.valueOf(value.getId().value()),
                value.getNodeType().name(),
                value.getName(),
                value.getSource().name(),
                value.getStatus().name(),
                String.valueOf(value.getLockVersion()));
    }

    public static GraphPublishedResponses.EdgeData toEdgeData(GraphPublishedEdge value) {
        return new GraphPublishedResponses.EdgeData(
                String.valueOf(value.getId().value()),
                String.valueOf(value.getSourceNodeId().value()),
                String.valueOf(value.getTargetNodeId().value()),
                value.getRelationType(),
                null,
                value.getSource().name(),
                value.getStatus().name(),
                String.valueOf(value.getLockVersion()));
    }

    public static GraphWorkbenchResponses.SearchData toSearchData(
            com.thundax.kuzhambu.knowledge.application.graph.result.GraphSearchResult value) {
        return new GraphWorkbenchResponses.SearchData(
                value.objectType(),
                value.node() == null ? null : toNodeData(value.node()),
                value.edge() == null ? null : toEdgeData(value.edge()));
    }
}
