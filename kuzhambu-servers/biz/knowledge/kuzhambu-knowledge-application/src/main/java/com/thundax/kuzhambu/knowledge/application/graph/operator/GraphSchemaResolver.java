package com.thundax.kuzhambu.knowledge.application.graph.operator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Error;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentDto;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphValidationIssueResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GraphSchemaResolver {

    private static final String SEVERITY_BLOCKING = "BLOCKING";
    private static final String OBJECT_TYPE_DOCUMENT = "DOCUMENT";

    private final ObjectMapper objectMapper;
    private final GraphSchemaProvider schemaProvider;

    public GraphSchemaResolver(ObjectMapper objectMapper, GraphSchemaProvider schemaProvider) {
        this.objectMapper = objectMapper;
        this.schemaProvider = schemaProvider;
    }

    public String identityQualifier(String propertiesJson) {
        JsonNode value = readOptionalJson(propertiesJson);
        JsonNode identityQualifier = value == null ? null : value.get("identityQualifier");
        if ((identityQualifier == null || identityQualifier.isNull()) && value != null && value.has("properties")) {
            identityQualifier = value.get("properties").get("identityQualifier");
        }
        return identityQualifier == null || identityQualifier.isNull() ? null : identityQualifier.asText();
    }

    public boolean directed(String relationType) {
        return !"SPOUSE_OF".equals(relationType) && !"ASSOCIATED_WITH".equals(relationType);
    }

    public Map<String, String> keyQualifiers(String relationType, String qualifiersJson) {
        JsonNode value = readOptionalJson(qualifiersJson);
        if (value == null || !value.isObject()) {
            return Map.of();
        }
        Map<String, String> qualifiers = new LinkedHashMap<>();
        putText(qualifiers, value, "place");
        putText(qualifiers, value, "role");
        putText(qualifiers, value, "sequence");
        return qualifiers;
    }

    public Map<String, List<String>> nodePropertyValues(String propertiesJson) {
        return propertyValues(propertiesJson);
    }

    public Map<String, List<String>> edgePropertyValues(String qualifiersJson) {
        return propertyValues(qualifiersJson);
    }

    public List<GraphValidationIssueResult> validateLoose(GraphDocumentDto document) {
        JsonNode jsonNode = objectMapper.valueToTree(document);
        return schemaProvider.schema().validate(jsonNode).stream()
                .map(this::toIssue)
                .toList();
    }

    public List<GraphValidationIssueResult> validateForPublication(GraphMaterialGraph graph) {
        try {
            graph.validate();
            return List.of();
        } catch (RuntimeException ex) {
            return List.of(new GraphValidationIssueResult(
                    "GRAPH_INVALID", SEVERITY_BLOCKING, OBJECT_TYPE_DOCUMENT, null, null, ex.getMessage()));
        }
    }

    private Map<String, List<String>> propertyValues(String json) {
        JsonNode value = readOptionalJson(json);
        if (value == null || !value.isObject()) {
            return Map.of();
        }
        Map<String, List<String>> result = new LinkedHashMap<>();
        value.fields().forEachRemaining(entry -> result.put(entry.getKey(), values(entry.getValue())));
        return result;
    }

    private List<String> values(JsonNode value) {
        if (value == null || value.isNull()) {
            return List.of();
        }
        if (value.isArray()) {
            List<String> values = new ArrayList<>();
            value.forEach(item -> values.add(item.asText()));
            return values;
        }
        return List.of(value.asText());
    }

    private void putText(Map<String, String> values, JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value != null && !value.isNull() && !value.asText().isBlank()) {
            values.put(fieldName, value.asText());
        }
    }

    private GraphValidationIssueResult toIssue(Error error) {
        return new GraphValidationIssueResult(
                "SCHEMA_VALIDATION",
                SEVERITY_BLOCKING,
                OBJECT_TYPE_DOCUMENT,
                null,
                error.getInstanceLocation().toString(),
                error.getMessage());
    }

    private JsonNode readOptionalJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
