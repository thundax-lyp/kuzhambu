package com.thundax.kuzhambu.knowledge.application.graph.operator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.exception.BizException;
import org.junit.jupiter.api.Test;

class GraphSnapshotResolverTest {

    private final GraphSnapshotResolver resolver =
            new GraphSnapshotResolver(new ObjectMapper(), new GraphSchemaProvider(new ObjectMapper()));

    @Test
    void shouldIgnoreWorkerWarningsWhenParsingCandidate() {
        var document = resolver.parseCandidate(
                """
                {
                  "schemaVersion": "1.0.0",
                  "nodes": [{"id": "n1", "nodeType": "PERSON", "name": "王先生"}],
                  "edges": [],
                  "warnings": []
                }
                """);

        assertThat(document.getNodes()).hasSize(1);
    }

    @Test
    void shouldKeepImportStrictAboutWarnings() {
        assertThatThrownBy(
                        () -> resolver.parseImport(
                                """
                        {
                          "schemaVersion": "1.0.0",
                          "nodes": [{"id": "n1", "nodeType": "PERSON", "name": "王先生"}],
                          "edges": [],
                          "warnings": []
                        }
                        """))
                .isInstanceOf(BizException.class)
                .hasMessage("Graph document JSON does not match schema");
    }
}
