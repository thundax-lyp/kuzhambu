package com.thundax.kuzhambu.knowledge.application.graph.operator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.Error;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentEdgeDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentNodeDto;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
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
        return parseAndValidate(resultPayload, true);
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
        return parseAndValidate(graphJson, false);
    }

    private GraphDocumentDto parseAndValidate(String graphJson, boolean candidatePayload) {
        if (graphJson == null || graphJson.trim().isEmpty()) {
            throw new BizException("Graph document JSON is required");
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(graphJson);
            if (candidatePayload && jsonNode instanceof ObjectNode objectNode) {
                objectNode.remove("warnings");
            }
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
        JsonNode properties = readOptionalJson(node.getPropertiesJson());
        documentNode.setId("node-" + node.getId().value());
        documentNode.setNodeType(node.getNodeType().value());
        documentNode.setName(node.getName());
        documentNode.setAliases(properties == null ? null : properties.get("aliases"));
        documentNode.setDescription(text(properties, "description"));
        documentNode.setIdentityQualifier(text(properties, "identityQualifier"));
        documentNode.setPeriod(properties == null ? null : properties.get("period"));
        documentNode.setCategoryCodes(properties == null ? null : properties.get("categoryCodes"));
        documentNode.setImageRefs(properties == null ? null : properties.get("imageRefs"));
        documentNode.setProperties(properties == null ? null : properties.get("properties"));
        return documentNode;
    }

    private String text(JsonNode object, String fieldName) {
        JsonNode value = object == null ? null : object.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
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
