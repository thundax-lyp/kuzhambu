package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.web.exception.ApiException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialApplyMode;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEdgeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEdgeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialImportCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeMergeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeSplitCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedEdgeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedEdgeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeMergeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeSplitCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphBatchPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphIncidentEdgesQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialImportQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialListQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialNodeMergeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialNodeSplitQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedEdgeDeleteQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedEdgeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeDeleteQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeMergeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeSplitQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphQualityQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphSearchQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphWithdrawalPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphGovernanceImpactResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphGovernanceOperationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialChangeImpactResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialImportPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedEdgeDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedNodeDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphValidationIssueResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWithdrawalPreviewResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgePropertyIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodePropertyIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphMaterialRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphPublicationRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphPublishedRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphWorkbenchRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphMaterialResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphPublicationResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphPublishedResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphWorkbenchResponses;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.lang.NonNull;

/** 管理端图谱 HTTP 协议转换。 */
public final class GraphInterfaceAssembler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GraphInterfaceAssembler() {}

    @NonNull
    public static ContentRef toContentRef(@NonNull GraphMaterialRequests.ContentRefRequest request) {
        Objects.requireNonNull(request, "request");
        return new ContentRef(request.getContentType(), Long.valueOf(request.getContentRefId()));
    }

    @NonNull
    public static GraphMaterialQuery toQuery(@NonNull GraphMaterialRequests.ContentRefRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialQuery(toContentRef(request));
    }

    @NonNull
    public static GraphMaterialListQuery toQuery(@NonNull GraphMaterialRequests.MaterialPageRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialListQuery(
                request.getKeyword(),
                request.getStatus() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus.valueOf(
                                request.getStatus()));
    }

    @NonNull
    public static GraphMaterialResponses.ContentRefData toContentRefData(@NonNull ContentRef value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.ContentRefData(value.getContentType(), String.valueOf(value.getContentId()));
    }

    private static GraphMaterialResponses.ContentRefData toNullableContentRefData(ContentRef value) {
        return value == null ? null : toContentRefData(value);
    }

    @NonNull
    public static GraphMaterialResponses.MaterialData toMaterialData(
            @NonNull com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.MaterialData(
                value.getContentRef() == null
                        ? null
                        : String.valueOf(value.getContentRef().getContentId()),
                toNullableContentRefData(value.getContentRef()),
                value.getContentTitleSnapshot(),
                value.getStatus().name(),
                String.valueOf(value.getLockVersion()),
                value.getPublishedAt() == null
                        ? null
                        : String.valueOf(value.getPublishedAt().toEpochMilli()),
                value.getStatus() == com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus.FAILED
                        ? value.getFailureReason()
                        : null,
                value.getStatus() == com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus.FAILED
                        ? value.getFailedOperation()
                        : null);
    }

    @NonNull
    public static GraphMaterialResponses.NodeData toMaterialNodeData(@NonNull GraphMaterialNode value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.NodeData(
                String.valueOf(value.getId().value()),
                value.getNodeType().name(),
                value.getName(),
                fromJsonObject(value.getPropertiesJson()),
                value.getSource().name());
    }

    @NonNull
    public static GraphMaterialResponses.EdgeData toMaterialEdgeData(@NonNull GraphMaterialEdge value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.EdgeData(
                String.valueOf(value.getId().value()),
                String.valueOf(value.getSourceNodeId().value()),
                String.valueOf(value.getTargetNodeId().value()),
                value.getRelationType(),
                fromJsonObject(value.getQualifiersJson()),
                value.getSource().name());
    }

    @NonNull
    public static GraphMaterialResponses.DetailData toDetailData(
            @NonNull GraphMaterialResult value, @NonNull List<GraphMaterialResponses.TaskData> extractionTasks) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(extractionTasks, "extractionTasks");
        return new GraphMaterialResponses.DetailData(
                toMaterialData(value.material()),
                value.nodes().stream()
                        .map(GraphInterfaceAssembler::toMaterialNodeData)
                        .toList(),
                value.edges().stream()
                        .map(GraphInterfaceAssembler::toMaterialEdgeData)
                        .toList(),
                extractionTasks);
    }

    @NonNull
    public static GraphMaterialResponses.GraphData toGraphData(@NonNull GraphMaterialResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.GraphData(
                toMaterialData(value.material()),
                value.nodes().stream()
                        .map(GraphInterfaceAssembler::toMaterialNodeData)
                        .toList(),
                value.edges().stream()
                        .map(GraphInterfaceAssembler::toMaterialEdgeData)
                        .toList());
    }

    @NonNull
    public static GraphMaterialResponses.ChangeImpactData toChangeImpactData(
            @NonNull GraphMaterialChangeImpactResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.ChangeImpactData(
                value.nodes().stream()
                        .map(GraphInterfaceAssembler::toMaterialNodeData)
                        .toList(),
                value.edges().stream()
                        .map(GraphInterfaceAssembler::toMaterialEdgeData)
                        .toList(),
                value.issues().stream()
                        .map(GraphInterfaceAssembler::toIssueData)
                        .toList(),
                value.executable());
    }

    @NonNull
    public static GraphMaterialResponses.ImportPreviewData toImportPreviewData(
            @NonNull GraphMaterialImportPreviewResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.ImportPreviewData(
                toGraphData(value.importedGraph()),
                value.createdNodeCount(),
                value.updatedNodeCount(),
                value.createdEdgeCount(),
                value.updatedEdgeCount(),
                value.issues().stream()
                        .map(GraphInterfaceAssembler::toIssueData)
                        .toList(),
                value.importable());
    }

    @NonNull
    public static GraphMaterialResponses.IssueData toIssueData(@NonNull GraphValidationIssueResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.IssueData(
                value.code(), value.severity(), value.objectType(), value.objectId(), value.field(), value.message());
    }

    @NonNull
    public static GraphMaterialNodeCommand toCommand(@NonNull GraphMaterialRequests.MaterialObjectRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeCommand(toMaterialNode(request), Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialEdgeCommand toCommand(@NonNull GraphMaterialRequests.MaterialEdgeRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialEdgeCommand(toMaterialEdge(request), Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialNodeDeleteCommand toCommand(
            @NonNull GraphMaterialRequests.MaterialObjectDeleteRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeDeleteCommand(
                toContentRef(request),
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getNodeId())),
                Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialEdgeDeleteCommand toCommand(
            @NonNull GraphMaterialRequests.MaterialEdgeDeleteRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialEdgeDeleteCommand(
                toContentRef(request),
                GraphMaterialEdgeIdCodec.toDomain(Long.valueOf(request.getEdgeId())),
                Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialNodeMergeQuery toQuery(
            @NonNull GraphMaterialRequests.MaterialNodeMergePreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeMergeQuery(
                toContentRef(request),
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getRetainedNodeId())),
                request.getMergedNodeIds().stream()
                        .map(Long::valueOf)
                        .map(GraphMaterialNodeIdCodec::toDomain)
                        .toList());
    }

    @NonNull
    public static GraphMaterialNodeMergeCommand toCommand(
            @NonNull GraphMaterialRequests.MaterialNodeMergeApplyRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeMergeCommand(
                toContentRef(request),
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getRetainedNodeId())),
                request.getMergedNodeIds().stream()
                        .map(Long::valueOf)
                        .map(GraphMaterialNodeIdCodec::toDomain)
                        .toList(),
                Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialNodeSplitQuery toQuery(
            @NonNull GraphMaterialRequests.MaterialNodeSplitPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeSplitQuery(
                toContentRef(request), GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getSourceNodeId())));
    }

    @NonNull
    public static GraphMaterialNodeSplitCommand toCommand(
            @NonNull GraphMaterialRequests.MaterialNodeSplitApplyRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeSplitCommand(
                toContentRef(request),
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getSourceNodeId())),
                toMaterialNode(toContentRef(request), request.getSplitNode()),
                request.getReassignedEdgeIds() == null
                        ? List.of()
                        : request.getReassignedEdgeIds().stream()
                                .map(Long::valueOf)
                                .map(GraphMaterialEdgeIdCodec::toDomain)
                                .toList(),
                Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialImportQuery toQuery(
            @NonNull GraphMaterialRequests.MaterialImportPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialImportQuery(toContentRef(request), request.getGraphJson());
    }

    @NonNull
    public static GraphMaterialImportCommand toCommand(
            @NonNull GraphMaterialRequests.MaterialImportApplyRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialImportCommand(
                toContentRef(request),
                request.getGraphJson(),
                GraphMaterialApplyMode.valueOf(request.getApplyMode()),
                Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphPublicationPreviewQuery toQuery(
            @NonNull GraphPublicationRequests.PublicationPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublicationPreviewQuery(toContentRef(request));
    }

    @NonNull
    public static GraphBatchPublicationPreviewQuery toQuery(
            @NonNull GraphPublicationRequests.BatchPublicationPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphBatchPublicationPreviewQuery(request.getContentRefs().stream()
                .map(GraphInterfaceAssembler::toContentRef)
                .toList());
    }

    @NonNull
    public static GraphWithdrawalPreviewQuery toQuery(@NonNull GraphPublicationRequests.WithdrawalRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphWithdrawalPreviewQuery(toContentRef(request));
    }

    @NonNull
    public static GraphPublicationCommand toCommand(
            @NonNull GraphPublicationRequests.PublicationConfirmRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublicationCommand(toContentRef(request), Long.valueOf(request.getMaterialLockVersion()), null);
    }

    @NonNull
    public static GraphBatchPublicationCommand toCommand(
            @NonNull GraphPublicationRequests.BatchPublicationConfirmRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphBatchPublicationCommand(request.getMaterials().stream()
                .map(GraphInterfaceAssembler::toCommand)
                .toList());
    }

    @NonNull
    public static GraphWithdrawalCommand toCommand(@NonNull GraphPublicationRequests.WithdrawalRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphWithdrawalCommand(toContentRef(request), Long.valueOf(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphPublicationResponses.PreviewData toPreviewData(@NonNull GraphPublicationPreviewResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublicationResponses.PreviewData(
                null,
                toNullableContentRefData(value.materialRef()),
                String.valueOf(value.materialLockVersion()),
                value.nodes().stream()
                        .map(node -> new GraphPublicationResponses.MatchData(
                                String.valueOf(node.materialNode().getId().value()),
                                node.matchedNode() == null ? "CREATE" : "REUSE",
                                node.matchedNode() == null
                                        ? null
                                        : String.valueOf(
                                                node.matchedNode().getId().value()),
                                node.matchedNode() == null
                                        ? null
                                        : String.valueOf(node.matchedNode().getLockVersion()),
                                node.issues().stream()
                                        .map(GraphInterfaceAssembler::toIssueData)
                                        .toList()))
                        .toList(),
                value.edges().stream()
                        .map(edge -> new GraphPublicationResponses.MatchData(
                                String.valueOf(edge.materialEdge().getId().value()),
                                edge.matchedEdge() == null ? "CREATE" : "REUSE",
                                edge.matchedEdge() == null
                                        ? null
                                        : String.valueOf(
                                                edge.matchedEdge().getId().value()),
                                edge.matchedEdge() == null
                                        ? null
                                        : String.valueOf(edge.matchedEdge().getLockVersion()),
                                edge.issues().stream()
                                        .map(GraphInterfaceAssembler::toIssueData)
                                        .toList()))
                        .toList(),
                value.issues().stream()
                        .map(GraphInterfaceAssembler::toIssueData)
                        .toList(),
                value.publishable());
    }

    @NonNull
    public static GraphPublicationResponses.PublicationData toPublicationData(@NonNull GraphPublicationResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublicationResponses.PublicationData(
                toNullableContentRefData(value.materialRef()),
                value.materialStatus().name(),
                value.success(),
                value.failureMessage(),
                value.createdNodeCount(),
                value.reusedNodeCount(),
                value.createdEdgeCount(),
                value.reusedEdgeCount(),
                value.issues().stream()
                        .map(GraphInterfaceAssembler::toIssueData)
                        .toList());
    }

    @NonNull
    public static GraphPublicationResponses.BatchPreviewData toBatchPreviewData(
            @NonNull List<GraphPublicationPreviewResult> values) {
        Objects.requireNonNull(values, "values");
        return new GraphPublicationResponses.BatchPreviewData(
                values.stream().map(GraphInterfaceAssembler::toPreviewData).toList());
    }

    @NonNull
    public static GraphPublicationResponses.BatchData toBatchData(@NonNull List<GraphPublicationResult> values) {
        Objects.requireNonNull(values, "values");
        return new GraphPublicationResponses.BatchData(values.stream()
                .map(value -> new GraphPublicationResponses.BatchItemData(
                        toNullableContentRefData(value.materialRef()),
                        value.success(),
                        value.success() ? toPublicationData(value) : null,
                        value.success() ? null : "GRAPH_PUBLICATION_FAILED",
                        value.failureMessage()))
                .toList());
    }

    @NonNull
    public static GraphPublicationResponses.WithdrawalPreviewData toWithdrawalPreviewData(
            @NonNull GraphWithdrawalPreviewResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublicationResponses.WithdrawalPreviewData(
                toNullableContentRefData(value.materialRef()),
                value.nodeMappingCount(),
                value.edgeMappingCount(),
                value.governedNodes().stream()
                        .map(GraphInterfaceAssembler::toNodeData)
                        .toList(),
                value.governedEdges().stream()
                        .map(GraphInterfaceAssembler::toEdgeData)
                        .toList());
    }

    @NonNull
    public static GraphPublishedNodeQuery toQuery(@NonNull GraphPublishedRequests.PublishedNodePageRequest request) {
        Objects.requireNonNull(request, "request");
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

    @NonNull
    public static GraphPublishedEdgeQuery toQuery(@NonNull GraphPublishedRequests.PublishedEdgePageRequest request) {
        Objects.requireNonNull(request, "request");
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

    @NonNull
    public static com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId toNodeId(
            @NonNull String value) {
        Objects.requireNonNull(value, "value");
        return GraphPublishedNodeIdCodec.toDomain(Long.valueOf(value));
    }

    @NonNull
    public static com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId toEdgeId(
            @NonNull String value) {
        Objects.requireNonNull(value, "value");
        return GraphPublishedEdgeIdCodec.toDomain(Long.valueOf(value));
    }

    @NonNull
    public static GraphIncidentEdgesQuery toQuery(@NonNull GraphWorkbenchRequests.IncidentEdgesListRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphIncidentEdgesQuery(
                request.getNodeIds().stream()
                        .map(Long::valueOf)
                        .map(GraphPublishedNodeIdCodec::toDomain)
                        .toList(),
                GraphPublishedEdgeIdCodec.toDomain(
                        request.getAfterEdgeId() == null ? null : Long.valueOf(request.getAfterEdgeId())));
    }

    @NonNull
    public static GraphSearchQuery toQuery(@NonNull GraphWorkbenchRequests.SearchPageRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphSearchQuery(request.getKeyword(), null, request.getRelationType());
    }

    @NonNull
    public static GraphQualityQuery toQuery(@NonNull GraphWorkbenchRequests.QualityGetRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphQualityQuery(request.getIssueType(), null);
    }

    @NonNull
    public static GraphPublishedResponses.NodeData toNodeData(@NonNull GraphPublishedNode value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.NodeData(
                String.valueOf(value.getId().value()),
                value.getNodeType().name(),
                value.getName(),
                value.getSource().name(),
                value.getStatus().name(),
                String.valueOf(value.getLockVersion()));
    }

    @NonNull
    public static GraphPublishedResponses.EdgeData toEdgeData(@NonNull GraphPublishedEdge value) {
        Objects.requireNonNull(value, "value");
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

    @NonNull
    public static GraphPublishedResponses.PropertyData toPropertyData(@NonNull GraphPublishedNodeProperty value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.PropertyData(
                String.valueOf(value.getId().value()),
                value.getPropertyKey(),
                value.getValue(),
                value.isPreferred(),
                null,
                null);
    }

    @NonNull
    public static GraphPublishedResponses.PropertyData toPropertyData(@NonNull GraphPublishedEdgeProperty value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.PropertyData(
                String.valueOf(value.getId().value()),
                value.getPropertyKey(),
                value.getValue(),
                value.isPreferred(),
                null,
                null);
    }

    @NonNull
    public static GraphPublishedResponses.NodeDetailData toNodeDetailData(
            @NonNull GraphPublishedNodeDetailResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.NodeDetailData(
                toNodeData(value.node()),
                value.properties().stream()
                        .map(GraphInterfaceAssembler::toPropertyData)
                        .toList(),
                value.materials().stream()
                        .map(material -> new GraphPublishedResponses.MappingData(
                                null,
                                "NODE",
                                null,
                                toNullableContentRefData(material.getMaterialRef()),
                                String.valueOf(material.getPublishedNodeId().value()),
                                fromJsonObject(material.getSourceSnapshotJson())))
                        .toList(),
                value.incidentEdges().stream()
                        .map(GraphInterfaceAssembler::toEdgeData)
                        .toList(),
                value.operations().stream()
                        .map(GraphInterfaceAssembler::toOperationData)
                        .toList());
    }

    @NonNull
    public static GraphPublishedResponses.EdgeDetailData toEdgeDetailData(
            @NonNull GraphPublishedEdgeDetailResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.EdgeDetailData(
                toEdgeData(value.edge()),
                toNodeData(value.sourceNode()),
                toNodeData(value.targetNode()),
                value.properties().stream()
                        .map(GraphInterfaceAssembler::toPropertyData)
                        .toList(),
                value.materials().stream()
                        .map(material -> new GraphPublishedResponses.MappingData(
                                null,
                                "EDGE",
                                null,
                                toNullableContentRefData(material.getMaterialRef()),
                                String.valueOf(material.getPublishedEdgeId().value()),
                                fromJsonObject(material.getSourceSnapshotJson())))
                        .toList(),
                value.operations().stream()
                        .map(GraphInterfaceAssembler::toOperationData)
                        .toList());
    }

    @NonNull
    public static GraphPublishedResponses.OperationData toOperationData(@NonNull GraphGovernanceOperationResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.OperationData(
                value.id() == null ? null : String.valueOf(value.id()),
                value.operationType(),
                value.targetType(),
                value.targetId() == null ? null : String.valueOf(value.targetId()),
                value.reason(),
                value.auditLogId() == null ? null : String.valueOf(value.auditLogId()),
                value.operatorId(),
                value.operatorName(),
                value.occurredAt() == null
                        ? null
                        : String.valueOf(value.occurredAt().toEpochMilli()),
                value.beforeSummary(),
                value.afterSummary());
    }

    @NonNull
    public static GraphPublishedResponses.GovernanceImpactData toGovernanceImpactData(
            @NonNull GraphGovernanceImpactResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.GovernanceImpactData(
                null,
                value.nodes().stream().map(GraphInterfaceAssembler::toNodeData).toList(),
                value.edges().stream().map(GraphInterfaceAssembler::toEdgeData).toList(),
                value.nodeMaterials().stream()
                        .map(material -> new GraphPublishedResponses.MappingData(
                                null,
                                "NODE",
                                null,
                                toNullableContentRefData(material.getMaterialRef()),
                                String.valueOf(material.getPublishedNodeId().value()),
                                fromJsonObject(material.getSourceSnapshotJson())))
                        .toList(),
                value.edgeMaterials().stream()
                        .map(material -> new GraphPublishedResponses.MappingData(
                                null,
                                "EDGE",
                                null,
                                toNullableContentRefData(material.getMaterialRef()),
                                String.valueOf(material.getPublishedEdgeId().value()),
                                fromJsonObject(material.getSourceSnapshotJson())))
                        .toList(),
                value.issues().stream()
                        .map(GraphInterfaceAssembler::toIssueData)
                        .toList(),
                value.executable());
    }

    @NonNull
    public static GraphPublishedNodeCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedNodeSaveRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeCommand(
                toPublishedNode(request.getNode(), request.getLockVersion()),
                toNodeProperties(request),
                request.getReason());
    }

    @NonNull
    public static GraphPublishedEdgeCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedEdgeSaveRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedEdgeCommand(
                toPublishedEdge(request.getEdge(), request.getLockVersion()),
                toEdgeProperties(request),
                request.getReason());
    }

    @NonNull
    public static GraphPublishedNodeDeleteQuery toQuery(
            @NonNull GraphPublishedRequests.PublishedNodeDeletePreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeDeleteQuery(toNodeId(request.getNodeId()), request.getCascadeEdges());
    }

    @NonNull
    public static GraphPublishedNodeDeleteCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedNodeDeleteRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeDeleteCommand(
                toNodeId(request.getNodeId()),
                request.getCascadeEdges(),
                Long.parseLong(request.getLockVersion()),
                request.getReason());
    }

    @NonNull
    public static GraphPublishedEdgeDeleteQuery toQuery(
            @NonNull GraphPublishedRequests.PublishedEdgeIdRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedEdgeDeleteQuery(toEdgeId(request.getEdgeId()));
    }

    @NonNull
    public static GraphPublishedEdgeDeleteCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedEdgeDeleteRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedEdgeDeleteCommand(
                toEdgeId(request.getEdgeId()), Long.parseLong(request.getLockVersion()), request.getReason());
    }

    @NonNull
    public static GraphPublishedNodeMergeQuery toQuery(
            @NonNull GraphPublishedRequests.PublishedNodeMergePreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeMergeQuery(
                toNodeId(request.getRetainedNodeId()),
                request.getMergedNodeIds().stream()
                        .map(GraphInterfaceAssembler::toNodeId)
                        .toList());
    }

    @NonNull
    public static GraphPublishedNodeMergeCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedNodeMergeRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeMergeCommand(
                toNodeId(request.getRetainedNodeId()),
                request.getMergedNodeIds().stream()
                        .map(GraphInterfaceAssembler::toNodeId)
                        .toList(),
                Long.parseLong(request.getRetainedNodeLockVersion()),
                request.getReason());
    }

    @NonNull
    public static GraphPublishedNodeSplitQuery toQuery(
            @NonNull GraphPublishedRequests.PublishedNodeSplitPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeSplitQuery(toNodeId(request.getNodeId()));
    }

    @NonNull
    public static GraphPublishedNodeSplitCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedNodeSplitRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeSplitCommand(
                toNodeId(request.getNodeId()),
                toPublishedNode(request.getSplitNode(), request.getSourceNodeLockVersion()),
                stringIdsToNodePropertyIds(request.getMovedPropertyIds()),
                stringIdsToNodePropertyIds(request.getCopiedPropertyIds()),
                stringIdsToEdgeIds(request.getReassignedEdgeIds()),
                request.getCopiedEdges() == null
                        ? List.of()
                        : request.getCopiedEdges().stream()
                                .map(edge -> toPublishedEdge(edge, null))
                                .toList(),
                request.getMovedMaterialRefs() == null
                        ? List.of()
                        : request.getMovedMaterialRefs().stream()
                                .map(GraphInterfaceAssembler::toContentRef)
                                .toList(),
                request.getCopiedMaterialRefs() == null
                        ? List.of()
                        : request.getCopiedMaterialRefs().stream()
                                .map(GraphInterfaceAssembler::toContentRef)
                                .toList(),
                Long.parseLong(request.getSourceNodeLockVersion()),
                request.getReason());
    }

    @NonNull
    public static GraphWorkbenchResponses.SearchData toSearchData(
            @NonNull com.thundax.kuzhambu.knowledge.application.graph.result.GraphSearchResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphWorkbenchResponses.SearchData(
                value.objectType(),
                value.node() == null ? null : toNodeData(value.node()),
                value.edge() == null ? null : toEdgeData(value.edge()));
    }

    private static GraphMaterialNode toMaterialNode(@NonNull GraphMaterialRequests.MaterialObjectRequest request) {
        return toMaterialNode(toContentRef(request), request.getNode());
    }

    private static GraphMaterialNode toMaterialNode(
            ContentRef materialRef, GraphMaterialRequests.MaterialObjectRequestData request) {
        var node = new GraphMaterialNode();
        node.setId(GraphMaterialNodeIdCodec.toDomain(request.getId() == null ? null : Long.valueOf(request.getId())));
        node.setMaterialRef(materialRef);
        node.setNodeType(GraphNodeType.valueOf(request.getNodeType()));
        node.setName(request.getName());
        node.setSource(GraphSourceType.valueOf(request.getSource()));
        node.setPropertiesJson(toJson(request.getProperties()));
        return node;
    }

    private static GraphMaterialEdge toMaterialEdge(@NonNull GraphMaterialRequests.MaterialEdgeRequest request) {
        var edge = new GraphMaterialEdge();
        edge.setId(GraphMaterialEdgeIdCodec.toDomain(
                request.getEdge().getId() == null
                        ? null
                        : Long.valueOf(request.getEdge().getId())));
        edge.setMaterialRef(toContentRef(request));
        edge.setSourceNodeId(
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getEdge().getSourceNodeId())));
        edge.setTargetNodeId(
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getEdge().getTargetNodeId())));
        edge.setRelationType(request.getEdge().getRelationType());
        edge.setSource(GraphSourceType.valueOf(request.getEdge().getSource()));
        edge.setQualifiersJson(toJson(request.getEdge().getQualifiers()));
        return edge;
    }

    private static GraphPublishedNode toPublishedNode(
            GraphPublishedRequests.PublishedNodeRequestData request, String lockVersion) {
        var node = new GraphPublishedNode();
        node.setId(GraphPublishedNodeIdCodec.toDomain(request.getId() == null ? null : Long.valueOf(request.getId())));
        node.setNodeType(GraphNodeType.valueOf(request.getNodeType()));
        node.setName(request.getName());
        node.setSource(GraphSourceType.valueOf(request.getSource()));
        node.setStatus(GraphPublishedStatus.valueOf(request.getStatus()));
        node.setLockVersion(lockVersion == null ? 0L : Long.parseLong(lockVersion));
        return node;
    }

    private static GraphPublishedEdge toPublishedEdge(
            GraphPublishedRequests.PublishedEdgeRequestData request, String lockVersion) {
        var edge = new GraphPublishedEdge();
        edge.setId(GraphPublishedEdgeIdCodec.toDomain(request.getId() == null ? null : Long.valueOf(request.getId())));
        edge.setSourceNodeId(GraphPublishedNodeIdCodec.toDomain(Long.valueOf(request.getSourceNodeId())));
        edge.setTargetNodeId(GraphPublishedNodeIdCodec.toDomain(Long.valueOf(request.getTargetNodeId())));
        edge.setRelationType(request.getRelationType());
        edge.setSource(GraphSourceType.valueOf(request.getSource()));
        edge.setQualifiersJson(toJson(request.getQualifiers()));
        edge.setStatus(GraphPublishedStatus.valueOf(request.getStatus()));
        edge.setLockVersion(lockVersion == null ? 0L : Long.parseLong(lockVersion));
        return edge;
    }

    private static List<GraphPublishedNodeProperty> toNodeProperties(
            @NonNull GraphPublishedRequests.PublishedNodeSaveRequest request) {
        var nodeId = request.getNode().getId() == null
                ? null
                : GraphPublishedNodeIdCodec.toDomain(
                        Long.valueOf(request.getNode().getId()));
        return request.getProperties().stream()
                .map(property -> new GraphPublishedNodeProperty(
                        GraphPublishedNodePropertyIdCodec.toDomain(
                                property.getId() == null ? null : Long.valueOf(property.getId())),
                        nodeId,
                        property.getPropertyName(),
                        String.valueOf(property.getValue()),
                        property.getPreferred()))
                .toList();
    }

    private static List<GraphPublishedEdgeProperty> toEdgeProperties(
            @NonNull GraphPublishedRequests.PublishedEdgeSaveRequest request) {
        var edgeId = request.getEdge().getId() == null
                ? null
                : GraphPublishedEdgeIdCodec.toDomain(
                        Long.valueOf(request.getEdge().getId()));
        return request.getProperties().stream()
                .map(property -> new GraphPublishedEdgeProperty(
                        GraphPublishedEdgePropertyIdCodec.toDomain(
                                property.getId() == null ? null : Long.valueOf(property.getId())),
                        edgeId,
                        property.getPropertyName(),
                        String.valueOf(property.getValue()),
                        property.getPreferred()))
                .toList();
    }

    private static List<com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodePropertyId>
            stringIdsToNodePropertyIds(List<String> values) {
        return values == null
                ? List.of()
                : values.stream()
                        .map(Long::valueOf)
                        .map(GraphPublishedNodePropertyIdCodec::toDomain)
                        .toList();
    }

    private static List<com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId>
            stringIdsToEdgeIds(List<String> values) {
        return values == null
                ? List.of()
                : values.stream()
                        .map(Long::valueOf)
                        .map(GraphPublishedEdgeIdCodec::toDomain)
                        .toList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> fromJsonObject(@NonNull String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(value, Map.class);
        } catch (JsonProcessingException exception) {
            return Map.of();
        }
    }

    private static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new ApiException(
                    "GRAPH-API-00001", "knowledge.graph.invalid-json-payload", "图谱 JSON 协议对象无效", exception);
        }
    }
}
