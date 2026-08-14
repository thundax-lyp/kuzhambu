package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphBatchPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphIncidentEdgesQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialListQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedEdgeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphQualityQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphSearchQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphWithdrawalPreviewQuery;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphMaterialRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphPublicationRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphPublishedRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphWorkbenchRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphMaterialResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphPublishedResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphWorkbenchResponses;

/** 管理端图谱 HTTP 协议转换。 */
public final class GraphInterfaceAssembler {
    private GraphInterfaceAssembler() {}

    public static ContentRef toContentRef(GraphMaterialRequests.ContentRefRequest request) {
        return new ContentRef(request.getContentType(), Long.valueOf(request.getContentRefId()));
    }

    public static GraphMaterialQuery toQuery(GraphMaterialRequests.ContentRefRequest request) {
        return new GraphMaterialQuery(toContentRef(request));
    }

    public static GraphMaterialListQuery toQuery(GraphMaterialRequests.MaterialPageRequest request) {
        return new GraphMaterialListQuery(
                request.getKeyword(),
                request.getStatus() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus.valueOf(
                                request.getStatus()));
    }

    public static GraphMaterialResponses.ContentRefData toContentRefData(ContentRef value) {
        return value == null
                ? null
                : new GraphMaterialResponses.ContentRefData(
                        value.getContentType(), String.valueOf(value.getContentId()));
    }

    public static GraphMaterialResponses.MaterialData toMaterialData(
            com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial value) {
        return new GraphMaterialResponses.MaterialData(
                value.getContentRef() == null
                        ? null
                        : String.valueOf(value.getContentRef().getContentId()),
                toContentRefData(value.getContentRef()),
                value.getContentTitleSnapshot(),
                value.getStatus().name(),
                String.valueOf(value.getLockVersion()),
                value.getPublishedAt() == null
                        ? null
                        : String.valueOf(value.getPublishedAt().toEpochMilli()),
                null,
                null);
    }

    public static GraphPublicationPreviewQuery toQuery(GraphPublicationRequests.PublicationPreviewRequest request) {
        return new GraphPublicationPreviewQuery(toContentRef(request));
    }

    public static GraphBatchPublicationPreviewQuery toQuery(
            GraphPublicationRequests.BatchPublicationPreviewRequest request) {
        return new GraphBatchPublicationPreviewQuery(request.getContentRefs().stream()
                .map(GraphInterfaceAssembler::toContentRef)
                .toList());
    }

    public static GraphWithdrawalPreviewQuery toQuery(GraphPublicationRequests.WithdrawalRequest request) {
        return new GraphWithdrawalPreviewQuery(toContentRef(request));
    }

    public static GraphPublicationCommand toCommand(GraphPublicationRequests.PublicationConfirmRequest request) {
        return new GraphPublicationCommand(toContentRef(request), Long.valueOf(request.getMaterialLockVersion()), null);
    }

    public static GraphBatchPublicationCommand toCommand(
            GraphPublicationRequests.BatchPublicationConfirmRequest request) {
        return new GraphBatchPublicationCommand(request.getMaterials().stream()
                .map(GraphInterfaceAssembler::toCommand)
                .toList());
    }

    public static GraphWithdrawalCommand toCommand(GraphPublicationRequests.WithdrawalRequest request) {
        return new GraphWithdrawalCommand(toContentRef(request), Long.valueOf(request.getMaterialLockVersion()));
    }

    public static GraphPublishedNodeQuery toQuery(GraphPublishedRequests.PublishedNodePageRequest request) {
        return new GraphPublishedNodeQuery(
                request.getKeyword(),
                request.getNodeType() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType.valueOf(
                                request.getNodeType()),
                request.getStatus() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus.valueOf(
                                request.getStatus()),
                request.getSource() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType.valueOf(
                                request.getSource()));
    }

    public static GraphPublishedEdgeQuery toQuery(GraphPublishedRequests.PublishedEdgePageRequest request) {
        return new GraphPublishedEdgeQuery(
                request.getKeyword(),
                request.getRelationType(),
                request.getStatus() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus.valueOf(
                                request.getStatus()),
                request.getSource() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType.valueOf(
                                request.getSource()));
    }

    public static com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId toNodeId(
            String value) {
        return GraphPublishedNodeIdCodec.toDomain(Long.valueOf(value));
    }

    public static com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId toEdgeId(
            String value) {
        return GraphPublishedEdgeIdCodec.toDomain(Long.valueOf(value));
    }

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
