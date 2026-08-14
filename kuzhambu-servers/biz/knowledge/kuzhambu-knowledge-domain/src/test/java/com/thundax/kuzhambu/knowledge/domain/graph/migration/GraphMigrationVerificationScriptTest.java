package com.thundax.kuzhambu.knowledge.domain.graph.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GraphMigrationVerificationScriptTest {

    @Test
    void scriptShouldRequireExplicitBaselineAndRefuseAutomaticPublication() throws IOException {
        String script = Files.readString(repoRoot().resolve("scripts/verify-graph-migration.sh"));

        assertThat(script).contains("set -euo pipefail");
        assertThat(script).contains("KUZHAMBU_GRAPH_MIGRATION_BASELINE_FILE is required");
        assertThat(script).contains("refusing to infer mappings or auto publish");
        assertThat(script).doesNotContain("publication/publish");
        assertThat(script).doesNotContain("material/import/apply");
    }

    @Test
    void scriptShouldCompareSancaiMaterialGraphAndMappingCounts() throws IOException {
        String script = Files.readString(repoRoot().resolve("scripts/verify-graph-migration.sh"));

        assertThat(script).contains("CONTENT_TYPE=\"SANCAI_ENTRY\"");
        assertThat(script).contains("material_count");
        assertThat(script).contains("material_node_count");
        assertThat(script).contains("material_edge_count");
        assertThat(script).contains("published_node_mapping_count");
        assertThat(script).contains("published_edge_mapping_count");
        assertThat(script).contains("node_mapping_count");
        assertThat(script).contains("edge_mapping_count");
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
