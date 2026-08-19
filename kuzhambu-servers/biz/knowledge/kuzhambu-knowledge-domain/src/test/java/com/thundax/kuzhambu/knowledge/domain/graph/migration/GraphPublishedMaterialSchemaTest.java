package com.thundax.kuzhambu.knowledge.domain.graph.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GraphPublishedMaterialSchemaTest {

    @Test
    void schemaShouldTrackPublishedMaterialAssociationChanges() throws IOException {
        String schema = Files.readString(repoRoot().resolve("db/schema/knowledge.sql"));

        assertThat(schema)
                .contains("CREATE TABLE IF NOT EXISTS `knowledge_graph_published_node_material`")
                .contains("CREATE TABLE IF NOT EXISTS `knowledge_graph_published_edge_material`")
                .contains("`changed_at` BIGINT NOT NULL,")
                .contains("KEY `idx_knowledge_graph_published_node_material_changed_at` (`changed_at`)")
                .contains("KEY `idx_knowledge_graph_published_edge_material_changed_at` (`changed_at`)");
    }

    private static Path repoRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("db/schema/knowledge.sql"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Repository root not found");
    }
}
