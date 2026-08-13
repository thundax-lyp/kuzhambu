package com.thundax.kuzhambu.knowledge.application.graph.operator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Error;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentEdgeDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentNodeDto;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialVersion;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GraphSnapshotResolver {

    private static final String SCHEMA_VERSION = "1.0.0";

    private final ObjectMapper objectMapper;
    private final GraphSchemaProvider schemaProvider;

    public GraphSnapshotResolver(ObjectMapper objectMapper, GraphSchemaProvider schemaProvider) {
        this.objectMapper = objectMapper;
        this.schemaProvider = schemaProvider;
    }

    public GraphDocumentDto parseImport(String graphJson) {
        return parseAndValidate(graphJson);
    }

    public GraphDocumentDto parseCandidate(String resultPayload) {
        return parseAndValidate(resultPayload);
    }

    public GraphDocumentDto parseVersion(GraphMaterialVersion version) {
        if (version == null) {
            throw new BizException("Graph material version is required");
        }
        return parseAndValidate(version.getSnapshotJson());
    }

    public String serialize(GraphMaterialGraph graph) {
        GraphDocumentDto document = new GraphDocumentDto();
        document.setSchemaVersion(SCHEMA_VERSION);
        document.setNodes(graph.nodes().stream().map(this::toDocumentNode).toList());
        document.setEdges(graph.edges().stream().map(this::toDocumentEdge).toList());
        try {
            return objectMapper.writeValueAsString(document);
        } catch (JsonProcessingException ex) {
            throw new BizException("Failed to serialize graph material snapshot");
        }
    }

    private GraphDocumentDto parseAndValidate(String graphJson) {
        if (graphJson == null || graphJson.trim().isEmpty()) {
            throw new BizException("Graph document JSON is required");
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(graphJson);
            List<Error> errors = schemaProvider.schema().validate(jsonNode);
            if (!errors.isEmpty()) {
                throw new BizException("Graph document JSON does not match schema");
            }
            return objectMapper.treeToValue(jsonNode, GraphDocumentDto.class);
        } catch (JsonProcessingException ex) {
            throw new BizException("Graph document JSON is invalid");
        }
    }

    private GraphDocumentNodeDto toDocumentNode(GraphMaterialNode node) {
        GraphDocumentNodeDto documentNode = new GraphDocumentNodeDto();
        documentNode.setId("node-" + node.getId().value());
        documentNode.setNodeType(node.getNodeType().value());
        documentNode.setName(node.getName());
        documentNode.setProperties(readOptionalJson(node.getPropertiesJson()));
        return documentNode;
    }

    private GraphDocumentEdgeDto toDocumentEdge(GraphMaterialEdge edge) {
        GraphDocumentEdgeDto documentEdge = new GraphDocumentEdgeDto();
        documentEdge.setId(edge.getId() == null ? null : "edge-" + edge.getId().value());
        documentEdge.setSourceId("node-" + edge.getSourceNodeId().value());
        documentEdge.setTargetId("node-" + edge.getTargetNodeId().value());
        documentEdge.setRelationType(edge.getRelationType());
        documentEdge.setQualifiers(readOptionalJson(edge.getQualifiersJson()));
        return documentEdge;
    }

    private JsonNode readOptionalJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new BizException("Graph material snapshot contains invalid JSON");
        }
    }
}
