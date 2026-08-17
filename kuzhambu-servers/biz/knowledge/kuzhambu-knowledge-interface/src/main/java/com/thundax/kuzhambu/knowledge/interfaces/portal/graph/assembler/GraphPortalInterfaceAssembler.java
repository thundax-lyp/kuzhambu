package com.thundax.kuzhambu.knowledge.interfaces.portal.graph.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.web.exception.ApiException;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedGraphResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.request.GraphPortalMaterialRequest;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.response.GraphPortalMaterialResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class GraphPortalInterfaceAssembler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private GraphPortalInterfaceAssembler() {}

    @NonNull
    public static GraphMaterialQuery toQuery(@NonNull GraphPortalMaterialRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialQuery(
                null, new ContentRef(request.getContentType(), Long.valueOf(request.getContentRefId())));
    }

    @NonNull
    public static GraphPortalMaterialResponse toResponse(@NonNull GraphPublishedGraphResult value) {
        Objects.requireNonNull(value, "value");
        return GraphPortalMaterialResponse.builder()
                .visible(value.visible())
                .contentRef(toContentRefData(value.materialRef()))
                .nodes(value.nodes().stream()
                        .map(GraphPortalInterfaceAssembler::toNodeData)
                        .toList())
                .edges(value.edges().stream()
                        .map(GraphPortalInterfaceAssembler::toEdgeData)
                        .toList())
                .build();
    }

    @NonNull
    public static GraphPortalMaterialResponse.ContentRefData toContentRefData(@NonNull ContentRef value) {
        Objects.requireNonNull(value, "value");
        return GraphPortalMaterialResponse.ContentRefData.builder()
                .contentType(value.getContentType())
                .contentRefId(String.valueOf(value.getContentId()))
                .build();
    }

    @NonNull
    public static GraphPortalMaterialResponse.NodeData toNodeData(@NonNull GraphPublishedNode value) {
        Objects.requireNonNull(value, "value");
        return GraphPortalMaterialResponse.NodeData.builder()
                .id(String.valueOf(GraphPublishedNodeIdCodec.toValue(value.getId())))
                .nodeType(value.getNodeType().name())
                .name(value.getName())
                .status(value.getStatus().name())
                .lockVersion(String.valueOf(value.getLockVersion()))
                .build();
    }

    @NonNull
    public static GraphPortalMaterialResponse.EdgeData toEdgeData(@NonNull GraphPublishedEdge value) {
        Objects.requireNonNull(value, "value");
        return GraphPortalMaterialResponse.EdgeData.builder()
                .id(String.valueOf(GraphPublishedEdgeIdCodec.toValue(value.getId())))
                .sourceNodeId(String.valueOf(GraphPublishedNodeIdCodec.toValue(value.getSourceNodeId())))
                .targetNodeId(String.valueOf(GraphPublishedNodeIdCodec.toValue(value.getTargetNodeId())))
                .relationType(value.getRelationType())
                .qualifiers(toQualifiers(value.getQualifiersJson()))
                .status(value.getStatus().name())
                .lockVersion(String.valueOf(value.getLockVersion()))
                .build();
    }

    @NonNull
    private static Map<String, Object> toQualifiers(String qualifiersJson) {
        if (qualifiersJson == null || qualifiersJson.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> value = OBJECT_MAPPER.readValue(qualifiersJson, MAP_TYPE);
            return value == null || value.isEmpty() ? Map.of() : new LinkedHashMap<>(value);
        } catch (Exception exception) {
            throw new ApiException(
                    "GRAPH-PORTAL-00001", "knowledge.graph.portal.invalid-edge-qualifiers", "门户图谱关系限定数据无效", exception);
        }
    }
}
