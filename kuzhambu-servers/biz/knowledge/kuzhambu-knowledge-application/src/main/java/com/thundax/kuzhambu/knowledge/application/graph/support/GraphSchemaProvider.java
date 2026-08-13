package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class GraphSchemaProvider {

    private static final String SCHEMA_RESOURCE = "schema/KNOWLEDGE-GRAPH-SCHEMA.json";

    private final JsonNode rawSchema;
    private final Schema schema;

    public GraphSchemaProvider(ObjectMapper objectMapper) {
        try {
            this.rawSchema = readSchema(objectMapper);
            this.schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                    .getSchema(rawSchema);
        } catch (IOException | RuntimeException ex) {
            throw new IllegalStateException("Failed to load knowledge graph JSON schema", ex);
        }
    }

    public JsonNode rawSchema() {
        return rawSchema;
    }

    public Schema schema() {
        return schema;
    }

    private JsonNode readSchema(ObjectMapper objectMapper) throws IOException {
        ClassPathResource resource = new ClassPathResource(SCHEMA_RESOURCE);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readTree(inputStream);
        }
    }
}
