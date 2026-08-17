package com.thundax.kuzhambu.knowledge.application.graph.operator;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class GraphSchemaProviderTest {

    @Test
    void shouldLoadKnowledgeGraphSchemaFromClasspath() {
        GraphSchemaProvider provider = new GraphSchemaProvider(new ObjectMapper());

        assertThat(provider.rawSchema().path("schemaId").asText()).isEqualTo("kuzhambu.knowledge-graph");
        assertThat(provider.schema()).isNotNull();
    }
}
